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

import com.zto.zms.dal.domain.service.HostServiceInstanceDTO;
import com.zto.zms.portal.dto.serve.*;
import com.zto.zms.portal.filter.Operation;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.serve.ServeInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务实例
 *
 * @author sun kai
 * @date 2020/1/13
 */
@RestController
@RequestMapping("/api/service")
public class ServeInstanceController extends BaseController {

    @Autowired
    private ServeInstanceService serveInstanceService;

    /**
     * 查询主机服务实例
     */
    @GetMapping(value = "/queryHostServiceInstance")
    public Result<List<HostServiceInstanceDTO>> queryHostServiceInstance(@RequestParam(value = "serviceId") Integer serviceId) {
        return Result.success(serveInstanceService.queryHostServiceInstance(serviceId));
    }

    /**
     * 逻辑删除服务实例
     */
    @Operation(value = "删除服务实例", isAdmin = true)
    @DeleteMapping(value = "/deleteServiceInstance/{ids}")
    public Result<Integer> deleteServiceInstance(@PathVariable List<Integer> ids) {
        return Result.success(serveInstanceService.deleteServiceInstance(ids));
    }

    /**
     * 根据实例ID查询实例服务环境信息
     */
    @GetMapping(value = "/getServiceAndEnvByInstanceId")
    public Result<EnvServiceInstanceDTO> getServiceAndEnvByInstanceId(@RequestParam(value = "instanceId") Integer instanceId) {
        return Result.success(serveInstanceService.getServiceAndEnvByInstanceId(instanceId));
    }

    /**
     * 查询服务、服务实例的配置项
     * 传入serviceId查询服务配置
     * 传入instanceId查询实例配置
     */
    @Operation(value = "查询服务和实例配置", isAdmin = true)
    @GetMapping(value = "/getServiceInstanceConfig")
    public Result<List<ServicePropertyInstanceQueryDTO>> getServiceInstanceConfig(@RequestParam(value = "serviceId", required = false) Integer serviceId,
                                                                                  @RequestParam(value = "instanceId", required = false) Integer instanceId) {
        if (null == serviceId && null == instanceId) {
            return Result.error("-1", "serviceId and instanceId at least one required");
        }
        return Result.success(serveInstanceService.getServiceInstanceConfig(serviceId, instanceId));
    }

    /**
     * 服务配置修改、实例配置修改
     */
    @Operation(value = "服务和实例配置修改", isAdmin = true)
    @PostMapping(value = "/saveServiceInstanceConfig")
    public Result<String> saveServiceInstanceConfig(@RequestBody ServicePropertySaveDTO configDto) {
        if (CollectionUtils.isEmpty(configDto.getPropertyQueryList())) {
            return Result.error("-1", "propertyValueRef is required");
        }
        if (null == configDto.getServiceId()) {
            return Result.error("-1", "serviceId is required");
        }
        serveInstanceService.saveServiceInstanceConfig(configDto, getCurrentUser().getRealName());
        return Result.success(null);
    }

    /**
     * 查询添加服务、添加服务实例的配置项
     * serviceType查询添加服务配置或者添加服务实例的配置
     * instanceTypes添加服务实例时必传
     */
    @Operation(value = "查询添加服务和实例配置", isAdmin = true)
    @GetMapping(value = {"/getAddServiceInstanceConfig/{envId}/{serviceType}/{instanceTypes}", "/getAddServiceInstanceConfig/{envId}/{serviceType}"})
    public Result<List<ServicePropertyQueryDTO>> getAddServiceInstanceConfig(@PathVariable Integer envId,
                                                                             @PathVariable String serviceType,
                                                                             @PathVariable(required = false) List<String> instanceTypes) {
        return Result.success(serveInstanceService.getAddServiceInstanceConfig(envId, serviceType, instanceTypes));
    }

    @Operation(value = "添加服务", isAdmin = true)
    @PostMapping(value = "/addService")
    public Result<List<Integer>> addService(@RequestBody ServiceInstanceAddDTO serviceAddDto) {
        return Result.success(serveInstanceService.addService(serviceAddDto, getCurrentUser().getRealName()));
    }

    @Operation(value = "添加服务实例", isAdmin = true)
    @PostMapping(value = "/addServiceInstance")
    public Result<List<Integer>> addServiceInstance(@RequestBody ServiceInstanceAddDTO serviceAddDto) {
        return Result.success(serveInstanceService.addServiceInstance(serviceAddDto, getCurrentUser().getRealName()));
    }


    @Operation(value = "停止服务实例", isAdmin = true)
    @PostMapping(value = "/stopServiceInstance")
    public Result<List<ServiceInstanceStartVO>> stopServiceInstance(@RequestBody List<Integer> instanceIds) {
        return Result.success(serveInstanceService.stopServiceInstance(instanceIds, getCurrentUser().getRealName()));
    }

    @Operation(value = "启动服务实例", isAdmin = true)
    @PostMapping(value = "/startServiceInstance")
    public Result<List<ServiceInstanceStartVO>> startServiceInstance(@RequestBody List<Integer> instanceIds) {
        return Result.success(serveInstanceService.startServiceInstance(instanceIds, getCurrentUser().getRealName()));
    }

    /**
     * 服务实例重启配置对比
     * 传入serviceId查询服务重启配置对比
     * 传入instanceIds查询实例重启配置对比
     */
    @Operation(value = "对比服务实例配置", isAdmin = true)
    @GetMapping(value = "/compareServiceProperty/{serviceId}/{instanceIds}")
    public Result<List<ServicePropertyCompareDTO>> compareServiceProperty(@PathVariable Integer serviceId,
                                                                          @PathVariable List<Integer> instanceIds) {
        return Result.success(serveInstanceService.compareServiceProperty(serviceId, instanceIds));
    }

    @Operation(value = "重启服务实例", isAdmin = true)
    @PostMapping(value = "/restartServiceInstance")
    public Result<List<ServiceInstanceStartVO>> restartServiceInstance(@RequestBody List<Integer> instanceIds) {
        return Result.success(serveInstanceService.restartServiceInstance(instanceIds, getCurrentUser().getRealName()));
    }

}

