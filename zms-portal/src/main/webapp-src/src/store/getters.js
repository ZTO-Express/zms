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

const getters = {
  sidebar: state => state.common.sidebar,
  permissions: state => state.user.userInfo.permissions,
  getCacheViews: state => {
    const defaultArr = []
    for (const v of state.tagsView.defaultViews) {
      if (v.cache) {
        defaultArr.push(v.name)
      }
    }
    return [...defaultArr, ...state.tagsView.cachedViews]
  },
  getVisitedViews: state => {
    return [...state.tagsView.defaultViews, ...state.tagsView.visitedViews]
  },
  userInfo: state => state.user.userInfo
}
export default getters
