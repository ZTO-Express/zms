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

import com.zto.zms.dal.domain.service.ZmsClusterServiceDTO;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.filter.Operation;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.serve.ServeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务
 * Created by sun kai on 2020/1/8
 */
@RestController
@RequestMapping("/api/service")
public class ServeController {

    @Autowired
    private ServeService serveService;

    /**
     * 集群下拉框查询：返回集群ID、名称、环境名称
     * 传入envId查询单一环境下的集群
     * 传入serviceType(ROCKETMQ或KAFKA)查询单一集群
     * 传入serviceName精确匹配
     */
    @GetMapping(value = "/listClusters")
    public Result<List<ZmsClusterServiceDTO>> listClusters(@RequestParam(value = "envId", required = false) Integer envId,
                                                           @RequestParam(value = "envName", required = false) String envName,
                                                           @RequestParam(value = "serviceType", required = false) String serviceType,
                                                           @RequestParam(value = "serviceName", required = false) String serviceName) {
        return Result.success(serveService.listClusters(envId, envName, serviceType, serviceName));
    }

    /**
     * 集群迁移-列表查询：返回集群、环境名称、集群实例
     * 传入envId查询单一环境下的集群
     * 传入serviceType(ROCKETMQ或KAFKA)查询单一集群
     * 传入serviceName精确匹配
     */
    @GetMapping(value = "/queryClusters")
    public Result<List<ZmsClusterServiceDTO>> queryClusters(@RequestParam(value = "envId", required = false) Integer envId,
                                                            @RequestParam(value = "keyWord", required = false) String keyWord
    ) {
        return Result.success(serveService.queryClusters(envId, keyWord));
    }

    /**
     * 环境下单一服务列表查询：返回服务ID、名称、类型
     */
    @GetMapping(value = "/listByEnvIdAndServiceType")
    public Result<List<ZmsServiceEntity>> listByEnvIdAndServiceType(@RequestParam(value = "envId") Integer envId,
                                                                    @RequestParam(value = "serviceType") String serviceType) {
        return Result.success(serveService.listByEnvIdAndServiceType(envId, serviceType));
    }

    /**
     * 根据服务ID查询服务和环境信息
     */
    @GetMapping(value = "/getServiceAndEnvById")
    public Result<ZmsClusterServiceDTO> getServiceAndEnvById(@RequestParam(value = "serviceId") Integer serviceId) {
        return Result.success(serveService.getServiceAndEnvById(serviceId));
    }

    @Operation(value = "服务重命名", isAdmin = true)
    @PutMapping(value = "/{id}/rename")
    public Result<Integer> renameService(@RequestBody ZmsServiceEntity zmsServiceEntity, @PathVariable Integer id) {
        return Result.success(serveService.renameService(id, zmsServiceEntity.getServerName()));
    }

    /**
     * 逻辑删除服务
     */
    @Operation(value = "删除服务", isAdmin = true)
    @DeleteMapping(value = "/deleteService/{id}")
    public Result<Integer> deleteService(@PathVariable Integer id) {
        return Result.success(serveService.deleteService(id));
    }

    /**
     * 添加服务-服务名称校验
     */
    @GetMapping(value = "/validServerName")
    public Result<Boolean> validServerName(@RequestParam(value = "envId") Integer envId, @RequestParam(value = "serverName") String serverName) {
        return Result.success(serveService.validServerName(envId, serverName));
    }

}
