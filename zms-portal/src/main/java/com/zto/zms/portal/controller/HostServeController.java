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

package com.zto.zms.portal.controller;

import com.zto.zms.dal.model.ZmsHost;
import com.zto.zms.portal.dto.serve.ZmsServiceVO;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.host.HostServeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author yuhao.zhang
 * @description
 * @date 2020/1/7
 */
@RestController
@RequestMapping("/api/hostserve")
public class HostServeController {

    @Autowired
    private HostServeService hostServeService;

    /**
     * 主机下服务列表
     *
     * @param envId
     * @param hostId
     * @return
     */
    @RequestMapping(value = "/env/{envId}/host/{hostId}", method = RequestMethod.GET)
    public Result<List<ZmsServiceVO>> queryHostServeList(@PathVariable(value = "envId") int envId,
                                                         @PathVariable(value = "hostId") int hostId) {

        return Result.success(this.hostServeService.queryHostServeList(envId, hostId));
    }

    /**
     * 查询环境下所有主机，主机对应的服务实例
     *
     * @param envId
     * @return
     */
    @RequestMapping(value = "/listHostInstanceByEnvId/{envId}", method = RequestMethod.GET)
    public Result<List<ZmsHost>> listHostInstanceByEnvId(@PathVariable(value = "envId") Integer envId) {
        return Result.success(this.hostServeService.listHostInstanceByEnvId(envId));
    }
}

