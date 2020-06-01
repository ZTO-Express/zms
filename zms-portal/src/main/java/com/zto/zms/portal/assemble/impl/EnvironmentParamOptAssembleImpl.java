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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <p>Class: EnvironmentParamOptAssembleImpl</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/12
 **/
@Component
public class EnvironmentParamOptAssembleImpl implements ParamOptAssemble {
    private static final String CONFIG_SCOPE_ENVIRONMENT = "environment";

    @Autowired
    private ProcessService processService;

    @Override
    public Map<String, String> assembleParamOpt(Integer processId) {
        Map<String, List<ProcessPropertyValueRef>> envParamMap = processService.processParamMap(processId, CONFIG_SCOPE_ENVIRONMENT);
        Map<String, String> resultEnvParamMap = Maps.newHashMap();
        //生成环境变量
        envParamMap.forEach((confApiKey, list) -> {
            StringBuilder stringBuilder = new StringBuilder();
            list.forEach(value -> stringBuilder.append(" ")
                    .append("-D")
                    .append(value.getPropertyName())
                    .append("=")
                    .append(value.getRealValue()));
            resultEnvParamMap.put(confApiKey, stringBuilder.substring(1));
        });
        return resultEnvParamMap;
    }
}

