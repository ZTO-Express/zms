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

import com.zto.zms.dal.model.ZmsEnvironment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZmsEnvironmentMapper {
    int insert(ZmsEnvironment record);

    /**
     * 查询所有环境
     *
     * @return
     */
    List<ZmsEnvironment> listAll();

    List<ZmsEnvironment> listByIds(@Param("ids") List<Integer> ids);

    /**
     * 查询生效环境
     *
     * @return
     */
    List<ZmsEnvironment> listEnableEnv();

    ZmsEnvironment getEnv(@Param("id") Integer id);

    /**
     * 环境名是否存在
     *
     * @param name
     * @return
     */
    boolean isExistName(String name);

    /**
     * 环境重命名
     *
     * @param id
     * @param environmentName
     * @return
     */
    int rename(@Param("id") Integer id, @Param("environmentName") String environmentName);

    /**
     * 是否存在环境数据
     *
     * @param id
     * @return
     */
    boolean isExist(Integer id);

    /**
     * 更新环境Zookeeper
     *
     * @param id
     * @param zkServiceId
     * @return
     */
    int updateDatabase(@Param("id") Integer id, @Param("zkServiceId") Integer zkServiceId, @Param("influxdbServiceId") Integer influxdbServiceId);

    /**
     * 环境名
     *
     * @param id
     * @return
     */
    String getNameById(Integer id);

    /**
     * 查询环境ID
     */
    Integer getIdByName(String env);
}
