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
const Types = {
  SET_VIEWTYPE: 'SET_VIEWTYPE',
  ADD_FRAME_VIEWS: 'ADD_FRAME_VIEWS',
  DEL_FRAME_VIEWS: 'DEL_FRAME_VIEWS',
  DEL_OTHERS_FRAMES: 'DEL_OTHERS_FRAMES',
  DEL_ALL_FRAMES: 'DEL_ALL_FRAMES'
}
const state = {
  viewType: true,
  frameViews: [] // frame容器name列表
}
const actions = {
  catViewType({ commit }, viewType) {
    commit(Types.SET_VIEWTYPE, viewType)
  },
  addFrameViews({ commit }, view) {
    commit(Types.ADD_FRAME_VIEWS, view)
  },
  delFrameViews({ commit }, view) {
    commit(Types.DEL_FRAME_VIEWS, view)
  },
  delOthersFrames({ commit }, view) {
    commit(Types.DEL_OTHERS_FRAMES, view)
  },
  delAllFrames({ commit }) {
    commit(Types.DEL_ALL_FRAMES)
  }
}
const mutations = {
  [Types.SET_VIEWTYPE](state, viewType) {
    state.viewType = viewType
  },
  [Types.ADD_FRAME_VIEWS](state, view) {
    if (state.frameViews.indexOf(view.meta.iframe) > -1) {
      state.frameViews.splice(state.frameViews.indexOf(view.meta.iframe), 1)
    }
    state.frameViews.push(view.meta.iframe)
  },
  [Types.DEL_FRAME_VIEWS](state, view) {
    for (const i of state.frameViews) {
      if (
        i === view.iframe &&
        (router.history.current.meta.iframeUrl === view.iframeUrl || router.history.current.meta.iframe !== view.iframe)
      ) {
        const index = state.frameViews.indexOf(i)
        state.frameViews.splice(index, 1)
        break
      }
    }
  },
  [Types.DEL_OTHERS_FRAMES](state, view) {
    const frameArr = []
    if (view.iframe) frameArr.push(view.iframe)
    state.frameViews = frameArr
  },
  [Types.DEL_ALL_FRAMES](state) {
    state.frameViews = []
  }
}

export default {
  namespaced: true,
  state,
  actions,
  mutations
}
