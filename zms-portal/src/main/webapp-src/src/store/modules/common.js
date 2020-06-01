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
import RouteMenu from '@/utils/route-menu'
import GLOBAL from '@/config/global'
const Types = {
  SET_SKIN: 'SET_SKIN',
  SET_NAV_CUR: 'SET_NAV_CUR',
  TOGGLE_SIDEBAR: 'TOGGLE_SIDEBAR'
}
const state = {
  // 皮肤版本
  skin: 'v1',
  sidebar: {
    opened: true //左侧菜单默认展开
  },
  //开启导航菜单
  navOpen: GLOBAL.navOpen,
  // 导航菜单列表
  navArr: [
    {
      prop: 'default',
      label: '默认'
    }
  ],
  navDefault: 'default', // 导航菜单默认选项
  navCur: 'default' // 导航菜单当前选项
}
const actions = {
  //添加详情页面
  addPageDetail({ rootState }, params) {
    return new Promise(resolve => {
      const routes = []
      const obj = {}
      const rm = new RouteMenu()
      routes.push(rm.pageDetail(params, obj, 1, params.routerDeep || rootState.user.routeDeep))
      router.addRoutes(routes)
      resolve()
    })
  },
  // 设置皮肤
  setSkin({ commit }, params) {
    return new Promise(resolve => {
      commit(Types.SET_SKIN, params)
      resolve()
    })
  },
  // 设置当前导航菜单
  setNavCur({ commit }, params) {
    return new Promise(resolve => {
      commit(Types.SET_NAV_CUR, params)
      resolve()
    })
  },
  //左侧菜单展开切换
  toggleSideBar({ commit }) {
    commit(Types.TOGGLE_SIDEBAR)
  }
}
const mutations = {
  [Types.SET_SKIN](state, params) {
    state.skin = params
  },
  [Types.SET_NAV_CUR]: (state, params) => {
    state.navCur = params || state.navDefault
  },
  [Types.TOGGLE_SIDEBAR]: state => {
    state.sidebar.opened = !state.sidebar.opened
  }
}
export default {
  namespaced: true,
  state,
  actions,
  mutations
}
