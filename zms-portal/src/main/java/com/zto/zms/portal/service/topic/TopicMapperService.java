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

package com.zto.zms.portal.service.topic;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zto.zms.client.common.ZmsMessage;
import com.zto.zms.common.BrokerType;
import com.zto.zms.common.ZmsException;
import com.zto.zms.common.ZmsType;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.metadata.TopicMetadata;
import com.zto.zms.service.domain.page.PageResult;
import com.zto.zms.client.producer.Producer;
import com.zto.zms.client.producer.SendResult;
import com.zto.zms.utils.Assert;
import com.zto.zms.dal.domain.alert.EnvironmentRefDTO;
import com.zto.zms.dal.mapper.AlertRuleDetailMapper;
import com.zto.zms.dal.mapper.TopicEnvironmentRefMapper;
import com.zto.zms.dal.mapper.TopicMapper;
import com.zto.zms.dal.mapper.ZmsServiceMapper;
import com.zto.zms.dal.model.AlertRuleConfig;
import com.zto.zms.dal.model.Topic;
import com.zto.zms.dal.model.TopicEnvironmentRef;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.common.ResourceStatus;
import com.zto.zms.portal.common.ZkRegister;
import com.zto.zms.portal.common.ZmsServiceStatusEnum;
import com.zto.zms.portal.dto.GatedDTO;
import com.zto.zms.portal.dto.topic.*;
import com.zto.zms.portal.filter.LdapUser;
import com.zto.zms.portal.kafka.TopicLoader;
import com.zto.zms.portal.manage.MultiEnvironmentProducerManager;
import com.zto.zms.portal.rocketmq.RocketmqTopicSummary;
import com.zto.zms.portal.rocketmq.StatsAllResultDto;
import com.zto.zms.portal.rocketmq.TopicStatusDto;
import com.zto.zms.portal.service.AlertService;
import com.zto.zms.portal.service.StatsAllService;
import com.zto.zms.service.domain.topic.BackupTopicMetadataDTO;
import com.zto.zms.service.domain.topic.TopicEnvironmentInfoVo;
import com.zto.zms.service.manager.ZmsContextManager;
import com.zto.zms.service.router.ZkClientRouter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by liangyong on 2019/3/4.
 */
@Service
public class TopicMapperService {

    public static final Logger logger = LoggerFactory.getLogger(TopicMapperService.class);

    @Autowired
    private TopicMapper topicMapper;
    @Autowired
    private ZkRegister zkRegister;
    @Autowired
    private TopicService topicService;
    @Autowired
    private StatsAllService statsAllService;
    @Autowired
    private TopicStatusService topicStatusService;
    @Autowired
    private AlertRuleDetailMapper mapper;
    @Autowired
    private AlertService alertService;
    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private TopicEnvironmentRefMapper environmentRefMapper;
    @Autowired
    private ZkClientRouter zkClientRouter;
    @Autowired
    private MultiEnvironmentProducerManager multiEnvironmentProducerManager;
    @Autowired
    private ExecutorService executorService;

    public List<Topic> getTopicsByCluster(Integer envId, Integer clusterId, String clusterName, String clusterType, String applicant) {
        return topicMapper.selectTopics(null, 2, applicant, null, envId, clusterId, clusterName, clusterType);
    }

    public List<Topic> getTopicsByClusterId(Integer clusterId) {
        return topicMapper.selectTopicsByServiceId(clusterId);
    }

    /**
     * 主题列表和主题迁移页面调用
     */
    public PageResult<TopicDTO> queryTopicsPage(TopicQueryDTO topicQueryDto) {
        //主题列表
        PageHelper.startPage(topicQueryDto.getCurrentPage(), topicQueryDto.getPageSize());
        List<Topic> resultList = topicMapper.selectTopicsByKeyWord(topicQueryDto.getEnvId(), topicQueryDto.getClusterId(), topicQueryDto.getName(), StringUtils.isNotEmpty(topicQueryDto.getKeyWord()) ? topicQueryDto.getKeyWord().toLowerCase() : null, topicQueryDto.getStatus(), topicQueryDto.getId());
        PageInfo<Topic> topicPage = new PageInfo<>(resultList);

        if (CollectionUtils.isEmpty(resultList)) {
            return new PageResult<>(topicQueryDto.getCurrentPage(), topicQueryDto.getPageSize());
        }
        List<Long> topicIds = resultList.stream().map(Topic::getId).collect(Collectors.toList());
        //主题资源列表
        List<TopicEnvironmentRef> environmentRefList = environmentRefMapper.listByTopicId(topicIds);
        Map<Long, List<TopicEnvironmentRef>> environmentRefMap = environmentRefList.stream()
                .collect(Collectors.groupingBy(TopicEnvironmentRef::getTopicId));
        List<Integer> gatedServiceIds = environmentRefList.stream().filter(item -> null != item.getGatedServiceId()).map(TopicEnvironmentRef::getGatedServiceId).collect(Collectors.toList());
        List<Integer> serviceIds = environmentRefList.stream().filter(item -> null != item.getServiceId()).map(TopicEnvironmentRef::getServiceId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(gatedServiceIds)) {
            serviceIds.addAll(gatedServiceIds);
        }
        //集群
        List<ZmsServiceEntity> zmsServiceEntities = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(serviceIds)) {
            zmsServiceEntities = zmsServiceMapper.listByIds(serviceIds);
        }
        Map<Integer, ZmsServiceEntity> serviceIdNameMap = zmsServiceEntities.stream()
                .collect(Collectors.toMap(ZmsServiceEntity::getId, item -> item));
        //环境信息
        List<TopicDTO> topicDTOS = resultList.stream().map(topic -> {
            TopicDTO topicDto = new TopicDTO();
            BeanUtils.copyProperties(topic, topicDto);
            //各环境资源
            Long topicId = topic.getId();
            if (environmentRefMap.containsKey(topicId)) {
                List<TopicEnvironmentInfoVo> topicEnvironmentRefs = environmentRefMap.get(topicId).stream().map(item -> {
                            TopicEnvironmentInfoVo environmentInfoVo = new TopicEnvironmentInfoVo();
                            environmentInfoVo.setEnvironmentId(item.getEnvironmentId());
                            environmentInfoVo.setEnvironmentName(item.getEnvironmentName());
                            if (item.getServiceId() != null && serviceIdNameMap.containsKey(item.getServiceId())) {
                                environmentInfoVo.setServiceId(item.getServiceId());
                                environmentInfoVo.setServerName(serviceIdNameMap.get(item.getServiceId()).getServerName());
                                environmentInfoVo.setServerType(serviceIdNameMap.get(item.getServiceId()).getServerType());
                            }
                            environmentInfoVo.setGatedIps(item.getGatedIps());
                            environmentInfoVo.setGatedServiceId(item.getGatedServiceId());
                            if (null != item.getGatedServiceId()) {
                                environmentInfoVo.setGatedServiceName(serviceIdNameMap.get(item.getGatedServiceId()).getServerName());
                            }
                            return environmentInfoVo;
                        }
                ).collect(Collectors.toList());
                topicDto.setEnvironments(topicEnvironmentRefs);
            }
            return topicDto;
        }).collect(Collectors.toList());
        return new PageResult<>(topicQueryDto.getCurrentPage(), topicQueryDto.getPageSize(), topicPage.getTotal(), topicDTOS);
    }

    /**
     * 新增主题
     */
    @Transactional(rollbackFor = Exception.class)
    public int addTopic(TopicDTO topicDto, String operator) {
        boolean isUnique = uniqueTopicCheck(topicDto.getName());
        Assert.that(isUnique, "主题名称重复");
        Topic topic = new Topic();
        BeanUtils.copyProperties(topicDto, topic);
        topic.setStatus(ResourceStatus.CREATE_NEW.getStatus());
        int count = topicMapper.insert(topic);
        topicDto.getEnvironments().forEach(item -> installTopicEnvRef(operator, topic, null, item.getEnvironmentId()));
        return count;
    }

    private TopicEnvironmentRef installTopicEnvRef(String operator, Topic topic, Integer clusterServiceId, int envId) {
        TopicEnvironmentRef topicEnvironmentRef = new TopicEnvironmentRef();
        topicEnvironmentRef.setCreator(operator);
        topicEnvironmentRef.setModifier(operator);
        topicEnvironmentRef.setEnvironmentId(envId);
        topicEnvironmentRef.setTopicId(topic.getId());
        topicEnvironmentRef.setServiceId(clusterServiceId);
        environmentRefMapper.insert(topicEnvironmentRef);
        return topicEnvironmentRef;
    }

    public Integer updateTopic(TopicDTO topicDto, LdapUser ldapUser) {
        Topic topic = topicMapper.selectByPrimaryKey(topicDto.getId());
        //权限判断
        if (!ldapUser.isAdmin() && StringUtils.compare(topic.getApplicant(), ldapUser.getRealName()) != 0) {
            throw ZmsException.NO_PERMISSION_MODIFY;
        }
        List<Topic> topicList = topicMapper.selectTopicName(topicDto.getName());
        if (topicList.size() > 0 && !topicList.get(0).getId().equals(topicDto.getId())) {
            throw new ZmsException("主题名称重复", 401);
        }
        //审批之后又修改
        if (!ResourceStatus.CREATE_NEW.getStatus().equals(topic.getStatus())) {
            if (!topicDto.getName().equals(topic.getName())) {
                throw new ZmsException("审批后主题名不可以修改", 401);
            }
        } else {
            //审核之前可以重新选择环境
            environmentRefMapper.deleteByTopicId(topic.getId());
            topicDto.getEnvironments().forEach(item -> installTopicEnvRef(ldapUser.getRealName(), topic, null, item.getEnvironmentId()));
        }
        Topic newTopic = new Topic();
        BeanUtils.copyProperties(topicDto, newTopic);
        return topicMapper.updateByPrimaryKeySelective(newTopic);
    }


    /**
     * 删除主题
     */
    public Integer deleteTopic(Long id) {
        Topic topic = topicMapper.selectByPrimaryKey(id);
        //主题资源
        List<TopicEnvironmentRef> environmentRefs = environmentRefMapper.listByTopicId(Lists.newArrayList(topic.getId()));
        //集群
        List<Integer> serviceIds = environmentRefs.stream()
                .filter(item -> null != item.getServiceId())
                .map(TopicEnvironmentRef::getServiceId)
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(serviceIds)) {
            List<ZmsServiceEntity> zmsServiceEntities = zmsServiceMapper.listByIds(serviceIds);
            for (ZmsServiceEntity service : zmsServiceEntities) {
                Integer envId = service.getEnvironmentId();
                ZmsContextManager.setEnv(envId);
                if (zkClientRouter.currentZkClient().checkPath(ZmsType.TOPIC, topic.getName())) {
                    ZmsTopicDeleteInfo deleteInfo = new ZmsTopicDeleteInfo();
                    deleteInfo.setClusterName(service.getServerName());
                    deleteInfo.setTopics(Lists.newArrayList(topic.getName()));
                    zkRegister.deleteTopic(topic.getName());
                    topicService.deleteTopic(deleteInfo);
                }
            }
        }
        //删除告警信息
        List<AlertRuleConfig> alertRules = mapper.listByTopic(topic.getName());
        if (!CollectionUtils.isEmpty(alertRules)) {
            alertRules.forEach(item -> alertService.delete(item));
        }
        return topicMapper.deleteById(topic.getId());
    }

    /**
     * 审核主题
     */
    public List<Integer> approveTopic(TopicDTO topicDto, String userName) {
        Topic topic = topicMapper.selectByPrimaryKey(topicDto.getId());
        List<TopicEnvironmentInfoVo> environmentInfoVos = topicDto.getEnvironments();
        List<ZmsServiceEntity> serviceList = listZmsServiceEntities(environmentInfoVos);
        long countCluster = serviceList.stream().map(ZmsServiceEntity::getServerType).distinct().count();
        Assert.that(countCluster == 1, "主题不同环境只能选择同一类型的集群");
        Map<Integer, ZmsServiceEntity> serviceIdNameMap = serviceList.stream().collect(Collectors.toMap(ZmsServiceEntity::getId, item -> item));
        //本地集群配置
        Map<Integer, TopicEnvironmentRef> localEnvIdServiceIdMap = environmentRefMapper.listByTopicId(Lists.newArrayList(topicDto.getId())).stream()
                .collect(Collectors.toMap(TopicEnvironmentRef::getEnvironmentId, item -> item));

        topicMapper.updateById(topicDto.getId(), topicDto.getPartitions(), topicDto.getReplication());
        List<Integer> failEnv = Lists.newArrayList();
        //初始化主题资源
        for (TopicEnvironmentInfoVo environmentInfoVo : environmentInfoVos) {
            Integer envId = environmentInfoVo.getEnvironmentId();
            Integer serviceId = environmentInfoVo.getServiceId();
            Integer localClusterId = localEnvIdServiceIdMap.get(envId).getServiceId();
            ZmsServiceEntity service = serviceIdNameMap.get(serviceId);
            try {
                //新增Zookeeper、集群资源
                boolean updated = initTopicResource(envId, topicDto.getPartitions(), topicDto.getReplication(), topic, localClusterId, service);
                if (!updated) {
                    continue;
                }
                // 当前环境集群改变
                environmentRefMapper.updateCluster(localEnvIdServiceIdMap.get(envId).getId(), service.getId(), userName);
            } catch (Exception e) {
                logger.error(MessageFormat.format("The current environmental subject approval failed:{0}", envId), e);
                failEnv.add(envId);
            }
        }
        Assert.that(failEnv.size() < environmentInfoVos.size(), "主题审批失败");
        topicMapper.updateStatusById(topicDto.getId(), ResourceStatus.CREATE_APPROVED.getStatus());
        return failEnv;
    }

    private List<ZmsServiceEntity> listZmsServiceEntities(List<TopicEnvironmentInfoVo> environmentInfoVos) {
        List<Integer> serviceIds = environmentInfoVos.stream().map(TopicEnvironmentInfoVo::getServiceId).collect(Collectors.toList());
        //审核集群的信息
        List<ZmsServiceEntity> serviceList = zmsServiceMapper.listClustersByServiceIds(serviceIds)
                .stream()
                .filter(item -> ZmsServiceStatusEnum.ENABLE.name().equals(item.getServerStatus()))
                .collect(Collectors.toList());
        Assert.that(serviceList.size() == serviceIds.size(), "无效的集群");
        return serviceList;
    }

    private boolean initTopicResource(Integer envId, Integer partitions, Integer replication, Topic topic, Integer localClusterServiceId, ZmsServiceEntity service) {
        ZmsContextManager.setEnv(envId);
        String clusterName = service.getServerName();
        String topicName = topic.getName();

        ZmsTopicConfigInfo zmsTopicConfigInfo = assembleZmsTopicConfigInfo(partitions, replication, clusterName, topicName);
        //新增或修改主题集群
        if (ResourceStatus.CREATE_NEW.getStatus().equals(topic.getStatus())
                || !service.getId().equals(localClusterServiceId)) {
            topicService.createTopic(zmsTopicConfigInfo);
            zkRegister.registerTopicToZk(clusterName, topicName, BrokerType.parse(service.getServerType()));
        } else if (!topic.getPartitions().equals(partitions)) {
            //partition变化不更改ZMS_ZK
            topicService.updateTopic(zmsTopicConfigInfo);
        } else {
            return false;
        }
        return true;
    }

    private ZmsTopicConfigInfo assembleZmsTopicConfigInfo(Integer partitions, Integer replication, String clusterName, String topicName) {
        ZmsTopicConfigInfo zmsTopicConfigInfo = new ZmsTopicConfigInfo();
        zmsTopicConfigInfo.setClusterName(clusterName);
        HashMap<String, Integer> topics = Maps.newHashMap();
        topics.put(topicName, partitions);
        zmsTopicConfigInfo.setTopics(topics);
        HashMap<String, Integer> replications = Maps.newHashMap();
        replications.put(topicName, replication);
        zmsTopicConfigInfo.setReplications(replications);
        return zmsTopicConfigInfo;
    }

    public boolean uniqueTopicCheck(String topicName) {
        return topicMapper.uniqueTopicCheck(topicName);
    }

    /**
     * 更新灰度集群
     */
    public Integer updateGated(GatedDTO dto, String userName) {
        Topic topic = topicMapper.selectByPrimaryKey(dto.getId());
        String topicName = topic.getName();

        List<TopicEnvironmentRef> environmentRefs = environmentRefMapper.listByTopicId(Lists.newArrayList(dto.getId()));
        String clusterType = environmentRefs.stream().map(TopicEnvironmentRef::getClusterType).distinct().findFirst().get();
        Map<Integer, TopicEnvironmentRef> envIdServiceIdMap = environmentRefs.stream()
                .collect(Collectors.toMap(TopicEnvironmentRef::getEnvironmentId, item -> item));
        List<TopicEnvironmentInfoVo> environmentInfoVos = dto.getEnvironments();
        //灰度集群
        List<ZmsServiceEntity> serviceList = listZmsServiceEntities(environmentInfoVos);
        List<Integer> gateServiceIds = environmentInfoVos.stream().map(TopicEnvironmentInfoVo::getGatedServiceId).collect(Collectors.toList());
        long countCluster = serviceList.stream().filter(item -> clusterType.equals(item.getServerType())).count();
        Assert.that(countCluster == gateServiceIds.size(), "灰度集群类型要和主题集群类型保持一致");

        Map<Integer, String> serviceIdNameMap = serviceList.stream()
                .collect(Collectors.toMap(ZmsServiceEntity::getId, ZmsServiceEntity::getServerName));
        //初始化灰度集群资源
        for (TopicEnvironmentInfoVo environmentInfoVo : environmentInfoVos) {
            Integer envId = environmentInfoVo.getEnvironmentId();
            Integer id = envIdServiceIdMap.get(envId).getId();
            //修改zookeeper
            updateGateCluster(envId, topicName, environmentInfoVo.getGatedIps(), serviceIdNameMap.get(environmentInfoVo.getGatedServiceId()));
            environmentRefMapper.updateGateCluster(id, environmentInfoVo.getGatedServiceId(), environmentInfoVo.getGatedIps(), userName);
        }
        return environmentInfoVos.size();
    }


    private void updateGateCluster(int envId, String topicName, String gatedIps, String gateClusterName) {
        ZmsContextManager.setEnv(envId);
        TopicMetadata topicMetadata = zkRegister.readTopicInfo(topicName);
        //如果一致，跳过
        if (null != topicMetadata
                && null != topicMetadata.getGatedCluster()
                && Objects.equals(topicMetadata.getGatedCluster().getClusterName(), gateClusterName)
                && Objects.equals(topicMetadata.getGatedIps(), gatedIps)) {
            return;
        }
        ClusterMetadata metadata = new ClusterMetadata();
        metadata.setClusterName(gateClusterName);
        if (topicMetadata == null) {
            topicMetadata = new TopicMetadata();
        }
        topicMetadata.setGatedCluster(metadata);
        topicMetadata.setGatedIps(gatedIps);
        //注册Zookeeper
        zkRegister.registerTopicToZk(topicMetadata);
    }

    /**
     * 发送消息
     */
    public SendResult sendMsg(SendTopicDTO dto) {
        //当前环境消息生产者
        Producer producer = multiEnvironmentProducerManager.getCurrentProducer(dto.getEnvId(), dto.getTopic());
        ZmsMessage message = new ZmsMessage();
        message.setPayload((dto.getMsg()).getBytes());
        message.setKey(dto.getKeys());
        message.setTags(dto.getTags());
        return producer.syncSend(message);
    }

    /**
     * 多环境同步
     */
    public boolean syncTopic(Long topicId, List<TopicEnvironmentInfoVo> environments, String operator) {
        Topic topic = topicMapper.selectByPrimaryKey(topicId);

        List<Integer> localEnvIds = environmentRefMapper.listByTopicId(Lists.newArrayList(topicId))
                .stream().map(TopicEnvironmentRef::getEnvironmentId).collect(Collectors.toList());
        //新增环境
        List<TopicEnvironmentInfoVo> addEnvList = environments.stream().filter(item -> !localEnvIds.contains(item.getEnvironmentId())).collect(Collectors.toList());
        List<Integer> serviceIds = addEnvList.stream().map(TopicEnvironmentInfoVo::getServiceId).collect(Collectors.toList());

        Assert.that(!CollectionUtils.isEmpty(addEnvList), "请选择将主题同步到哪些环境");
        //审核集群的信息
        Map<Integer, ZmsServiceEntity> serviceIdServiceMap = zmsServiceMapper.listClustersByServiceIds(serviceIds)
                .stream()
                .filter(item -> ZmsServiceStatusEnum.ENABLE.name().equals(item.getServerStatus()))
                .collect(Collectors.toMap(ZmsServiceEntity::getId, item -> item));

        List<TopicEnvironmentRef> topicEnvironmentRefs = Lists.newArrayList();

        addEnvList.forEach(item -> {
            TopicEnvironmentRef environmentRef = installTopicEnvRef(operator, topic, null, item.getEnvironmentId());
            topicEnvironmentRefs.add(environmentRef);
        });

        Map<Integer, TopicEnvironmentRef> localEnvIdServiceIdMap =
                topicEnvironmentRefs.stream().collect(Collectors.toMap(TopicEnvironmentRef::getEnvironmentId, item -> item));

        for (TopicEnvironmentInfoVo topicEnvironmentInfoVo : addEnvList) {
            //环境
            Integer envId = topicEnvironmentInfoVo.getEnvironmentId();
            ZmsContextManager.setEnv(envId);
            ZmsServiceEntity clusterService = serviceIdServiceMap.get(topicEnvironmentInfoVo.getServiceId());
            String topicName = topic.getName();
            String clusterName = clusterService.getServerName();
            String clusterType = clusterService.getServerType();
            Integer clusterServiceId = clusterService.getId();

            //创建主题资源
            ZmsTopicConfigInfo zmsTopicConfigInfo = assembleZmsTopicConfigInfo(topic.getPartitions(), topic.getReplication(), clusterName, topicName);
            topicService.createTopic(zmsTopicConfigInfo);
            zkRegister.registerTopicToZk(clusterName, topicName, BrokerType.parse(clusterType));

            //当前环境集群改变
            environmentRefMapper.updateCluster(localEnvIdServiceIdMap.get(envId).getId(), clusterServiceId, operator);
        }
        return true;
    }

    /**
     * 主题元数据同步
     */
    public void backupTopicMetadata(BackupTopicMetadataDTO backupTopicMetadataDTO) {
        logger.info("Backup topic metadata :{}", backupTopicMetadataDTO);
        Integer envId = backupTopicMetadataDTO.getCurrentEnvId();
        String clusterName = backupTopicMetadataDTO.getClusterName();
        Topic topic = topicMapper.getByTopicName(backupTopicMetadataDTO.getTopic());
        Assert.that(null != topic, "主题不存在");

        ZmsServiceEntity clusterServiceEntity = zmsServiceMapper.getByEnvIdAndName(envId, clusterName);
        Assert.that(null != clusterServiceEntity, "集群不存在不存在");
        List<TopicEnvironmentRef> topicEnvironmentRefs = environmentRefMapper.listByTopicId(Lists.newArrayList(topic.getId()));

        boolean isExistEnv = topicEnvironmentRefs.stream().filter(item -> envId.equals(item.getEnvironmentId())).count() > 0;
        if (isExistEnv) {
            logger.warn("该主题已经存在当前环境资源{}", backupTopicMetadataDTO);
            return;
        }
        List<TopicEnvironmentInfoVo> environments = Lists.newArrayList();
        TopicEnvironmentInfoVo topicEnvironmentInfoVo = new TopicEnvironmentInfoVo();
        topicEnvironmentInfoVo.setEnvironmentId(envId);
        topicEnvironmentInfoVo.setServiceId(clusterServiceEntity.getId());
        environments.add(topicEnvironmentInfoVo);

        syncTopic(topic.getId(), environments, null);
    }

    public List<EnvironmentRefDTO> queryEnvironmentRefByTopicName(String topicName) {
        return environmentRefMapper.queryEnvironmentRefByTopicName(topicName, null);
    }


    /**
     * 主题详情
     */
    public TopicSummaryDTO topicSummary(Integer envId, Long topicId) throws Exception {
        ZmsContextManager.setEnv(envId);
        Topic topic = topicMapper.selectByPrimaryKey(topicId);
        String topicName = topic.getName();
        TopicLoader loader = new TopicLoader();
        //获取集群
        TopicEnvironmentRef environmentRef = environmentRefMapper.getByEnvIdAndTopicId(envId, topicId);
        ZmsServiceEntity zmsServiceEntity = zmsServiceMapper.getById(environmentRef.getServiceId());
        String clusterName = zmsServiceEntity.getServerName();

        if (BrokerType.KAFKA.name().equals(zmsServiceEntity.getServerType())) {
            ClusterMetadata clusterMetadata = zkClientRouter.currentZkClient().readClusterMetadata(clusterName);
            return loader.load(clusterName, clusterMetadata.getBootAddr(), clusterMetadata.getServerIps(), topicName);
        } else if (BrokerType.ROCKETMQ.name().equals(zmsServiceEntity.getServerType())) {
            return rocketmqTopicSummary(clusterName, topicName);
        } else {
            throw new ZmsException("不支持的消息类型", 401);
        }
    }


    public RocketmqTopicSummary rocketmqTopicSummary(String clusterName, String topicName) throws Exception {
        RocketmqTopicSummary rocketmqTopicSummary = new RocketmqTopicSummary();

        Future<List<TopicStatusDto>> topicStatusFuture = executorService.submit(() -> topicStatusService.topicStatus(clusterName, topicName));
        Future<List<StatsAllResultDto>> statsAllResultFuture = executorService.submit(() -> statsAllService.statsAll(clusterName, topicName));

        List<TopicStatusDto> topicStatusDtoS = topicStatusFuture.get();
        List<StatsAllResultDto> statsAllResultDtoS = statsAllResultFuture.get();

        rocketmqTopicSummary.setCluster(clusterName);
        rocketmqTopicSummary.setTopic(topicName);
        rocketmqTopicSummary.setStatsAllResultDtoList(statsAllResultDtoS);
        rocketmqTopicSummary.setTopicStatusDtoList(topicStatusDtoS);
        return rocketmqTopicSummary;
    }
}

