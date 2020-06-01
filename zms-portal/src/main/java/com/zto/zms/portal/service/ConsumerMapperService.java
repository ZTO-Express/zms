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

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.zto.zms.common.BrokerType;
import com.zto.zms.common.ZmsException;
import com.zto.zms.common.ZmsType;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.metadata.ConsumerGroupMetadata;
import com.zto.zms.service.domain.page.PageResult;
import com.zto.zms.utils.Assert;
import com.zto.zms.dal.domain.alert.EnvironmentRefDTO;
import com.zto.zms.dal.mapper.*;
import com.zto.zms.dal.model.*;
import com.zto.zms.portal.common.ResourceStatus;
import com.zto.zms.portal.common.ZkRegister;
import com.zto.zms.portal.common.ZmsServiceStatusEnum;
import com.zto.zms.portal.controller.BaseController;
import com.zto.zms.portal.dto.GatedDTO;
import com.zto.zms.portal.dto.consumer.ConsumerDTO;
import com.zto.zms.portal.dto.consumer.ConsumerQueryDTO;
import com.zto.zms.portal.filter.LdapUser;
import com.zto.zms.service.domain.cluster.ClusterDTO;
import com.zto.zms.service.domain.consumer.BackupConsumerMetadataDTO;
import com.zto.zms.service.domain.consumer.ConsumerCopyMsgVO;
import com.zto.zms.service.domain.consumer.ConsumerEnvironmentRefVO;
import com.zto.zms.service.manager.AbstractMessageMiddlewareManager;
import com.zto.zms.service.manager.MessageAdminManagerAdapt;
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
import org.springside.modules.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.zto.zms.portal.common.ResourceStatus.*;

/**
 * Created by liangyong on 2019/3/4.
 */
@Service
public class ConsumerMapperService extends BaseController {

    private Logger logger = LoggerFactory.getLogger(ConsumerMapperService.class);

    @Autowired
    private ConsumerMapper consumerMapper;
    @Autowired
    private ZkRegister zkRegister;
    @Autowired
    private ConsumerService consumerService;
    @Autowired
    private AlertRuleDetailMapper alertRuleDetailMapper;
    @Autowired
    private AlertService alertService;
    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private ConsumerEnvironmentRefMapper consumerEnvironmentRefMapper;
    @Autowired
    private TopicEnvironmentRefMapper topicEnvironmentRefMapper;
    @Autowired
    private ZkClientRouter zkClientRouter;
    @Autowired
    private MessageAdminManagerAdapt messageAdminManagerAdapt;
    @Autowired
    private ZmsEnvironmentMapper zmsEnvironmentMapper;

    public List<Consumer> getConsumersByClusterId(Integer clusterId) {
        return consumerMapper.selectConsumersByServiceId(clusterId);
    }

    /**
     * 消费组列表
     */
    public PageResult<ConsumerDTO> queryConsumersByService(ConsumerQueryDTO queryDto) {
        PageHelper.startPage(queryDto.getCurrentPage(), queryDto.getPageSize());
        List<Consumer> consumerList = consumerMapper.selectConsumersByKeyWord(queryDto.getEnvId(), queryDto.getServiceId(),
                queryDto.getName(), StringUtils.isNotEmpty(queryDto.getKeyWord()) ? queryDto.getKeyWord().toLowerCase().trim() : null, queryDto.getStatus(), queryDto.getId());
        //列表为空,直接返回
        if (CollectionUtil.isEmpty(consumerList)) {
            return new PageResult<>(queryDto.getCurrentPage(), queryDto.getPageSize(), 0, Lists.newArrayList());
        }
        PageInfo<Consumer> consumerPage = new PageInfo<>(consumerList);
        List<Long> consumerIds = consumerList.stream().map(Consumer::getId).collect(Collectors.toList());
        //消费组环境映射关系
        List<ConsumerEnvironmentRef> consumerEnvironmentRefs = consumerEnvironmentRefMapper.getconsumerenvs(consumerIds);
        Map<Long, List<ConsumerEnvironmentRef>> consumerEnvMap = consumerEnvironmentRefs
                .stream()
                .collect(Collectors.groupingBy(ConsumerEnvironmentRef::getConsumerId));
        //消费组列表数据拼装
        List<ConsumerDTO> resultLst = consumerList.stream().map(item -> {
            ConsumerDTO consumerDto = new ConsumerDTO();
            List<ConsumerEnvironmentRefVO> consumerEnvironmentRefVos = new ArrayList<>();
            List<Integer> envIds = new ArrayList<>();
            try {
                BeanUtils.copyProperties(item, consumerDto);
            } catch (Exception e) {
                logger.warn("BeanUtils.copyProperties error, errMsg={}", e.getMessage());
            }
            List<ConsumerEnvironmentRef> consumerEnvRefs = consumerEnvMap.get(item.getId());
            //环境列表
            if (CollectionUtil.isNotEmpty(consumerEnvRefs)) {
                for (ConsumerEnvironmentRef consumerEnvironmentRef : consumerEnvRefs) {
                    ConsumerEnvironmentRefVO consumerEnvironmentRefVo = new ConsumerEnvironmentRefVO();
                    if (!envIds.contains(consumerEnvironmentRef.getEnvironmentId())) {
                        envIds.add(consumerEnvironmentRef.getEnvironmentId());
                        BeanUtils.copyProperties(consumerEnvironmentRef, consumerEnvironmentRefVo);
                        consumerEnvironmentRefVo.setEnvironmentId(consumerEnvironmentRef.getEnvironmentId());
                        consumerEnvironmentRefVo.setEnvironmentName(consumerEnvironmentRef.getEnvironmentName());
                        consumerEnvironmentRefVos.add(consumerEnvironmentRefVo);
                    }
                }
            }
            consumerDto.setConsumerEnvironmentRefVos(consumerEnvironmentRefVos);
            return consumerDto;
        }).collect(Collectors.toList());

        return new PageResult<>(queryDto.getCurrentPage(), queryDto.getPageSize(), consumerPage.getTotal(), resultLst);
    }

    /**
     * 新增消费组
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer addConsumer(ConsumerDTO consumerDto) {
        //获取topic下不同环境
        List<TopicEnvironmentRef> topicEnvironmentRefs = topicEnvironmentRefMapper.selectTopicAndCluster(consumerDto.getTopicId());
        Consumer consumer = new Consumer();
        BeanUtils.copyProperties(consumerDto, consumer);
        consumer.setStatus(ResourceStatus.CREATE_NEW.getStatus());
        consumer.setTopicId(consumerDto.getTopicId());
        Integer count = consumerMapper.insert(consumer);
        for (TopicEnvironmentRef topicEnvironmentRef : topicEnvironmentRefs) {
            if (BrokerType.KAFKA.getName().equalsIgnoreCase(topicEnvironmentRef.getClusterType()) && consumerDto.getBroadcast()) {
                throw new RuntimeException("broadcast must be false when cluster type is kafka");
            }
            //添加消费组到多环境
            ConsumerEnvironmentRef consumerEnvironmentRef = new ConsumerEnvironmentRef();
            consumerEnvironmentRef.setServiceId(topicEnvironmentRef.getServiceId());
            consumerEnvironmentRef.setConsumerId(consumer.getId());
            consumerEnvironmentRef.setEnvironmentId(topicEnvironmentRef.getEnvironmentId());
            consumerEnvironmentRefMapper.insert(consumerEnvironmentRef);
        }
        return count;
    }

    /**
     * 编辑消费组
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateConsumer(ConsumerDTO consumerDto, LdapUser ldapUser) {
        Consumer consumer = consumerMapper.selectByPrimaryKey(consumerDto.getId());
        if (null == consumer) {
            throw ZmsException.CONSUMER_NOT_EXISTS_EXCEPTION;
        }
        //权限判断
        if (!ldapUser.isAdmin() && StringUtils.compare(consumer.getApplicant(), ldapUser.getRealName()) != 0) {
            throw ZmsException.NO_PERMISSION_MODIFY;
        }
        //获取topic下不同环境
        List<TopicEnvironmentRef> topicEnvironmentRefs = topicEnvironmentRefMapper.selectTopicAndCluster(consumerDto.getTopicId());

        //topic变更
        boolean isTopicChanged = !consumer.getTopicId().equals(consumerDto.getTopicId());
        //topic变更后删除消费组和环境对应关系
        if (isTopicChanged) {
            consumerEnvironmentRefMapper.deleteByConsumerId(consumerDto.getId());
        }
        //审批之后又修改
        if (!ResourceStatus.CREATE_NEW.getStatus().equals(consumer.getStatus())) {
            if (!consumerDto.getName().equals(consumer.getName())) {
                throw ZmsException.CONSUMER_CAN_NOT_EXISTS_CHANGE;
            }
            boolean isBroadCastChanged = !consumerDto.getBroadcast().equals(consumer.getBroadcast());
            boolean isConsumerFromChanged = !consumerDto.getConsumerFrom().equals(consumer.getConsumerFrom());
            if (isBroadCastChanged || isConsumerFromChanged || isTopicChanged) {
                //ZMS_ZK信息需要更新
                consumerDto.setNeedUpdateZk(1);
                consumerDto.setStatus(UPDATE_NEW.getStatus());
            }
        }
        List<ConsumerEnvironmentRef> consumerEnvironmentRefs = new ArrayList<>();
        //迭代不同环境下topic
        for (TopicEnvironmentRef topicEnvironmentRef : topicEnvironmentRefs) {
            //kafka 不能是广播模式
            if (BrokerType.KAFKA.getName().equalsIgnoreCase(topicEnvironmentRef.getClusterType()) && consumerDto.getBroadcast()) {
                throw new RuntimeException("broadcast must be false when cluster type is kafka");
            }
            //topic changed.
            if (isTopicChanged) {
                //同步消费组到多环境
                ConsumerEnvironmentRef consumerEnvironmentRef = new ConsumerEnvironmentRef();
                consumerEnvironmentRef.setServiceId(topicEnvironmentRef.getServiceId());
                consumerEnvironmentRef.setConsumerId(consumer.getId());
                consumerEnvironmentRef.setEnvironmentId(topicEnvironmentRef.getEnvironmentId());
                consumerEnvironmentRef.setCreator(ldapUser.getRealName());
                consumerEnvironmentRef.setModifier(ldapUser.getRealName());
                consumerEnvironmentRefs.add(consumerEnvironmentRef);
            }
        }
        if (CollectionUtil.isNotEmpty(consumerEnvironmentRefs)) {
            consumerEnvironmentRefMapper.insertList(consumerEnvironmentRefs);
        }
        BeanUtils.copyProperties(consumerDto, consumer);
        consumerMapper.updateByPrimaryKeySelective(consumer);
        return true;
    }

    public Boolean uniqueCheck(ConsumerDTO consumerDto) {
        List<Consumer> resultLst = consumerMapper.selectConsumerByName(consumerDto.getName().toLowerCase(), consumerDto.getId());
        return CollectionUtils.isEmpty(resultLst);
    }

    /**
     * 同步消费组到多环境
     */
    public List<ConsumerCopyMsgVO> copyConsumers(List<Integer> envIds, List<Long> consumerIds) {

        List<ConsumerCopyMsgVO> wrongList = new ArrayList<>();
        Map<Integer, ZmsEnvironment> envs = zmsEnvironmentMapper.listByIds(envIds).stream()
                .collect(Collectors.toMap(ZmsEnvironment::getId, a -> a));
        //获取所有消费组
        List<Consumer> consumers = consumerMapper.selectConsumerInEnvIds(null, consumerIds);
        List<Long> topics = consumers.stream().map(Consumer::getTopicId).collect(Collectors.toList());
        //topic对应环境集群
        List<TopicEnvironmentRef> topicEnvironmentRefs = topicEnvironmentRefMapper.listByTopicId(topics);
        //topic环境map
        Map<Long, List<TopicEnvironmentRef>> topicIdMap = topicEnvironmentRefs.stream()
                .collect(Collectors.groupingBy(TopicEnvironmentRef::getTopicId));
        //同步消费组到多环境
        List<ConsumerEnvironmentRef> consumerEnvironmentRefs = new ArrayList<>();
        //同步消费组到不同环境
        for (Integer envId : envIds) {
            for (Consumer consumer : consumers) {
                Assert.that(consumer.getStatus().equals(UPDATE_APPROVED.getStatus()) || consumer.getStatus().equals(CREATE_APPROVED.getStatus()), "审核后才能进行多环境同步");
                //校验topic是否存在于当前环境
                List<TopicEnvironmentRef> topicEnvs = topicIdMap.get(consumer.getTopicId());
                Map<Integer, List<TopicEnvironmentRef>> envTopics = topicEnvs.stream()
                        .collect(Collectors.groupingBy(TopicEnvironmentRef::getEnvironmentId));
                //topic所在环境
                List<Integer> topicEnvIds = topicEnvs.stream().map(TopicEnvironmentRef::getEnvironmentId).collect(Collectors.toList());
                //该环境下没有此topic
                if (CollectionUtil.isNotEmpty(topicEnvIds) && !topicEnvIds.contains(envId)) {
                    ConsumerCopyMsgVO copyMsgVO = new ConsumerCopyMsgVO();
                    copyMsgVO.setEnvName(envs.get(envId).getEnvironmentName());
                    copyMsgVO.setErrorMsg(consumer.getTopicName() + "主题不存在");
                    wrongList.add(copyMsgVO);
                    continue;
                }
                //获取集群信息
                List<TopicEnvironmentRef> serviceIds = topicEnvs.stream()
                        .filter(item -> null != item.getEnvironmentId() &&
                                envId.equals(item.getEnvironmentId()) &&
                                null != item.getServiceId())
                        .collect(Collectors.toList());
                if (CollectionUtil.isEmpty(serviceIds)) {
                    ConsumerCopyMsgVO copyMsgVO = new ConsumerCopyMsgVO();
                    copyMsgVO.setEnvName(envs.get(envId).getEnvironmentName());
                    copyMsgVO.setErrorMsg(consumer.getClusterName() + " 集群不存在");
                    wrongList.add(copyMsgVO);
                    continue;
                }
                TopicEnvironmentRef topicEnvironmentRef = serviceIds.get(0);
                ConsumerEnvironmentRef checkEnvId = new ConsumerEnvironmentRef(consumer.getId(), envId);
                if (!consumer.getEnvs().contains(checkEnvId)) {
                    ConsumerEnvironmentRef consumerEnvironmentRef = new ConsumerEnvironmentRef();
                    consumerEnvironmentRef.setServiceId(topicEnvironmentRef.getServiceId());
                    consumerEnvironmentRef.setConsumerId(consumer.getId());
                    consumerEnvironmentRef.setEnvironmentId(envId);
                    consumerEnvironmentRefs.add(consumerEnvironmentRef);
                }
                //同步zk信息
                ZmsContextManager.setEnv(envId);
                ConsumerDTO consumerDto = new ConsumerDTO();
                BeanUtils.copyProperties(consumer, consumerDto);
                consumerDto.setClusterName(topicEnvironmentRef.getClusterName());
                try {
                    registerConsumerToZk(topicEnvironmentRef.getClusterType(), consumerDto);
                    registerConsumerToServer(consumerDto);
                } catch (Exception e) {
                    ConsumerCopyMsgVO copyMsgVO = new ConsumerCopyMsgVO();
                    copyMsgVO.setEnvName(envs.get(envId).getEnvironmentName());
                    copyMsgVO.setErrorMsg("注册消费组失败");
                    wrongList.add(copyMsgVO);
                }
            }
        }
        if (CollectionUtil.isNotEmpty(consumerEnvironmentRefs)) {
            consumerEnvironmentRefMapper.insertList(consumerEnvironmentRefs);
        }
        return wrongList;
    }

    /**
     * 审批消费组
     */
    public Boolean approveConsumer(ConsumerDTO consumerDto) {
        List<TopicEnvironmentRef> topicEnvironmentRefs = topicEnvironmentRefMapper.selectTopicAndCluster(consumerDto.getTopicId());
        //该topic没有在任何环境
        if (CollectionUtils.isEmpty(topicEnvironmentRefs)) {
            throw ZmsException.TOPIC_ENV_NOT_EXISTS_EXCEPTION;
        }
        //消费组信息
        Consumer origConsumer = consumerMapper.selectByPrimaryKey(consumerDto.getId());
        if (null == origConsumer) {
            throw ZmsException.CONSUMER_NOT_EXISTS_EXCEPTION;
        }
        //审批时变更Topic
        boolean isTopicChanged = !origConsumer.getTopicId().equals(consumerDto.getTopicId());
        if (isTopicChanged) {
            consumerEnvironmentRefMapper.deleteByConsumerId(consumerDto.getId());
        }
        List<ConsumerEnvironmentRef> consumerEnvs = new ArrayList<>();
        for (TopicEnvironmentRef topicEnvironmentRef : topicEnvironmentRefs) {
            consumerDto.setTopicName(topicEnvironmentRef.getTopicName());
            //指定zk客户端
            ZmsContextManager.setEnv(topicEnvironmentRef.getEnvironmentId());
            //拼装集群信息
            ClusterDTO inputCluster = new ClusterDTO((long) topicEnvironmentRef.getClusterId(), topicEnvironmentRef.getClusterName(), topicEnvironmentRef.getClusterType());
            consumerDto.setClusterName(inputCluster.getName());
            consumerDto.setClusterId(inputCluster.getId());
            //新增状态
            if (ResourceStatus.CREATE_NEW.getStatus().equals(origConsumer.getStatus())) {
                registerConsumerToZk(inputCluster.getClusterType(), consumerDto);
                registerConsumerToServer(inputCluster.getName(), consumerDto);
            }

            // 1.已经审批后用户编辑了信息（编辑时是否需要修改zms_zk及server记录到数据库标志位）
            // 2.重新审批（审批可以多次审批，判断信息变更）审批时变更消费方式,审批时变更起始消费位点
            boolean isBroadCastChanged = !consumerDto.getBroadcast().equals(origConsumer.getBroadcast());
            boolean isConsumerFromChanged = !consumerDto.getConsumerFrom().equals(origConsumer.getConsumerFrom());
            if (isTopicChanged) {
                //同步消费组到多环境
                ConsumerEnvironmentRef consumerEnvironmentRef = new ConsumerEnvironmentRef();
                consumerEnvironmentRef.setServiceId(topicEnvironmentRef.getServiceId());
                consumerEnvironmentRef.setConsumerId(origConsumer.getId());
                consumerEnvironmentRef.setEnvironmentId(topicEnvironmentRef.getEnvironmentId());
                consumerEnvs.add(consumerEnvironmentRef);
            }
            //用户编辑变更或者审批变更需要更新zms_zk
            if (origConsumer.getNeedUpdateZk() > 0 || isTopicChanged
                    || isBroadCastChanged || isConsumerFromChanged) {
                registerConsumerToZk(inputCluster.getClusterType(), consumerDto);
            }
            //用户编辑变更或者审批变更需要更新Server
            if (origConsumer.getNeedUpdateServer() > 0) {
                registerConsumerToServer(inputCluster.getName(), consumerDto);
            }
        }
        if (CollectionUtil.isNotEmpty(consumerEnvs)) {
            consumerEnvironmentRefMapper.insertList(consumerEnvs);
        }
        Integer status = ResourceStatus.CREATE_NEW.getStatus().equals(origConsumer.getStatus()) ?
                ResourceStatus.CREATE_APPROVED.getStatus() : ResourceStatus.UPDATE_APPROVED.getStatus();
        BeanUtils.copyProperties(consumerDto, origConsumer);
        origConsumer.setStatus(status);
        updateConsumerDbInfo(origConsumer);
        return true;
    }

    private void updateConsumerDbInfo(Consumer consumer) {
        //状态恢复
        consumer.setNeedUpdateZk(0);
        //状态恢复
        consumer.setNeedUpdateServer(0);
        consumerMapper.updateByPrimaryKeySelective(consumer);
    }

    private void registerConsumerToZk(String clusterType, ConsumerDTO dto) {
        if (BrokerType.KAFKA.getName().equalsIgnoreCase(clusterType)) {
            zkRegister.registerConsumerToZk(dto, BrokerType.KAFKA);
        } else if (BrokerType.ROCKETMQ.getName().equalsIgnoreCase(clusterType)) {
            zkRegister.registerConsumerToZk(dto, BrokerType.ROCKETMQ);
        }
    }

    public void registerConsumerToServer(String clusterName, ConsumerDTO dto) {
        AbstractMessageMiddlewareManager middlewareManager = messageAdminManagerAdapt.getOrCreateAdmin(clusterName);
        middlewareManager.createConsumerGroup(dto.getName(), dto.getBroadcast(), dto.getConsumerFrom());
    }

    public void registerConsumerToServer(ConsumerDTO dto) {
        AbstractMessageMiddlewareManager middlewareManager = messageAdminManagerAdapt.getOrCreateAdmin(dto.getClusterName());
        middlewareManager.createConsumerGroup(dto.getName(), dto.getBroadcast(), dto.getConsumerFrom());
    }

    public Integer deleteConsumer(ConsumerDTO dto) {
        List<Consumer> consumers = consumerMapper.selectConsumerGroupByEnv(null, dto.getId());
        if (CollectionUtil.isEmpty(consumers)) {
            throw ZmsException.CONSUMER_NOT_EXISTS_EXCEPTION;
        }
        Consumer origConsumer = consumers.get(0);
        if (CollectionUtil.isNotEmpty(origConsumer.getEnvs())) {
            for (ConsumerEnvironmentRef consumerEnvironmentRef : origConsumer.getEnvs()) {
                if (null == consumerEnvironmentRef.getEnvironmentId()) {
                    continue;
                }
                ZmsContextManager.setEnv(consumerEnvironmentRef.getEnvironmentId());
                //delete consumerGroup in cluster
                if (zkClientRouter.currentZkClient().checkPath(ZmsType.CONSUMER_GRUOP, origConsumer.getName())) {
                    ConsumerGroupMetadata consumerGroupMetadata = zkClientRouter.currentZkClient().readConsumerGroupMetadata(origConsumer.getName());
                    if (consumerGroupMetadata.getClusterMetadata().getBrokerType().equals(BrokerType.ROCKETMQ)) {
                        consumerService.deleteSubGroup(consumerGroupMetadata.getClusterMetadata().getClusterName(), origConsumer.getName());
                    }
                }
                //delete in zms_zk
                zkRegister.deleteConsumer(origConsumer.getName());
            }
        }
        //删除消费告警
        List<AlertRuleConfig> alertRules = alertRuleDetailMapper.queryAlertRules(origConsumer.getName(), null, null);
        if (!CollectionUtils.isEmpty(alertRules)) {
            alertRules.forEach(item -> alertService.delete(item));
        }
        origConsumer.setStatus(ResourceStatus.SOFT_DELETED.getStatus());
        Integer count = consumerMapper.updateByPrimaryKeySelective(origConsumer);
        consumerEnvironmentRefMapper.deleteByConsumerId(origConsumer.getId());
        return count;
    }


    /**
     * 灰度配置
     */
    public ConsumerGroupMetadata updateGated(GatedDTO dto) {
        ConsumerGroupMetadata groupMetadata = zkRegister.readConsumerInfo(dto.getName());
        if (groupMetadata != null
                && Objects.equals(groupMetadata.getGatedCluster(), dto.getGatedClusterId())
                && Objects.equals(groupMetadata.getGatedIps(), dto.getGatedIps())) {
            return groupMetadata;
        }
        ClusterMetadata metadata = new ClusterMetadata();
        metadata.setClusterName(dto.getGatedCluster());
        if (groupMetadata == null) {
            groupMetadata = new ConsumerGroupMetadata();
        }
        groupMetadata.setGatedCluster(metadata);
        groupMetadata.setGatedIps(dto.getGatedIps());
        try {
            zkRegister.registerConsumerToZk(groupMetadata);
            consumerMapper.updateGatedInfo(dto.getId(), dto.getGatedClusterId(), dto.getGatedIps());
        } catch (Exception ex) {
            logger.error("update group {} metadata error", dto.getName(), ex);
            throw ex;
        }
        return groupMetadata;
    }

    /**
     * 灰度集群配置
     */
    public int updateGated(GatedDTO dto, String userName) {
        List<ConsumerEnvironmentRefVO> environmentInfoVos = dto.getConsumerEnvironmentRefVos();
        Map<Integer, List<ConsumerEnvironmentRef>> envIdServiceIdMap = consumerEnvironmentRefMapper.listByConsumerId(Lists.newArrayList(dto.getId())).stream()
                .collect(Collectors.groupingBy(ConsumerEnvironmentRef::getEnvironmentId));
        List<Integer> gateServiceIds = environmentInfoVos.stream().map(ConsumerEnvironmentRefVO::getGatedServiceId).collect(Collectors.toList());
        //灰度集群
        List<ZmsServiceEntity> serviceList = zmsServiceMapper.listClustersByServiceIds(gateServiceIds)
                .stream()
                .filter(item -> ZmsServiceStatusEnum.ENABLE.name().equals(item.getServerStatus()))
                .collect(Collectors.toList());
        Map<Integer, String> serviceIdNameMap = serviceList.stream()
                .collect(Collectors.toMap(ZmsServiceEntity::getId, ZmsServiceEntity::getServerName));
        //初始化灰度集群资源
        for (ConsumerEnvironmentRefVO environmentInfoVo : environmentInfoVos) {
            Integer envId = environmentInfoVo.getEnvironmentId();
            List<Integer> idrefs = envIdServiceIdMap.get(envId).stream().map(ConsumerEnvironmentRef::getId).collect(Collectors.toList());

            //修改zookeeper
            updateGateCluster(envId, dto.getName(), environmentInfoVo.getGatedIps(), serviceIdNameMap.get(environmentInfoVo.getGatedServiceId()));
            consumerEnvironmentRefMapper.updateGateCluster(idrefs, environmentInfoVo.getGatedServiceId(), environmentInfoVo.getGatedIps(), userName);
        }
        return environmentInfoVos.size();
    }

    private void updateGateCluster(int envId, String consumerName, String gatedIps, String gateClusterName) {
        ZmsContextManager.setEnv(envId);
        ConsumerGroupMetadata groupMetadata = zkRegister.readConsumerInfo(consumerName);
        if (groupMetadata != null
                && Objects.equals(groupMetadata.getGatedCluster().getClusterName(), gateClusterName)
                && Objects.equals(groupMetadata.getGatedIps(), gatedIps)) {
            return;
        }
        ClusterMetadata metadata = new ClusterMetadata();
        metadata.setClusterName(gateClusterName);
        if (groupMetadata == null) {
            groupMetadata = new ConsumerGroupMetadata();
        }
        groupMetadata.setGatedCluster(metadata);
        groupMetadata.setGatedIps(gatedIps);
        zkRegister.registerConsumerToZk(groupMetadata);
    }

    /**
     * 根据集群查询消费组
     */
    public List<ConsumerDTO> queryApprovedConsumersByService(Integer envId, Integer clusterId, String clusterName, String clusterType,
                                                             Long topicId, String topicName) {

        return consumerMapper.selectApprovedConsumersByService(envId, clusterId, clusterName, clusterType, topicId, topicName).stream().map(item -> {
            ConsumerDTO consumerDto = new ConsumerDTO();
            try {
                BeanUtils.copyProperties(item, consumerDto);
            } catch (Exception e) {
                logger.warn("BeanUtils.copyProperties error, errMsg={}", e.getMessage());
            }
            return consumerDto;
        }).collect(Collectors.toList());
    }

    public List<EnvironmentRefDTO> queryEnvironmentRefByConsumerName(String consumerName) {
        return consumerEnvironmentRefMapper.queryEnvironmentRefByConsumerName(consumerName, null);
    }


    @Transactional(rollbackFor = Exception.class)
    public void backupConsumerMetadata(BackupConsumerMetadataDTO backupConsumerMetadataDTO) {
        logger.info("Backup consumer metadata :{}", backupConsumerMetadataDTO);

        String consumerName = backupConsumerMetadataDTO.getName();
        Integer envId = backupConsumerMetadataDTO.getCurrentEnvId();
        String clusterName = backupConsumerMetadataDTO.getClusterName();

        Consumer consumer = consumerMapper.getConsumerByName(consumerName);
        Assert.that(null != consumer, "消费组不存在");

        List<ConsumerEnvironmentRef> environmentRefList = consumerEnvironmentRefMapper.listByConsumerId(Lists.newArrayList(consumer.getId()));
        boolean isExistEnv = environmentRefList.stream().filter(item -> envId.equals(item.getEnvironmentId())).count() > 0;
        if (isExistEnv) {
            logger.warn("该消费组已经存在当前环境资源");
            return;
        }

        ZmsServiceEntity zmsServiceEntity = zmsServiceMapper.getByEnvIdAndName(envId, clusterName);
        Assert.that(null != zmsServiceEntity, "集群不存在");

        ZmsContextManager.setEnv(envId);
        ConsumerDTO consumerDto = new ConsumerDTO();
        consumerDto.setTopicName(backupConsumerMetadataDTO.getBindingTopic());
        consumerDto.setClusterName(clusterName);
        consumerDto.setBroadcast(consumer.getBroadcast());
        consumerDto.setConsumerFrom(consumer.getConsumerFrom());
        consumerDto.setName(consumerName);
        insertConsumerEnvRef(envId, consumer, zmsServiceEntity.getId());
        //写入zookeeper数据源
        registerConsumerToZk(zmsServiceEntity.getServerType(), consumerDto);
        //集群新建消费组
        registerConsumerToServer(clusterName, consumerDto);
    }

    public void insertConsumerEnvRef(Integer envId, Consumer consumer, Integer serviceId) {
        ConsumerEnvironmentRef consumerEnvironmentRef = new ConsumerEnvironmentRef();
        consumerEnvironmentRef.setServiceId(serviceId);
        consumerEnvironmentRef.setEnvironmentId(envId);
        consumerEnvironmentRef.setConsumerId(consumer.getId());
        consumerEnvironmentRefMapper.insert(consumerEnvironmentRef);
    }
}

