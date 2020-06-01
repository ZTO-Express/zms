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

import http from './request'
import apiUrl from '@/api/api-url'
function getApi(key) {
  if (key.indexOf('_') === -1) {
    return apiUrl[key]
  } else {
    let api = apiUrl
    key.split('_').forEach(k => {
      api = api[k]
    })
    return api
  }
}
export default {
  /**
   * 请求调用函数
   * @param apiUrl 接口名 对应api-url.js
   * @param params 请求参数
   * @param headers 自定义请求头
   */
  accessAPI: function({ apiUrl = '', params = {}, headers = {} }) {
    const group = { headers }
    const api = getApi(apiUrl)
    Object.assign(group, {
      url: api.url,
      method: api.method
    })
    if (api.method === 'get') {
      Object.assign(group, { params })
    } else if (api.method === 'post' || api.method === 'put' || api.method === 'patch' || api.method === 'delete') {
      Object.assign(group, { data: params })
    }
    return http(group)
  }
}
