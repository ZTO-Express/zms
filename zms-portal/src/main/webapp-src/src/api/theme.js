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

// 集群下拉列表
export function clusterList(params) {
  return request({
    url: `${HostName}/api/service/listClusters`,
    method: 'get',
    params: params
  })
}

// 主题下拉列表
export function themeListByCluster(params) {
  return request({
    url: `${HostName}/api/topic/getTopicsByCluster`,
    method: 'get',
    params: params
  })
}

// 申请人
export function applicantList() {
  return request({
    url: `${HostName}/api/userinfo/queryApplicants`,
    method: 'get'
  })
}

// 主题表格
export function themeQuery(params) {
  return request({
    url: `${HostName}/api/topic/querypage`,
    method: 'get',
    params: params
  })
}

// 新增主题
export function createThemeSave(data) {
  return request({
    url: `${HostName}/api/topic/add`,
    method: 'post',
    data: data
  })
}

// 编辑主题
export function editThemeSave(id, data) {
  return request({
    url: `${HostName}/api/topic/${id}/update`,
    method: 'put',
    data: data
  })
}

// 审批主题
export function approvalTheme(id, data) {
  return request({
    url: `${HostName}/api/topic/${id}/approve`,
    method: 'put',
    data: data
  })
}

// 配置主题
export function configureTheme(id, data) {
  return request({
    url: `${HostName}/api/topic/${id}/updateGated`,
    method: 'put',
    data: data
  })
}

// 同步主题
export function syncTheme(id, data) {
  return request({
    url: `${HostName}/api/topic/${id}/syncTopic`,
    method: 'put',
    data: data
  })
}

// 删除主题
export function deleteTheme(id) {
  return request({
    url: `${HostName}/api/topic/${id}/delete`,
    method: 'delete'
  })
}

// 环境列表
export function envList() {
  return request({
    url: `${HostName}/api/env/listEnvironment`,
    method: 'get'
  })
}

// 主题详情
export function getThemeDetail(id, data) {
  return request({
    url: `${HostName}/api/topic/${id}/topicSummary`,
    method: 'get',
    params: data
  })
}

// 主题监控 tps
export function themeMonitorTps(params) {
  return request({
    url: `${HostName}/api/statistics/queryTopicMetrics`,
    method: 'get',
    params: params
  })
}

// daily msg
export function themeMonitorDailyMsg(params) {
  return request({
    url: `${HostName}/api/statistics/queryTopicOffset`,
    method: 'get',
    params: params
  })
}

// 客户端监控
// ip获取
export function getClientIp(params) {
  return request({
    url: `${HostName}/api/statistics/queryProducerClients`,
    method: 'get',
    params: params
  })
}

// 发送耗时指标
export function clientCostRate(params) {
  return request({
    url: `${HostName}/api/statistics/querySendCostRate`,
    method: 'get',
    params: params
  })
}

// 发送成功指标
export function clientSuccessRate(params) {
  return request({
    url: `${HostName}/api/statistics/queryProducerSuccessRate`,
    method: 'get',
    params: params
  })
}

// 发送失败指标
export function clientFailRate(params) {
  return request({
    url: `${HostName}/api/statistics/queryProducerFailureRate`,
    method: 'get',
    params: params
  })
}

// 发送耗时分布（单位毫秒）
export function clientDistributeRate(params) {
  return request({
    url: `${HostName}/api/statistics/queryProducerDistributionMetrics`,
    method: 'get',
    params: params
  })
}

// 发送消息体大小分布（单位KB）
export function clientMsgSize(params) {
  return request({
    url: `${HostName}/api/statistics/queryProducerMsgBodyMetrics`,
    method: 'get',
    params: params
  })
}

// 发消息
export function sendThemeMsg(data) {
  return request({
    url: `${HostName}/api/topic/sendMsg`,
    method: 'post',
    data: data
  })
}
