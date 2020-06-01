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

import Layout from '@/modules/layout/Layout.vue'
// 基础数据路由
export default {
  path: '/',
  redirect: '/welcome',
  component: Layout,
  children: [
    {
      path: 'welcome',
      name: 'Welcome',
      component: () => import('@/views/common/welcome'),
      meta: {
        label: '首页', // 标题
        cache: true, // 是否缓存
        closable: false, // 是否标签可关闭
        nav: 'default'
      }
    },
    {
      path: 'env/:id',
      name: 'envHome',
      component: () => import('@/views/env/index'),
      meta: {
        closable: true // 是否标签可关闭，
      }
    },
    {
      path: 'service/:id/:tab',
      name: 'serviceHome',
      component: () => import('@/views/service/index'),
      meta: {
        closable: true // 是否标签可关闭，
      }
    },
    {
      path: 'instance/:id',
      name: 'instanceHome',
      component: () => import('@/views/service/instance/index'),
      meta: {
        closable: true // 是否标签可关闭，
      }
    },
    {
      path: 'service-add/:envId/:envName',
      name: 'serviceAddHome',
      component: () => import('@/views/service/add/index'),
      meta: {
        closable: true // 是否标签可关闭，
      }
    },
    {
      path: 'instance-add/:envId/:serverId/:serverName/:serverType',
      name: 'instanceAddHome',
      component: () => import('@/views/service/addInstance/index'),
      meta: {
        closable: true // 是否标签可关闭，
      }
    }
  ]
}
