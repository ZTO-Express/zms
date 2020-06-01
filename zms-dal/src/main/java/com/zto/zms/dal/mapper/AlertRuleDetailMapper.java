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

import com.zto.zms.dal.model.AlertRuleConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRuleDetailMapper {

    List<AlertRuleConfig> selectEffectAlertRulesByEnvId(Integer envId);

    AlertRuleConfig selectByPrimaryKey(Long id);

    int deleteByPrimaryKey(Long id);

    List<AlertRuleConfig> queryAlertRules(@Param("name") String name, @Param("field") String field, @Param("type") String type);

    int countAlertUserRules(@Param("keyWord") String keyWord);

    List<AlertRuleConfig> queryAlertUserRulesPage(@Param("keyWord") String keyWord, @Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    List<String> queryAlertUserRuleNames();

    int insert(AlertRuleConfig alertRule);

    int updateById(AlertRuleConfig alertRule);

    int uniqueCheck(@Param("name") String name, @Param("field") String field, @Param("type") String type, @Param("id") Long id);


    /**
     * 根据主题查询告警
     *
     * @param topicName
     * @return
     */
    List<AlertRuleConfig> listByTopic(String topicName);

}
