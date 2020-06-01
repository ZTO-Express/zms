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

import com.zto.zms.dal.model.Consumer;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumerMapper {

    List<Consumer> selectTopicInfoByConsumerName(@Param("consumerName") String consumerName);

    List<Consumer> selectConsumers(@Param("envId") Integer envId, @Param("name") String name, @Param("topicName") String topicName,
                                   @Param("serverName") String serverName, @Param("status") Integer status,
                                   @Param("applicant") String applicant, @Param("domain") String domain);

    List<Consumer> selectExactConsumers(@Param("envId") Integer envId, @Param("name") String name, @Param("topicName") String topicName,
                                        @Param("serverName") String serverName, @Param("status") Integer status,
                                        @Param("applicant") String applicant, @Param("domain") String domain);

    /**
     * 新接口 消费组列表
     *
     * @param name
     * @param topicName
     * @param serviceId
     * @param status
     * @param applicant
     * @return
     */
    List<Consumer> selectConsumersByService(@Param("name") String name, @Param("topicName") String topicName,
                                            @Param("serviceId") Integer serviceId, @Param("status") Integer status,
                                            @Param("applicant") String applicant, @Param("envId") Integer envId);

    /**
     * @param keyWord (name
     *                * @param topicName
     *                * @param serviceId
     *                * @param applicant)
     * @return
     */
    List<Consumer> selectConsumersByKeyWord(@Param("envId") Integer envId, @Param("serviceId") Integer serviceId,
                                            @Param("name") String name, @Param("keyWord") String keyWord, @Param("status") Integer status, @Param("id") Integer id);

    /**
     * 获取该环境下对应消费组名称和集群名称
     *
     * @param envId
     * @param consumerId
     * @return
     */
    List<Consumer> selectConsumerGroupByEnv(@Param("envId") Integer envId, @Param("consumerId") Long consumerId);

    /**
     * 获取不同环境下的所有消费组
     *
     * @param envIds
     * @return
     */
    List<Consumer> selectConsumerInEnvIds(@Param("envIds") List<Integer> envIds, @Param("consumerIds") List<Long> consumerIds);

    List<Consumer> selectApprovedConsumers(@Param("clusterId") Long clusterId, @Param("clusterType") String clusterType);


    /**
     * 新接口 根据集群获取消费组
     *
     * @param envId
     * @param clusterId
     * @param clusterName
     * @param clusterType
     * @param topicId
     * @param topicName
     * @return
     */
    List<Consumer> selectApprovedConsumersByService(@Param("envId") Integer envId,
                                                    @Param("clusterId") Integer clusterId,
                                                    @Param("clusterName") String clusterName,
                                                    @Param("clusterType") String clusterType,
                                                    @Param("topicId") Long topicId,
                                                    @Param("topicName") String topicName);

    Integer softDelete(@Param("id") Long id);

    Integer updateUpdateServer(@Param("id") Long id);

    Integer updateGatedInfo(@Param("id") Long id, @Param("gatedClusterId") Long gatedClusterId, @Param("gatedIps") String gatedIps);

    List<Consumer> selectMigratedConsumersById(@Param("consumersIds") List<Long> consumersIds, @Param("envId") Integer envId);

    List<Consumer> selectConsumerByName(@Param("name") String name, @Param("id") Long id);

    /**
     * 根据集群名称和集群环境模糊获取消费组
     *
     * @param name
     * @param envId
     * @return
     */
    List<Consumer> selectConsumerByNameAndEnvId(@Param("name") String name, @Param("envId") int envId);

    List<Consumer> selectConsumerByNameAndEnvId(@Param("name") String name, @Param("envId") int envId, @Param("clusterType") String clusterType);

    Integer insert(Consumer consumer);

    Consumer selectByPrimaryKey(Long id);

    Integer updateByPrimaryKeySelective(Consumer newConsumer);

    List<Consumer> selectAll();

    List<Consumer> selectConsumersByServiceId(@Param("serviceId") Integer serviceId);

    List<Consumer> listConsumerByNames(@Param("names") List<String> names);

    /**
     * 获取消费组积压阈值
     *
     * @return
     */
    List<Consumer> selectConsumerNameAnddDelayhold();

    Consumer getConsumerByName(@Param("name") String name);
}
