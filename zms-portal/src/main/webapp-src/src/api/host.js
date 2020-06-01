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
export function hostListQuery(params) {
  return request({
    url: `${HostName}/api/master/env/${params.envId}/host?pageSize=${params.pageSize}&currentPage=${params.currentPage}&hostIp=${params.hostIp}`,
    method: 'get'
  })
}
// 主机详情
export function hostDetail(envId, hostId) {
  return request({
    url: `${HostName}/api/master/env/${envId}/host/${hostId}`,
    method: 'get'
  })
}

// 获取安装脚本链接
export function getInstallUrl(envId) {
  return request({
    url: `${HostName}/api/master/env/${envId}/hostInit`,
    method: 'get'
  })
}

// 主机停用
export function hostDisabled(envId, params) {
  return request({
    url: `${HostName}/api/master/env/${envId}/hostDisabled`,
    method: 'post',
    data: params
  })
}
// 主机启用
export function hostEnabled(envId, params) {
  return request({
    url: `${HostName}/api/master/env/${envId}/hostEnabled`,
    method: 'post',
    data: params
  })
}

// 查询环境下所有主机，主机对应的服务实例
export function listHostInstanceByEnvId(envId) {
  return request({
    url: `${HostName}/api/hostserve/listHostInstanceByEnvId/${envId}`,
    method: 'get'
  })
}
