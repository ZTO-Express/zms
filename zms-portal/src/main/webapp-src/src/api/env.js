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

// 新增环境
export function addEnv(data) {
  return request({
    url: `${HostName}/api/env/add`,
    method: 'post',
    data: data
  })
}

// 重命名环境
export function renameEnv(id, data) {
  return request({
    url: `${HostName}/api/env/${id}/rename`,
    method: 'put',
    data: data
  })
}

// 配置环境database
export function loadDatabase(id, data) {
  return request({
    url: `${HostName}/api/env/${id}/loadDatabase`,
    method: 'put',
    data: data
  })
}

// 环境下单一服务列表查询
export function listByEnvIdAndServiceType(params) {
  return request({
    url: `${HostName}/api/service/listByEnvIdAndServiceType`,
    method: 'get',
    params: params
  })
}

// 环境列表
export function listEnvironment(params) {
  return request({
    url: `${HostName}/api/env/listEnvironment`,
    method: 'get',
    params: params
  })
}
