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

// 主机列表查询 todo:接口路径env后面的参数需动态获取
export function queryApprovedConsumers(params) {
  return request({
    url: `${HostName}/api/consumer/queryApprovedConsumers`,
    method: 'get',
    params: params
  })
}
// 延迟列表
export function delayRocketQuery(params) {
  return request({
    url: `${HostName}/api/rocketmq/consumer/consumerLatenciesByPage`,
    method: 'get',
    params: params
  })
}
// kafka列表
export function delayKafkaQuery(params) {
  return request({
    url: `${HostName}/api/kafka/consumer/consumerLatenciesByPage`,
    method: 'get',
    params: params
  })
}
// kafka列表
export function allDelayQuery(params) {
  return request({
    url: `${HostName}/api/consumer/delay/consumerLatenciesByPage`,
    method: 'get',
    params: params
  })
}
