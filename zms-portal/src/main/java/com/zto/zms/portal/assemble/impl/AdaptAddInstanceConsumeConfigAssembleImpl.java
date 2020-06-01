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
import com.zto.zms.dal.mapper.ServiceInstanceMapper;
import com.zto.zms.dal.mapper.ZmsServiceMapper;
import com.zto.zms.dal.model.ServiceInstance;
import com.zto.zms.dal.model.ServicePropertyValueRef;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.assemble.AddInstanceConsumeConfigAssemble;
import com.zto.zms.portal.dto.AddInstanceAssembleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Class: AdaptAddInstanceConsumeConfigAssemble</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/17
 **/
@Component
public class AdaptAddInstanceConsumeConfigAssembleImpl implements AddInstanceConsumeConfigAssemble {
    @Autowired
    private Map<String, AddInstanceConsumeConfigAssemble> consumeConfigAssembleMap;
    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ZmsServiceMapper serviceMapper;

    @Override
    public List<AddInstanceAssembleDTO> assembleAddInstance(Integer serviceId, List<Integer> instanceIds, List<ServicePropertyValueRef> servicePropertyValueRefs) {
        ZmsServiceEntity serviceEntity = serviceMapper.getById(serviceId);
        List<ServiceInstance> serviceInstances = serviceInstanceMapper.listByInstanceIds(instanceIds);
        Map<String, List<ServiceInstance>> instanceTypeInstancesMap = serviceInstances.stream().collect(Collectors.groupingBy(ServiceInstance::getInstanceType));
        List<AddInstanceAssembleDTO> assembleDtoList = Lists.newArrayList();

        for (Map.Entry<String, List<ServiceInstance>> instances : instanceTypeInstancesMap.entrySet()) {
            String programType = MessageFormat.format("{0}-{1}", serviceEntity.getServerType(), instances.getKey()).toLowerCase();
            if (consumeConfigAssembleMap.containsKey(programType)) {
                List<Integer> instanceIdsByType = instances.getValue().stream().map(ServiceInstance::getId).collect(Collectors.toList());
                List<AddInstanceAssembleDTO> instanceAssembleDtoList = consumeConfigAssembleMap.get(programType).assembleAddInstance(serviceId, instanceIdsByType, servicePropertyValueRefs);
                assembleDtoList.addAll(instanceAssembleDtoList);
            }
        }
        return assembleDtoList;
    }
}

