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

export function consumerSummary(params) {
  return request({
    url: `${HostName}/api/kafka/cmd/consumerSummary`,
    method: 'get',
    params: params
  })
}

export function resetOffset(envId, data) {
  return request({
    url: `${HostName}/api/kafka/cmd/resetOffset?envId=${envId}`,
    method: 'post',
    data: data
  })
}

export function topicSummary(params) {
  return request({
    url: `${HostName}/api/kafka/cmd/topicSummary`,
    method: 'get',
    params: params
  })
}
