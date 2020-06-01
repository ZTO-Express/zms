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

package com.zto.zms.collector.report;

import com.zto.zms.service.domain.influxdb.KafkaConsumerNumberInfo;
import com.zto.zms.service.domain.influxdb.KafkaTopicInfo;
import com.zto.zms.service.domain.influxdb.MqConsumerNumberInfo;
import com.zto.zms.service.influx.*;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ExtensionRepository {

    @Autowired
    private InfluxdbClient influxdbClient;

    @Value("${rocketmq.latency.minTps:0}")
    private int mqMinTpsFilter;

    @Value("${rocketmq.latency.maxTps:8000}")
    private int mqMaxTpsFilter;

    @Value("${kafka.latency.minTps:0}")
    private int kafkaMinTpsFilter;

    @Value("${kafka.latency.maxTps:8000}")
    private int kafkaMaxTpsFilter;

    public static final String TOPIC_MAX_TPS = "select last(value) as value,* from mq_topic_info where time>now()-5m " +
            "and \"name\"='tps' group by topicName";

    public static final String CONSUMER_LATENCY = "SELECT last(value) as value,* FROM mq_consumer_number_info " +
            "WHERE  \"name\" = 'latency' and time > now()-5m  group by consumerGroup";

    public static final String MQ_GROUP_TPS = "SELECT last(value)  as value FROM mq_consumer_number_info " +
            "WHERE  \"name\" = 'tps' and time > now()-5m group by consumerGroup";

    private static final String KAFKA_TOPIC_MAX_TPS = "select last(value) as value,* from kafka_topic_info where time>now()-5m " +
            "and \"name\"='oneMinuteRate' and \"attributeName\"='MessagesInPerSec' group by topicName";

    private static final String KAFKA_CONSUMER_LATENCY = "SELECT last(value) as value,* FROM kafka_consumer_number_info " +
            "WHERE  \"name\" = 'latency' and time > now()-5m  group by consumerGroup";

    public static final String KAFKA_GROUP_TPS = "SELECT last(value)  as value FROM kafka_consumer_number_info " +
            "WHERE  \"name\" = 'tps' and time > now()-5m  group by consumerGroup";

    /**
     * rocketMQ最近五分钟topic最大tps
     * @return
     */
    public List<MqTopicInfo> queryMqTopicMaxTps() {
        return this.query(TOPIC_MAX_TPS, MqTopicInfo.class);
    }

    /**
     * rocketMQ消费组延迟
     * @return
     */
    public List<MqConsumerNumberInfo> queryMqConsumerLatency() {
        return this.query(CONSUMER_LATENCY, MqConsumerNumberInfo.class);
    }

    /**
     * rocketMQ消费组最近五分钟最大tps
     * @return
     */
    public List<MqConsumerNumberInfo> queryMqConsumerGroupTps() {
        return this.query(MQ_GROUP_TPS, MqConsumerNumberInfo.class);
    }

    /**
     * kafka主题最近五分钟最大tps
     * @return
     */
    public List<KafkaTopicInfo> queryKafkaTopicMaxTps() {
        return this.query(KAFKA_TOPIC_MAX_TPS, KafkaTopicInfo.class);
    }


    /**
     * kafka消费组近五分钟最大延迟
     * @return
     */
    public List<KafkaConsumerNumberInfo> queryKafkaConsumerLatency() {
        return this.query(KAFKA_CONSUMER_LATENCY, KafkaConsumerNumberInfo.class);
    }

    /**
     * kafka消费组最近五分钟最大tps
     * @return
     */
    public List<KafkaConsumerNumberInfo> queryKafkaConsumerGroupTps() {
        return this.query(KAFKA_GROUP_TPS, KafkaConsumerNumberInfo.class);
    }

    public Map<String, Double> filterMqTps(List<MqConsumerNumberInfo> consumerTpsInfos) {
        return consumerTpsInfos.stream()
                .filter(item -> item.getValue() > mqMinTpsFilter && item.getValue() < mqMaxTpsFilter)
                .collect(Collectors.toMap(MqConsumerNumberInfo::getConsumerGroup, item -> item.getValue(), (k1, k2) -> k1));
    }

    public Map<String, Double> filterKafkaTps(List<KafkaConsumerNumberInfo> consumerTpsInfos) {
        return consumerTpsInfos.stream()
                .filter(item -> item.getValue() > kafkaMinTpsFilter && item.getValue() < kafkaMaxTpsFilter)
                .collect(Collectors.toMap(KafkaConsumerNumberInfo::getConsumerGroup, item -> item.getValue(), (k1, k2) -> k1));
    }

    public <T> List<T> query(String sql, Class<T> clazz) {
        QueryResult queryResult = influxdbClient.query(sql);
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper.toPOJO(queryResult, clazz);
    }

}

