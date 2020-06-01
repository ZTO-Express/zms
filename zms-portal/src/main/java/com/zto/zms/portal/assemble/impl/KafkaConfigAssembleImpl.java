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
import com.zto.zms.dal.mapper.ServiceInstanceMapper;
import com.zto.zms.dal.mapper.ServicePropertyMapper;
import com.zto.zms.dal.mapper.ServicePropertyValueRefMapper;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.dal.model.ServicePropertyValueRef;
import com.zto.zms.portal.assemble.AddInstanceConsumeConfigAssemble;
import com.zto.zms.portal.assemble.ConfigFileAssemble;
import com.zto.zms.portal.assemble.RunCommonConfigAssemble;
import com.zto.zms.portal.common.ServiceStartUserEnum;
import com.zto.zms.portal.dto.AddInstanceAssembleDTO;
import com.zto.zms.portal.service.serve.ProcessService;
import com.zto.zms.portal.service.serve.ServeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zto.zms.portal.common.ServicePropertiesNameEnum.*;

/**
 * <p>Class: KafkaConfigAssembleImpl</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/11
 **/
@Component("kafka-broker")
public class KafkaConfigAssembleImpl implements ConfigFileAssemble, RunCommonConfigAssemble, AddInstanceConsumeConfigAssemble {

    private static final String KAFKA = "kafka";
    private static final String KAFKA_START = "kafka/kafka-server-start.sh";
    private static final String KAFKA_SERVER_CONFIG = "kafka-server-config";
    private static final String ZOOKEEPER_CONNECT = "zookeeper.connect";
    private static final String LISTENERS = "listeners";

    @Autowired
    private ProcessService processService;
    @Autowired
    private ServeService serveService;
    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ServicePropertyMapper servicePropertyMapper;
    @Autowired
    private ServicePropertyValueRefMapper servicePropertyValueRefMapper;


    @Override
    public List<ProcessPropertyValueRef> assembleConfigFile(Integer serviceId, Integer instanceId, List<ProcessPropertyValueRef> propertyValueRefs) {
        Integer zkPropertyId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.KAFKA.name(),
                ZK_SERVICE.getInstanceType(), ZK_SERVICE.getGroup(), ZK_SERVICE.getName());
        Integer listenerPropertyId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.KAFKA.name(),
                KAFKA_PORT.getInstanceType(), KAFKA_PORT.getGroup(), KAFKA_PORT.getName());

        Integer zookeeperChRootPropertyId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.KAFKA.name(),
                KAFKA_ZOOKEEPER_CHROOT.getInstanceType(), KAFKA_ZOOKEEPER_CHROOT.getGroup(), KAFKA_ZOOKEEPER_CHROOT.getName());

        List<ServicePropertyValueRef> propertyValueRefDtoS = servicePropertyValueRefMapper.listByServiceIdAndPropertyId(serviceId, null, zkPropertyId);
        //kafka zookeeper配置
        Assert.that(!CollectionUtils.isEmpty(propertyValueRefDtoS), "[kafka]未配置zookeeper依赖");
        Integer zkServiceId = Integer.valueOf(propertyValueRefDtoS.get(0).getCurrentValue());
        List<String> zookeeperAddr = serveService.listZookeeperAddr(zkServiceId);
        Assert.that(!CollectionUtils.isEmpty(zookeeperAddr), "[kafka]配置的zookeeper地址无效");
        List<ProcessPropertyValueRef> valueRefs = Lists.newArrayList();
        String zookeeperConnect = Joiner.on(",").join(zookeeperAddr);

        //zookeeper node根目录
        List<ServicePropertyValueRef> zookeeperChRoots = servicePropertyValueRefMapper.listByServiceIdAndPropertyId(serviceId, null, zookeeperChRootPropertyId);
        if (!CollectionUtils.isEmpty(zookeeperChRoots)) {
            String zookeeperChRoot = zookeeperChRoots.get(0).getCurrentValue();
            if (StringUtils.isNotBlank(zookeeperChRoot)) {
                zookeeperConnect += zookeeperChRoot;
            }
        }
        addProperties(valueRefs, 0, ZOOKEEPER_CONNECT, zkServiceId.toString(), zookeeperConnect);

        //kafka listeners配置  listeners = listener_name://host_name:port
        List<ServicePropertyValueRef> listenersPropertyValue = servicePropertyValueRefMapper.listByServiceIdAndPropertyId(serviceId, instanceId, listenerPropertyId);
        Assert.that(!CollectionUtils.isEmpty(listenersPropertyValue), "[kafka]未配置listeners");
        String listenersPort = listenersPropertyValue.get(0).getCurrentValue();

        HostServiceInstanceDTO hostServiceInstanceDto = serviceInstanceMapper.getHostByInstanceId(instanceId);
        String listeners = MessageFormat.format("PLAINTEXT://{0}:{1}", hostServiceInstanceDto.getHostIp(), listenersPort);
        addProperties(valueRefs, listenerPropertyId, LISTENERS, listenersPort, listeners);
        return valueRefs;
    }

    @Override
    public RunCommonConfig assembleRunCommon(Integer processId) {
        String programName = processService.getProgramName(processId);
        Map<String, String> environmentConfigMap = processService.environmentConfig(processId);

        RunCommonConfig commonConfig = new RunCommonConfig();
        commonConfig.setAutoRestart("unexpected");
        commonConfig.setCommand(KAFKA_START);
        commonConfig.setEnvironment(environmentConfigMap);
        commonConfig.setExitCodes("0,2");
        commonConfig.setUser(ServiceStartUserEnum.KAFKA.getUser());
        commonConfig.setGroup(ServiceStartUserEnum.KAFKA.getGroup());
        commonConfig.setKillAsGroup(true);
        commonConfig.setStartSecs(20);
        commonConfig.setStopWaitSecs(30);
        commonConfig.setProgramName(programName);
        commonConfig.setLibDir(KAFKA);
        return commonConfig;
    }

    private void addProperties(List<ProcessPropertyValueRef> valueRefs, Integer propertyId, String name, String currentValue, String realValue) {
        ProcessPropertyValueRef propertyValueRef = new ProcessPropertyValueRef();
        propertyValueRef.setPropertyName(name);
        propertyValueRef.setRealValue(realValue);
        propertyValueRef.setCurrentValue(currentValue);
        propertyValueRef.setConfApiKey(KAFKA_SERVER_CONFIG);
        propertyValueRef.setPropertyId(propertyId);
        propertyValueRef.setPropertyGroup("");
        valueRefs.add(propertyValueRef);
    }

    @Override
    public List<AddInstanceAssembleDTO> assembleAddInstance(Integer serviceId, List<Integer> instanceIds, List<ServicePropertyValueRef> servicePropertyValueRefs) {
        //broker.id
        Integer brokerIdPropertyId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.KAFKA.name(),
                KAFKA_BROKERID.getInstanceType(), KAFKA_BROKERID.getGroup(), KAFKA_BROKERID.getName());
        List<ServicePropertyValueRef> propertyValueRefDtos = servicePropertyValueRefMapper.listByServiceIdAndPropertyId(serviceId, null, brokerIdPropertyId);

        List<AddInstanceAssembleDTO> propertyValueRefs = Lists.newArrayList();
        List<Integer> brokerIds = propertyValueRefDtos.stream()
                .map(item -> Integer.valueOf(item.getCurrentValue()))
                .sorted()
                .collect(Collectors.toList());

        int brokerId = brokerIds.size() > 0 ? brokerIds.get(brokerIds.size() - 1) : 0;
        for (Integer instanceId : instanceIds) {
            brokerId++;
            AddInstanceAssembleDTO servicePropertyValueRef = new AddInstanceAssembleDTO();
            servicePropertyValueRef.setServiceId(serviceId);
            servicePropertyValueRef.setInstanceId(instanceId);
            servicePropertyValueRef.setPropertyId(brokerIdPropertyId);
            servicePropertyValueRef.setCurrentValue(String.valueOf(brokerId));
            servicePropertyValueRef.setInstanceName("broker-" + brokerId);
            propertyValueRefs.add(servicePropertyValueRef);
        }
        return propertyValueRefs;
    }
}

