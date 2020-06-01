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

import com.google.common.collect.Lists;
import com.zto.zms.common.RunCommonConfig;
import com.zto.zms.dal.mapper.ServiceInstanceMapper;
import com.zto.zms.dal.model.ServiceInstance;
import com.zto.zms.dal.model.ServicePropertyValueRef;
import com.zto.zms.portal.assemble.AddInstanceConsumeConfigAssemble;
import com.zto.zms.portal.assemble.RunCommonConfigAssemble;
import com.zto.zms.portal.common.InstanceTypeEnum;
import com.zto.zms.portal.common.ServiceStartUserEnum;
import com.zto.zms.portal.dto.AddInstanceAssembleDTO;
import com.zto.zms.portal.service.serve.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Class: RocketMqNameSvrConfigAssembleImpl</p>
 * <p>Description: rocketMq nameSvr启动</p>
 *
 * @author lidawei
 * @date 2020/3/12
 **/

@Component("rocketmq-namesvr")
public class RocketMqNameSvrConfigAssembleImpl implements RunCommonConfigAssemble, AddInstanceConsumeConfigAssemble {

    private static final String ROCKET_MQ = "rocketmq";
    private static final String ROCKET_MQ_NAME_SERVER_START = "rocketmq/rocketmq-namesvr-start.sh";
    @Autowired
    private ProcessService processService;
    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;

    @Override
    public RunCommonConfig assembleRunCommon(Integer processId) {
        String programName = processService.getProgramName(processId);
        Map<String, String> environmentConfigMap = processService.environmentConfig(processId);

        RunCommonConfig commonConfig = new RunCommonConfig();
        commonConfig.setAutoRestart("unexpected");
        commonConfig.setCommand(ROCKET_MQ_NAME_SERVER_START);
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

    @Override
    public List<AddInstanceAssembleDTO> assembleAddInstance(Integer serviceId, List<Integer> instanceIds, List<ServicePropertyValueRef> servicePropertyValueRefs) {
        if (CollectionUtils.isEmpty(instanceIds)) {
            return Lists.newArrayList();
        }
        List<AddInstanceAssembleDTO> propertyValueRefs = Lists.newArrayList();
        List<ServiceInstance> serviceInstanceDtoS = serviceInstanceMapper.listByServiceIdAndInstanceType(serviceId, InstanceTypeEnum.NAMESVR.name());
        serviceInstanceDtoS = serviceInstanceDtoS.stream().filter(item -> !instanceIds.contains(item.getId())).collect(Collectors.toList());
        int nameSvrCount = serviceInstanceDtoS.size();
        for (Integer instanceId : instanceIds) {
            nameSvrCount++;
            AddInstanceAssembleDTO brokerNameValueRef = new AddInstanceAssembleDTO();
            brokerNameValueRef.setServiceId(serviceId);
            brokerNameValueRef.setInstanceId(instanceId);
            brokerNameValueRef.setInstanceName("nameSvr-" + nameSvrCount);
            propertyValueRefs.add(brokerNameValueRef);
        }
        return propertyValueRefs;
    }
}

