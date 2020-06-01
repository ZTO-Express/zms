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

// 新增编辑代码
import { mapState, mapActions } from 'vuex'
import { CO_NUM_CHAR } from '@/utils/validate-func'
import { createConsumerSave, editConsumerSave, consumeDelete } from '@/api/consumer'
import OpFreight from '@/modules/modal/freight/OpFreight'

export default {
  data() {
    return {
      components: {
        OpFreight
      },
      diaOptions: {
        visible: false,
        ref: 'consumerform',
        status: 0,
        id: 0,
        topicId: 0,
        diatitle: ['新增', '编辑'],
        forms: [
          {
            prop: 'name',
            label: '消费者名称',
            // 接口校验输入值
            checkApi: {
              apiUrl: 'consumer_validCode',
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
            apiUrl: 'consumer_getApproveTopicList',
            labelkeyname: 'name',
            valuekeyname: 'id',
            autoget: true,
            // remoteParams: { clusterId: 1 },
            // relativeProp: [{ prop: 'clusterId', paramkey: 'clusterId' }],
            rules: [{ required: true, message: '主题名为必填项', trigger: 'change' }]
          },
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
            defaultValue: 1000,
            stepStrictly: true,
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
        currentFormValue: {},
        loading: false
      }
    }
  },
  computed: {
    ...mapState({
      userInfo: state => state.user.userInfo,
      envListData: state => state.theme.envListData
    })
  },
  methods: {
    ...mapActions({
      getEnvList: 'theme/getEnvList'
    }),
    // 集群选择后的操作 扩展参数并设置值
    topicChange(val) {
      this.diaOptions.currentValue.topicId = val.id
      this.$refs.consumerform.specialSet({ prop: 'topicId', val: val.id })
    },
    // 获取环境信息
    async EnvList() {
      let _envListData = this.envListData
      this.diaOptions.loading = true
      if (_envListData === undefined) {
        await this.getEnvList()
      }
      this.diaOptions.loading = false
    },
    // 新增弹窗
    async operateOpen() {
      // await this.EnvList()
      this.handleDia({
        options: this.diaOptions,
        cb: () => {
          this.diaOptions.status = 0
          Object.assign(this.diaOptions.forms[0], {
            defaultValue: '',
            // apiUrl: 'name_validCode',
            disabled: false
          })
          //主题下拉
          Object.assign(this.diaOptions.forms[1], {
            defaultValue: '',
            // itemType: 'remoteselect',
            // remoteUrl: 'consumer_getApproveTopicList',
            // relativeProp: [{ prop: 'clusterId', paramkey: 'clusterId' }],
            disabled: false
          })
          // 申请人为当前登录用户，且不可编辑
          Object.assign(this.diaOptions.forms[4], {
            defaultValue: this.userInfo.realName,
            disabled: true,
            itemType: 'input'
          })
          // 环境列表
          // Object.assign(this.diaOptions.forms[3], {
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
    // 编辑弹窗
    async operateEdit(row) {
      this.id = row.id
      // await this.EnvList()
      // 编辑表单的当前值
      let _row = { ...row }
      if (Array.isArray(_row.environments)) {
        _row.environments = row.environments.map(item => {
          return item.environmentId
        })
      }
      this.handleDia({
        options: this.diaOptions,
        row: _row,
        cb: () => {
          this.diaOptions.status = 1
          Object.assign(this.diaOptions.forms[0], {
            defaultValue: '',
            disabled: true
          })
          // 申请人为下拉框
          Object.assign(this.diaOptions.forms[4], {
            defaultValue: '',
            itemType: 'remoteselect',
            apiUrl: 'theme_applicant',
            disabled: false
          })
          //主题名不可编辑
          Object.assign(this.diaOptions.forms[1], {
            defaultValue: '',
            // itemType: 'remoteselect',
            disabled: true
          })

          // 环境列表options填充
          // Object.assign(this.diaOptions.forms[3], {
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
    closeDia() {
      Object.assign(this.diaOptions, {
        visible: false
      })
    },
    saveData() {
      this.handleValidate({
        refname: 'consumerform',
        cb: async value => {
          const environments = value.environments
          console.info(JSON.stringify(environments))
          value.id = this.id
          if (this.diaOptions.status === 0) {
            // 新增保存
            await createConsumerSave(value)
          } else {
            const id = this.diaOptions.currentFormValue.id
            // 编辑保存
            await editConsumerSave(id, value)
          }

          // 关闭弹窗
          this.closeDia()
          // 重新load表格数据
          this.loadTabledata()
        }
      })
    },
    // 删除
    // 删除
    handleDelete(row) {
      const params = { id: row.id }
      this.isDo({
        title: `您是否确定删除【${row.name}】消费者？删除后不可恢复！`,
        cb: async () => {
          this.maintable.loading = true
          await consumeDelete(params)
          // 提示
          this.message('success', '删除成功')
          // reload table
          this.loadTabledata()
        }
      })
    }
  }
}
