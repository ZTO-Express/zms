/**
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

import request from '@/utils/request'
import * as env from '@/config/env.js'
const HostName = env.HostName

// 查询主机服务实例
export function queryHostServiceInstance(params) {
  return request({
    url: `${HostName}/api/service/queryHostServiceInstance`,
    method: 'get',
    params: params
  })
}

// 根据服务ID查询服务和环境信息
export function getServiceAndEnvById(params) {
  return request({
    url: `${HostName}/api/service/getServiceAndEnvById`,
    method: 'get',
    params: params
  })
}

// 删除服务
export function deleteService(id) {
  return request({
    url: `${HostName}/api/service/deleteService/${id}`,
    method: 'delete'
  })
}

// 删除服务实例
export function deleteServiceInstance(id) {
  return request({
    url: `${HostName}/api/service/deleteServiceInstance/${id}`,
    method: 'delete'
  })
}

// 根据实例ID查询实例、服务、环境信息
export function getServiceAndEnvByInstanceId(params) {
  return request({
    url: `${HostName}/api/service/getServiceAndEnvByInstanceId`,
    method: 'get',
    params: params
  })
}

// 重命名服务
export function renameService(id, data) {
  return request({
    url: `${HostName}/api/service/${id}/rename`,
    method: 'put',
    data: data
  })
}

// 查询服务、服务实例的配置项
export function getServiceInstanceConfig(params) {
  return request({
    url: `${HostName}/api/service/getServiceInstanceConfig`,
    method: 'get',
    params: params
  })
}

// 服务配置修改、实例配置修改
export function saveServiceInstanceConfig(data) {
  return request({
    url: `${HostName}/api/service/saveServiceInstanceConfig`,
    method: 'post',
    data: data
  })
}

// 添加服务-服务名称校验
export function validServerName(params) {
  return request({
    url: `${HostName}/api/service/validServerName`,
    method: 'get',
    params: params
  })
}

// 查询添加服务、添加服务实例的配置项
export function getAddServiceInstanceConfig(envId, serviceType, instanceTypes) {
  return request({
    url: `${HostName}/api/service/getAddServiceInstanceConfig/${envId}/${serviceType}/${instanceTypes}`,
    method: 'get'
  })
}

// 添加服务
export function addService(data) {
  return request({
    url: `${HostName}/api/service/addService`,
    method: 'post',
    data: data
  })
}

// 添加服务实例
export function addServiceInstance(data) {
  return request({
    url: `${HostName}/api/service/addServiceInstance`,
    method: 'post',
    data: data
  })
}

// 服务实例重启配置对比
export function compareServiceProperty(id1, id2) {
  return request({
    url: `${HostName}/api/service/compareServiceProperty/${id1}/${id2}`,
    method: 'get'
  })
}

// 停止服务实例
export function stopServiceInstance(data) {
  return request({
    url: `${HostName}/api/service/stopServiceInstance`,
    method: 'post',
    data: data
  })
}

// 启动服务实例
export function startServiceInstance(data) {
  return request({
    url: `${HostName}/api/service/startServiceInstance`,
    method: 'post',
    data: data
  })
}

// 重启服务实例
export function restartServiceInstance(data) {
  return request({
    url: `${HostName}/api/service/restartServiceInstance`,
    method: 'post',
    data: data
  })
}

// 集群下拉列表
export function clusterList(params) {
  return request({
    url: `${HostName}/api/service/listClusters`,
    method: 'get',
    params: params
  })
}
