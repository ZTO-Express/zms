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

import com.zto.zms.service.domain.page.PageResult;
import com.zto.zms.common.ZmsHost;
import com.zto.zms.dal.domain.host.ZmsHostVO;
import com.zto.zms.portal.filter.Operation;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.host.HostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author yuhao.zhang
 * @description
 * @date 2020/1/6
 */
@RestController
@RequestMapping("/api/master")
public class HostController {
    public static final Logger logger = LoggerFactory.getLogger(HostController.class);

    @Autowired
    private HostService hostService;

    /**
     * 主机列表
     *
     * @param envId
     * @param hostIp
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/env/{envId}/host", method = RequestMethod.GET)
    public Result<PageResult<ZmsHostVO>> pageHostList(@PathVariable(required = false) int envId,
                                                      @RequestParam(value = "hostIp", required = false) String hostIp,
                                                      @RequestParam(defaultValue = "1") int currentPage,
                                                      @RequestParam(defaultValue = "10") int pageSize) {

        return Result.success(this.hostService.pageHostList(envId, hostIp, currentPage, pageSize));
    }

    /**
     * 主机详情
     *
     * @param envId
     * @param hostId
     * @return
     */
    @RequestMapping(value = "/env/{envId}/host/{hostId}", method = RequestMethod.GET)
    public Result<ZmsHostVO> queryHostDetail(@PathVariable(value = "envId") int envId,
                                             @PathVariable(value = "hostId") int hostId) {

        return Result.success(this.hostService.queryHostDetail(envId, hostId));
    }


    /**
     * 获取主机初始化脚本地址
     *
     * @param envId
     * @return
     */
    @Operation(value = "主机安装脚本链接", isAdmin = true)
    @RequestMapping(value = "/env/{envId}/hostInit", method = RequestMethod.GET)
    public Result<String> hostInit(@PathVariable(value = "envId") int envId) {

        return Result.success(this.hostService.hostInit(envId));
    }

    @RequestMapping(value = "/addHost", method = RequestMethod.POST)
    public Result<Integer> addHost(@RequestBody ZmsHost zmsHost) {

        return Result.success(this.hostService.addHost(zmsHost));
    }

    /**
     * 启用主机
     *
     * @param envId
     * @return
     */
    @Operation(value = "启用主机", isAdmin = true)
    @RequestMapping(value = "/env/{envId}/hostEnabled", method = RequestMethod.POST)
    public Result<Integer> hostEnabled(@PathVariable(value = "envId") int envId, @RequestBody ZmsHost zmsHost) {

        return Result.success(this.hostService.hostEnabled(envId, zmsHost.getHostIds()));
    }

    /**
     * 停用主机
     *
     * @param envId
     * @return
     */
    @Operation(value = "停用主机", isAdmin = true)
    @RequestMapping(value = "/env/{envId}/hostDisabled", method = RequestMethod.POST)
    public Result<Integer> hostDisabled(@PathVariable(value = "envId") int envId, @RequestBody ZmsHost zmsHost) throws Exception {

        return Result.success(this.hostService.hostDisabled(envId, zmsHost.getHostIds()));
    }


}

