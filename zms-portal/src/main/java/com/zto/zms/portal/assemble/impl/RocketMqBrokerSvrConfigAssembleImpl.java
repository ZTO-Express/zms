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

package com.zto.zms.portal.assemble.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.zto.zms.common.RunCommonConfig;
import com.zto.zms.common.ZmsServiceTypeEnum;
import com.zto.zms.utils.Assert;
import com.zto.zms.dal.domain.service.HostServiceInstanceDTO;
import com.zto.zms.dal.mapper.*;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.dal.model.ServiceProcess;
import com.zto.zms.dal.model.ServicePropertyValueRef;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.assemble.AddInstanceConsumeConfigAssemble;
import com.zto.zms.portal.assemble.ConfigFileAssemble;
import com.zto.zms.portal.assemble.RunCommonConfigAssemble;
import com.zto.zms.portal.common.InstanceTypeEnum;
import com.zto.zms.portal.common.ProcessStatusEnum;
import com.zto.zms.portal.common.ServiceStartUserEnum;
import com.zto.zms.portal.dto.AddInstanceAssembleDTO;
import com.zto.zms.portal.service.serve.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zto.zms.portal.common.ServicePropertiesNameEnum.*;

/**
 * <p>Class: RocketMqBrokerSvrConfigAssembleImpl</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/12
 **/
@Component("rocketmq-broker")
public class RocketMqBrokerSvrConfigAssembleImpl implements ConfigFileAssemble, RunCommonConfigAssemble, AddInstanceConsumeConfigAssemble {
    private static final String NAMESRV_ADDR = "namesrvAddr";
    private static final String BROKER_IP1 = "brokerIP1";
    private static final String BROKER_CLUSTER_NAME = "brokerClusterName";
    private static final String ROCKETMQ_BROKER_CONFIG = "rocketmq-broker-config";
    private static final String ROCKETMQ_BROKER_START = "rocketmq/rocketmq-broker-start.sh";
    private static final String ROCKET_MQ = "rocketmq";
    private static final String[] WORDS = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ServiceProcessMapper serviceProcessMapper;
    @Autowired
    private ProcessPropertyValueRefMapper processPropertyValueRefMapper;
    @Autowired
    private ProcessService processService;
    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private ServicePropertyMapper servicePropertyMapper;
    @Autowired
    private ServicePropertyValueRefMapper servicePropertyValueRefMapper;

    /**
     * broker的nameSrvAddr、brokerIP1、brokerClusterName配置
     *
     * @param instanceId 当前服务instanceId
     */
    @Override
    public List<ProcessPropertyValueRef> assembleConfigFile(Integer serviceId, Integer instanceId, List<ProcessPropertyValueRef> propertyValueRefs) {
        //获取服务id
        ZmsServiceEntity zmsService = zmsServiceMapper.getById(serviceId);
        //所有实例
        Map<String, List<HostServiceInstanceDTO>> instancesTypeMap =
                serviceInstanceMapper.queryHostServiceInstance(serviceId)
                        .stream().collect(Collectors.groupingBy(HostServiceInstanceDTO::getInstanceType));

        Integer nameSvrPropertyId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.ROCKETMQ.name(),
                ROCKETMQ_NAMESERVER_PORT.getInstanceType(), ROCKETMQ_NAMESERVER_PORT.getGroup(), ROCKETMQ_NAMESERVER_PORT.getName());

        ServiceProcess currentProcess = serviceProcessMapper.listLastProcessByInstance(instanceId);
        boolean preCompare = !ProcessStatusEnum.INIT.name().equals(currentProcess.getStatus());

        //nameSvr配置
        List<HostServiceInstanceDTO> nameSvrInstances = instancesTypeMap.get(InstanceTypeEnum.NAMESVR.name());
        List<String> addrList = Lists.newArrayList();
        for (HostServiceInstanceDTO instance : nameSvrInstances) {
            String listenPort;
            //当前正在运行的进程
            ServiceProcess lastProcess = serviceProcessMapper.listRunningProcessByInstance(instance.getId());
            if (null == lastProcess) {
                continue;
            }
            //如果最近一次进程配置是否生效，如果生效取生效的配置
            //如果配置没有生效，取当前配置
            if (!preCompare && ProcessStatusEnum.SUCCESS.name().equals(lastProcess.getStatus())) {
                ProcessPropertyValueRef listenersProperValue =
                        processPropertyValueRefMapper.getProcessPropertyValueRef(lastProcess.getId(), nameSvrPropertyId);
                listenPort = listenersProperValue.getCurrentValue();
            } else {
                ServicePropertyValueRef servicePropertyValueRef =
                        servicePropertyValueRefMapper.listByServiceIdAndPropertyId(lastProcess.getServiceId(), lastProcess.getInstanceId(), nameSvrPropertyId).get(0);
                listenPort = servicePropertyValueRef.getCurrentValue();
            }
            String hostIp = instance.getHostIp();
            addrList.add(MessageFormat.format("{0}:{1}", hostIp, listenPort));
        }
        Assert.that(!CollectionUtils.isEmpty(addrList), "集群未安装nameSvr");

        String nameSrvAddr = Joiner.on(";").join(addrList);
        //服务实例主机ip
        String brokerIp1 = instancesTypeMap.get(InstanceTypeEnum.BROKER.name())
                .stream()
                .filter(item -> item.getId().equals(instanceId))
                .findFirst().orElse(new HostServiceInstanceDTO()).getHostIp();

        List<ProcessPropertyValueRef> addProperties = Lists.newArrayList();
        addProperties(addProperties, NAMESRV_ADDR, nameSrvAddr);
        addProperties(addProperties, BROKER_IP1, brokerIp1);
        addProperties(addProperties, BROKER_CLUSTER_NAME, zmsService.getServerName());
        return addProperties;
    }


    @Override
    public RunCommonConfig assembleRunCommon(Integer processId) {
        String programName = processService.getProgramName(processId);
        Map<String, String> environmentConfigMap = processService.environmentConfig(processId);

        RunCommonConfig commonConfig = new RunCommonConfig();
        commonConfig.setAutoRestart("unexpected");
        commonConfig.setCommand(ROCKETMQ_BROKER_START);
        commonConfig.setEnvironment(environmentConfigMap);
        commonConfig.setExitCodes("0,2");
        commonConfig.setUser(ServiceStartUserEnum.ROCKERMQ.getUser());
        commonConfig.setGroup(ServiceStartUserEnum.ROCKERMQ.getGroup());
        commonConfig.setKillAsGroup(true);
        commonConfig.setStopAsGroup(true);
        commonConfig.setStartSecs(15);
        commonConfig.setStopWaitSecs(15);
        commonConfig.setProgramName(programName);
        commonConfig.setLibDir(ROCKET_MQ);
        return commonConfig;
    }

    private void addProperties(List<ProcessPropertyValueRef> valueRefs, String name, String value) {
        ProcessPropertyValueRef propertyValueRef = new ProcessPropertyValueRef();
        propertyValueRef.setPropertyName(name);
        propertyValueRef.setRealValue(value);
        propertyValueRef.setCurrentValue(value);
        propertyValueRef.setConfApiKey(ROCKETMQ_BROKER_CONFIG);
        propertyValueRef.setPropertyId(0);
        propertyValueRef.setPropertyGroup("");
        valueRefs.add(propertyValueRef);
    }

    @Override
    public List<AddInstanceAssembleDTO> assembleAddInstance(Integer serviceId, List<Integer> instanceIds, List<ServicePropertyValueRef> servicePropertyValueRefs) {
        if (CollectionUtils.isEmpty(instanceIds)) {
            return Lists.newArrayList();
        }
        List<AddInstanceAssembleDTO> propertyValueRefs = Lists.newArrayList();
        //brokerName
        Integer brokerNamePropertyId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.ROCKETMQ.name(),
                ROCKETMQ_BROKER_NAME.getInstanceType(), ROCKETMQ_BROKER_NAME.getGroup(), ROCKETMQ_BROKER_NAME.getName());
        //brokerId
        Integer brokerIdPropertyId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.ROCKETMQ.name(),
                ROCKETMQ_BROKER_ID.getInstanceType(), ROCKETMQ_BROKER_ID.getGroup(), ROCKETMQ_BROKER_ID.getName());

        Map<Integer, String> instanceIdNameMap = servicePropertyValueRefs
                .stream()
                .filter(item -> brokerNamePropertyId.equals(item.getPropertyId()))
                .collect(Collectors.toMap(ServicePropertyValueRef::getInstanceId, ServicePropertyValueRef::getCurrentValue));


        Map<Integer, String> instanceIdBrokerIdMap = servicePropertyValueRefs
                .stream()
                .filter(item -> brokerIdPropertyId.equals(item.getPropertyId()))
                .collect(Collectors.toMap(ServicePropertyValueRef::getInstanceId, ServicePropertyValueRef::getCurrentValue));

        //生成brokerName
        for (int i = 0; i < instanceIds.size(); i++) {
            Integer instanceId = instanceIds.get(i);
            String brokerName = instanceIdNameMap.containsKey(instanceId) ? instanceIdNameMap.get(instanceId) : "broker-" + WORDS[i];
            boolean master = "0".equals(instanceIdBrokerIdMap.get(instanceId));
            String instanceName = master ? brokerName : brokerName + "-s";
            AddInstanceAssembleDTO brokerNameValueRef = new AddInstanceAssembleDTO();
            brokerNameValueRef.setServiceId(serviceId);
            brokerNameValueRef.setInstanceId(instanceId);
            brokerNameValueRef.setPropertyId(brokerNamePropertyId);
            brokerNameValueRef.setCurrentValue(brokerName);
            brokerNameValueRef.setInstanceName(instanceName);
            propertyValueRefs.add(brokerNameValueRef);
        }
        return propertyValueRefs;
    }
}

