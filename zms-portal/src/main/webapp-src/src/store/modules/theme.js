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

import { envList } from '@/api/theme'

const Types = {
  SET_ENV_LIST: 'SET_ENV_LIST'
}

const state = {
  envListData: undefined
}

const actions = {
  async getEnvList({ commit }) {
    const res = await envList()
    commit(Types.SET_ENV_LIST, res.result)
  }
}

const mutations = {
  [Types.SET_ENV_LIST](state, list) {
    state.envListData = list
  }
}

export default {
  namespaced: true,
  state,
  actions,
  mutations
}
