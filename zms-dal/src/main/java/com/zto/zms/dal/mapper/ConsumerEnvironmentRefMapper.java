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

import com.zto.zms.dal.domain.alert.EnvironmentRefDTO;
import com.zto.zms.dal.model.ConsumerEnvironmentRef;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumerEnvironmentRefMapper {

    List<EnvironmentRefDTO> queryEnvironmentRefByConsumerName(@Param("name") String name, @Param("envIds") List<Integer> envIds);

    /**
     * 多环境插入消费组
     *
     * @param consumerEnvironmentRef
     * @return
     */
    Integer insert(ConsumerEnvironmentRef consumerEnvironmentRef);


    /**
     * 批量插入
     *
     * @param list
     * @return
     */
    Integer insertList(@Param("list") List<ConsumerEnvironmentRef> list);

    /**
     * 灰度集群配置
     *
     * @param id
     * @param gateClusterId
     * @param gatedIps
     * @param modifier
     * @return
     */
    int updateGateCluster(@Param("ids") List<Integer> ids, @Param("gateClusterId") Integer gateClusterId,
                          @Param("gatedIps") String gatedIps, @Param("modifier") String modifier);

    /**
     * 查询环境管理关系
     *
     * @param consumerIds
     * @return
     */
    List<ConsumerEnvironmentRef> listByConsumerId(@Param("consumerIds") List<Long> consumerIds);

    /**
     * 获取消费组多环境信息
     *
     * @param ids
     * @return
     */
    List<ConsumerEnvironmentRef> getconsumerenvs(@Param("ids") List<Long> ids);

    /**
     * 根据消费组名称获取消费组多环境信息
     *
     * @param
     * @return
     */
    List<EnvironmentRefDTO> getconsumerenvsByName(@Param("name") String name);

    int deleteByConsumerId(Long consumerId);

    /**
     * 批量更新consumer的集群ID
     */
    int updateMigrateConsumers(@Param("srcClusterId") Integer srcClusterId, @Param("targetClusterId") Integer targetClusterId, @Param("consumerIds") List<Long> consumerIds);

    String getByEnvIdAndConsumerName(@Param("envId") Integer envId, @Param("consumerName") String consumerName);
}

