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

// 多环境同步
import { consumerSync, envList } from '@/api/consumer'
import OpFreight from '@/modules/modal/freight/OpFreight'
export default {
  data() {
    return {
      components: {
        OpFreight
      },
      diaSyncOptions: {
        visible: false,
        ref: 'diaSyncform',
        diatitle: '环境选择',
        forms: [
          {
            prop: 'envIds',
            label: '环境选择',
            itemType: 'checkbox',
            options: [],
            rules: [{ required: true, message: '请选择同步环境', trigger: 'blur' }]
          }
        ],
        currentFormValue: {},
        loading: false
      },
      consumerId: [],
      serverType: '',
      environmentId: undefined,
      envList: undefined
    }
  },
  methods: {
    // 获取环境信息
    async operateEnvList() {
      this.diaSyncOptions.loading = true
      const res = await envList()
      this.envList = res.result
      this.diaSyncOptions.loading = false
    },
    // 同步弹窗
    async syncOpen() {
      await this.operateEnvList()
      this.selections = this.$refs.maintable.getTableSelections()
      const { selections } = this
      if (selections.length === 0 || selections.length < 1) {
        let msg = '请选择数据！'
        selections.length > 1 && (msg = '请选择一条数据！')
        this.message('error', msg)
        return
      }
      this.consumerId = []
      this.selections.forEach(element => {
        this.consumerId.push(element.id)
      })
      this.handleDia({
        options: this.diaSyncOptions,
        cb: () => {
          // 环境列表
          Object.assign(this.diaSyncOptions.forms[0], {
            options: this.envList.map(item => {
              return {
                label: item.environmentName,
                value: item.id,
                ...item
              }
            })
          })
        }
      })
    },
    // 关闭弹窗
    closeSyncDia() {
      Object.assign(this.diaSyncOptions, {
        visible: false
      })
    },
    // 开始同步
    beginSync() {
      this.handleValidate({
        refname: 'diaSyncform',
        cb: async value => {
          value.consumerIds = this.consumerId
          // 同步consumer
          const res = await consumerSync(value)
          if (res.result) {
            let msg = ''
            res.result.map(item => {
              msg += '该环境 :' + item.envName + ',' + item.errorMsg + ';'
            })
            // 提示
            this.message('success', msg == null ? '同步成功' : msg)
          }
          // 关闭弹窗
          this.closeSyncDia()
          // 重新load表格数据
          this.loadTabledata()
        }
      })
    }
  }
}
