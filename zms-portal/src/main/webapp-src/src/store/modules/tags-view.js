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

const Types = {
  ADD_VISITED_VIEWS: 'ADD_VISITED_VIEWS',
  DEL_VISITED_VIEWS: 'DEL_VISITED_VIEWS',
  DEL_OTHERS_VIEWS: 'DEL_OTHERS_VIEWS',
  DEL_ALL_VIEWS: 'DEL_ALL_VIEWS',
  SET_TAGS_SHADOW: 'SET_TAGS_SHADOW'
}
const state = {
  visitedViews: [], // 所有正在游览的页面,不包括defaultViews
  cachedViews: [], // 需要做缓存的页面,不包括defaultViews
  // defaultViews 默认标签。使路由里的 closable,cache 失效。
  // defaultViews closable 固定为false 不可关闭。
  // 总浏览页面 visitedViews+defaultViews，总缓存页面cachedViews+defaultViews
  defaultViews: [
    // 数据格式
    {
      label: '首页', // 展示的标题
      name: 'Welcome', // tab对应的router name
      closable: false, // 此参数为false不可修改
      cache: true,
      query: {},
      params: {}
    }
  ],
  tagsShadow: false //多标签tag 阴影
}

const actions = {
  addVisitedView({ commit }, view) {
    commit(Types.ADD_VISITED_VIEWS, view)
  },
  delVisitedView({ commit, state }, view) {
    return new Promise(resolve => {
      commit(Types.DEL_VISITED_VIEWS, view)
      resolve([...state.visitedViews])
    })
  },
  delOthersViews({ commit }, view) {
    return new Promise(resolve => {
      commit(Types.DEL_OTHERS_VIEWS, view)
      resolve()
    })
  },
  delAllViews({ commit }) {
    return new Promise(resolve => {
      commit(Types.DEL_ALL_VIEWS)
      resolve()
    })
  },
  //设置多标签tag 滚动阴影
  setTagsShadow({ commit }, params) {
    commit(Types.SET_TAGS_SHADOW, params)
  }
}
const mutations = {
  [Types.ADD_VISITED_VIEWS]: (state, view) => {
    // defaultViews不添加
    if (state.defaultViews.some(v => v.name === view.name)) {
      return
    }
    // 已存在于visitedViews的 如果是详情页，详情id也存在的。不添加
    for (const v of state.visitedViews) {
      if (
        v.name === view.name &&
        JSON.stringify(v.params) === JSON.stringify(view.params) &&
        JSON.stringify(v.query) === JSON.stringify(view.query)
      ) {
        return
      }
    }
    let label = view.meta.label
    Object.keys(view.params).forEach(key => {
      label += '-' + view.params[key]
    })
    // 添加visitedViews
    state.visitedViews.push({
      query: view.query,
      params: view.params,
      name: view.name,
      label: label,
      closable: view.meta.closable,
      cache: view.meta.cache,
      iframe: view.meta.iframe,
      iframeUrl: view.meta.iframeUrl
    })
    // 添加cachedViews
    if (view.meta.cache) {
      const cachedViews = [...state.cachedViews]
      cachedViews.push(view.name)
      state.cachedViews = Array.from(new Set([...cachedViews]))
    }
  },
  [Types.DEL_VISITED_VIEWS]: (state, view) => {
    for (const [i, v] of state.visitedViews.entries()) {
      if (
        v.name === view.name &&
        JSON.stringify(v.params) === JSON.stringify(view.params) &&
        JSON.stringify(v.query) === JSON.stringify(view.query)
      ) {
        state.visitedViews.splice(i, 1)
        break
      }
    }
    for (const i of state.cachedViews) {
      if (i === view.name) {
        const index = state.cachedViews.indexOf(i)
        state.cachedViews.splice(index, 1)
        break
      }
    }
  },
  [Types.DEL_OTHERS_VIEWS]: (state, view) => {
    const visitedArr = []
    const cachedArr = []
    for (const v of state.visitedViews) {
      if (
        !v.closable ||
        (v.name === view.name &&
          JSON.stringify(v.params) === JSON.stringify(view.params) &&
          JSON.stringify(v.query) === JSON.stringify(view.query))
      ) {
        visitedArr.push(v)
        if (v.cache) {
          cachedArr.push(v.name)
        }
      }
    }
    state.visitedViews = visitedArr
    state.cachedViews = cachedArr
  },
  [Types.DEL_ALL_VIEWS]: state => {
    const visitedArr = []
    const cachedArr = []
    for (const v of state.visitedViews) {
      // 不可关闭的
      if (!v.closable) {
        visitedArr.push(v)
        if (v.cache) {
          cachedArr.push(v.name)
        }
      }
    }
    state.visitedViews = visitedArr
    state.cachedViews = cachedArr
  },
  [Types.SET_TAGS_SHADOW](state, params) {
    state.tagsShadow = params
  }
}

export default {
  namespaced: true,
  state,
  actions,
  mutations
}
