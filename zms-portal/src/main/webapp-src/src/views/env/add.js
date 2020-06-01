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

// 环境新增代码
import { addEnv } from '@/api/env'

export default {
  data() {
    return {
      envAddOptions: {
        visible: false,
        ref: 'envAddForm',
        diatitle: '新增环境',
        forms: [
          {
            prop: 'environmentName',
            label: '名称',
            defaultValue: '',
            rules: [{ required: true, message: '请输入名称', trigger: 'blur' }]
          }
        ],
        currentFormValue: {},
        loading: false
      }
    }
  },
  methods: {
    // 新增弹窗
    async openAddEnv() {
      this.handleDia({
        options: this.envAddOptions,
        cb: () => {}
      })
    },
    // 关闭弹窗
    closeEnvAddDia() {
      Object.assign(this.envAddOptions, {
        visible: false
      })
    },
    saveEnvAddData() {
      this.handleValidate({
        refname: 'envAddForm',
        cb: async value => {
          // 新增保存
          await addEnv(value)
          this.closeEnvAddDia()
          this.loadData()
        }
      })
    }
  }
}
