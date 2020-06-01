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

// 环境下拉列表
export function environmentList(params) {
  return request({
    url: `${HostName}/api/env/listEnvironment`,
    method: 'get',
    params: params
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

// 告警名称下拉列表
export function alertNamesList() {
  return request({
    url: `${HostName}/api/alert/queryAlertNames`,
    method: 'get'
  })
}

// 接受人
export function applicantList() {
  return request({
    url: `${HostName}/api/userinfo/queryApplicants`,
    method: 'get'
  })
}

// 告警列表查询
export function alertsQuery(params) {
  return request({
    url: `${HostName}/api/alert/queryAlerts`,
    method: 'get',
    params: params
  })
}

// 告警新增和复制
export function createAlertSave(data) {
  return request({
    url: `${HostName}/api/alert/addAlert`,
    method: 'post',
    data: data
  })
}

// 告警编辑
export function editAlertSave(id, data) {
  return request({
    url: `${HostName}/api/alert/${id}/updateAlert`,
    method: 'put',
    data: data
  })
}

// 告警删除
export function deleteAlert(id) {
  return request({
    url: `${HostName}/api/alert/deleteAlert/${id}`,
    method: 'delete'
  })
}

// 唯一性校验
export function uniqueCheck(params) {
  return request({
    url: `${HostName}/api/alert/uniqueCheck`,
    method: 'get',
    params: params
  })
}

// 根据主题名称查询关联环境
export function queryEnvironmentRefByTopicName(params) {
  return request({
    url: `${HostName}/api/topic/queryEnvironmentRefByTopicName`,
    method: 'get',
    params: params
  })
}

// 根据消费组名称查询关联环境
export function queryEnvironmentRefByConsumerName(params) {
  return request({
    url: `${HostName}/api/consumer/queryEnvironmentRefByConsumerName`,
    method: 'get',
    params: params
  })
}
