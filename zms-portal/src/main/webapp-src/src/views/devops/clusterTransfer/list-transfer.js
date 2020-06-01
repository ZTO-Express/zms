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
import { migrateClusterTopicZk, migrateClusterConsumerZk } from '@/api/transfer'

export default {
  data() {
    return {
      diaOptions: {
        visible: false,
        ref: 'diaform',
        diatitle: '【集群】迁移进度',
        forms: [
          {
            prop: 'environmentName',
            label: '环境',
            disabled: true
          },
          {
            prop: 'serverName',
            label: '集群源',
            disabled: true
          },
          {
            prop: 'targetClusterId',
            label: '目标集群',
            itemType: 'remoteselect',
            apiUrl: 'cluster_list',
            relativeProp: [{ prop: 'environmentName', paramkey: 'envName' }],
            labelkeyname: 'serverName',
            valuekeyname: 'id',
            rules: [{ required: true, message: '请选择集群', trigger: 'change' }]
          },
          {
            prop: 'type',
            label: '迁移内容',
            itemType: 'radio',
            options: [
              { label: '迁移topic', value: 'topic' },
              { label: '迁移consumer', value: 'consumer' }
            ],
            defaultValue: 'topic',
            rules: [{ required: true, message: '请选择迁移内容', trigger: 'blur' }]
          }
        ],
        currentFormValue: {},
        loading: false
      },
      srcClusterId: undefined,
      serverType: '',
      environmentId: undefined
    }
  },
  methods: {
    // 开始迁移弹窗
    transferOpen() {
      this.selections = this.$refs.maintable.getTableSelections()
      const { selections } = this
      if (selections.length === 0 || selections.length > 1) {
        let msg = '请选择数据！'
        selections.length > 1 && (msg = '请选择一条数据！')
        this.message('error', msg)
        return
      }
      let _row = selections[0]
      this.serverType = _row.serverType
      this.environmentId = _row.environmentId
      this.srcClusterId = _row.id
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
    // 开始迁移
    beginTransfer() {
      this.handleValidate({
        refname: 'diaform',
        cb: async value => {
          delete value.environmentName
          delete value.serverName
          value.srcClusterId = this.srcClusterId
          value.envId = this.environmentId
          if (value.type === 'topic') {
            // 迁移topic
            await migrateClusterTopicZk(value)
          } else {
            // 迁移consumer
            await migrateClusterConsumerZk(value)
          }
          // 提示
          this.message('success', '迁移成功')
          // 关闭弹窗
          this.closeDia()
          // 重新load表格数据
          this.loadTabledata()
        }
      })
    }
  }
}
