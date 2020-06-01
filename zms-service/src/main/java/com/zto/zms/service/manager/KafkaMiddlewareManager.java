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

package com.zto.zms.service.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zto.zms.common.ZmsException;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.service.kafka.reset.KafkaEarliestResetter;
import com.zto.zms.service.kafka.reset.KafkaLatestResetter;
import com.zto.zms.service.kafka.reset.KafkaOffsetResetter;
import com.zto.zms.service.kafka.reset.KafkaTimeResetter;
import com.zto.zms.zookeeper.ZmsZkClient;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * <p>Class: KafkaAdminManager</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/4/2
 **/
public class KafkaMiddlewareManager extends AbstractMessageMiddlewareManager {

    public static final Logger logger = LoggerFactory.getLogger(KafkaMiddlewareManager.class);

    private AdminClient adminClient;

    public KafkaMiddlewareManager(ZmsZkClient zmsZkClient, ClusterMetadata clusterMetadata) {
        super(zmsZkClient, clusterMetadata, null);
    }

    public KafkaMiddlewareManager(ZmsZkClient zmsZkClient, ClusterMetadata clusterMetadata, RollBack rollBack) {
        super(zmsZkClient, clusterMetadata, rollBack);
    }

    public AdminClient getKafkaAdmin() {
        return adminClient;
    }

    @Override
    public void create() {
        Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, clusterMetadata.getBootAddr());
        adminClient = AdminClient.create(properties);
    }

    @Override
    public void destroy() {
        if (null != adminClient) {
            adminClient.close();
            adminClient = null;
        }
        if (null != super.rollBack) {
            super.rollBack.destroy();
        }
    }

    @Override
    public void createTopic(String topic, int partitions, Integer replication) {
        if (isTopicExist(topic)) {
            logger.info(" topic {}  exist for cluster {}", topic, getClusterMetadata().getClusterName());
            return;
        }
        NewTopic newTopic = new NewTopic(topic, partitions, replication.shortValue());
        CreateTopicsResult createTopicsResult = adminClient.createTopics(Lists.newArrayList(newTopic));
        try {
            createTopicsResult.values().get(topic).get();
        } catch (Exception e) {
            logger.error("create topic error", e);
            throw new ZmsException(e.getMessage(), 9999);
        }
    }

    @Override
    public boolean isTopicExist(String topic) {
        DescribeTopicsResult result = adminClient.describeTopics(Lists.newArrayList(topic));
        try {
            TopicDescription topicDescription = result.values().get(topic).get();
            return null != topicDescription;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                return false;
            }
            logger.error(MessageFormat.format("check topic partitions error:{0}", topic), e);
            throw ZmsException.CHECK_TOPIC_PARTITIONS_EXCEPTION;
        } catch (Exception e) {
            logger.error(MessageFormat.format("check topic partitions error:{0}", topic), e);
            throw ZmsException.CHECK_TOPIC_PARTITIONS_EXCEPTION;
        }
    }

    @Override
    public void updateTopic(String topic, int partitions) {
        if (!checkPartition(topic, partitions)) {
            logger.error("can't update topic {} partition to {} for only increase partition", topic, partitions);
            throw new RuntimeException("Partitions can only be increase, can't decrease");
        }
        Map<String, NewPartitions> newPartitions = Maps.newHashMap();
        newPartitions.put(topic, NewPartitions.increaseTo(partitions));
        adminClient.createPartitions(newPartitions);
    }

    @Override
    public void deleteTopic(String topic) {
        try {
            DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(Lists.newArrayList(topic));
            deleteTopicsResult.values().get(topic).get();
        } catch (Exception e) {
            logger.error("delete topic error", e);
            throw new ZmsException(e.getMessage(), 9999);
        }
    }

    @Override
    public void createConsumerGroup(String consumerGroup, Boolean broadcast, Boolean consumerFrom) {
        if (broadcast) {
            throw ZmsException.BROADCAST_MUST_BE_FALSE;
        }
    }

    @Override
    public void updateConsumerGroup(String consumerGroup, Boolean broadcast, Boolean consumerFrom) {
        if (broadcast) {
            throw ZmsException.BROADCAST_MUST_BE_FALSE;
        }
    }

    @Override
    public void deleteConsumerGroup(String consumerName) {
    }

    @Override
    public void resetMessageToEarliest(String consumerGroup, String topic) {
        KafkaEarliestResetter earliestResetter = new KafkaEarliestResetter();
        String bootstraps = clusterMetadata.getBootAddr();
        logger.info("resetToEarliest cluster {} topic {} consumer {}", bootstraps, topic, consumerGroup);
        try {
            earliestResetter.execute(bootstraps, topic, consumerGroup);
        } catch (Exception e) {
            logger.error(MessageFormat.format("resetMessageToEarliest error,consumerGroup:{0},topic:{1}", consumerGroup, topic), e);
            throw new ZmsException(e.getMessage(), 9999);
        }
    }

    @Override
    public void resetMessageToLatest(String consumerGroup, String topic) {
        KafkaLatestResetter latestReSetter = new KafkaLatestResetter();
        String bootstraps = clusterMetadata.getBootAddr();
        logger.info("resetToLatest cluster {} topic {} consumer {}", bootstraps, topic, consumerGroup);
        try {
            latestReSetter.execute(bootstraps, topic, consumerGroup);
        } catch (Exception e) {
            logger.error(MessageFormat.format("resetMessageToLatest error,consumerGroup:{0},topic:{1}", consumerGroup, topic), e);
            throw new ZmsException(e.getMessage(), 9999);
        }
    }

    @Override
    public void resetMessageToOffset(String consumerGroup, String topic, long offset) {
        KafkaOffsetResetter offsetReSetter = new KafkaOffsetResetter(offset);
        String bootstraps = clusterMetadata.getBootAddr();
        logger.info("resetToOffset cluster {} topic {} consumer {} offset {}", bootstraps, topic, consumerGroup, offset);
        try {
            offsetReSetter.execute(bootstraps, topic, consumerGroup);
        } catch (Exception e) {
            logger.error(MessageFormat.format("resetMessageToOffset error,consumerGroup:{0},topic:{1}", consumerGroup, topic), e);
            throw new ZmsException(e.getMessage(), 9999);
        }
    }

    @Override
    public void resetMessageToTime(String consumerGroup, String topic, long time) {
        KafkaTimeResetter reSetter = new KafkaTimeResetter(time);
        String bootstraps = clusterMetadata.getBootAddr();
        logger.info("resetToTime cluster {} topic {} consumer {} time {}", bootstraps, topic, consumerGroup, time);
        try {
            reSetter.execute(bootstraps, topic, consumerGroup);
        } catch (Exception e) {
            logger.error(MessageFormat.format("resetMessageToTime error,consumerGroup:{0},topic:{1}", consumerGroup, topic), e);
            throw new ZmsException(e.getMessage(), 9999);
        }
    }

    @Override
    public void recoverTopic(String topic, int partitions, Integer replication) {
        if (!isTopicExist(topic)) {
            createTopic(topic, partitions, replication);
            logger.info("topic {} created to cluster {}", topic, getClusterMetadata().getClusterName());
        }
    }

    @Override
    public void recoverConsumerGroup(String consumerGroup, Boolean broadcast, Boolean consumerFrom) {
    }


    private boolean checkPartition(String topic, int partitions) {
        KafkaFuture<TopicDescription> topicDescriptionKafkaFuture = adminClient.describeTopics(Lists.newArrayList(topic)).values().get(topic);

        int originalPartitionNum;
        try {
            originalPartitionNum = topicDescriptionKafkaFuture.get().partitions().size();
        } catch (Exception e) {
            logger.error("check kafka partition error", e);
            throw new ZmsException(e.getMessage(), 9999);
        }
        return originalPartitionNum <= partitions;
    }
}

