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

export const LocalStorage = {
  set(name, obj) {
    if (typeof obj === 'object') {
      localStorage.setItem(name, JSON.stringify(obj))
    } else {
      localStorage.setItem(name, obj)
    }
  },
  get(name) {
    const obj = localStorage.getItem(name)
    if (obj && (obj.trim().startsWith('[') || obj.trim().startsWith('{'))) {
      return JSON.parse(obj)
    } else {
      return obj
    }
  }
}
export const SessionStorage = {
  set(name, obj) {
    if (typeof obj === 'object') {
      sessionStorage.setItem(name, JSON.stringify(obj))
    } else {
      sessionStorage.setItem(name, obj)
    }
  },
  get(name) {
    const obj = sessionStorage.getItem(name)
    if (obj && (obj.trim().startsWith('[') || obj.trim().startsWith('{'))) {
      return JSON.parse(obj)
    } else {
      return obj
    }
  }
}
