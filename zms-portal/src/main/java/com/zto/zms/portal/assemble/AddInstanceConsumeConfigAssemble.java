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


import com.zto.zms.dal.model.ServicePropertyValueRef;
import com.zto.zms.portal.dto.AddInstanceAssembleDTO;

import java.util.List;

/**
 * <p>Class: AddInstanceConsumeConfigAssemble</p>
 * <p> Description: 添加服务实例初始化配置</p>
 *
 * @author lidawei
 * @version 1.0
 * @date 2020/3/16
 */
public interface AddInstanceConsumeConfigAssemble {

    /**
     * 添加服务实例服务初始化配置
     *
     * @param serviceId
     * @param instanceIds
     * @return
     */
    List<AddInstanceAssembleDTO> assembleAddInstance(Integer serviceId, List<Integer> instanceIds, List<ServicePropertyValueRef> servicePropertyValueRefs);
}

