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

// 登入
export function login(data) {
  return request({
    url: `${HostName}/api/login`,
    method: 'post',
    data: data
  })
}
// 后端自定义获取用户信息
export function getUserInfo() {
  return request({
    url: `${HostName}/api/user/getLoginUserInfo`,
    method: 'get'
  })
}
// 后端自定义清除后端 session
export function clearSysSession() {
  return request({
    url: `${HostName}/api/logout`,
    method: 'get'
  })
}
