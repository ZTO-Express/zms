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

import com.zto.zms.dal.model.ZmsEnvironment;
import com.zto.zms.portal.dto.ZmsEnvironmentRespDTO;
import com.zto.zms.portal.filter.Operation;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.environment.ZmsEnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/1/6
 **/
@RestController
@RequestMapping("/api/env")
public class ZmsEnvironmentController extends BaseController {

    @Autowired
    private ZmsEnvironmentService zmsEnvironmentService;

    @Operation(value = "新增环境", isAdmin = true)
    @PostMapping(value = "/add")
    public Result<Integer> add(@RequestBody ZmsEnvironment zmsEnvironment) {
        zmsEnvironment.setCreator(getCurrentUser().getRealName());
        return Result.success(zmsEnvironmentService.addEvn(zmsEnvironment));
    }

    /**
     * 查询生效的环境
     */
    @GetMapping(value = "/listEnvironment")
    public Result<List<ZmsEnvironment>> listEnvironment() {
        return Result.success(zmsEnvironmentService.listEnvironment());
    }

    @GetMapping(value = "/listService")
    public Result<List<ZmsEnvironmentRespDTO>> list(@RequestParam(value = "envId", required = false) Integer envId) {
        return Result.success(zmsEnvironmentService.listAll(envId));
    }

    @Operation(value = "环境重命名", isAdmin = true)
    @PutMapping(value = "/{id}/rename")
    public Result<Integer> rename(@RequestBody ZmsEnvironment zmsEnvironment, @PathVariable Integer id) {
        return Result.success(zmsEnvironmentService.rename(id, zmsEnvironment.getEnvironmentName()));
    }

    @Operation(value = "设置环境database", isAdmin = true)
    @PutMapping(value = "/{id}/loadDatabase")
    public Result<Integer> setDatabase(@RequestBody ZmsEnvironment zmsEnvironment, @PathVariable Integer id) {
        return Result.success(zmsEnvironmentService.loadDatabase(id, zmsEnvironment.getZkServiceId(), zmsEnvironment.getInfluxdbServiceId()));
    }
}

