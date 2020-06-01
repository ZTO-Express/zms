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
import { configUpdateGated } from '@/api/consumer'

export default {
  data() {
    return {
      configForms: [
        {
          prop: 'name',
          label: '消费名',
          disabled: true,
          rules: [{ required: true, message: '请输入主题名', trigger: 'blur' }]
        },
        {
          prop: 'consumerEnvironmentRefVos',
          label: '环境选择',
          itemType: 'checkbox',
          options: [],
          rules: [{ required: true, message: '请选择主题环境', trigger: 'blur' }],
          disabled: true
        }
      ],
      configOptions: {
        visible: false,
        diatitle: '【消费】灰度配置',
        ref: 'configform',
        id: 0,
        envId: 1,
        forms: []
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
    gatedClusterChange(val) {
      this.configOptions.currentValue.gatedClusterId = val.id
      this.$refs.configform.specialSet({ prop: 'gatedIps', val: val.ips })
    },
    // 获取环境信息
    async configEnvList() {
      let _envListData = this.envListData
      this.configOptions.loading = true
      if (_envListData === undefined) {
        await this.getEnvList()
      }
      this.configOptions.loading = false
    },
    // 编辑弹窗
    async handleConfig(row) {
      this.id = row.id
      await this.configEnvList()
      this.configOptions.forms = [...this.configForms]

      // 编辑表单的当前值
      let _row = { ...row }
      _row.consumerEnvironmentRefVos = []

      if (Array.isArray(_row.consumerEnvironmentRefVos)) {
        row.consumerEnvironmentRefVos.forEach(item => {
          _row.consumerEnvironmentRefVos.push(item.environmentId)
          _row['cluster_' + item.environmentId] = item.gatedServiceId
          _row['clusterIp_' + item.environmentId] = item.gatedIps
        })
      }
      // 动态添加集群选项
      const _clusterForm = _row.consumerEnvironmentRefVos.map(item => {
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
          // 环境列表options填充
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
    closeConfigOptions() {
      Object.assign(this.configOptions, {
        visible: false
      })
    },
    saveConfigData() {
      this.handleValidate({
        refname: 'configform',
        cb: async value => {
          const consumerEnvironmentRefVos = value.consumerEnvironmentRefVos
          value.consumerEnvironmentRefVos = consumerEnvironmentRefVos.map(item => {
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
          // const environments = value.environments
          value.id = this.id
          console.info('value:' + JSON.stringify(value))
          await configUpdateGated(value)
          // 关闭弹窗
          this.closeConfigOptions()
          // 重新load表格数据
          this.loadTabledata()
        }
      })
    }
  }
}
