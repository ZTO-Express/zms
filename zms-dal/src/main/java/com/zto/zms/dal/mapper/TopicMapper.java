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

import com.zto.zms.dal.model.Topic;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicMapper {

    /**
     * 查询主题列表
     *
     * @param name
     * @param status
     * @param applicant
     * @param envId
     * @param serviceId
     * @param serverName
     * @return
     */
    List<Topic> selectTopics(@Param("name") String name, @Param("status") Integer status,
                             @Param("applicant") String applicant, @Param("domain") String domain,
                             @Param("envId") Integer envId, @Param("serviceId") Integer serviceId,
                             @Param("serverName") String serverName, @Param("serverType") String serverType);

    List<Topic> selectExactTopics(@Param("name") String name, @Param("status") Integer status,
                                  @Param("applicant") String applicant, @Param("domain") String domain,
                                  @Param("envId") Integer envId, @Param("serviceId") Integer serviceId,
                                  @Param("serverName") String serverName, @Param("serverType") String serverType);

    List<Topic> selectTopicsByKeyWord(@Param("envId") Integer envId, @Param("serviceId") Integer serviceId, @Param("name") String name,
                                      @Param("keyWord") String keyWord, @Param("status") Integer status, @Param("id") Integer id);

    /**
     * 集群迁移-查询集群下审批的topic
     */
    List<Topic> selectTopicsByServiceId(@Param("serviceId") Integer serviceId);

    /**
     * 主题迁移-查询审批的topic
     */
    List<Topic> selectMigratedTopicsById(@Param("topicIds") List<Long> topicIds, @Param("envId") Integer envId);

    List<Topic> selectTopicNames(@Param("name") String name);

    /**
     * 根据主题名称查询主题
     *
     * @param name
     * @return
     */
    List<Topic> selectTopicName(@Param("name") String name);


    Topic getByTopicName(@Param("name") String name);

    Integer softDelete(@Param("id") Long id);

    Integer updateGatedInfo(@Param("id") Long id, @Param("gatedClusterId") Long gatedClusterId, @Param("gatedIps") String gatedIps);

    List<Topic> selectTopicByName(@Param("name") String name, @Param("id") Long id);

    List<Topic> selectTopicAndService(@Param("name") String name, @Param("id") Long id, @Param("envId") int envId);

    Topic selectByPrimaryKey(Long topicId);

    Integer insert(Topic topic);

    Integer updateByPrimaryKeySelective(Topic topic);

    List<Topic> selectAll();

    /**
     * 删除主题
     *
     * @param id
     * @return
     */
    int deleteById(Long id);

    /**
     * 审批
     *
     * @param id
     * @param status
     * @return
     */
    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);


    int updateById(@Param("id") Long id, @Param("partitions") Integer partitions, @Param("replication") Integer replication);

    boolean uniqueTopicCheck(String topicName);

}
