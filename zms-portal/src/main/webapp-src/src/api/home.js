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
export function getListService(params) {
  return request({
    url: `${HostName}/api/env/listService`,
    method: 'get',
    params: params
  })
}

// 集群指标
export function getclusterMetric(params) {
  return request({
    url: `${HostName}/api/statistics/queryClusterMetrics`,
    method: 'get',
    params: params
  })
}

// 集群日消息量
export function queryClusterOffset(params) {
  return request({
    url: `${HostName}/api/statistics/queryClusterOffset`,
    method: 'get',
    params: params
  })
}
