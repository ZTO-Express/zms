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

// 新增复制编辑代码
import { checkPhoneNull, emails } from '@/utils/validate-func'
import {
  createAlertSave,
  editAlertSave,
  uniqueCheck,
  queryEnvironmentRefByTopicName,
  queryEnvironmentRefByConsumerName
} from '@/api/alert'

const nameRemoteUrls = {
  topic: 'theme_getTopicsByClusterId',
  consumer: 'consumer_getConsumersByClusterId'
}
const fieldOptions = {
  topic: ['tps'],
  consumer: ['tps', 'lastConsumingTime', 'latency']
}
export default {
  data() {
    return {
      diaOptions: {
        inline: true,
        labelWidth: '100px',
        visible: false,
        ref: 'diaform',
        status: 0,
        insertNum: 5,
        diatitle: ['新增', '编辑'],
        forms: [
          {
            prop: 'type',
            label: '告警类型',
            itemType: 'select',
            options: ['topic', 'consumer'],
            defaultValue: 'topic',
            change: this.typeSelect,
            rules: [{ required: true, message: '请选择告警类型' }]
          },
          {
            prop: 'field',
            label: '指标名称',
            itemType: 'select',
            options: fieldOptions.topic,
            defaultValue: 'tps',
            relativeProp: [{ prop: 'type' }],
            rules: [{ required: true, message: '请选择字段' }]
          },
          {
            prop: 'name',
            label: '消费组/主题名',
            itemType: 'remoteselect',
            apiUrl: nameRemoteUrls.topic,
            labelkeyname: 'name',
            valuekeyname: 'name',
            change: this.nameSelect,
            relativeProp: [{ prop: 'type' }],
            rules: [
              { required: true, message: '请选择', trigger: 'change' },
              { validator: this.validateUniqueCode, trigger: 'change' }
            ]
          },
          {
            prop: 'operator',
            label: '阀值比较符',
            itemType: 'select',
            options: ['>', '=', '<'],
            defaultValue: '>',
            rules: [{ required: true, message: '请选择阀值比较符' }]
          },
          {
            prop: 'target',
            label: '阀值',
            itemType: 'number',
            min: 1,
            rules: [{ required: true, message: '请输入目标阀值' }]
          },
          {
            prop: 'scope',
            label: '时间间隔',
            defaultValue: '',
            rules: [
              { required: true, message: '请输入时间间隔', trigger: 'blur' },
              { validator: this.timeValid, trigger: 'blur' }
            ]
          },
          {
            prop: 'triggerOperator',
            label: '次数比较符',
            itemType: 'select',
            options: ['>', '=', '<'],
            defaultValue: '>',
            rules: [{ required: true, message: '请选择次数比较符' }]
          },
          {
            prop: 'triggerTimes',
            label: '触发次数',
            itemType: 'number',
            min: 1,
            rules: [{ required: true, message: '请输入触发次数' }]
          },
          {
            prop: 'alertUser',
            label: '接收人',
            itemType: 'remoteselect',
            apiUrl: 'theme_applicant',
            rules: [{ required: true, message: '请选择接收人', trigger: 'change' }]
          },
          {
            prop: 'alertMobile',
            label: '接收电话',
            defaultValue: '',
            rules: [
              { required: true, message: '请输入接收电话', trigger: 'blur' },
              { validator: checkPhoneNull, message: '请输入正确手机号', trigger: 'blur' }
            ]
          },
          {
            prop: 'alertEmail',
            label: '接收邮箱',
            defaultValue: '',
            rules: [{ pattern: emails, message: '请输入正确的邮箱', trigger: 'blur' }]
          },
          {
            prop: 'effectFrom',
            label: '开始时间',
            itemType: 'time',
            format: 'HH:mm',
            valueFormat: 'HH:mm',
            rules: [{ required: true, message: '请选择', trigger: 'change' }]
          },
          {
            prop: 'effectTo',
            label: '结束时间',
            itemType: 'time',
            format: 'HH:mm',
            valueFormat: 'HH:mm',
            rules: [{ required: true, message: '请选择', trigger: 'change' }]
          },
          {
            prop: 'effect',
            label: '生效',
            itemType: 'radio',
            defaultValue: false,
            options: [
              { label: '是', value: true },
              { label: '否', value: false }
            ]
          },
          {
            prop: 'environments',
            label: '环境选择',
            itemType: 'checkbox',
            options: [],
            inline: false,
            relativeProp: [{ prop: 'name' }],
            defaultValue: [],
            rules: [{ required: true, message: '请选择主题环境', trigger: 'blur' }]
          },
          {
            prop: 'alertDingding',
            label: 'DingDing机器人',
            inputType: 'textarea',
            defaultValue: '',
            inline: false
          },
          {
            prop: 'description',
            label: '告警信息',
            inputType: 'textarea',
            defaultValue: '',
            disabled: true,
            rows: 4,
            inline: false
          }
        ],
        currentFormValue: {},
        loading: false
      },
      envListData: []
    }
  },
  methods: {
    typeSelect(value) {
      const _forms = this.diaOptions.forms.map((item, index) => {
        if (index === 2) {
          item.apiUrl = nameRemoteUrls[value]
        } else if (index === 1) {
          item.options = fieldOptions[value]
        }
        return item
      })
      this.diaOptions = Object.assign({}, this.diaOptions, { forms: _forms })
    },
    nameSelect(val) {
      if (!val) {
        return
      }
      this.$refs.diaform.getParams(value => {
        this.operateEnvList(val, value.type)
      })
    },
    // 获取环境信息
    async operateEnvList(name, type) {
      this.envListData = []
      let params = {}
      if (type === 'topic') {
        params.topicName = name
        const { result } = await queryEnvironmentRefByTopicName(params)
        if (result) {
          this.envListData = result
        }
      } else {
        params.consumerName = name
        const { result } = await queryEnvironmentRefByConsumerName(params)
        if (result) {
          this.envListData = result
        }
      }
      // 环境列表options填充
      // this.diaOptions.currentFormValue.environments = []
      Object.assign(this.diaOptions.forms[14], {
        options: this.envListData.map(item => {
          return {
            label: item.environmentName,
            value: item.environmentId,
            ...item
          }
        })
      })
      if (this.envListData.length == 1) {
        const sel = this.envListData.map(item => {
          return item.environmentId
        })
        // this.diaOptions.currentFormValue = { ...this.diaOptions.currentFormValue, environments: sel }
        this.diaOptions.currentFormValue = sel
      }
    },
    async validateUniqueCode(rule, value, callback) {
      const _value = {}
      this.$refs.diaform.getParams(value => {
        _value.type = value.type
        _value.field = value.field
        _value.name = value.name
      })
      if (this.diaOptions.status === 1) {
        _value.id = this.diaOptions.currentFormValue.id
      }
      const { result } = await uniqueCheck(_value)
      if (!result) {
        callback(new Error('已存在或者以前使用过，请更换'))
        return
      }
      callback()
    },
    // 时间验证
    timeValid(rule, value, callback) {
      const reg = /^[0-9]*$/
      if (value !== '') {
        if (!reg.test(value)) {
          callback(new Error('请输入数值'))
          return
        }
      }
      if (this.timeUnit === 'm' && Number(value) < 5) {
        callback(new Error('时间间隔应大于5分钟'))
        return
      }
      callback()
    },
    // 新增弹窗
    async operateOpen() {
      this.handleDia({
        options: this.diaOptions,
        cb: () => {
          this.diaOptions.status = 0
        }
      })
    },
    // 编辑弹窗
    async operateEdit(row, option) {
      // 编辑表单的当前值
      let _row = { ...row }
      if (Array.isArray(_row.environmentRefDtos)) {
        _row.environments = _row.environmentRefDtos.map(item => {
          return item.environmentId
        })
      }
      this.handleDia({
        options: this.diaOptions,
        row: _row,
        cb: () => {
          this.diaOptions.status = option
          this.timeUnit = _row.scope.slice(-1)
          _row.scope = _row.scope.substring(0, _row.scope.length - 1)
          this.operateEnvList(_row.name, _row.type)
          this.typeSelect(_row.type)
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
      this.preview()
      this.handleValidate({
        refname: 'diaform',
        cb: async value => {
          let _value = { ...value }
          _value.scope += this.timeUnit
          _value.refList = _value.environments.map(item => {
            return {
              environmentId: item
            }
          })
          if (this.diaOptions.status === 0) {
            // 新增保存
            await createAlertSave(_value)
          } else {
            // 编辑保存
            await editAlertSave(this.diaOptions.currentFormValue.id, _value)
          }
          // 关闭弹窗
          this.closeDia()
          // 重新load表格数据
          this.loadTabledata()
        }
      })
    },
    // 预览生成
    preview() {
      this.$refs.diaform.getParams(value => {
        const { timeUnit } = this
        const _value = {}
        Object.keys(value).map(key => {
          if (value[key] === undefined) {
            _value[key] = ' '
          } else {
            _value[key] = value[key]
          }
        })
        const { name, scope, type, field, operator, target, triggerTimes, triggerOperator } = _value
        let triggerOperatortxt = ''
        if (triggerOperator === '>') {
          triggerOperatortxt = '超过'
        } else if (triggerOperator === '<') {
          triggerOperatortxt = '少于'
        }
        const str = `告警名称：${name}\n告警类型：${type}\n告警阀值：${scope}${timeUnit}时间内，${triggerOperatortxt}${triggerTimes}次触发${field}${operator}${target}`
        this.$refs.diaform.specialSet({ prop: 'description', val: str })
      })
    }
  }
}
