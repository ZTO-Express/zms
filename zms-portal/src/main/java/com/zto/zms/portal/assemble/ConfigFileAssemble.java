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

package com.zto.zms.portal.assemble;

import com.zto.zms.dal.model.ProcessPropertyValueRef;

import java.util.List;

/**
 * <p>Class: ConfigFileAssemble</p>
 * <p> Description: 自定义实例配置 </p>
 *
 * @author lidawei
 * @version 1.0
 * @date 2020/3/9
 */
public interface ConfigFileAssemble {


    /**
     * 根据服务实例生成配置特定配置
     *
     * @param serviceId
     * @param instanceId
     * @param propertyValueRefs
     * @return
     */
    List<ProcessPropertyValueRef> assembleConfigFile(Integer serviceId, Integer instanceId, List<ProcessPropertyValueRef> propertyValueRefs);
}

