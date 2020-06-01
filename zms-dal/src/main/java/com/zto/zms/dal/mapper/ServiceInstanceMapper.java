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

import com.zto.zms.dal.domain.service.HostServiceInstanceDTO;
import com.zto.zms.dal.model.ServiceInstance;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceInstanceMapper {

    List<ServiceInstance> hostServices(@Param("id") Integer id);

    int insert(ServiceInstance record);

    int insertList(@Param("list") List<ServiceInstance> records);

    ServiceInstance getById(Integer id);

    /**
     * 可用实例列表
     *
     * @param serviceId
     * @returnHostServiceInstanceDTO
     */
    List<ServiceInstance> listStartInstanceByServiceId(Integer serviceId);

    List<ServiceInstance> listInstanceByServiceId(@Param("serviceId") Integer serviceId);

    List<ServiceInstance> listInstanceByServiceIds(@Param("serviceIds") List<Integer> serviceIds);

    List<HostServiceInstanceDTO> queryHostServiceInstance(Integer serviceId);

    int deleteByIds(@Param("ids") List<Integer> ids);

    List<ServiceInstance> findByHostIdsAndStatus(@Param("hostIds") List<Integer> hostIds, @Param("instanceStatus") String instanceStatus);

    HostServiceInstanceDTO getHostByInstanceId(Integer instanceId);

    List<ServiceInstance> listByInstanceIds(@Param("instanceIds") List<Integer> instanceIds);

    List<HostServiceInstanceDTO> listHostByInstanceIds(@Param("instanceIds") List<Integer> instanceIds);

    int updateInstanceNameById(@Param("instanceId") Integer instanceId, @Param("instanceName") String instanceName);

    int stopInstance(@Param("instanceId") Integer instanceId, @Param("modifier") String modifier);

    int updateInstanceStatus(@Param("instanceId") Integer instanceId, @Param("instanceStatus") String instanceStatus);

    List<ServiceInstance> listByServiceIdAndInstanceType(@Param("serviceId") Integer serviceId, @Param("instanceType") String instanceType);

    List<ServiceInstance> listAllByServiceId(Integer serviceId);

    List<ServiceInstance> listAllByEnvId(Integer envId);
}
