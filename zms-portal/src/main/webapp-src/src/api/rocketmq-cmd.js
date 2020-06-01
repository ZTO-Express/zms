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

//rocketmq查询 todo:接口路径env后面的参数需动态获取
export function statsAll(params) {
  return request({
    url: `${HostName}/api/rocketmq/cmd/statsAll`,
    method: 'get',
    params: params
  })
}

export function getPerm(params) {
  return request({
    url: `${HostName}/api/rocketmq/cmd/getPerm`,
    method: 'get',
    params: params
  })
}

export function resetOffset(envId, data) {
  return request({
    url: `${HostName}/api/rocketmq/cmd/resetOffset?envId=${envId}`,
    method: 'post',
    data: data
  })
}

export function updatePerm(envId, data) {
  return request({
    url: `${HostName}/api/rocketmq/cmd/updatePerm?envId=${envId}`,
    method: 'post',
    data: data
  })
}

export function getBrokerConfig(params) {
  return request({
    url: `${HostName}/api/rocketmq/cmd/getBrokerConfig`,
    method: 'get',
    params: params
  })
}

export function consumerStatusAll(params) {
  return request({
    url: `${HostName}/api/rocketmq/cmd/consumerStatusAll`,
    method: 'get',
    params: params
  })
}

export function clusterList(params) {
  return request({
    url: `${HostName}/api/rocketmq/cmd/clusterList`,
    method: 'get',
    params: params
  })
}

export function topicStatus(params) {
  return request({
    url: `${HostName}/api/rocketmq/cmd/topicStatus`,
    method: 'get',
    params: params
  })
}

export function topicInMsgTop(params) {
  return request({
    url: `${HostName}/api/rocketmq/cmd/topicInMsgTop`,
    method: 'get',
    params: params
  })
}

export function consumerSummary(params) {
  return request({
    url: `${HostName}/api/kafka/cmd/consumerSummary`,
    method: 'get',
    params: params
  })
}
