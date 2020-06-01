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

import com.zto.zms.dal.model.ServiceProperty;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePropertyMapper {
    int insert(ServiceProperty record);


    /**
     * 根据服务类型和属性名称，查询属性id
     *
     * @param serviceType
     * @param instanceType
     * @param propertyGroup
     * @param propertyName
     * @return
     */
    Integer getIdByServiceTypeAndName(@Param("serviceType") String serviceType,
                                      @Param("instanceType") String instanceType,
                                      @Param("propertyGroup") String propertyGroup,
                                      @Param("propertyName") String propertyName);

    /**
     * 查询集群的所有配置参数
     */
    List<ServiceProperty> getClusterProperty();

    List<ServiceProperty> getServiceProperty(@Param("serviceType") String serviceType,
                                             @Param("instanceTypes") List<String> instanceTypes,
                                             @Param("scopes") List<String> scopes);

    /**
     * 查询被依赖的服务配置项ID
     */
    List<Integer> getIdByServiceType(@Param("serviceType") String serviceType);
}
