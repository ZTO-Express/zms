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

package com.zto.zms.service.kafka.reset;

import com.google.common.collect.Maps;
import com.zto.zms.service.kafka.KafkaOperator;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public abstract class AbstractKafkaResetter extends KafkaOperator {

    java.util.Map<TopicPartition, Long> targetOffsets;
    KafkaConsumer kafkaConsumer = null;
    AdminClient adminClient = null;
    Logger logger = LoggerFactory.getLogger(getClass().getName());

    public void execute(String server, String topic, String consumer) throws ExecutionException, InterruptedException {

        try {
            kafkaConsumer = getConsumer(server, consumer);
            adminClient = getClient(server);
            Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsets = getClient(server).listConsumerGroupOffsets(consumer).partitionsToOffsetAndMetadata().get();
            targetOffsets = findOffsets(server, consumer, topicPartitionOffsets.keySet());
            java.util.Map<TopicPartition, OffsetAndMetadata> offsets = Maps.newHashMap();
            targetOffsets.forEach((key, value) -> {
                if (StringUtils.isNotBlank(topic)) {
                    if (key.topic().equals(topic)) {
                        offsets.put(key, new OffsetAndMetadata(value));
                    }
                } else {
                    offsets.put(key, new OffsetAndMetadata(value));
                }
            });

            kafkaConsumer.commitSync(offsets);
        } catch (Exception ex) {
            logger.error("{} reset error ", this.getClass().getName(), ex);
            throw ex;
        } finally {
            closeOperator();
        }
    }

    /**
     * 查询消费组消费进度
     *
     * @param server
     * @param group
     * @param keys
     * @return
     */
    abstract java.util.Map<TopicPartition, Long> findOffsets(String server, String group, Set<TopicPartition> keys);


}

