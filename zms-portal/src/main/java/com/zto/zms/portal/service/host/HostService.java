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

package com.zto.zms.portal.service.host;

import com.zto.zms.service.domain.page.PageResult;
import com.zto.zms.common.ZmsHost;
import com.zto.zms.dal.domain.host.ZmsHostVO;

import java.util.List;

public interface HostService {

    /**
     * 主机列表
     *
     * @param envId
     * @param hostIp
     * @param pageNo
     * @param pageSize
     * @return
     */
    PageResult<ZmsHostVO> pageHostList(int envId, String hostIp, int pageNo, int pageSize);


    /**
     * 主机详情
     *
     * @param envId
     * @param hostId
     * @return
     */
    ZmsHostVO queryHostDetail(int envId, int hostId);

    /**
     * 向环境添加主机
     *
     * @param envId
     * @return
     */
    String hostInit(int envId);

    /**
     * 添加主机
     *
     * @param zmsHost
     * @return
     */
    int addHost(ZmsHost zmsHost);

    /**
     * 生成token
     *
     * @param envId
     * @return
     */
    String getToken(int envId);

    /**
     * 启用主机
     *
     * @param envId
     * @param hostIds
     * @return
     */
    int hostEnabled(int envId, List<Integer> hostIds);

    /**
     * 停用主机
     *
     * @param envId
     * @param hostIds
     * @return
     * @throws Exception
     */
    int hostDisabled(int envId, List<Integer> hostIds) throws Exception;
}

