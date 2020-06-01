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

// 审批代码
import { mapState, mapActions } from 'vuex'
import { updatePerm } from '@/api/rocketmq-cmd'

export default {
  data() {
    return {
      diaOptions: {
        visible: false,
        ref: 'diaform',
        diatitle: ['新增', '编辑'],
        forms: [
          {
            prop: 'clusterName',
            label: 'cluster',
            disabled: true
          },
          {
            prop: 'topicName',
            label: 'topic',
            disabled: true,
            // itemType: 'elautocomplete',
            // labelkey: 'name',
            // remoteUrl: 'theme_queryApproveTopics',
            // relativeProp: [{ prop: 'cluster', paramkey: 'clusterName' }],
            rules: [{ required: true, message: '请选择topic', trigger: 'change' }]
          },
          {
            prop: 'broker',
            label: 'broker',
            rules: [{ required: true, message: '请输入broker', trigger: 'change' }]
          },
          {
            prop: 'perm',
            label: 'perm',
            itemType: 'number',
            min: 2,
            max: 6,
            step: 2,
            stepStrictly: true,
            rules: [{ required: true, message: '请输入perm', trigger: 'change' }]
          }
        ],
        currentFormValue: {},
        loading: false,
        envId: 0
      }
    }
  },
  computed: {
    ...mapState({
      envListData: state => state.theme.envListData
    })
  },
  methods: {
    ...mapActions({
      getEnvList: 'theme/getEnvList'
    }),
    closeDia() {
      Object.assign(this.diaOptions, {
        visible: false
      })
    },

    saveData() {
      this.handleValidate({
        refname: 'diaform',
        cb: async value => {
          const envId = this.diaOptions.envId
          await updatePerm(envId, value)
          // 关闭弹窗
          this.closeDia()
          // 重新load表格数据
          this.searchHandler()
        }
      })
    },
    // 打开弹窗
    async handleData(row) {
      let _row = { ...row }
      this.handleDia({
        row: _row,
        options: this.diaOptions,
        cb: () => {
          this.diaOptions.envId = row.envId
        }
      })
    }
  }
}
