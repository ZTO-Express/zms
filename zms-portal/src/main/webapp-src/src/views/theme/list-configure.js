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

import { mapState, mapActions } from 'vuex'
import { configureTheme } from '@/api/theme'
export default {
  data() {
    return {
      configForms: [
        {
          prop: 'name',
          label: '主题名',
          disabled: true,
          rules: [{ required: true, message: '请输入主题名', trigger: 'blur' }]
        },
        {
          prop: 'environments',
          label: '环境选择',
          itemType: 'checkbox',
          options: [],
          rules: [{ required: true, message: '请选择主题环境', trigger: 'blur' }],
          disabled: true
        }
      ],
      configOptions: {
        visible: false,
        ref: 'configForm',
        diatitle: '主题配置',
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
    async configEnvList() {
      let _envListData = this.envListData
      this.configOptions.loading = true
      if (_envListData === undefined) {
        await this.getEnvList()
      }
      this.configOptions.loading = false
    },
    // 配置弹窗
    async configure(row) {
      // 填入forms
      this.configOptions.forms = [...this.configForms]

      await this.configEnvList()
      // 环境多选
      let _row = { ...row }
      _row.environments = []
      if (Array.isArray(_row.environments)) {
        row.environments.forEach(item => {
          _row.environments.push(item.environmentId)
          _row['cluster_' + item.environmentId] = item.gatedServiceId
          _row['clusterIp_' + item.environmentId] = item.gatedIps
        })
      }

      // 动态添加集群选项
      const _clusterForm = _row.environments.map(item => {
        const env = this.envListData.filter(v => v.id === item)[0]
        return [
          {
            prop: 'cluster_' + item,
            label: env.environmentName + '灰度集群',
            itemType: 'remoteselect',
            apiUrl: 'cluster_list',
            remoteParams: { envId: item },
            labelkeyname: 'serverName',
            valuekeyname: 'id',
            autoget: true,
            rules: [{ required: true, message: '请选择', trigger: 'blur' }]
          },
          {
            prop: 'clusterIp_' + item,
            label: env.environmentName + '灰度IP'
          }
        ]
      })
      this.configOptions.forms = this.configOptions.forms.concat(..._clusterForm)

      this.handleDia({
        options: this.configOptions,
        row: _row,
        cb: () => {
          // 环境列表
          Object.assign(this.configOptions.forms[1], {
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
    // 关闭弹窗
    closeConfigDia() {
      Object.assign(this.configOptions, {
        visible: false
      })
    },
    saveConfigData() {
      this.handleValidate({
        refname: 'configForm',
        cb: async value => {
          const environments = value.environments
          value.environments = environments.map(item => {
            const gatedServiceId = value['cluster_' + item]
            delete value['cluster_' + item]
            const gatedIps = value['clusterIp_' + item]
            delete value['clusterIp_' + item]
            return {
              environmentId: item,
              gatedServiceId,
              gatedIps
            }
          })
          const id = this.configOptions.currentFormValue.id
          this.configOptions.loading = true
          await configureTheme(id, { environments: value.environments })
          this.configOptions.loading = false
          this.message('success', '配置成功')
          // 关闭弹窗
          this.closeConfigDia()
          // 重新load表格数据
          this.loadTabledata()
        }
      })
    }
  }
}
