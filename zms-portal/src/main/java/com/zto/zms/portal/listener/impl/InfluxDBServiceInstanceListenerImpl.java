/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zto.zms.portal.listener.impl;

import com.alibaba.fastjson.JSON;
import com.zto.zms.common.ZmsServiceTypeEnum;
import com.zto.zms.dal.mapper.ProcessPropertyValueRefMapper;
import com.zto.zms.dal.mapper.ServiceInstanceMapper;
import com.zto.zms.dal.mapper.ServicePropertyMapper;
import com.zto.zms.dal.mapper.ZmsHostMapper;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.dal.model.ServiceInstance;
import com.zto.zms.portal.common.ServicePropertiesNameEnum;
import com.zto.zms.portal.listener.ServiceInstanceListener;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yuhao.zhang
 * @description
 * @date 2020/4/8
 */
@Component("ServiceEventListener-influxdb")
public class InfluxDBServiceInstanceListenerImpl implements ServiceInstanceListener {

    private static final Logger logger = LoggerFactory.getLogger(InfluxDBServiceInstanceListenerImpl.class);
    @Autowired
    private ProcessPropertyValueRefMapper processPropValueRefMapper;
    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ZmsHostMapper zmsHostMapper;
    @Autowired
    private ServicePropertyMapper servicePropertyMapper;

    private static final String INFLUX_DB_DATABASE_NAME = "zms_monitor";

    private static final Integer RETRIES = 3;


    @Override
    public void start(Integer serviceId, Integer processId) {
        initInfluxDbUrl(serviceId, processId);
    }

    /**
     * 获取influxDB的IP地址
     */
    private void initInfluxDbUrl(Integer serviceId, Integer processId) {
        logger.info("init influxDB serviceId:{} ,processId :{}", serviceId, processId);
        //获取host
        List<ServiceInstance> serviceInstance = serviceInstanceMapper.listInstanceByServiceId(serviceId);
        logger.info("init influxDB serviceInstance:{}", JSON.toJSONString(serviceInstance));
        ServiceInstance instance = serviceInstance.get(0);
        String hostIp = zmsHostMapper.getEnableIPById(instance.getHostId());
        logger.info("init influxDB hostIp:{}", hostIp);

        //获取属性id
        Integer servicePropertyId = servicePropertyMapper
                .getIdByServiceTypeAndName(ZmsServiceTypeEnum.INFLUXDB.name(),
                        ServicePropertiesNameEnum.INFLUX_DB_PORT.getInstanceType(),
                        ServicePropertiesNameEnum.INFLUX_DB_PORT.getGroup(),
                        ServicePropertiesNameEnum.INFLUX_DB_PORT.getName());
        //服务配置属性
        List<ProcessPropertyValueRef> propertyValueRefs = processPropValueRefMapper.listByProcessId(Arrays.asList(processId), servicePropertyId);
        //获取bind-address
        String bindAddress = propertyValueRefs.get(0).getCurrentValue();
        String influxDbUrl = MessageFormat.format("http://{0}:{1}", hostIp, bindAddress);
        AtomicInteger threadNum = new AtomicInteger(0);
        initInfluxDb(influxDbUrl, threadNum);
        logger.info("init influxDB:{}", influxDbUrl);
    }


    private void initInfluxDb(String influxDbUrl, AtomicInteger threadNum) {
        InfluxDBImpl influxDb = null;
        /*
         * 连接数据库 ，若不存在则创建
         */
        try {
            TimeUnit.SECONDS.sleep(20);
            influxDb = (InfluxDBImpl) InfluxDBFactory.connect(influxDbUrl);
            if (null != influxDb && !influxDb.databaseExists(INFLUX_DB_DATABASE_NAME)) {
                createDb(influxDb);
            }
        } catch (Exception e) {
            if (threadNum.getAndIncrement() < RETRIES) {
                initInfluxDb(influxDbUrl, threadNum);
            }
            logger.info("create influx db failed, error: {}", e.getMessage());
        } finally {
            if (null != influxDb) {
                influxDb.close();
            }
        }
    }

    private void createDb(InfluxDBImpl influxDb) {
        influxDb.createDatabase(INFLUX_DB_DATABASE_NAME);
        influxDb.setDatabase(INFLUX_DB_DATABASE_NAME);
        String rpName = "zmsRetentionPolicy";
        influxDb.query(new Query("CREATE RETENTION POLICY " + rpName + " ON " + INFLUX_DB_DATABASE_NAME + " DURATION 60d REPLICATION 1 DEFAULT", INFLUX_DB_DATABASE_NAME));
        influxDb.setRetentionPolicy(rpName);
        influxDb.enableBatch(BatchOptions.DEFAULTS);
        influxDb.setLogLevel(InfluxDB.LogLevel.BASIC);
    }

    @Override
    public void remove(ServiceInstance serviceInstance) {

    }
}

