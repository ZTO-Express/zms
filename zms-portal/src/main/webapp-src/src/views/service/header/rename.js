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

// 重命名服务代码
import { renameService } from '@/api/service'

export default {
  data() {
    return {
      serviceRenameOptions: {
        visible: false,
        ref: 'serviceRenameForm',
        diatitle: '',
        forms: [
          {
            prop: 'serverName',
            label: '名称',
            defaultValue: '',
            rules: [
              { required: true, message: '请输入名称', trigger: 'blur' },
              { validator: this.serviceNameValid, trigger: 'blur' }
            ]
          }
        ],
        currentFormValue: {},
        loading: false
      },
      serviceId: '',
      originalName: '',
      original: 0 //0首页弹出的菜单，1服务页面弹出的菜单
    }
  },
  methods: {
    // 重命名弹窗
    async openRenameService(id, originalName, original) {
      this.serviceId = id
      this.originalName = originalName
      this.original = original
      this.serviceRenameOptions.diatitle = '重命名服务：' + originalName
      this.handleDia({
        options: this.serviceRenameOptions,
        cb: () => {}
      })
    },
    // 关闭弹窗
    closeServiceRenameDia() {
      Object.assign(this.serviceRenameOptions, {
        visible: false
      })
    },
    // 环境名称验证
    serviceNameValid(rule, value, callback) {
      if (value !== '' && this.originalName === value) {
        callback(new Error('请输入不同的服务名称'))
      }
      callback()
    },
    saveServiceRenameData() {
      this.handleValidate({
        refname: 'serviceRenameForm',
        cb: async value => {
          // 新增保存
          await renameService(this.serviceId, value)
          this.closeServiceRenameDia()
          if (this.original === 0) {
            this.loadData()
          } else {
            this.resetServerName(value.serverName)
          }
        }
      })
    }
  }
}
