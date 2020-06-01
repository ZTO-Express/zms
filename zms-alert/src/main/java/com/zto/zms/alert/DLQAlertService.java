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

package com.zto.zms.alert;

import com.google.common.collect.Maps;
import com.zto.zms.utils.ExecutorServiceUtils;
import com.zto.zms.service.influx.InfluxdbClient;
import com.zto.zms.service.domain.influxdb.DLQAlertRule;
import com.zto.zms.service.domain.influxdb.DLQTopicInfo;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liangyong on 2018/12/17.
 */
@Service
public class DLQAlertService {

    private Map<String,Double> dlqTopicOffsets = Maps.newHashMap();

    private static final String QUERY_SQL = "select * from statistic_dlq_topic_offsets_info where \"time\" > now()-%s";

    private static final String ALERT_MSG = "集群[%s],死信队列[%s]有新增消息";

    @Autowired
    private InfluxdbClient influxdbClient;
    private ScheduledExecutorService dlqScheduleService;

    public void start(){
        List<DLQTopicInfo> dlqTopicInfoLst = getQueryResult(String.format(QUERY_SQL,"3m"));
        for(DLQTopicInfo dlqTopicInfo : dlqTopicInfoLst){
            String dlqKey = dlqTopicInfo.getClusterName()+ "_" + dlqTopicInfo.getTopicName();
            dlqTopicOffsets.put(dlqKey,dlqTopicInfo.getValue());
        }

        dlqScheduleService = Executors.newScheduledThreadPool(1, new ThreadFactory() {
            AtomicInteger i = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "DLQAlertService-"+ i.incrementAndGet());
            }
        });

        dlqScheduleService.scheduleAtFixedRate(() -> {
            List<DLQTopicInfo> dlqTopicInfoLst1 = getQueryResult(String.format(QUERY_SQL,"1m"));
            for(DLQTopicInfo dlqTopicInfo : dlqTopicInfoLst1){
                String dlqKey = dlqTopicInfo.getClusterName()+ "_" + dlqTopicInfo.getTopicName();
                if(!dlqTopicOffsets.containsKey(dlqKey)){
                    sendAlertMsg(dlqTopicInfo);
                    dlqTopicOffsets.put(dlqKey,dlqTopicInfo.getValue());
                    continue;
                }
                if(dlqTopicInfo.getValue()!= dlqTopicOffsets.get(dlqKey)){
                    sendAlertMsg(dlqTopicInfo);
                    dlqTopicOffsets.put(dlqKey,dlqTopicInfo.getValue());
                }
            }
        },60, 60, TimeUnit.SECONDS);

    }

    private void sendAlertMsg(DLQTopicInfo dlqTopicInfo) {
        DLQAlertRule dlqAlertRule = new DLQAlertRule();
        dlqAlertRule.setClusterName(dlqTopicInfo.getClusterName());
        dlqAlertRule.setTopicName(dlqTopicInfo.getTopicName());
        dlqAlertRule.setMsg(String.format(ALERT_MSG,dlqTopicInfo.getClusterName(),dlqTopicInfo.getTopicName()));
        this.influxdbClient.insertDLQNotifications(dlqAlertRule);
    }


    public List<DLQTopicInfo> getQueryResult(String sql){
        QueryResult queryResult = influxdbClient.query(sql);
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        List<DLQTopicInfo> resultLst = resultMapper.toPOJO(queryResult, DLQTopicInfo.class);
        return resultLst;
    }

    @PreDestroy
    public void shutdown() {
        if(dlqScheduleService!=null){
            ExecutorServiceUtils.gracefullyShutdown(dlqScheduleService);
        }
    }

}

