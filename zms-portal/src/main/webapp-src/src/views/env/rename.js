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

// 重命名环境代码
import { renameEnv } from '@/api/env'

export default {
  data() {
    return {
      envRenameOptions: {
        visible: false,
        ref: 'envRenameForm',
        diatitle: '',
        forms: [
          {
            prop: 'environmentName',
            label: '名称',
            defaultValue: '',
            rules: [
              { required: true, message: '请输入名称', trigger: 'blur' },
              { validator: this.environmentNameValid, trigger: 'blur' }
            ]
          }
        ],
        currentFormValue: {},
        loading: false
      },
      envId: '',
      originalName: ''
    }
  },
  methods: {
    // 重命名弹窗
    async openRenameEnv(id, originalName) {
      this.envId = id
      this.originalName = originalName
      this.envRenameOptions.diatitle = '重命名环境：' + originalName
      this.handleDia({
        options: this.envRenameOptions,
        cb: () => {}
      })
    },
    // 关闭弹窗
    closeEnvRenameDia() {
      Object.assign(this.envRenameOptions, {
        visible: false
      })
    },
    // 环境名称验证
    environmentNameValid(rule, value, callback) {
      if (value !== '' && this.originalName === value) {
        callback(new Error('请输入不同的环境名称'))
      }
      callback()
    },
    saveEnvRenameData() {
      this.handleValidate({
        refname: 'envRenameForm',
        cb: async value => {
          // 新增保存
          await renameEnv(this.envId, value)
          this.closeEnvRenameDia()
          this.loadData()
        }
      })
    }
  }
}
