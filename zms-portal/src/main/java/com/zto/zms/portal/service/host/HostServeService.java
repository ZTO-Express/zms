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

import com.zto.zms.dal.model.ZmsHost;
import com.zto.zms.portal.dto.serve.ZmsServiceVO;

import java.util.List;

/**
 * @author yuhao.zhang
 * @description
 * @date 2020/1/7
 */
public interface HostServeService {

    /**
     * 主机服务列表
     *
     * @param envId
     * @param hostId
     * @return
     */
    List<ZmsServiceVO> queryHostServeList(int envId, int hostId);

    /**
     * 查询环境下所有主机，主机对应的服务实例
     *
     * @param envId
     * @return
     */
    List<ZmsHost> listHostInstanceByEnvId(Integer envId);
}

