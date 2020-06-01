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
import com.zto.zms.common.ZmsServiceTypeEnum;
import com.zto.zms.dal.domain.service.HostServiceInstanceDTO;
import com.zto.zms.dal.domain.service.ServicePropertyValueRefDTO;
import com.zto.zms.dal.mapper.*;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.dal.model.ServiceProcess;
import com.zto.zms.dal.model.ServicePropertyValueRef;
import com.zto.zms.portal.assemble.AddInstanceConsumeConfigAssemble;
import com.zto.zms.portal.assemble.ConfigFileAssemble;
import com.zto.zms.portal.assemble.RunCommonConfigAssemble;
import com.zto.zms.portal.common.ProcessStatusEnum;
import com.zto.zms.portal.common.ServiceStartUserEnum;
import com.zto.zms.portal.dto.AddInstanceAssembleDTO;
import com.zto.zms.portal.service.serve.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zto.zms.portal.common.ServicePropertiesNameEnum.*;

/**
 * <p>Class: ZookeeperConfigFileAssembleImpl</p>
 * <p>Description: zookeeper定制配置</p>
 *
 * @author lidawei
 * @date 2020/3/9
 **/
@Component("zookeeper-instance")
public class ZookeeperConfigAssembleImpl implements ConfigFileAssemble, RunCommonConfigAssemble, AddInstanceConsumeConfigAssemble {

    /**
     * 配置关联config api
     */
    private static final String ZOOKEEPER_SERVER_CONFIG = "zookeeper-server-config";

    private static final String ZK_SERVER_START = "zookeeper/zookeeper-server-start.sh";

    private static final String ZOOKEEPER = "zookeeper";

    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ServiceProcessMapper serviceProcessMapper;
    @Autowired
    private ServicePropertyValueRefMapper servicePropertyValueRefMapper;
    @Autowired
    private ProcessPropertyValueRefMapper processPropertyValueRefMapper;
    @Autowired
    private ProcessService processService;
    @Autowired
    private ServicePropertyMapper servicePropertyMapper;

    @Override
    public List<ProcessPropertyValueRef> assembleConfigFile(Integer serviceId, Integer instanceId, List<ProcessPropertyValueRef> propertyValueRefList) {

        List<ProcessPropertyValueRef> propertyValueRefs = Lists.newArrayList();

        ServiceProcess currentProcess = serviceProcessMapper.listLastProcessByInstance(instanceId);
        //是否是对比配置
        boolean preCompare = !ProcessStatusEnum.INIT.name().equals(currentProcess.getStatus());

        List<HostServiceInstanceDTO> instances = serviceInstanceMapper.queryHostServiceInstance(serviceId);
        for (HostServiceInstanceDTO instance : instances) {
            Map<String, String> propertyValueRefDtoS;

            //当前正在运行的进程
            ServiceProcess lastProcess = serviceProcessMapper.listRunningProcessByInstance(instance.getId());
            if (null == lastProcess) {
                continue;
            }
            //如果最近一次进程配置是否生效，如果生效取生效的配置
            //如果配置没有生效，取当前配置
            if (!preCompare && ProcessStatusEnum.SUCCESS.name().equals(lastProcess.getStatus())) {
                propertyValueRefDtoS = processPropertyValueRefMapper.listByServiceProcessId(lastProcess.getId())
                        .stream().collect(Collectors.toMap(ProcessPropertyValueRef::getPropertyName, ProcessPropertyValueRef::getCurrentValue, (k1, k2) -> k2));
            } else {
                propertyValueRefDtoS = servicePropertyValueRefMapper.listByServiceIdAndInstanceId(serviceId, instance.getId())
                        .stream().collect(Collectors.toMap(ServicePropertyValueRefDTO::getPropertyName, ServicePropertyValueRefDTO::getCurrentValue, (k1, k2) -> k2));
            }
            String myId = propertyValueRefDtoS.get(ZOOKEEPER_MYID.getName());
            String serverIp = instance.getHostIp();
            String quorumPort = propertyValueRefDtoS.get(ZOOKEEPER_QUORUM.getName());
            String electionPort = propertyValueRefDtoS.get(ZOOKEEPER_ELECTION_PORT.getName());
            //主机ip
            String zkCluster = MessageFormat.format("server.{0}", myId);
            String zkClusterValue = MessageFormat.format("{0}:{1}:{2}", serverIp, quorumPort, electionPort);
            ProcessPropertyValueRef processPropertyValueRef = new ProcessPropertyValueRef();
            processPropertyValueRef.setPropertyName(zkCluster);
            processPropertyValueRef.setCurrentValue(zkClusterValue);
            processPropertyValueRef.setRealValue(zkClusterValue);
            processPropertyValueRef.setConfApiKey(ZOOKEEPER_SERVER_CONFIG);
            processPropertyValueRef.setPropertyId(0);
            processPropertyValueRef.setPropertyGroup("");
            propertyValueRefs.add(processPropertyValueRef);
        }
        return propertyValueRefs;
    }

    @Override
    public RunCommonConfig assembleRunCommon(Integer processId) {
        String programName = processService.getProgramName(processId);
        RunCommonConfig commonConfig = new RunCommonConfig();
        Map<String, String> environmentConfigMap = processService.environmentConfig(processId);
        Map<String, String> propertiesMap = processService.listConfig(processId, null);

        String myId = propertiesMap.get(ZOOKEEPER_MYID.getName());
        String dataDir = propertiesMap.get(ZOOKEEPER_DATADIR.getName());
        commonConfig.setAutoRestart("unexpected");
        commonConfig.setArgs(Lists.newArrayList(myId, dataDir));
        commonConfig.setCommand(ZK_SERVER_START);
        commonConfig.setEnvironment(environmentConfigMap);
        commonConfig.setExitCodes("0,2");
        commonConfig.setKillAsGroup(true);
        commonConfig.setUser(ServiceStartUserEnum.ZOOKEEPER.getUser());
        commonConfig.setGroup(ServiceStartUserEnum.ZOOKEEPER.getGroup());
        commonConfig.setStartSecs(20);
        commonConfig.setStopWaitSecs(30);
        commonConfig.setProgramName(programName);
        commonConfig.setLibDir(ZOOKEEPER);
        return commonConfig;
    }

    @Override
    public List<AddInstanceAssembleDTO> assembleAddInstance(Integer serviceId, List<Integer> instanceIds, List<ServicePropertyValueRef> servicePropertyValueRefs) {
        //设置myid
        Integer myIdPropertyId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.ZOOKEEPER.name(), ZOOKEEPER_MYID.getInstanceType(),
                ZOOKEEPER_MYID.getGroup(), ZOOKEEPER_MYID.getName());
        List<ServicePropertyValueRef> propertyValueRefDtoS = servicePropertyValueRefMapper.listByServiceIdAndPropertyId(serviceId, null, myIdPropertyId);

        List<AddInstanceAssembleDTO> propertyValueRefs = Lists.newArrayList();
        List<Integer> myIds = propertyValueRefDtoS.stream()
                .map(item -> Integer.valueOf(item.getCurrentValue()))
                .sorted()
                .collect(Collectors.toList());
        int myId = 0;
        for (Integer instanceId : instanceIds) {
            do {
                myId++;
            } while (myIds.contains(myId));
            AddInstanceAssembleDTO servicePropertyValueRef = new AddInstanceAssembleDTO();
            servicePropertyValueRef.setServiceId(serviceId);
            servicePropertyValueRef.setInstanceId(instanceId);
            servicePropertyValueRef.setPropertyId(myIdPropertyId);
            servicePropertyValueRef.setCurrentValue(String.valueOf(myId));
            servicePropertyValueRef.setInstanceName("instance-" + myId);
            propertyValueRefs.add(servicePropertyValueRef);
        }
        return propertyValueRefs;
    }
}

