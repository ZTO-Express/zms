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

package com.zto.zms.portal.kafka;

import com.google.common.collect.Lists;
import com.zto.zms.service.kafka.KafkaOperator;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsOptions;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.*;

public class ConsumerGroupLoader extends KafkaOperator {

    private static Logger logger = LoggerFactory.getLogger(ConsumerGroupLoader.class);

    public KafkaConsumerSummary load(String server, String group) {
        KafkaConsumerSummary summary = new KafkaConsumerSummary();
        ArrayList<KafkaConsumerInfo> consumerGroups = consumerGroup(server, group);
        long totalLag = 0L;
        for (KafkaConsumerInfo consumerGroup : consumerGroups) {
            totalLag += consumerGroup.getLag();
        }
        summary.setTotalLag(totalLag);
        summary.setConsumerInfos(consumerGroups);
        return summary;
    }

    public ArrayList<KafkaConsumerInfo> consumerGroup(String server, String group) {
        ArrayList<KafkaConsumerInfo> consumerInfos = Lists.newArrayList();
        try {
            DescribeConsumerGroupsResult consumerGroupSummary = getClient(server)
                    .describeConsumerGroups(Lists.newArrayList(group), new DescribeConsumerGroupsOptions().timeoutMs(30000));
            Map<TopicPartition, OffsetAndMetadata> listGroupOffsets = getClient(server).listConsumerGroupOffsets(group)
                    .partitionsToOffsetAndMetadata().get();

            Collection<MemberDescription> memberDescriptions = consumerGroupSummary.describedGroups().get(group).get().members();

            if (!CollectionUtils.isEmpty(memberDescriptions)) {
                for (MemberDescription consumerSummary : memberDescriptions) {

                    consumerSummary.consumerId();
                    consumerSummary.clientId();
                    Set<TopicPartition> assignment = consumerSummary.assignment().topicPartitions();
                    Map<TopicPartition, Long> endOffsets = getConsumer(server, group).endOffsets(assignment);

                    for (TopicPartition topicPartition : assignment) {
                        KafkaConsumerInfo info = new KafkaConsumerInfo();
                        info.setClientId(consumerSummary.clientId());
                        info.setConsumerId(consumerSummary.consumerId());
                        info.setConsumer(group);
                        info.setTopic(topicPartition.topic());
                        info.setPartition(topicPartition.partition());
                        info.setHost(consumerSummary.host());
                        info.setBrokerOffset(endOffsets.get(topicPartition));
                        if (listGroupOffsets.containsKey(topicPartition)) {
                            info.setConsumerOffset(listGroupOffsets.get(topicPartition).offset());
                            info.setLag(info.getBrokerOffset() - info.getConsumerOffset());
                        }
                        consumerInfos.add(info);
                    }

                }
            } else {
                Map<TopicPartition, Long> endOffsets = getConsumer(server, group).endOffsets(listGroupOffsets.keySet());

                for (HashMap.Entry<TopicPartition, OffsetAndMetadata> entry : listGroupOffsets.entrySet()) {

                    KafkaConsumerInfo info = new KafkaConsumerInfo();
                    info.setConsumer(group);
                    info.setTopic(entry.getKey().topic());
                    info.setPartition(entry.getKey().partition());
                    info.setBrokerOffset(endOffsets.get(entry.getKey()));
                    info.setConsumerOffset(entry.getValue().offset());

                    info.setLag(info.getBrokerOffset() - info.getConsumerOffset());
                    consumerInfos.add(info);
                }
            }
        } catch (Exception e) {
            logger.error(MessageFormat.format("query consumer summary error:{0}", group), e);
        } finally {
            closeOperator();
        }

        Collections.sort(consumerInfos, (o1, o2) -> {
            if (o1.getPartition() > o2.getPartition()) {
                return 1;
            } else {
                return -1;
            }
        });
        return consumerInfos;
    }

}

