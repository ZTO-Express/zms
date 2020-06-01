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

/**
 * 验证手机号
 * @param 手机号
 * @returns {boolean}
 */
export const validPhone = str => {
  const reg = /^1[3|4|5|7|8][0-9]\d{8}$/
  return reg.test(str)
}
// 手机号验证，可空
export const checkPhoneNull = (rule, value, callback) => {
  if (value === '' || value === null || value === undefined) {
    callback()
  } else if (!validPhone(value)) {
    callback(new Error('请输入正确的11位手机号码'))
  } else {
    callback()
  }
}

/**
 * regexp
 */
// 数字、字母、下划线
export const CO_NUM_CHAR = /^[A-Za-z0-9_-]+$/
// 正整数
export const PO_INTEGER = /^[1-9]\d*$/

// export const Num = /^[0-9]*$/

// eslint-disable-next-line
export const emails = /^((([a-z0-9_\.-]+)@([\da-z\.-]+)\.([a-z\.]{2,6}\,))*(([a-z0-9_\.-]+)@([\da-z\.-]+)\.([a-z\.]{2,6})))$/
