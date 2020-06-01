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

import common from './common'
import theme from './theme'
import consumer from './consumer'
import notice from './notice'
import devops from './devops'
import host from './host'

// 根据权限访问的路由
const asyncRouter = []
asyncRouter.push(common)
asyncRouter.push(theme)
asyncRouter.push(consumer)

asyncRouter.push(notice)
asyncRouter.push(devops)
asyncRouter.push(host)

asyncRouter.push({ path: '*', redirect: '/404', meta: { hidden: true } })

export const asyncRouterMap = asyncRouter
