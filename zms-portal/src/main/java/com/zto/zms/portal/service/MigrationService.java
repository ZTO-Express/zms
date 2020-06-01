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

package com.zto.zms.portal.service;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zto.zms.client.common.ConsumeFromWhere;
import com.zto.zms.common.ZmsException;
import com.zto.zms.metadata.ConsumerGroupMetadata;
import com.zto.zms.metadata.TopicMetadata;
import com.zto.zms.dal.domain.service.ZmsClusterServiceDTO;
import com.zto.zms.dal.mapper.*;
import com.zto.zms.dal.model.Consumer;
import com.zto.zms.dal.model.Topic;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.common.ZkRegister;
import com.zto.zms.portal.dto.MigrationDTO;
import com.zto.zms.portal.dto.topic.ZmsTopicConfigInfo;
import com.zto.zms.portal.service.topic.TopicService;
import com.zto.zms.service.manager.ZmsContextManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class MigrationService {

    private Logger logger = LoggerFactory.getLogger(MigrationService.class);

    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private TopicMapper topicMapper;
    @Autowired
    private TopicEnvironmentRefMapper topicEnvironmentRefMapper;
    @Autowired
    private ConsumerMapper consumerMapper;
    @Autowired
    private ConsumerEnvironmentRefMapper consumerEnvironmentRefMapper;
    @Autowired
    private TopicService topicService;
    @Autowired
    private ZkRegister zkRegister;
    @Autowired
    private ConsumerService consumerService;
    @Autowired
    private ExecutorService executorService;

    /**
     * 集群迁移Topic类型
     */
    public List<String> migrateClusterTopicZk(MigrationDTO dto) {
        // 校验原集群和目标集群
        checkCluster(dto);
        // 设置ZK环境
        ZmsContextManager.setEnv(dto.getEnvId());
        // 查询原集群下的topic
        List<Topic> topics = topicMapper.selectTopicsByServiceId(dto.getSrcClusterId());
        return migrateTopics(dto, topics);
    }

    /**
     * 集群迁移consumerGroup类型
     */
    public List<String> migrateClusterConsumerZk(MigrationDTO dto) {
        // 校验原集群和目标集群
        // 设置ZK环境
        ZmsContextManager.setEnv(dto.getEnvId());
        ZmsServiceEntity srcServiceEntity = zmsServiceMapper.getById(dto.getSrcClusterId());
        ZmsServiceEntity targetServiceEntity = zmsServiceMapper.getById(dto.getTargetClusterId());
        if (!srcServiceEntity.getServerType().equals(targetServiceEntity.getServerType())) {
            throw new ZmsException("集群类型必须保持一致", 401);
        }
        dto.setTargetCluster(targetServiceEntity.getServerName());
        // 查询原集群下的consumer
        List<Consumer> consumers = consumerMapper.selectConsumersByServiceId(dto.getSrcClusterId());
        return migrationConsumers(dto, consumers);
    }

    /**
     * 校验原集群和目标集群
     */
    private void checkCluster(MigrationDTO dto) {
        ZmsClusterServiceDTO targetCluster = zmsServiceMapper.getServiceAndEnvById(dto.getTargetClusterId());
        ZmsClusterServiceDTO srcCluster = zmsServiceMapper.getServiceAndEnvById(dto.getSrcClusterId());
        if (targetCluster == null) {
            throw new RuntimeException("targetCluster is not exist, clusterId=" + dto.getTargetClusterId());
        }
        if (srcCluster == null) {
            throw new RuntimeException("srcCluster is not exist, clusterId=" + dto.getTargetClusterId());
        }
        if (!targetCluster.getEnvironmentId().equals(srcCluster.getEnvironmentId())) {
            throw new RuntimeException("srcCluster and targetCluster's env must be the same, " +
                    "srcEnvName=" + srcCluster.getEnvironmentName() + ",targetEnvName=" + targetCluster.getEnvironmentName());
        }
        if (!targetCluster.getServerType().equalsIgnoreCase(srcCluster.getServerType())) {
            throw new RuntimeException("srcCluster and targetCluster's clusterType must be the same, " +
                    "srcClusterType=" + srcCluster.getServerType() + ",targetClusterType=" + targetCluster.getServerType());
        }
        dto.setTargetCluster(targetCluster.getServerName());
        dto.setSrcCluster(srcCluster.getServerName());
        dto.setEnvId(targetCluster.getEnvironmentId());
    }

    /**
     * 主题迁移&消费组迁移
     */
    public List<String> migrateZk(MigrationDTO dto) {
        ZmsServiceEntity targetCluster = zmsServiceMapper.getById(dto.getTargetClusterId());
        if (targetCluster == null) {
            throw new RuntimeException("targetCluster is not exist, clusterId=" + dto.getTargetClusterId());
        }
        // 设置ZK环境
        ZmsContextManager.setEnv(targetCluster.getEnvironmentId());
        dto.setTargetCluster(targetCluster.getServerName());
        List<String> failed = Lists.newArrayList();
        if (!StringUtils.isEmpty(dto.getTopics())) {
            List<Long> topicIds = Arrays.stream(dto.getTopics().split(","))
                    .map(Long::parseLong).collect(Collectors.toList());
            // 多个topic可能原集群不同，查询这些topic，在目标集群所在的环境中，存在的原集群ID
            List<Topic> topics = topicMapper.selectMigratedTopicsById(topicIds, targetCluster.getEnvironmentId());
            if (CollectionUtils.isEmpty(topics)) {
                logger.warn("no topics in DB to migrate");
                return failed;
            }
            failed = migrateTopics(dto, topics);
        } else if (!StringUtils.isEmpty(dto.getConsumers())) {
            List<Long> consumersIds = Arrays.stream(dto.getConsumers().split(","))
                    .map(Long::parseLong).collect(Collectors.toList());
            // 多个consumer可能原集群不同，查询这些consumer，在目标集群所在的环境中，存在的原集群ID
            List<Consumer> consumers = consumerMapper.selectMigratedConsumersById(consumersIds, targetCluster.getEnvironmentId());
            if (CollectionUtils.isEmpty(consumers)) {
                logger.warn("no consumers in DB to migrate");
                return failed;
            }
            failed = migrationConsumers(dto, consumers);
        }
        return failed;
    }

    /**
     * 迁移topic
     */
    private List<String> migrateTopics(MigrationDTO dto, List<Topic> topics) {
        List<Topic> successTopics = Lists.newArrayList();
        List<String> failed = Lists.newArrayList();
        ZmsTopicConfigInfo zmsTopicConfigInfo = new ZmsTopicConfigInfo();
        zmsTopicConfigInfo.setClusterName(dto.getTargetCluster());
        HashMap<String, Integer> replications = Maps.newHashMap();
        HashMap<String, Integer> topicsToCreate = Maps.newHashMap();
        topics.forEach(t -> {
            topicsToCreate.put(t.getName(), t.getPartitions());
            replications.put(t.getName(), t.getReplication());
        });
        zmsTopicConfigInfo.setTopics(topicsToCreate);
        zmsTopicConfigInfo.setReplications(replications);
        // 目标集群创建topic
        topicService.createTopic(zmsTopicConfigInfo);
        logger.info("migrateClusterTopicZk topics {} created to server", JSON.toJSONString(topicsToCreate));
        topics.forEach(topic -> {
            try {
                TopicMetadata topicMetadata = zkRegister.readTopicInfo(topic.getName());
                topicMetadata.getClusterMetadata().setClusterName(dto.getTargetCluster());
                zkRegister.registerTopicToZk(topicMetadata);
                successTopics.add(topic);
            } catch (Throwable throwable) {
                logger.error("migrate {} zk info error", topic, throwable);
                failed.add(topic.getName());
            }
        });
        if (!CollectionUtils.isEmpty(failed)) {
            logger.info("migrateClusterTopicZk topics {} failed crated to ZK", Utils.join(failed, ","));
        }
        asyncMigrateTopicsInDb(dto.getSrcClusterId(), dto.getTargetClusterId(), successTopics);
        return failed;
    }

    /**
     * topic迁移后的集群ID更新到数据库中
     */
    private void asyncMigrateTopicsInDb(Integer srcClusterId, Integer targetClusterId, List<Topic> successTopics) {
        if (!CollectionUtils.isEmpty(successTopics)) {
            executorService.execute(() -> {
                try {
                    if (null != srcClusterId) {
                        // 集群迁移
                        topicEnvironmentRefMapper.updateMigrateTopics(srcClusterId, targetClusterId,
                                successTopics.stream().map(Topic::getId).collect(Collectors.toList()));
                    } else {
                        // 主题迁移
                        successTopics.forEach(item ->
                                topicEnvironmentRefMapper.updateMigrateTopics(item.getClusterId().intValue(), targetClusterId,
                                        Lists.newArrayList(item.getId())));
                    }
                } catch (Throwable ex) {
                    logger.error("migrate to db failed : " + successTopics.stream().map(Topic::getName).collect(Collectors.joining(",")), ex);
                }
            });
        }
    }


    /**
     * 迁移consumerGroup
     */
    private List<String> migrationConsumers(MigrationDTO dto, List<Consumer> consumers) {
        List<String> failed = Lists.newArrayList();
        List<Consumer> successConsumers = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(consumers)) {
            consumers.forEach(c -> {
                try {
                    ConsumerGroupMetadata consumerGroupMetadata = zkRegister.readConsumerInfo(c.getName());
                    consumerGroupMetadata.getClusterMetadata().setClusterName(dto.getTargetCluster());

                    boolean consumeFromMinEnable;
                    // 迁移consumer的时候，设置从头开始消费，避免消息丢失
                    if (ConsumeFromWhere.LATEST.getName().equalsIgnoreCase(dto.getConsumePosition())) {
                        consumeFromMinEnable = false;
                        consumerGroupMetadata.setConsumeFrom(ConsumeFromWhere.LATEST.getName());
                    } else {
                        consumeFromMinEnable = true;
                        consumerGroupMetadata.setConsumeFrom(ConsumeFromWhere.EARLIEST.getName());
                    }
                    consumerService.createAndUpdateSubscriptionGroupConfig(dto.getTargetCluster(), c.getName(), c.getBroadcast(), consumeFromMinEnable);
                    logger.info(c.getName() + " created to mq broker");

                    zkRegister.registerConsumerToZk(consumerGroupMetadata);
                    logger.info(c.getName() + " updated to zookeeper");
                    successConsumers.add(c);
                } catch (Throwable throwable) {
                    logger.error("migrate {} zk info error", c.getName(), throwable);
                    failed.add(c.getName());
                }
            });
        }
        asyncMigrateConsumersInDb(dto.getSrcClusterId(), dto.getTargetClusterId(), successConsumers);
        return failed;
    }

    /**
     * consumer迁移后的集群ID更新到数据库中
     */
    private void asyncMigrateConsumersInDb(Integer srcClusterId, Integer targetClusterId, List<Consumer> successConsumers) {
        if (!CollectionUtils.isEmpty(successConsumers)) {
            executorService.execute(() -> {
                try {
                    if (null != srcClusterId) {
                        // 集群迁移
                        consumerEnvironmentRefMapper.updateMigrateConsumers(srcClusterId, targetClusterId,
                                successConsumers.stream().map(Consumer::getId).collect(Collectors.toList()));
                    } else {
                        // 消费迁移
                        successConsumers.forEach(item ->
                                consumerEnvironmentRefMapper.updateMigrateConsumers(item.getClusterId().intValue(), targetClusterId,
                                        Lists.newArrayList(item.getId())));
                    }
                } catch (Throwable ex) {
                    logger.error("migrate to db failed : " + successConsumers.stream().map(Consumer::getName).collect(Collectors.joining(",")), ex);
                }
            });
        }
    }

}

