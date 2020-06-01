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

import router from '@/router/index'
import store from '@/store/index'
import NProgress from 'nprogress' // progress bar
import { getLogin } from '@/utils/login'
import { util } from '@/utils/util'
const whiteList = ['/login']

NProgress.configure({ showSpinner: false }) // NProgress Configuration
router.beforeEach((to, from, next) => {
  NProgress.start() // 开启Progress
  const hasLogin = getLogin()
  if (!hasLogin) {
    if (whiteList.indexOf(to.path) !== -1) {
      // in the free login whitelist, go directly
      next()
    } else {
      // other pages that do not have permission to access are redirected to the login page.
      next(`${util.getLoginPath()}`)
    }
    NProgress.done()
    return
  }
  if (hasLogin && to.path === '/login') {
    next({ path: '/', replace: true })
    NProgress.done()
    return
  }
  // 有权限数据。因为是动态添加的路由，目前的路由都是有权限访问。无权限的对应的菜单路由不存在，都跳到404
  if (store.getters.permissions && store.getters.permissions.length > 0) {
    next()
    NProgress.done()
  } else {
    store.dispatch('user/getUserInfo').then(addRouters => {
      // 生成可访问的路由表
      router.addRoutes(addRouters)
      next({ ...to, replace: true }) // hack方法 确保addRoutes已完成
    })
  }
})
router.afterEach(() => {
  NProgress.done()
  // 路由完成后做些事情
})
