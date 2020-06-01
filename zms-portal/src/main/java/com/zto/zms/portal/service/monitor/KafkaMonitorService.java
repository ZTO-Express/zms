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

package com.zto.zms.portal.service.monitor;

import com.zto.zms.service.domain.influxdb.ConsumerInfo;
import com.zto.zms.service.influx.InfluxdbClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/4/14
 **/
@Service("MonitorService-kafka")
public class KafkaMonitorService implements IMonitorService {

    @Autowired
    private InfluxdbClient influxdbClient;

    private static final String READ_LATENCY_TEMPLATE = "SELECT consumerGroup,last(\"value\")  as value FROM \"kafka_consumer_number_info\" WHERE \"clusterName\" = '%s' AND \"consumerGroup\" = '%s' AND \"name\" = 'latency' and time > now()-1h";

    private static final String READ_TPS_TEMPLATE = "SELECT consumerGroup,last(\"value\")  as value FROM \"kafka_consumer_number_info\" WHERE \"clusterName\" = '%s' AND \"consumerGroup\" = '%s' AND \"name\" = 'tps' and time > now()-5m";

    private static final String ALL_LATENCIES_TEMPLATE = "SELECT last(\"value\")  as value FROM \"kafka_consumer_number_info\" WHERE  \"name\" = 'latency' and time > now()-1h group by clusterName,consumerGroup";

    private static final String ALL_TPSES_TEMPLATE = "SELECT last(\"value\")  as value FROM \"kafka_consumer_number_info\" WHERE  \"name\" = 'tps' and time > now()-5m group by clusterName,consumerGroup";

    private static final String ALL_LATENCIES_BY_CLUSTER_TEMPLATE = "SELECT last(\"value\")  as value FROM \"kafka_consumer_number_info\" WHERE  \"clusterName\" = '%s' and \"name\" = 'latency' and time > now()-1h group by consumerGroup";

    private static final String ALL_TPSES_BY_CLUSTER_TEMPLATE = "SELECT last(\"value\")  as value FROM \"kafka_consumer_number_info\" WHERE \"clusterName\" = '%s' and \"name\" = 'tps' and time > now()-5m group by consumerGroup";

    @Override
    public ConsumerInfo readLatency(String cluster, String consumer) {
        String format = String.format(READ_LATENCY_TEMPLATE, cluster, consumer);
        ConsumerInfo consumerLatency = influxdbClient.query(format, ConsumerInfo.class);
        if (consumerLatency != null) {
            consumerLatency.setClusterName(cluster);
        }
        return consumerLatency;
    }

    @Override
    public ConsumerInfo readTps(String cluster, String consumer) {
        String format = String.format(READ_TPS_TEMPLATE, cluster, consumer);
        ConsumerInfo consumerLatency = influxdbClient.query(format, ConsumerInfo.class);
        if (consumerLatency != null) {
            consumerLatency.setClusterName(cluster);
        }
        return consumerLatency;
    }

    @Override
    public List<ConsumerInfo> readLatencies() {
        return influxdbClient.queryList(ALL_LATENCIES_TEMPLATE, ConsumerInfo.class);
    }

    @Override
    public List<ConsumerInfo> readTpses() {
        return influxdbClient.queryList(ALL_TPSES_TEMPLATE, ConsumerInfo.class);
    }

    @Override
    public List<ConsumerInfo> readLatencies(List<String> clusters) {
        return influxdbClient.read(clusters, ALL_LATENCIES_BY_CLUSTER_TEMPLATE);
    }

    @Override
    public List<ConsumerInfo> readTpses(List<String> clusters) {
        return influxdbClient.read(clusters, ALL_TPSES_BY_CLUSTER_TEMPLATE);
    }
}

