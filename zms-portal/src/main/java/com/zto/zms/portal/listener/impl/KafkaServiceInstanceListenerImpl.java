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
import com.zto.zms.portal.listener.ServiceEventListener;
import com.zto.zms.portal.listener.ServiceInstanceListener;
import com.zto.zms.portal.service.serve.ServeService;
import com.zto.zms.service.kafka.KafkaAdminClient;
import com.zto.zms.service.manager.ZmsContextManager;
import com.zto.zms.service.router.ZkClientRouter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springside.modules.utils.collection.CollectionUtil;

import java.text.MessageFormat;
import java.util.List;

import static com.zto.zms.portal.common.ServicePropertiesNameEnum.KAFKA_PORT;
import static com.zto.zms.portal.common.ServicePropertiesNameEnum.ZK_SERVICE;

/**
 * <p>Class: KafkaServiceInstanceListenerImpl</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/20
 **/
@Component("ServiceEventListener-kafka")
public class KafkaServiceInstanceListenerImpl implements ServiceInstanceListener, ServiceEventListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaServiceInstanceListenerImpl.class);

    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private ProcessPropertyValueRefMapper processPropertyValueRefMapper;
    @Autowired
    private KafkaAdminClient kafkaAdminClient;

    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ServiceProcessMapper serviceProcessMapper;
    @Autowired
    private ServeService serveService;
    @Autowired
    private ZkClientRouter zkClientRouter;

    @Override
    public void start(Integer serviceId, Integer processId) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error("线程休眠中断异常", e);
        }
        ZmsServiceEntity zmsService = zmsServiceMapper.getById(serviceId);
        ZmsContextManager.setEnv(zmsService.getEnvironmentId());
        //当前集群元数据
        ClusterMetadata currentClusterMetadata = assembleClusterDto(zmsService, processId);
        //如果元数据变更，修改zk配置
        zkClientRouter.writeClusterInfoNecessary(currentClusterMetadata);
        kafkaAdminClient.createInitTopic(currentClusterMetadata.getClusterName());
    }


    @Override
    public void remove(ServiceInstance serviceInstance) {
        Integer serviceId = serviceInstance.getServiceId();
        ZmsServiceEntity zmsService = zmsServiceMapper.getById(serviceId);
        ZmsContextManager.setEnv(zmsService.getEnvironmentId());
        //服务进程信息
        Integer processId = serviceProcessMapper.listRunningProcessByInstance(serviceInstance.getId()).getId();
        //当前集群元数据
        ClusterMetadata currentClusterMetadata = assembleClusterDto(zmsService, processId);
        //元数据变更
        zkClientRouter.writeClusterInfo(currentClusterMetadata);
        logger.info("Write zookeeper node:cluster:{}", currentClusterMetadata);
    }


    private ClusterMetadata assembleClusterDto(ZmsServiceEntity zmsService, Integer processId) {


        List<ProcessPropertyValueRef> zkProcessPropertyValue = processPropertyValueRefMapper.listByProcessIdName(processId, ZK_SERVICE.getName());
        Assert.that(CollectionUtil.isNotEmpty(zkProcessPropertyValue), "[kafka]未配置zookeeper依赖");
        Integer zkServiceId = Integer.valueOf(zkProcessPropertyValue.get(0).getCurrentValue());

        List<String> zookeeperAddr = serveService.listZookeeperAddr(zkServiceId);
        //kafka服务实例
        List<String> brokerAddrList = Lists.newArrayList();
        List<HostServiceInstanceDTO> instances = serviceInstanceMapper.queryHostServiceInstance(zmsService.getId());
        for (HostServiceInstanceDTO instance : instances) {
            Integer lastProcessId = serviceProcessMapper.listLastSuccessProcessByInstance(instance.getId());
            ProcessPropertyValueRef listenersProperValue =
                    processPropertyValueRefMapper.listByProcessIdName(lastProcessId, KAFKA_PORT.getName()).get(0);
            Assert.that(null != listenersProperValue && !StringUtils.isEmpty(listenersProperValue.getCurrentValue()),
                    MessageFormat.format("实例:{0}未配置listeners", instance.getInstanceName()));
            String hostIp = instance.getHostIp();
            assert listenersProperValue != null;
            String listenersPort = listenersProperValue.getCurrentValue();
            brokerAddrList.add(MessageFormat.format("{0}:{1}", hostIp, listenersPort));
        }
        //查询属性
        //ips
        String ips = Joiner.on(",").join(zookeeperAddr);
        //bootstraps
        String bootstraps = Joiner.on(",").join(brokerAddrList);
        String clusterType = BrokerType.KAFKA.getName();
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
        //删除RocketMq zk节点信息
        ZmsContextManager.setEnv(zmsServiceEntity.getEnvironmentId());
        zkClientRouter.deleteClusterInfo(zmsServiceEntity.getServerName());
        logger.info("Delete the zookeeper node:cluster:{}", zmsServiceEntity.getServerName());
    }
}

