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
import { approvalConsumer } from '@/api/consumer'
import { CO_NUM_CHAR } from '@/utils/validate-func'

export default {
  data() {
    return {
      forms: [
        {
          prop: 'name',
          label: '消费者名称',
          // 接口校验输入值
          checkApi: {
            apiUrl: 'name_validCode',
            message: '已存在或者以前使用过，请更换',
            paramkey: 'name'
          },
          rules: [
            { required: true, message: '请输入主题名', trigger: 'blur' },
            { max: 100, message: '小于100个字符', trigger: 'blur' },
            { pattern: CO_NUM_CHAR, message: '请输入数字、字母、下划线、中划线', trigger: 'blur' }
          ]
        },
        {
          prop: 'topicId',
          label: '主题名',
          itemType: 'remoteselect',
          labelkeyname: 'name',
          valuekeyname: 'id',
          disabled: true,
          autoget: true,
          apiUrl: 'consumer_getApproveTopicList',
          // remoteParams: { clusterId: 1 },
          // relativeProp: [{ prop: 'clusterId', paramkey: 'clusterId' }],
          rules: [{ required: true, message: '主题名为必填项', trigger: 'change' }]
        },
        // {
        //   prop: 'clusterName',
        //   label: '集群名',
        //   itemType: 'remoteselect',
        //   apiUrl: 'cluster_list',
        //   labelkeyname: 'serverName',
        //   valuekeyname: 'id',
        //   relativeProp: [{ prop: 'envId', paramkey: 'envId' }],
        //   rules: [{ required: true, message: '请选择', trigger: 'change' }]
        // },
        {
          prop: 'broadcast',
          label: '广播消费',
          itemType: 'radio',
          defaultValue: false,
          options: [
            { label: '是', value: true },
            { label: '否', value: false }
          ],
          rules: [{ required: true, message: '是否为广播消费主题名为必选项', trigger: 'blur' }]
        },
        {
          prop: 'consumerFrom',
          label: '最早消费',
          itemType: 'radio',
          defaultValue: false,
          options: [
            { label: '是', value: true },
            { label: '否', value: false }
          ],
          rules: [{ required: true, message: '是否为最早消费主题名为必选项', trigger: 'blur' }]
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
          rules: [{ required: true, message: '请输入申请域(appId)', trigger: 'blur' }]
        },
        {
          prop: 'delayThreadhold',
          label: '积压阈值',
          itemType: 'number',
          min: 0,
          step: 500,
          stepStrictly: true,
          placeholder: '最大积压阈值',
          slots: [{ type: 'append', text: '' }],
          rules: [{ required: true, message: '请输入积压阈值' }]
        },
        {
          prop: 'memo',
          label: '备注',
          inputType: 'textarea',
          placeholder: '1.申请消费组业务用途 \n2.广播消费原因描述 \n3.最早消费原因描述',
          rules: [
            { required: true, message: '请输入备注', trigger: 'blur' },
            { max: 100, message: '最多输入100个字符', trigger: 'blur' }
          ]
        }
      ],
      approvalOptions: {
        visible: false,
        ref: 'approvalForm',
        diatitle: '消费组审批',
        forms: [],
        currentFormValue: {},
        loading: false,
        id: 0
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
    async approvalConsumer(row) {
      let _row = { ...row }
      this.id = row.id
      // const broadcast = _row.broadcast == '是' ? true : false
      // row.broadcast = broadcast
      // 填入forms
      this.approvalOptions.forms = [...this.forms]

      await this.approvalEnvList()
      this.handleDia({
        options: this.approvalOptions,
        row: _row,
        cb: () => {
          let disabled = false
          if (row && row.status !== 0) {
            // 新增未审批
            disabled = true
          }
          this.approvalOptions.forms[0].disabled = disabled

          // 环境列表
          // Object.assign(this.approvalOptions.forms[2], {
          //   options: this.envListData.map(item => {
          //     return {
          //       label: item.environmentName,
          //       value: item.id,
          //       ...item
          //     }
          //   })
          // })
        }
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
          // const environments = value.environments
          // value.environments = environments.map(item => {
          //   const serviceId = value['cluster_' + item]
          //   delete value['cluster_' + item]
          //   return {
          //     environmentId: item,
          //     serviceId
          //   }
          // })
          let consumerId = this.approvalOptions.currentFormValue.id
          value.id = consumerId
          await approvalConsumer(consumerId, value)
          // 关闭弹窗
          this.closeApprovalDia()
          // 重新load表格数据
          this.loadTabledata()
        }
      })
    }
  }
}
