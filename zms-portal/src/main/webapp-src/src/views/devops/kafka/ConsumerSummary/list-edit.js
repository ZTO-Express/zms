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
import { resetOffset } from '@/api/kafka-cmd'

export default {
  data() {
    return {
      diaOptions: {
        visible: false,
        ref: 'diaform',
        diatitle: '编辑',
        formrefname: 'diaform',
        forms: [
          {
            prop: 'envId',
            label: '环境',
            itemType: 'remoteselect',
            apiUrl: 'environment_list',
            labelkeyname: 'environmentName',
            valuekeyname: 'id',
            autoget: true,
            rules: [{ required: true, message: '请选择', trigger: 'change' }]
          },
          {
            prop: 'clusterName',
            label: '集群名',
            itemType: 'remoteselect',
            apiUrl: 'cluster_list',
            labelkeyname: 'serverName',
            remoteParams: { serviceType: 'KAFKA' },
            relativeProp: [{ prop: 'envId', paramkey: 'envId' }],
            valuekeyname: 'serverName',
            rules: [{ required: true, message: '请选择', trigger: 'change' }]
          },
          {
            prop: 'consumerName',
            label: '消费组',
            itemType: 'remoteselect',
            labelkeyname: 'name',
            valuekeyname: 'name',
            apiUrl: 'consumer_list',
            selectInfo: this.consumeChange,
            remoteParams: { clusterType: 'KAFKA' },
            relativeProp: [
              { prop: 'clusterName', paramkey: 'clusterName' },
              { prop: 'envId', paramkey: 'envId' }
            ],
            rules: [{ required: true, message: '请选择消费组', trigger: 'change' }]
          },
          {
            prop: 'topicName',
            label: '主题名',
            relativeProp: [{ prop: 'envId' }],
            rules: [{ required: true, message: '请选择主题名', trigger: 'change' }]
          },
          {
            prop: 'type',
            label: '类型',
            itemType: 'select',
            options: [
              { label: '时间', value: 'time' },
              { label: '最早位点', value: 'earliest' },
              { label: '最新位点', value: 'latest' },
              { label: '偏移量', value: 'offset' }
            ],
            defaultValue: 'time',
            change: this.typeSelect,
            rules: [{ required: true, message: '请选择类型', trigger: 'change' }]
          },
          {
            prop: 'time',
            label: '时间',
            itemType: 'date',
            dateType: 'datetime',
            valueFormat: 'timestamp',
            format: 'yyyy-MM-dd HH:mm:ss',
            rules: [{ required: true, message: '请选择时间', trigger: 'change' }]
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
    consumeChange(val) {
      this.$refs.diaform.specialSet({ prop: 'topicName', val: val.topicName })
    },
    typeSelect(val) {
      console.info('val', val)
      this.diaOptions.forms.splice(5, 1)
      switch (val) {
        case 'time':
          this.diaOptions.forms.push({
            prop: 'time',
            label: '时间',
            itemType: 'date',
            dateType: 'datetime',
            valueFormat: 'timestamp',
            format: 'yyyy-MM-dd HH:mm:ss',
            rules: [{ required: true, message: '请选择时间', trigger: 'change' }]
          })
          break
        case 'offset':
          this.diaOptions.forms.push({
            prop: 'offset',
            label: '偏移量',
            itemType: 'number',
            min: 0,
            step: 1,
            stepStrictly: true,
            rules: [{ required: true, message: '请输入偏移量', trigger: 'change' }]
          })
          break
        default:
          break
      }
    },
    saveData() {
      this.handleValidate({
        refname: 'diaform',
        cb: async value => {
          const envId = value.envId
          var param = {
            clusterName: value.clusterName,
            consumerName: value.consumerName,
            topicName: value.topicName,
            type: value.type,
            time: value.time,
            offset: value.offset
          }
          await resetOffset(envId, param)
          this.message('success', '发送成功')
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
          // this.diaOptions.envId = row.envId
        }
      })
    }
  }
}
