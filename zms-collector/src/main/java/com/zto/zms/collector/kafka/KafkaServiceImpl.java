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

package com.zto.zms.collector.kafka;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.service.kafka.KafkaAdminManage;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by liangyong on 2018/10/30.
 */

@Service
public class KafkaServiceImpl implements KafkaService {

    private static Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Autowired
    private KafkaAdminManage kafkaAdminManage;

    private ArrayList<String> kafkaInternalTopic = Lists.newArrayList("__consumer_offsets");


    @Override
    public Set<String> getKafkaConsumerTopics(ClusterMetadata cluster, String group) {
        JSONArray consumerGroups = getKafkaMetadata(cluster, group);
        Set<String> topics = new HashSet<>();
        for (Object object : consumerGroups) {
            JSONObject consumerGroup = (JSONObject) object;
            for (Object topicObject : consumerGroup.getJSONArray("topicSub")) {
                JSONObject topic = (JSONObject) topicObject;
                topics.add(topic.getString("topic"));
            }
        }
        return topics;
    }

    @Override
    public Map<String, List<Integer>> findTopicPartitions(ClusterMetadata cluster, Collection<String> topics) {
        Map<String, List<Integer>> topicPartitions = Maps.newHashMap();
        Set<String> topicSet = topics.stream().collect(Collectors.toSet());

        for (String topic : topicSet) {
            if (kafkaInternalTopic.contains(topic)) {
                topicPartitions.put(topic, Lists.newArrayList());
            }
        }
        topicSet.removeAll(topicPartitions.keySet());
        if (CollectionUtils.isEmpty(topics)) {
            return topicPartitions;
        }
        Map<String, TopicDescription> topicsMap = null;
        try {
            AdminClient adminClient = kafkaAdminManage.getKafkaAdmin(cluster.getClusterName());
            DescribeTopicsResult ret = adminClient.describeTopics(topicSet);
            topicsMap = ret.all().get();
        } catch (Exception e) {
            logger.error(MessageFormat.format("Find topic partition error:{0}", topics), e);
            return topicPartitions;
        }

        for (Map.Entry<String, TopicDescription> entry : topicsMap.entrySet()) {
            topicPartitions.put(entry.getKey(), Lists.transform(entry.getValue().partitions(), TopicPartitionInfo::partition));
        }
        return topicPartitions;
    }

    @Override
    public long getLogSize(ClusterMetadata cluster, String topic, int partition) {
        // java.util.ConcurrentModificationException: KafkaConsumer is not safe for multi-threaded access
        // 一个线程独立使用一个consumer.
        KafkaConsumer consumer = KConsumerPoolManager.getKafkaConsumerClient(cluster);
        try {
            Long offset = 0L;
            TopicPartition topicPartition = new TopicPartition(topic, partition);
            Map<TopicPartition, Long> mateDataMap = consumer.endOffsets(Lists.newArrayList(topicPartition));
            if (!mateDataMap.isEmpty()) {
                Iterator<TopicPartition> iter = mateDataMap.keySet().iterator();
                offset = mateDataMap.get(iter.next());
            }
            return offset;
        } finally {
            KConsumerPoolManager.release(cluster, consumer);
        }
    }

    @Override
    public Map<TopicPartition, Long> getKafkaOffset(ClusterMetadata cluster, String consumerGroup) {
        Map<TopicPartition, Long> resultMap = new HashMap<>();
        try {
            AdminClient adminClient = kafkaAdminManage.getKafkaAdmin(cluster.getClusterName());
            Map<TopicPartition, OffsetAndMetadata> consumerGroupOffset = adminClient.listConsumerGroupOffsets(consumerGroup)
                    .partitionsToOffsetAndMetadata()
                    .get();

            resultMap = consumerGroupOffset.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    item -> item.getValue().offset()));
        } catch (Exception e) {
            logger.error("Get consumer group offset error,", e);
        }
        return resultMap;
    }

    private JSONArray getKafkaMetadata(ClusterMetadata cluster, String group) {
        JSONArray consumerGroups = new JSONArray();
        try {
            AdminClient adminClient = kafkaAdminManage.getKafkaAdmin(cluster.getClusterName());

            DescribeConsumerGroupsResult cgs = adminClient
                    .describeConsumerGroups(Lists.newArrayList(group), new DescribeConsumerGroupsOptions().timeoutMs(5 * 60 * 1000));

            for (MemberDescription consumerSummary : cgs.describedGroups().get(group).get().members()) {
                Iterator<TopicPartition> topics = consumerSummary.assignment().topicPartitions().iterator();
                JSONObject topicSub = new JSONObject();
                JSONArray topicSubs = new JSONArray();
                while (topics.hasNext()) {
                    JSONObject object = new JSONObject();
                    TopicPartition topic = topics.next();
                    object.put("topic", topic.topic());
                    object.put("partition", topic.partition());
                    topicSubs.add(object);
                }
                topicSub.put("owner", consumerSummary.consumerId());
                topicSub.put("node", consumerSummary.host().replaceAll("/", ""));
                topicSub.put("topicSub", topicSubs);
                consumerGroups.add(topicSub);
            }
        } catch (Exception e) {
            logger.error("DescribeConsumerGroup error,", e);
        }
        return consumerGroups;
    }

}

