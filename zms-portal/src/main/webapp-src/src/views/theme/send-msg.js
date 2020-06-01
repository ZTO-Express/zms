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

import { sendThemeMsg, themeListByCluster } from '@/api/theme'
import { checkPerm } from '@/utils/permission'
export default {
  data() {
    return {
      sendMsgOptions: {
        visible: false,
        ref: 'sendMsgForm',
        diatitle: '发消息',
        forms: [
          {
            prop: 'envId',
            label: '环境',
            itemType: 'select',
            options: [],
            change: this.changeEnvId,
            rules: [{ required: true, message: '请选择主题环境', trigger: 'blur' }]
          },
          {
            prop: 'topic',
            label: '主题',
            itemType: 'select',
            options: [],
            rules: [{ required: true, message: '请选择主题', trigger: 'blur' }]
          },
          {
            prop: 'keys',
            label: 'keys'
          },
          {
            prop: 'tags',
            label: 'tags'
          },
          {
            prop: 'msg',
            label: '消息体',
            inputType: 'textarea',
            rules: [{ required: true, message: '请输入消息体', trigger: 'blur' }]
          }
        ],
        loading: false
      }
    }
  },
  methods: {
    async changeEnvId(val) {
      this.$refs.sendMsgForm.specialSet({ prop: 'topic', val: undefined })
      let params = { envId: val }
      if (!checkPerm(['admin'])) {
        params.applicant = this.userInfo.realName
      }
      const theme = await themeListByCluster(params)
      if (!theme.result) {
        return
      }
      // 主题下拉框
      Object.assign(this.sendMsgOptions.forms[1], {
        options: theme.result.map(item => {
          return {
            label: item.name,
            value: item.name,
            ...item
          }
        })
      })
    },
    // 获取环境信息
    async msgEnvList() {
      let _envListData = this.envListData
      this.sendMsgOptions.loading = true
      if (_envListData === undefined) {
        await this.getEnvList()
      }
      this.sendMsgOptions.loading = false
    },
    // 发消息弹窗
    async handleSendMsg() {
      await this.msgEnvList()
      this.handleDia({
        options: this.sendMsgOptions,
        cb: () => {
          // 环境列表
          Object.assign(this.sendMsgOptions.forms[0], {
            options: this.envListData.map(item => {
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
    closeMsgDia() {
      Object.assign(this.sendMsgOptions, {
        visible: false
      })
    },
    saveMsgData() {
      this.handleValidate({
        refname: 'sendMsgForm',
        cb: async value => {
          this.sendMsgOptions.loading = true
          await sendThemeMsg(value)
          this.sendMsgOptions.loading = false
          this.message('success', '发送成功')
          this.closeMsgDia()
        }
      })
    }
  }
}
