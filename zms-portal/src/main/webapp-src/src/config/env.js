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

import GLOBAL from '@/config/global'
// 接口地址默认使用前端访问地址，本地配置场景优先级更高
// 域名
const URL = {}

// 后台接口的域名
const API = {
  // 开发环境
  DEV: {
    HostName: 'http://localhost:8088'
  }
}

// 后台接口的域名。默认前端地址
let HostName = window.location.origin
// 开发环境使用本地配置
if (process.env.NODE_ENV === 'develop') {
  HostName = API.DEV.HostName
}

// 若本地配置数据，则根据域名匹配为HostName重新赋值
Object.keys(URL).some(key => {
  const urls = URL[key]
  if (urls.length && urls.some(checkUrl)) {
    if (API[key]) {
      HostName = API[key].HostName || ''
      return true
    }
  }
})
function checkUrl(url) {
  return window.location.href.indexOf(url) === 0
}

// 本地mocal时，为空字符串
HostName = GLOBAL.mockLocal ? '' : HostName

export { HostName }
