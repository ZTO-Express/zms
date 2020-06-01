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
 * 全局变量、系统配置。注册到vue里
 */
const mockLocal = false // 开关本地mock,调用真实接口时需要把它关了。mock分为两类1、本地mock（mockData.js） 2、接口mock(如网关提供的mock接口)
const navOpen = true // 开关导航菜单
const pagination = {
  pageSizes: [10, 25, 50, 100],
  pageSize: 25
}
const modalTime = 250 // 模态框间隔时间
// 101货代维护状态,
const filter = {
  f101: {
    1: '停用',
    2: '启用'
  }
}
const arr = {
  a101: [
    {
      label: '停用',
      value: 1
    },
    {
      label: '启用',
      value: 2
    }
  ]
}
const cols = {
  selection: 40,
  number: 60,
  op1: 80,
  op2: 100,
  op3: 125,
  op4: 160,
  huge: 270,
  larger: 160,
  big: 140,
  medium: 120,
  common: 100,
  small: 80,
  mini: 60
}
const fFormLabel = '75px'
const mFormLabel = '100px'

const select = {
  size: 'mini',
  placeholder: '请选择',
  filterable: true,
  clearable: true
}

const button = {
  size: 'mini',
  type: 'primary'
}

const column = {
  align: 'center',
  width: 100
}

const chartHeight = '320px'

const chartConfig = {
  initflg: true,
  border: false
}

export default {
  mockLocal,
  navOpen,
  pagination,
  modalTime,
  filter,
  arr,
  cols,
  fFormLabel,
  mFormLabel,
  select,
  button,
  column,
  chartHeight,
  chartConfig
}
