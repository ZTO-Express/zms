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

import com.zto.zms.dal.domain.service.ServicePropertyValueRefDTO;
import com.zto.zms.dal.model.ServicePropertyValueRef;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePropertyValueRefMapper {

    int insert(ServicePropertyValueRef record);

    int insertList(@Param("list") List<ServicePropertyValueRef> records);

    /**
     * 根据主键修改服务或实例配置
     */
    int updateById(ServicePropertyValueRef record);

    /**
     * 根据主键删除服务或实例配置
     */
    int deleteByIds(@Param("ids") List<Integer> ids);

    /**
     * 查询服务实例配置，不包含服务配置
     */
    List<ServicePropertyValueRefDTO> queryByInstanceId(Integer instanceId);

    /**
     * 查询服务配置，不包含服务实例配置
     */
    List<ServicePropertyValueRefDTO> queryByServiceId(Integer serviceId);

    /**
     * 查询所有服务配置
     *
     * @param serviceId
     * @return
     */
    List<ServicePropertyValueRefDTO> listAllByServiceId(Integer serviceId);

    /**
     * 服务、实例配置数据
     */
    List<ServicePropertyValueRef> listByServiceIdAndPropertyId(@Param("serviceId") Integer serviceId,
                                                               @Param("instanceId") Integer instanceId,
                                                               @Param("propertyId") Integer propertyId);

    /**
     * 删除服务实例配置，不包含服务配置
     */
    int deleteByInstanceId(@Param("instanceId") Integer instanceId);

    /**
     * 删除服务配置，不包含服务实例配置
     */
    int deleteByServiceId(@Param("serviceId") Integer serviceId);

    List<ServicePropertyValueRefDTO> listAllByServiceIdAndPropertyId(Integer serviceId, Integer clusterPropertyId);

    /**
     * 更新配置值
     *
     * @param id
     * @param currentValue
     * @return
     */
    int updateCurrentValueById(@Param("id") Integer id, @Param("currentValue") String currentValue);

    List<ServicePropertyValueRefDTO> listByServiceIdAndInstanceId(@Param("serviceId") Integer serviceId, @Param("instanceId") Integer instanceId);
}
