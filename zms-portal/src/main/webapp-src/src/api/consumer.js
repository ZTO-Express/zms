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

export function queryApprovedConsumers(params) {
  return request({
    url: `${HostName}/api/consumer/queryApprovedConsumers`,
    method: 'get',
    params: params
  })
}
//消费组下拉框
export function getConsumersByClusterId(params) {
  return request({
    url: `${HostName}/api/consumer/getConsumersByClusterId`,
    method: 'get',
    params: params
  })
}
// 消费组列表
export function consumerQuery(params) {
  return request({
    url: `${HostName}/api/consumer/query`,
    method: 'get',
    params: params
  })
}

// 新增消费组
export function createConsumerSave(data) {
  return request({
    url: `${HostName}/api/consumer/add`,
    method: 'post',
    data: data
  })
}

// 编辑消费组
export function editConsumerSave(id, data) {
  return request({
    url: `${HostName}/api/consumer/update`,
    method: 'put',
    data: data
  })
}

// 审批消费组
export function approvalConsumer(id, data) {
  return request({
    url: `${HostName}/api/consumer/approve`,
    method: 'post',
    data: data
  })
}

// 删除消费组
export function consumeDelete(data) {
  return request({
    url: `${HostName}/api/consumer/delete`,
    method: 'post',
    data: data
  })
}

// 审批消费组
export function configUpdateGated(data) {
  return request({
    url: `${HostName}/api/consumer/updateGated`,
    method: 'post',
    data: data
  })
}
// 消费组Rocket详情
export function getConsumerRocketDetail(data) {
  return request({
    url: `${HostName}/api/rocketmq/cmd/consumerStatusAllNew`,
    method: 'get',
    params: data
  })
}

// 消费组Kafka详情
export function getConsumerKafkaDetail(data) {
  return request({
    url: `${HostName}/api/kafka/cmd/consumerSummary`,
    method: 'get',
    params: data
  })
}
// consumer多环境同步
export function consumerSync(data) {
  return request({
    url: `${HostName}/api/consumer/copyConsumers`,
    method: 'post',
    data: data
  })
}
// 获取消费端注册地址
export function consumerZmsRegister(data) {
  return request({
    url: `${HostName}/api/consumer/consumerZmsRegister`,
    method: 'post',
    data: data
  })
}
// 消费组 tps
export function consumerMonitorTps(params) {
  return request({
    url: `${HostName}/api/statistics/queryConsumerGroupMetricsNew`,
    method: 'get',
    params: params
  })
}
// topic tps
export function topicMonitorTps(params) {
  return request({
    url: `${HostName}/api/statistics/queryTopicMetricsNew`,
    method: 'get',
    params: params
  })
}
// 客户端监控
// ip获取
export function getClientIp(params) {
  return request({
    url: `${HostName}/api/statistics/queryConsumerClients`,
    method: 'get',
    params: params
  })
}
// 消费耗时指标
export function queryConsumerUserCostTimeMs(params) {
  return request({
    url: `${HostName}/api/statistics/queryConsumerUserCostTimeMs`,
    method: 'get',
    params: params
  })
}
// 消费成功率
export function queryConsumeSuccessRate(params) {
  return request({
    url: `${HostName}/api/statistics/queryConsumeSuccessRate`,
    method: 'get',
    params: params
  })
}
// 消费失败率
export function queryConsumeFailureRate(params) {
  return request({
    url: `${HostName}/api/statistics/queryConsumeFailureRate`,
    method: 'get',
    params: params
  })
}

// 环境列表
export function envList() {
  return request({
    url: `${HostName}/api/env/listEnvironment`,
    method: 'get'
  })
}
