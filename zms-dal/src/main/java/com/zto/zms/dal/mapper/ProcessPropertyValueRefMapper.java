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

package com.zto.zms.dal.mapper;

import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.dal.model.ServiceInstance;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessPropertyValueRefMapper {

    int insert(ProcessPropertyValueRef record);

    int insertList(@Param("list") List<ProcessPropertyValueRef> records);

    /**
     * 进程属性配置信息
     *
     * @param processIds
     * @param servicePropertyId
     * @return
     */
    List<ProcessPropertyValueRef> listByProcessId(@Param("processIds") List<Integer> processIds, @Param("servicePropertyId") Integer servicePropertyId);

    List<ProcessPropertyValueRef> listByProcessIdAndPropertyId(@Param("processIds") List<Integer> processIds, @Param("propertyIds") List<Integer> propertyIds);

    List<ProcessPropertyValueRef> listByServiceProcessId(Integer serviceProcessId);

    /**
     * 进程属性,带属性类型
     *
     * @param serviceProcessId
     * @return
     */
    List<ProcessPropertyValueRef> listValueTypeByServiceProcessId(Integer serviceProcessId);


    ProcessPropertyValueRef getProcessPropertyValueRef(@Param("serviceProcessId") Integer serviceProcessId, @Param("propertyId") Integer propertyId);

    List<ProcessPropertyValueRef> listByProcessIdAndApiKey(@Param("serviceProcessId") Integer serviceProcessId, @Param("configScope") String configScope);

    List<ProcessPropertyValueRef> listByProcessIdName(@Param("processId") Integer processId, @Param("propertyName") String propertyName);

    List<String> listRunningService(@Param("propertyIds") List<Integer> propertyIds, @Param("serviceId") Integer serviceId);

    List<ServiceInstance> listRunningServiceByProcessId(@Param("processId") Integer processId);
}
