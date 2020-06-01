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

import com.zto.zms.dal.domain.service.ZmsClusterServiceDTO;
import com.zto.zms.dal.model.ZmsServiceEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZmsServiceMapper {

    int insert(ZmsServiceEntity record);

    List<ZmsServiceEntity> listByEnvId(Integer envId);

    ZmsServiceEntity getById(Integer id);

    ZmsClusterServiceDTO getServiceAndEnvById(Integer id);

    /**
     * 查询环境下的消息集群
     * 传入envId查询单一环境下的集群
     * 传入serviceType(ROCKETMQ或KAFKA)查询单一集群
     */
    List<ZmsClusterServiceDTO> queryClusters(@Param("envId") Integer envId, @Param("envName") String envName,
                                             @Param("serviceId") Integer serviceId, @Param("serviceType") String serviceType,
                                             @Param("serviceName") String serviceName);

    /**
     * 集群迁移列表
     *
     * @param envId
     * @param keyWord
     * @return
     */
    List<ZmsClusterServiceDTO> queryClustersByKeyWord(@Param("envId") Integer envId, @Param("keyWord") String keyWord);

    /**
     * 根据集群类型获取集群信息
     *
     * @param envId
     * @param serviceTypes
     * @return
     */
    List<ZmsServiceEntity> queryClustersByType(@Param("envId") Integer envId, @Param("serviceTypes") List<String> serviceTypes);

    /**
     * 主机下服务列表
     *
     * @param envId
     * @param hostId
     * @return
     */
    List<ZmsServiceEntity> queryHostServeList(@Param("envId") int envId, @Param("hostId") int hostId);

    /**
     * 查询环境指定类型服务
     *
     * @param envId
     * @param serverType
     * @return
     */
    List<ZmsServiceEntity> listByEnvIdAndType(@Param("envId") Integer envId, @Param("serverType") String serverType);

    /**
     * 查询环境指定类型启动过的服务
     */
    List<ZmsServiceEntity> findByEnvIdAndType(@Param("envId") Integer envId, @Param("serverType") String serverType);

    /**
     * 根据id查询服务
     *
     * @param serviceIds
     * @return
     */
    List<ZmsServiceEntity> listByIds(@Param("serviceIds") List<Integer> serviceIds);

    /**
     * 集群列表
     *
     * @param serviceIds
     * @return
     */
    List<ZmsServiceEntity> listClustersByServiceIds(@Param("serviceIds") List<Integer> serviceIds);

    /**
     * 根据环境和集群名称查询
     *
     * @param envId
     * @param clusterName
     * @return
     */
    ZmsServiceEntity getClusterByEnvIdAndName(@Param("envId") Integer envId, @Param("clusterName") String clusterName);

    /**
     * 根据环境和服务名称查询
     *
     * @param envId
     * @param serverName
     * @return
     */
    ZmsServiceEntity getByEnvIdAndName(@Param("envId") Integer envId, @Param("serverName") String serverName);

    /**
     * 重命名服务
     *
     * @param id
     * @param serverName
     * @return
     */
    int rename(@Param("id") Integer id, @Param("serverName") String serverName);

    int deleteById(Integer id);
}
