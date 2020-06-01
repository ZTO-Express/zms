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

// 迁移代码
import { migrateZk } from '@/api/transfer'

export default {
  data() {
    return {
      diaOptions: {
        visible: false,
        ref: 'diaform',
        diatitle: '【消费】迁移进度',
        forms: [
          {
            prop: 'consumerNames',
            label: '迁移消费',
            disabled: true,
            defaultValue: ''
          },
          {
            prop: 'targetClusterId',
            label: '目标集群',
            itemType: 'remoteselect',
            apiUrl: 'cluster_list',
            labelkeyname: 'serverName',
            valuekeyname: 'id',
            rules: [{ required: true, message: '请选择集群', trigger: 'change' }]
          }
        ],
        currentFormValue: {},
        loading: false
      }
    }
  },
  methods: {
    // 打开迁移弹窗
    async transferOpen() {
      this.selections = this.$refs.maintable.getTableSelections()
      const { selections } = this
      if (selections.length === 0) {
        this.message('error', '请选择数据！')
        return
      }
      let _row = { consumerNames: this.selections.map(item => item.name).join() }
      this.handleDia({
        options: this.diaOptions,
        row: _row,
        cb: () => {}
      })
    },
    // 关闭弹窗
    closeDia() {
      Object.assign(this.diaOptions, {
        visible: false
      })
    },
    // 执行迁移
    beginTransfer() {
      this.handleValidate({
        refname: 'diaform',
        cb: async value => {
          delete value.consumerNames
          value.consumers = this.selections.map(item => item.id).join()
          // 迁移
          await migrateZk(value)
          // 关闭弹窗
          this.closeDia()
          // 重新load表格数据
          this.loadTabledata()
        }
      })
    }
  }
}
