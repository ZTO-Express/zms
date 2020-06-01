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
import { emails } from '@/utils/validate-func'
import { approvalTheme } from '@/api/theme'
export default {
  data() {
    return {
      forms: [
        {
          prop: 'name',
          label: '主题名',
          disabled: true
        },
        {
          prop: 'partitions',
          label: '分片数',
          itemType: 'number',
          min: 1,
          max: 64,
          rules: [{ required: true, message: '请输入分片数' }]
        },
        {
          prop: 'replication',
          label: '副本数',
          itemType: 'number',
          min: 1,
          max: 64,
          disabled: false,
          rules: [{ required: true, message: '请输入kafka主题副本数' }]
        },
        {
          prop: 'environments',
          label: '环境选择',
          itemType: 'checkbox',
          options: [],
          rules: [{ required: true, message: '请选择主题环境', trigger: 'blur' }],
          disabled: true
        },
        {
          prop: 'applicant',
          label: '申请人',
          disabled: true,
          rules: [{ required: true, message: '申请人为必填项', trigger: 'change' }]
        },
        {
          prop: 'domain',
          label: '申请域(appId)',
          placeholder: '业务英文名字',
          disabled: true,
          rules: [{ required: true, message: '请输入申请域(appId)', trigger: 'blur' }]
        },
        {
          prop: 'tps',
          label: '发送速度',
          placeholder: '该topic最大发送速度',
          itemType: 'number',
          slots: [{ type: 'append', text: '条/秒' }],
          min: 0,
          step: 500,
          stepStrictly: true,
          disabled: true,
          rules: [{ required: true, message: '请输入发送速度' }]
        },
        {
          prop: 'msgszie',
          label: '消息体',
          itemType: 'number',
          min: 0,
          step: 512,
          stepStrictly: true,
          placeholder: '发送到topic最大消息',
          disabled: true,
          slots: [{ type: 'append', text: '字节' }],
          rules: [{ required: true, message: '请输入消息体' }]
        },
        {
          prop: 'alertEmails',
          label: '邮箱通知列表',
          inputType: 'textarea',
          placeholder: '示例：xx@zto.cn,aa@zto.cn',
          disabled: true,
          rules: [
            { required: true, message: '请输入邮箱通知列表', trigger: 'blur' },
            { pattern: emails, message: '请输入正确的邮箱，多个邮箱使用逗号【,】分隔', trigger: 'blur' },
            { max: 100, message: '最多输入100个字符', trigger: 'blur' }
          ]
        },
        {
          prop: 'memo',
          label: '备注',
          inputType: 'textarea',
          disabled: true,
          placeholder: '第一行：业务含义\n第二行：集群类型偏好Kafka还是RocketMQ',
          rules: [
            { required: true, message: '请输入备注', trigger: 'blur' },
            { max: 100, message: '最多输入100个字符', trigger: 'blur' }
          ]
        }
      ],
      approvalOptions: {
        visible: false,
        ref: 'approvalForm',
        diatitle: '主题审批',
        forms: [],
        currentFormValue: {},
        loading: false
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
    // 获取环境信息
    async approvalEnvList() {
      let _envListData = this.envListData
      this.approvalOptions.loading = true
      if (_envListData === undefined) {
        await this.getEnvList()
      }
      this.approvalOptions.loading = false
    },
    // 审批弹窗
    async approval(row) {
      // 填入forms
      this.approvalOptions.forms = [...this.forms]

      await this.approvalEnvList()
      // 环境多选
      let _row = { ...row }
      _row.environments = []
      if (Array.isArray(row.environments)) {
        row.environments.forEach(item => {
          _row.environments.push(item.environmentId)
          _row['cluster_' + item.environmentId] = item.serviceId
        })
      }
      // 动态添加集群选项
      const _clusterForm = _row.environments.map(item => {
        const env = this.envListData.filter(v => v.id === item)[0]
        return {
          prop: 'cluster_' + item,
          label: env.environmentName + '集群',
          itemType: 'remoteselect',
          apiUrl: 'cluster_list',
          labelkeyname: 'serverName',
          valuekeyname: 'id',
          remoteParams: { envId: env.id },
          autoget: true,
          selectInfo: this.changeCluserInfo,
          rules: [{ required: true, message: '请选择' + env.environmentName + '环境集群', trigger: 'blur' }]
        }
      })
      this.approvalOptions.forms.splice(4, 0, ..._clusterForm)

      this.handleDia({
        options: this.approvalOptions,
        row: _row,
        cb: () => {
          // 环境列表
          Object.assign(this.approvalOptions.forms[3], {
            options: this.envListData.map(item => {
              return {
                label: item.environmentName,
                value: item.id,
                ...item
              }
            })
          })
          Object.assign(this.approvalOptions.forms[2], {
            disabled: !(_row.status === 0 || _row.status === 2)
          })
        }
      })
    },
    changeCluserInfo(val) {
      if (!val) {
        return
      }
      Object.assign(this.approvalOptions.forms[2], {
        disabled: val.serverType === 'ROCKETMQ'
      })
    },
    // 关闭弹窗
    closeApprovalDia() {
      Object.assign(this.approvalOptions, {
        visible: false
      })
    },
    saveApprovalData() {
      this.handleValidate({
        refname: 'approvalForm',
        cb: async value => {
          const environments = value.environments
          value.environments = environments.map(item => {
            const serviceId = value['cluster_' + item]
            delete value['cluster_' + item]
            return {
              environmentId: item,
              serviceId
            }
          })
          const id = this.approvalOptions.currentFormValue.id
          this.approvalOptions.loading = true
          await approvalTheme(id, value)
          this.approvalOptions.loading = false
          this.message('success', '审批成功')
          // 关闭弹窗
          this.closeApprovalDia()
          // 重新load表格数据
          this.loadTabledata()
        }
      })
    }
  }
}
