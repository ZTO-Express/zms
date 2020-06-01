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
import com.google.common.collect.Maps;
import com.zto.zms.service.kafka.KafkaOperator;
import kafka.zk.AdminZkClient;
import kafka.zk.KafkaZkClient;
import kafka.zookeeper.ZooKeeperClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.utils.Time;
import org.apache.rocketmq.common.Pair;
import org.springside.modules.utils.collection.CollectionUtil;

import java.util.*;

public class TopicLoader extends KafkaOperator {


    public KafkaTopicSummary load(String clusterName, String server, String zk, String topic) throws Exception {
        List<Pair<String, String>> configs = readTopicExtraConfig(topic, zk);
        try {

            DescribeTopicsResult describeTopicsResult = getClient(server).describeTopics(Lists.newArrayList(topic));
            Map<TopicPartition, KafkaTopicInfo> infos = Maps.newHashMap();

            for (Map.Entry<String, KafkaFuture<TopicDescription>> entry : describeTopicsResult.values().entrySet()) {
                TopicDescription topicDescription = entry.getValue().get();
                for (TopicPartitionInfo partitionInfo : topicDescription.partitions()) {
                    TopicPartition topicPartition = new TopicPartition(topic, partitionInfo.partition());

                    KafkaTopicInfo info = new KafkaTopicInfo();
                    info.setTopic(topic);
                    info.setPartition(partitionInfo.partition());
                    info.setIsr(nodesToString(partitionInfo.isr()));
                    info.setReplicas(nodesToString(partitionInfo.replicas()));
                    info.setLeader(nodesToString(partitionInfo.leader()));
                    infos.put(topicPartition, info);
                }

            }

            Map<TopicPartition, Long> endOffsets = getConsumer(server, topic).endOffsets(infos.keySet());

            for (Map.Entry<TopicPartition, Long> entry : endOffsets.entrySet()) {
                KafkaTopicInfo kafkaTopicInfo = infos.get(entry.getKey());
                if (kafkaTopicInfo != null) {
                    kafkaTopicInfo.setOffset(endOffsets.get(entry.getKey()));
                }

            }
            ArrayList<KafkaTopicInfo> result = Lists.newArrayList(infos.values());

            Collections.sort(result, (o1, o2) -> {
                if (o1.getPartition() > o2.getPartition()) {
                    return 1;
                } else {
                    return -1;
                }
            });
            KafkaTopicSummary summary = new KafkaTopicSummary();
            summary.setTopicInfos(result);
            summary.setTopic(topic);
            summary.setTopicConfigs(configs);
            summary.setCluster(clusterName);
            List<Name> topicConsumers = getTopicConsumers(server, topic);
            summary.setConsumers(topicConsumers);

            return summary;
        } finally {
            closeOperator();
        }
    }


    private List<Name> getTopicConsumers(String server, String topic) throws Exception {
        ArrayList<Name> result = Lists.newArrayList();

        Collection<ConsumerGroupListing> groupOverviews = getClient(server).listConsumerGroups().all().get();
        Map<String, KafkaFuture<Map<TopicPartition, OffsetAndMetadata>>> consumerOffsetMap = Maps.newHashMap();

        groupOverviews.forEach(item -> {
                    KafkaFuture<Map<TopicPartition, OffsetAndMetadata>> listGroupOffsets = getClient(server)
                            .listConsumerGroupOffsets(item.groupId())
                            .partitionsToOffsetAndMetadata();
                    consumerOffsetMap.put(item.groupId(), listGroupOffsets);
                }
        );

        for (Map.Entry<String, KafkaFuture<Map<TopicPartition, OffsetAndMetadata>>> view : consumerOffsetMap.entrySet()) {
            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : view.getValue().get().entrySet()) {
                if (entry.getKey().topic().equalsIgnoreCase(topic)) {
                    result.add(new Name(view.getKey()));
                    break;
                }

            }
        }

        return result;
    }

    private List<Pair<String, String>> readTopicExtraConfig(String topic, String zk) {
        List<Pair<String, String>> configs = Lists.newArrayList();
        int sessionTimeoutMs = 10 * 1000;
        int connectionTimeoutMs = 8 * 1000;
        ZooKeeperClient zkClient = null;
        KafkaZkClient kafkaZkClient = null;
        try {
            zkClient = new ZooKeeperClient(
                    zk,
                    sessionTimeoutMs,
                    connectionTimeoutMs, 1, Time.SYSTEM, "kafka.server", "ZooKeeperClientConnect");
            kafkaZkClient = new KafkaZkClient(zkClient, false, Time.SYSTEM);
            Properties topicProps = new AdminZkClient(kafkaZkClient).fetchEntityConfig("topics", topic);

            for (Map.Entry<Object, Object> entry : topicProps.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                configs.add(new Pair<>(key, value));
            }

        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
            if (kafkaZkClient != null) {
                kafkaZkClient.close();
            }

        }

        return configs;
    }

    private String nodesToString(List<Node> nodes) {
        StringBuilder sb = new StringBuilder();
        if (CollectionUtil.isNotEmpty(nodes)) {
            for (Node node : nodes) {
                sb.append(node.id());
                sb.append("-");
                sb.append(node.host());
                sb.append(",");
            }
        }
        return sb.toString();

    }

    private String nodesToString(Node node) {
        StringBuilder sb = new StringBuilder();
        if (node != null) {
            sb.append(node.id());
            sb.append("-");
            sb.append(node.host());
        }
        return sb.toString();

    }


}

