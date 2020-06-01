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
import com.zto.zms.common.ZmsException;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.service.mq.MQAdminExtImpl;
import com.zto.zms.zookeeper.ZmsZkClient;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.KVTable;
import org.apache.rocketmq.common.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.common.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.apache.rocketmq.tools.command.topic.DeleteTopicSubCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>Class: RocketmqAdminManager</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/4/2
 **/
public class RocketMqMiddlewareManager extends AbstractMessageMiddlewareManager {

    public static final Logger logger = LoggerFactory.getLogger(RocketMqMiddlewareManager.class);

    private DefaultMQAdminExt defaultMQAdminExt;

    public RocketMqMiddlewareManager(ZmsZkClient zmsZkClient, ClusterMetadata clusterMetadata) {
        this(zmsZkClient, clusterMetadata, null);
    }

    public RocketMqMiddlewareManager(ZmsZkClient zmsZkClient, ClusterMetadata clusterMetadata, RollBack rollBack) {
        super(zmsZkClient, clusterMetadata, rollBack);
    }

    public DefaultMQAdminExt getMqAdmin() {
        return defaultMQAdminExt;
    }

    @Override
    public void create() {
        try {
            defaultMQAdminExt = new MQAdminExtImpl();
            defaultMQAdminExt.setNamesrvAddr(clusterMetadata.getBootAddr());
            defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
            defaultMQAdminExt.setVipChannelEnabled(false);
            defaultMQAdminExt.start();
        } catch (MQClientException e) {
            logger.error("Failed to create MQAdminExtImpl:" + clusterMetadata, e);
            throw new ZmsException(e.getErrorMessage(), 9999);
        }
    }

    @Override
    public void destroy() {
        if (null != defaultMQAdminExt) {
            defaultMQAdminExt.shutdown();
            defaultMQAdminExt = null;
        }
        if (null != super.rollBack) {
            super.rollBack.destroy();
        }
    }


    @Override
    public void createTopic(String topic, int partitions, Integer replication) {
        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setTopicName(topic);
        topicConfig.setWriteQueueNums(partitions);
        topicConfig.setReadQueueNums(partitions);
        topicConfig.setPerm(6);
        List<String> failedServers = Lists.newArrayList();
        try {
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();

            Set<String> brokerNames = clusterInfo.getClusterAddrTable().get(clusterName);
            for (String brokerName : brokerNames) {
                defaultMQAdminExt.createAndUpdateTopicConfig(clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr(), topicConfig);
                logger.info("topic {} created to {}", topicConfig.getTopicName(), clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr());
            }
            TopicRouteData topicRouteData;
            Thread.sleep(1000);
            try {
                topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(topic);
            } catch (Throwable ex) {
                logger.warn("examine topic failed", ex);
                Thread.sleep(3000);
                topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(topic);
            }
            List<String> createdBrokers = topicRouteData.getBrokerDatas().stream().map(t -> {
                logger.info("{} created in {}", topicConfig.getTopicName(), t.selectBrokerAddr());
                return t.getBrokerName();
            }).collect(Collectors.toList());

            for (String brokerName : brokerNames) {
                if (!createdBrokers.contains(brokerName)) {
                    failedServers.add(brokerName);
                }
            }
        } catch (Exception err) {
            logger.error("create topic error", err);
            throw new ZmsException(err.getMessage(), 9999);
        }
        if (!CollectionUtils.isEmpty(failedServers)) {
            logger.error("Failed to create topic resource,:{},cluster:{}", failedServers, clusterName);
            throw new ZmsException("Failed to create topic resource", 9999);
        }
    }

    @Override
    public boolean isTopicExist(String topic) {
        try {
            TopicRouteData topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(topic);
            if (null != topicRouteData) {
                return true;
            }
            return false;
        } catch (MQClientException e) {
            if (e.getResponseCode() == ResponseCode.TOPIC_NOT_EXIST) {
                return false;
            }
            logger.error("Check topic error:" + topic, e);
            throw new ZmsException("Check topic error:" + topic, 9999);
        } catch (Exception e) {
            logger.error("Check topic error:" + topic, e);
            throw new ZmsException(e.getMessage(), 9999);
        }
    }

    @Override
    public void updateTopic(String topic, int partitions) {
        createTopic(topic, partitions, null);
    }

    @Override
    public void deleteTopic(String topic) {
        try {
            Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
            defaultMQAdminExt.deleteTopicInBroker(masterSet, topic);
            String[] ns = clusterMetadata.getBootAddr().split(";");
            Set<String> nameServerSet = new HashSet(Arrays.asList(ns));
            defaultMQAdminExt.deleteTopicInNameServer(nameServerSet, topic);
        } catch (Exception e) {
            logger.error("delete topic error", e);
            throw new ZmsException(e.getMessage(), 9999);
        }
    }

    @Override
    public void createConsumerGroup(String consumerGroup, Boolean broadcast, Boolean consumerFrom) {
        SubscriptionGroupConfig subscriptionGroupConfig = new SubscriptionGroupConfig();
        subscriptionGroupConfig.setGroupName(consumerGroup);
        subscriptionGroupConfig.setConsumeBroadcastEnable(broadcast);
        subscriptionGroupConfig.setConsumeFromMinEnable(consumerFrom);

        List<String> failedServers = Lists.newArrayList();
        try {
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            Set<String> brokerNames = clusterInfo.getClusterAddrTable().get(clusterName);
            for (String brokerName : brokerNames) {
                String brokerAddr = clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr();
                defaultMQAdminExt.createAndUpdateSubscriptionGroupConfig(brokerAddr, subscriptionGroupConfig);
                logger.info("consumerGroup {} created to {}", consumerGroup, clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr());
            }
            //等待创建subscriptiongroup的异常操作完成
            Thread.sleep(1000);

            for (String brokerName : brokerNames) {
                String brokerAddr = clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr();
                SubscriptionGroupWrapper allSubscriptionGroup = defaultMQAdminExt.getAllSubscriptionGroup(brokerAddr, 1000);
                if (!allSubscriptionGroup.getSubscriptionGroupTable().containsKey(consumerGroup)) {
                    failedServers.add(clusterInfo.getBrokerAddrTable().get(brokerName).selectBrokerAddr());
                } else {
                    logger.info("{} created for server {}", consumerGroup, brokerAddr);
                }
            }
        } catch (Exception e) {
            logger.error("create or update consumer {} error", consumerGroup, e);
            throw new ZmsException(e.getMessage(), 9999);
        }
        if (!CollectionUtils.isEmpty(failedServers)) {
            logger.error("create or update consumer {} error:{}", consumerGroup, failedServers);
            throw new ZmsException("create consumerGroup in these brokers failed, brokerLst=" + failedServers.toString(), 9999);
        }
    }

    @Override
    public void updateConsumerGroup(String consumerGroup, Boolean broadcast, Boolean consumerFrom) {
        createConsumerGroup(consumerGroup, broadcast, consumerFrom);
    }

    @Override
    public void deleteConsumerGroup(String consumerName) {
        try {
            Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, clusterName);
            for (String master : masterSet) {
                defaultMQAdminExt.deleteSubscriptionGroup(master, consumerName);
                logger.info("addr={} groupName={} deleted", master, consumerName);
            }
            DeleteTopicSubCommand.deleteTopic(defaultMQAdminExt, clusterName, MixAll.RETRY_GROUP_TOPIC_PREFIX
                    + consumerName);
            DeleteTopicSubCommand.deleteTopic(defaultMQAdminExt, clusterName, MixAll.DLQ_GROUP_TOPIC_PREFIX
                    + consumerName);
        } catch (Exception e) {
            logger.error(MessageFormat.format("delete consumer {0} error", consumerName), e);
            throw new ZmsException(e.getMessage(), 9999);
        }
    }

    @Override
    public void resetMessageToEarliest(String consumerGroup, String topic) {
        throw ZmsException.ROCKET_MQ_NOT_SUPPORT_RESET_TO_EARLIEST;
    }

    @Override
    public void resetMessageToLatest(String consumerGroup, String topic) {
        throw ZmsException.ROCKET_MQ_NOT_SUPPORT_RESET_TO_LATEST;
    }

    @Override
    public void resetMessageToOffset(String consumerGroup, String topic, long offset) {
        throw ZmsException.ROCKET_MQ_NOT_SUPPORT_RESET_TO_OFFSET;
    }

    @Override
    public void resetMessageToTime(String consumerGroup, String topic, long time) {
        try {
            ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();
            Collection<BrokerData> brokerAdd = clusterInfo.getBrokerAddrTable().values();
            if (CollectionUtils.isEmpty(brokerAdd)) {
                throw new ZmsException("brokerAddr is empty.", 401);
            }
            BrokerData brokerData = brokerAdd.stream().findFirst().get();
            String brokerAddr = brokerData.getBrokerAddrs().values().stream().findFirst().get();
            KVTable kvTable = defaultMQAdminExt.fetchBrokerRuntimeStats(brokerAddr);
            String version = kvTable.getTable().get("brokerVersion");
            if (Integer.parseInt(version) < MQVersion.Version.V3_8_1_SNAPSHOT.ordinal()) {
                defaultMQAdminExt.resetOffsetByTimestampOld(consumerGroup, topic, time, true);
            } else {
                defaultMQAdminExt.resetOffsetNew(consumerGroup, topic, time);
            }
        } catch (ZmsException e) {
            throw e;
        } catch (Exception e) {
            logger.error(MessageFormat.format("resetMessageToTime error,consumerGroup:{0},topic:{1}", consumerGroup, topic), e);
            throw new ZmsException(e.getMessage(), 9999);
        }
    }

    @Override
    public void recoverTopic(String topic, int partitions, Integer replication) {
        if (isTopicExist(topic)) {
            return;
        }
        try {
            createTopic(topic, partitions, replication);
            logger.info("topic {} created to cluster {}", topic, clusterName);
        } catch (Exception e) {
            logger.error("recoverTopic error:" + topic, e);
        }
    }

    @Override
    public void recoverConsumerGroup(String consumerGroup, Boolean broadcast, Boolean consumerFrom) {
        if (isSubscriptionGroupExist(consumerGroup)) {
            return;
        }
        try {
            createConsumerGroup(consumerGroup, false, true);
        } catch (Exception e) {
            logger.error(MessageFormat.format("topic {} created to cluster {}", consumerGroup, clusterName), e);
        }
    }


    private boolean isSubscriptionGroupExist(String subscriptionGroup) {
        try {
            defaultMQAdminExt.examineConsumeStats(subscriptionGroup);
            return true;
        } catch (Exception e) {
            logger.error("examine subscriptionGroup {} status info err", subscriptionGroup, e);
            return false;
        }
    }

}

