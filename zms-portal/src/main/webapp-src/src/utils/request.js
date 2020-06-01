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

import axios from 'axios'
import { Message } from 'element-ui'
import { util } from '@/utils/util'
import { removeLogin } from '@/utils/login'
// 创建一个axios实例
const service = axios.create({
  // baseURL: "", 存在一些场景，调用多个后端地址
  // timeout: 5000, // 超时时间
  withCredentials: true // 允许携带cookie
})

service.interceptors.request.use(
  config => {
    //  config.headers['X-Token'] = 'Bearer '+ getLogin() // 来自@/utils/login getLogin
    return config
  },
  error => {
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// respone拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (!res.status) {
      if (res.statusCode === '1000') {
        removeLogin()
        window.location.href = util.getLoginPath()
        window.location.reload()
        return
      }
      // 请求异常
      Message({
        message: res.message,
        type: 'error',
        duration: 5 * 1000,
        showClose: true
      })
      return Promise.reject('error')
    } else {
      // 请求成功
      return res
    }
  },
  error => {
    console.log('err' + error) // for debug
    Message({
      message: error.message,
      type: 'error',
      duration: 5 * 1000,
      showClose: true
    })
    return Promise.reject(error)
  }
)

export default service
