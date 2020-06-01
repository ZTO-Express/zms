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

package com.zto.zms.portal.listener.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.zto.zms.common.BrokerType;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.utils.Assert;
import com.zto.zms.dal.domain.service.HostServiceInstanceDTO;
import com.zto.zms.dal.mapper.ProcessPropertyValueRefMapper;
import com.zto.zms.dal.mapper.ServiceInstanceMapper;
import com.zto.zms.dal.mapper.ServiceProcessMapper;
import com.zto.zms.dal.mapper.ZmsServiceMapper;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.dal.model.ServiceInstance;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.common.InstanceTypeEnum;
import com.zto.zms.portal.listener.ServiceEventListener;
import com.zto.zms.portal.listener.ServiceInstanceListener;
import com.zto.zms.service.manager.ZmsContextManager;
import com.zto.zms.service.router.ZkClientRouter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zto.zms.portal.common.ServicePropertiesNameEnum.ROCKETMQ_BROKER_ID;
import static com.zto.zms.portal.common.ServicePropertiesNameEnum.ROCKETMQ_NAMESERVER_PORT;

/**
 * <p>Class: RocketMqServiceInstanceListenerImpl</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/20
 **/
@Component("ServiceEventListener-rocketmq")
public class RocketMqServiceInstanceListenerImpl implements ServiceInstanceListener, ServiceEventListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaServiceInstanceListenerImpl.class);

    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ServiceProcessMapper serviceProcessMapper;
    @Autowired
    private ProcessPropertyValueRefMapper processPropertyValueRefMapper;
    @Autowired
    private ZkClientRouter zkClientRouter;

    @Override
    public void start(Integer serviceId, Integer processId) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error("线程休眠中断异常", e);
        }
        updateClusterInfo(serviceId);
    }

    @Override
    public void remove(ServiceInstance serviceInstance) {
        updateClusterInfo(serviceInstance.getServiceId());
    }

    /**
     * 更新集群元数据
     *
     * @param serviceId 集群ID
     */
    private void updateClusterInfo(Integer serviceId) {
        ZmsServiceEntity zmsService = zmsServiceMapper.getById(serviceId);
        ZmsContextManager.setEnv(zmsService.getEnvironmentId());
        ClusterMetadata currentClusterMetadata = assembleClusterDto(zmsService);
        zkClientRouter.writeClusterInfoNecessary(currentClusterMetadata);
    }

    private List<String> getAddrList(List<HostServiceInstanceDTO> instances, String propertyName) {
        if (CollectionUtils.isEmpty(instances)) {
            return Lists.newArrayList();
        }
        List<String> addrList = Lists.newArrayList();
        for (HostServiceInstanceDTO instance : instances) {
            Integer lastProcessId = serviceProcessMapper.listLastSuccessProcessByInstance(instance.getId());
            ProcessPropertyValueRef listenersProperValue =
                    processPropertyValueRefMapper.listByProcessIdName(lastProcessId, propertyName).get(0);
            String listenPort = listenersProperValue.getCurrentValue();
            String hostIp = instance.getHostIp();
            addrList.add(MessageFormat.format("{0}:{1}", hostIp, listenPort));
        }
        return addrList;
    }

    private List<String> getMasterBrokerAddrList(List<HostServiceInstanceDTO> instances) {
        if (CollectionUtils.isEmpty(instances)) {
            return Lists.newArrayList();
        }
        List<String> addrList = Lists.newArrayList();
        for (HostServiceInstanceDTO instance : instances) {
            Integer lastProcessId = serviceProcessMapper.listLastSuccessProcessByInstance(instance.getId());
            ProcessPropertyValueRef processPropertyValueRef = processPropertyValueRefMapper.listByProcessIdName(lastProcessId, ROCKETMQ_BROKER_ID.getName()).get(0);
            //brokerId=0的节点
            if (!"0".equals(processPropertyValueRef.getRealValue())) {
                continue;
            }
            ProcessPropertyValueRef listenersProperValue =
                    processPropertyValueRefMapper.listByProcessIdName(lastProcessId, ROCKETMQ_NAMESERVER_PORT.getName()).get(0);
            String listenPort = listenersProperValue.getCurrentValue();
            String hostIp = instance.getHostIp();
            addrList.add(MessageFormat.format("{0}:{1}", hostIp, listenPort));
        }
        return addrList;
    }

    private ClusterMetadata assembleClusterDto(ZmsServiceEntity zmsService) {

        //所有实例
        Map<String, List<HostServiceInstanceDTO>> instancesTypeMap =
                serviceInstanceMapper.queryHostServiceInstance(zmsService.getId())
                        .stream()
                        .collect(Collectors.groupingBy(HostServiceInstanceDTO::getInstanceType));
        //broker
        List<HostServiceInstanceDTO> brokerInstances = instancesTypeMap.get(InstanceTypeEnum.BROKER.name());
        List<String> brokerAddrList = getMasterBrokerAddrList(brokerInstances);
        //nameSvr
        List<HostServiceInstanceDTO> nameSvrInstances = instancesTypeMap.get(InstanceTypeEnum.NAMESVR.name());
        List<String> nameSvrAddrList = getAddrList(nameSvrInstances, ROCKETMQ_NAMESERVER_PORT.getName());
        //ips
        String ips = Joiner.on(";").join(brokerAddrList);
        //bootstraps
        String bootstraps = Joiner.on(";").join(nameSvrAddrList);
        String clusterType = BrokerType.ROCKETMQ.getName();
        String clusterName = zmsService.getServerName();

        ClusterMetadata metadata = new ClusterMetadata();
        metadata.setClusterName(clusterName);
        metadata.setBrokerType(BrokerType.parse(clusterType));
        metadata.setBootAddr(bootstraps);
        metadata.setServerIps(ips);
        return metadata;
    }

    @Override
    public void remove(ZmsServiceEntity zmsServiceEntity) {
        Assert.that(StringUtils.isNotBlank(zmsServiceEntity.getServerName()), "删除服务zk节点失败:服务名称为空");
        Assert.that(null != zmsServiceEntity.getEnvironmentId(), "删除服务zk节点失败:环境没指定");
        //删除rocketmq的zk节点信息
        ZmsContextManager.setEnv(zmsServiceEntity.getEnvironmentId());
        zkClientRouter.deleteClusterInfo(zmsServiceEntity.getServerName());
        logger.info("Delete the zookeeper node:cluster:{}", zmsServiceEntity.getServerName());
    }
}

