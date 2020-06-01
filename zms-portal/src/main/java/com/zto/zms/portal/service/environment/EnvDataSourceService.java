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

package com.zto.zms.portal.service.environment;

import com.google.common.base.Joiner;
import com.zto.zms.utils.Assert;
import com.zto.zms.dal.mapper.ServiceInstanceMapper;
import com.zto.zms.dal.mapper.ZmsEnvironmentMapper;
import com.zto.zms.dal.mapper.ZmsHostMapper;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.dal.model.ServiceInstance;
import com.zto.zms.dal.model.ZmsEnvironment;
import com.zto.zms.portal.common.ServicePropertiesNameEnum;
import com.zto.zms.portal.service.serve.ServeService;
import com.zto.zms.service.manager.InfluxDBDatasourceManagerAdapt;
import com.zto.zms.service.manager.ZookeeperDatasourceManagerAdapt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.List;

/**
 * <p>Description: 环境数据源管理服务</p>
 *
 * @author lidawei
 * @date 2020/1/7
 **/
@Service
public class EnvDataSourceService {

    private static final Logger logger = LoggerFactory.getLogger(EnvDataSourceService.class);

    @Autowired
    private InfluxDBDatasourceManagerAdapt influxDBDatasourceManagerAdapt;
    @Autowired
    private ZookeeperDatasourceManagerAdapt zookeeperDatasourceManagerAdapt;
    @Autowired
    private ZmsEnvironmentMapper environmentMapper;
    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ZmsHostMapper zmsHostMapper;
    @Autowired
    private ServeService serveService;

    /**
     * 初始化所有环境数据源
     */
    @PostConstruct
    public void init() {
        List<ZmsEnvironment> zmsEnvironments = environmentMapper.listEnableEnv();
        for (ZmsEnvironment environment : zmsEnvironments) {
            try {
                if (null == environment.getZkServiceId()) {
                    logger.error("The environment is not configured with a zookeeper data source:{}", environment.getEnvironmentName());
                } else {
                    reloadEnvZkClient(environment.getId(), environment.getZkServiceId());
                }
            } catch (Exception e) {
                logger.error(MessageFormat.format("The environment initializes the zookeeper data source exception:{0}", environment.getEnvironmentName()), e);
            }

            try {
                if (null == environment.getInfluxdbServiceId()) {
                    logger.error("The environment is not configured with a influxdb data source:{}", environment.getEnvironmentName());
                } else {
                    reloadEnvInfluxDb(environment.getId(), environment.getInfluxdbServiceId());
                }
            } catch (Exception e) {
                logger.error(MessageFormat.format("The environment initializes the influxdb data source exception:{0}", environment.getEnvironmentName()), e);
            }

        }
    }

    /**
     * 重新加载环境zk
     *
     * @param envId       环境ID
     * @param zkServiceId zookeeper服务ID
     */
    public void reloadEnvZkClient(int envId, int zkServiceId) {
        List<String> zookeepers = serveService.listZookeeperAddr(zkServiceId);
        //zk地址
        String zookeeper = Joiner.on(',').join(zookeepers);
        //初始化环境Zookeeper数据源
        logger.info("reload environment:{} zookeeper:{}", envId, zookeeper);
        zookeeperDatasourceManagerAdapt.reload(envId, zookeeper);
    }

    public void removeEnvZkClient(int envId) {
        zookeeperDatasourceManagerAdapt.remove(envId);
    }

    public void removeEnvInfluxDb(int envId) {
        influxDBDatasourceManagerAdapt.remove(envId);
    }

    /**
     * 重新加载InfluxDB数据源
     *
     * @param envId             环境ID
     * @param influxDbServiceId influxDB服务ID
     */
    public void reloadEnvInfluxDb(int envId, int influxDbServiceId) {
        List<ServiceInstance> serviceInstance = serviceInstanceMapper.listStartInstanceByServiceId(influxDbServiceId);
        Assert.that(serviceInstance.size() == 1, "Configure a single InfluxDB instance");
        ServiceInstance instance = serviceInstance.get(0);
        //服务配置属性
        List<ProcessPropertyValueRef> propertyValueRefs =
                serveService.getServicePropertyValueRefs(influxDbServiceId, ServicePropertiesNameEnum.INFLUX_DB_PORT);
        //获取host
        String hostIp = zmsHostMapper.getEnableIPById(instance.getHostId());
        //获取bind-address
        String bindAddress = propertyValueRefs.get(0).getCurrentValue();
        String influxDb = MessageFormat.format("http://{0}:{1}", hostIp, bindAddress);
        influxDBDatasourceManagerAdapt.reload(envId, influxDb);
        logger.info("reload environment:{} influxDB:{}", envId, influxDb);
    }

}

