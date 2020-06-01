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

// 启动服务
export function startProcess(id) {
  return request({
    url: `${HostName}/api/process/${id}/start`,
    method: 'post'
  })
}

// 停止服务
export function stopProcess(id) {
  return request({
    url: `${HostName}/api/process/${id}/stop`,
    method: 'post'
  })
}

// 查询进程运行状态
export function getRunningStatus(ids) {
  return request({
    url: `${HostName}/api/process/${ids}/getRunningStatus`,
    method: 'get'
  })
}

export function tailProcessStderrLog(processId, params) {
  return request({
    url: `${HostName}/api/process/${processId}/tailProcessStderrLog`,
    method: 'get',
    params: params
  })
}

export function tailProcessStdoutLog(processId, params) {
  return request({
    url: `${HostName}/api/process/${processId}/tailProcessStdoutLog`,
    method: 'get',
    params: params
  })
}
