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

import { getUserInfo } from '@/api/auth'
import RouteMenu from '@/utils/route-menu'
import { asyncRouterMap } from '@/router/main/index'

const rm = new RouteMenu(asyncRouterMap)
const Types = {
  SET_USER_INFO: 'SET_USER_INFO',
  SET_PERMISSIONS: 'SET_PERMISSIONS',
  SET_MENU: 'SET_MENU'
}
const state = {
  userInfo: {
    realName: '',
    userId: '',
    mobile: '',
    email: '',
    permissions: [],
    menus: {
      default: [],
      test: []
    }
  },
  routeDeep: 2 // 路由的深度
}

const mutations = {
  [Types.SET_USER_INFO]: (state, params) => {
    state.userInfo.realName = params.realName
    state.userInfo.email = params.email
    state.userInfo.mobile = params.mobile
    state.userInfo.userId = params.userId
  },
  [Types.SET_PERMISSIONS]: (state, admin) => {
    rm.preRouteArr()
    rm.fillPerms('commonUser')
    admin && rm.fillPerms('admin')
    state.userInfo.permissions = rm.getPermission()
    rm.addRouters = rm.filterAsyncRouter()
    let routeDeep = rm.getFloor(rm.addRouters, false)
    routeDeep = routeDeep === 1 ? 2 : routeDeep
    state.routeDeep = routeDeep
  },
  [Types.SET_MENU]: (state, rootState) => {
    for (const v of rootState.common.navArr) {
      state.userInfo.menus[v.prop] = rm.menuTree(v.prop, rootState.common.navDefault)
    }
  }
}

const actions = {
  // 获取用户信息
  async getUserInfo({ rootState, commit }) {
    const { result } = await getUserInfo()
    commit(Types.SET_USER_INFO, result)
    commit(Types.SET_PERMISSIONS, result.admin)
    commit(Types.SET_MENU, rootState)
    return rm.addRouters
  }
}

export default {
  namespaced: true,
  state,
  actions,
  mutations
}
