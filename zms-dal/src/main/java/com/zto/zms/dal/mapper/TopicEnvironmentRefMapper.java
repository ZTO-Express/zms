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
import com.zto.zms.dal.model.TopicEnvironmentRef;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicEnvironmentRefMapper {
    int insert(TopicEnvironmentRef record);

    /**
     * 根据主题名称获取环境信息
     *
     * @param name
     * @return
     */
    List<EnvironmentRefDTO> gettopicenvsByName(@Param("name") String name);

    /**
     * 查询环境管理关系
     *
     * @param topicIds
     * @return
     */
    List<TopicEnvironmentRef> listByTopicId(@Param("topicIds") List<Long> topicIds);

    /**
     * 获取该topic不同环境以及集群
     *
     * @param id
     * @return
     */
    List<TopicEnvironmentRef> selectTopicAndCluster(@Param("id") Long id);

    /**
     * 更新主题集群
     *
     * @param id
     * @param serviceId
     * @param userName
     * @return
     */
    int updateCluster(@Param("id") Integer id, @Param("serviceId") Integer serviceId, @Param("modifier") String userName);


    int updateGateCluster(@Param("id") Integer id, @Param("gateClusterId") Integer gateClusterId,
                          @Param("gatedIps") String gatedIps, @Param("modifier") String modifier);

    /**
     * 批量更新topic的集群ID
     */
    int updateMigrateTopics(@Param("srcClusterId") Integer srcClusterId, @Param("targetClusterId") Integer targetClusterId, @Param("topicIds") List<Long> topicIds);

    List<EnvironmentRefDTO> queryEnvironmentRefByTopicName(@Param("name") String name, @Param("envIds") List<Integer> envIds);

    /**
     * 查询指定环境的主题集群
     *
     * @param environmentId
     * @param topicId
     * @return
     */
    TopicEnvironmentRef getByEnvIdAndTopicId(@Param("environmentId") Integer environmentId, @Param("topicId") Long topicId);

    /**
     * 查询指定环境的主题集群
     */
    String getByEnvIdAndTopicName(@Param("environmentId") Integer environmentId, @Param("topicName") String topicName);

    int deleteByTopicId(Long topicId);
}
