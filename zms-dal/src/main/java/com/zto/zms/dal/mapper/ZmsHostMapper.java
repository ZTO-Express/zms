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

import com.zto.zms.common.ZmsHost;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZmsHostMapper {

    int insert(ZmsHost record);

    int updateById(ZmsHost record);

    int countByEnvId(Integer envId);

    /**
     * 主机列表
     *
     * @param envId
     * @param hostIp
     * @return
     */
    List<com.zto.zms.dal.model.ZmsHost> pageHostList(@Param("envId") int envId, @Param("hostIp") String hostIp);

    /**
     * 主机详情
     *
     * @param envId
     * @param hostId
     * @return
     */
    com.zto.zms.dal.model.ZmsHost queryHostDetail(@Param("envId") int envId, @Param("hostId") int hostId);

    /**
     * 获取有效主机的ip
     *
     * @param hostId
     * @return
     */
    String getEnableIPById(Integer hostId);

    /**
     * 主机状态变更
     *
     * @param envId
     * @param hostIds
     * @param hoststatus
     * @return
     */
    int updateHostEnabledOrDisabled(@Param("envId") int envId, @Param("hostIds") List<Integer> hostIds, @Param("hoststatus") String hoststatus);

    /**
     * 获取实例部署ip
     *
     * @param instanceId
     * @return
     */
    String getHostIpByInstanceId(Integer instanceId);

    /**
     * 查询环境下所有主机，主机对应的服务实例
     */
    List<com.zto.zms.dal.model.ZmsHost> listHostInstanceByEnvId(@Param("envId") Integer envId);

    com.zto.zms.dal.model.ZmsHost getByInstanceId(Integer instanceId);

    com.zto.zms.dal.model.ZmsHost getByEnvIdHostIp(@Param("envId") Integer envId, @Param("hostIp") String hostIp);
}
