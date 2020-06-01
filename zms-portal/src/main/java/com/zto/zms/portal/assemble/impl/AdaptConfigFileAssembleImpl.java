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
import com.zto.zms.dal.mapper.ServiceProcessMapper;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.portal.assemble.ConfigFileAssemble;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>Class: AdaptConfigFileAssembleImpl</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/9
 **/
@Component
public class AdaptConfigFileAssembleImpl implements ConfigFileAssemble {
    @Autowired
    private Map<String, ConfigFileAssemble> configFileAssembleMap;
    @Autowired
    private ServiceProcessMapper serviceProcessMapper;

    @Override
    public List<ProcessPropertyValueRef> assembleConfigFile(Integer serviceId, Integer instanceId, List<ProcessPropertyValueRef> propertyValueRefs) {
        String programType = serviceProcessMapper.getProgramTypeByInstanceId(instanceId);
        if (!configFileAssembleMap.containsKey(programType)) {
            return Lists.newArrayList();
        }
        return configFileAssembleMap.get(programType).assembleConfigFile(serviceId, instanceId, propertyValueRefs);
    }
}

