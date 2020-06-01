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

import Layout from '@/modules/layout/Layout'
import Nest2 from '@/modules/layout/Nest2'

export default {
  path: '/host',
  name: 'host',
  redirect: 'noredirect',
  component: Layout,
  children: [
    {
      path: '/',
      component: Nest2,
      children: [
        {
          path: 'list/:envId',
          name: 'hostList',
          component: () => import('@/views/host/list'),
          meta: {
            cache: true, // 是否缓存
            closable: true // 是否标签可关闭，
          }
        },
        {
          path: 'detail/:id/env/:envId',
          name: 'hostDetail',
          component: () => import('@/views/host/detail'),
          meta: {
            closable: true // 是否标签可关闭，
          }
        }
      ]
    }
  ]
}
