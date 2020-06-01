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

import com.google.common.collect.Maps;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.portal.assemble.ParamOptAssemble;
import com.zto.zms.portal.service.serve.ProcessService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * <p>Class: JmxParamOptAssembleImpl</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/12
 **/
@Component
public class JmxParamOptAssembleImpl implements ParamOptAssemble {

    private static final String CONFIG_SCOPE_JMX = "jmx";

    private static final String JMX_REMOTE = "com.sun.management.jmxremote";

    @Autowired
    private ProcessService processService;

    @Override
    public Map<String, String> assembleParamOpt(Integer processId) {
        Map<String, List<ProcessPropertyValueRef>> jmxParamMap = processService.processParamMap(processId, CONFIG_SCOPE_JMX);
        if (CollectionUtils.isEmpty(jmxParamMap)) {
            return Maps.newHashMap();
        }

        Map<String, String> resultEnvParamMap = Maps.newHashMap();
        //生成环境变量
        jmxParamMap.forEach((confApiKey, propertyValueRefs) -> {
            StringBuilder stringBuilder = new StringBuilder();
            propertyValueRefs.stream()
                    .filter(value -> StringUtils.isNotEmpty(value.getRealValue()))
                    .forEach(value -> stringBuilder.append(" ")
                            .append("-D")
                            .append(value.getPropertyName())
                            .append("=")
                            .append(value.getRealValue()));
            if (StringUtils.isNotEmpty(stringBuilder.toString())) {
                stringBuilder.append(" ")
                        .append("-D")
                        .append(JMX_REMOTE);
            }
            resultEnvParamMap.put(confApiKey, stringBuilder.substring(1));
        });
        return resultEnvParamMap;
    }
}

