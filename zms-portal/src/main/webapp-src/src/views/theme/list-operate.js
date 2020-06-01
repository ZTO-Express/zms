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
import { CO_NUM_CHAR, emails } from '@/utils/validate-func'
import { createThemeSave, editThemeSave } from '@/api/theme'
export default {
  data() {
    return {
      diaOptions: {
        visible: false,
        ref: 'diaform',
        status: 0,
        diatitle: ['新增', '编辑'],
        forms: [
          {
            prop: 'name',
            label: '主题名',
            // 接口校验输入值
            checkApi: {
              apiUrl: 'theme_validCode',
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
            prop: 'environments',
            label: '环境选择',
            itemType: 'checkbox',
            options: [],
            rules: [{ required: true, message: '请选择主题环境', trigger: 'blur' }]
          },
          {
            prop: 'tps',
            label: '发送速度',
            placeholder: '该topic最大发送速度',
            itemType: 'number',
            slots: [{ type: 'append', text: '条/秒' }],
            min: 0,
            step: 500,
            defaultValue: 1000,
            stepStrictly: true,
            rules: [{ required: true, message: '请输入发送速度' }]
          },
          {
            prop: 'msgszie',
            label: '消息体',
            itemType: 'number',
            min: 0,
            step: 512,
            defaultValue: 1024,
            stepStrictly: true,
            placeholder: '发送到topic最大消息',
            slots: [{ type: 'append', text: '字节' }],
            rules: [{ required: true, message: '请输入消息体' }]
          },
          {
            prop: 'alertEmails',
            label: '邮箱通知列表',
            inputType: 'textarea',
            placeholder: '示例：xx@zto.cn,aa@zto.cn',
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
            placeholder: '第一行：业务含义\n第二行：集群类型偏好Kafka还是RocketMQ',
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
    // 获取环境信息
    async operateEnvList() {
      let _envListData = this.envListData
      this.diaOptions.loading = true
      if (_envListData === undefined) {
        await this.getEnvList()
      }
      this.diaOptions.loading = false
    },
    // 新增弹窗
    async operateOpen() {
      await this.operateEnvList()
      this.handleDia({
        options: this.diaOptions,
        cb: () => {
          this.diaOptions.status = 0
          // 已审批不可修改主题名
          Object.assign(this.diaOptions.forms[0], {
            disabled: false
          })
          // 新增主题 申请人为当前登录用户，且不可编辑
          Object.assign(this.diaOptions.forms[1], {
            defaultValue: this.userInfo.realName,
            disabled: true,
            itemType: 'input'
          })
          // 环境列表
          Object.assign(this.diaOptions.forms[3], {
            disabled: false,
            options: this.envListData.map(item => {
              return {
                label: item.environmentName,
                value: item.id,
                ...item
              }
            }),
            defaultValue: this.envListData.map(item => {
              return item.id
            })
          })
        }
      })
    },
    // 编辑弹窗
    async operateEdit(row) {
      await this.operateEnvList()
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
          // 申请人为下拉框
          Object.assign(this.diaOptions.forms[1], {
            defaultValue: '',
            itemType: 'remoteselect',
            apiUrl: 'theme_applicant',
            disabled: false
          })
          const approvaled = !(_row.status === 0 || _row.status === 2)
          // 环境列表options填充
          Object.assign(this.diaOptions.forms[3], {
            disabled: approvaled ? true : false,
            options: this.envListData.map(item => {
              return {
                label: item.environmentName,
                value: item.id,
                ...item
              }
            })
          })
          // 已审批不可修改主题名
          Object.assign(this.diaOptions.forms[0], {
            disabled: approvaled ? true : false
          })
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
        refname: 'diaform',
        cb: async value => {
          const environments = value.environments
          value.environments = environments.map(item => {
            return {
              environmentId: item
            }
          })
          this.diaOptions.loading = true
          if (this.diaOptions.status === 0) {
            // 新增保存
            await createThemeSave(value)
          } else {
            const id = this.diaOptions.currentFormValue.id
            // 编辑保存
            await editThemeSave(id, value)
          }
          this.diaOptions.loading = false
          this.message('success', '保存成功')
          // 关闭弹窗
          this.closeDia()
          // 重新load表格数据
          this.loadTabledata()
        }
      })
    }
  }
}
