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

// 通用页面
export const constantRouterMap = [
  {
    path: '/login', // 登陆页
    component: () => import('@/views/common/login')
  },
  {
    path: '/403', // 无权限页面
    component: () => import('@/views/common/error-pages/403')
  },
  {
    path: '/404', // 404页面
    component: () => import('@/views/common/error-pages/404')
  },
  {
    path: '/500', // 服务器错误
    component: () => import('@/views/common/error-pages/500')
  }
]
