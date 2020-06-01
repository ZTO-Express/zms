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

// 搜索代码
import { environmentList, clusterList, alertNamesList, applicantList } from '@/api/alert'
export default {
  data() {
    return {
      // 搜索表单参数
      formData: {
        environmentId: '',
        serviceId: '', // 集群
        name: '', //告警名称
        user: '', //接受人
        type: undefined //类型
      },
      SearchEnv: {
        options: [],
        loading: false
      },
      SearchCluster: {
        options: [],
        loading: false
      },
      SearchAlertName: {
        options: [],
        loading: false
      },
      SearchUser: {
        options: [],
        loading: false
      },
      SearchType: {
        options: [
          { label: 'topic', value: 'topic' },
          { label: 'consumer', value: 'consumer' }
        ]
      }
    }
  },
  computed: {
    environmentId() {
      return this.formData.environmentId
    }
  },
  watch: {
    // 监听环境值变化，重置集群选项值
    environmentId() {
      this.formData.serviceId = ''
    }
  },
  methods: {
    // 获取环境下拉列表
    async getEnvOptions() {
      this.SearchEnv.loading = true
      const res = await environmentList()
      if (res.result) {
        this.SearchEnv.options = res.result.map(item => {
          return {
            label: item.environmentName,
            value: item.id,
            ...item
          }
        })
        this.SearchEnv.loading = false
      }
    },
    // 获取集群下拉列表
    async getClusterOptions() {
      this.SearchCluster.loading = true
      const params = { envId: this.formData.envId }
      const res = await clusterList(params)
      if (res.result) {
        this.SearchCluster.options = res.result.map(item => {
          return {
            label: item.serverName,
            value: item.id,
            ...item
          }
        })
        this.SearchCluster.loading = false
      }
    },
    // 获取告警名称下拉列表
    async getAlertNameOptions() {
      this.SearchAlertName.loading = true
      const res = await alertNamesList()
      if (res.result) {
        this.SearchAlertName.options = res.result.map(item => {
          return {
            label: item,
            value: item
          }
        })
      }
      this.SearchAlertName.loading = false
    },
    // 获取申请人下列表
    async getUserOptions() {
      this.SearchUser.loading = true
      const res = await applicantList()
      if (res.result) {
        this.SearchUser.options = res.result.map(item => {
          return {
            label: item,
            value: item
          }
        })
      }
      this.SearchUser.loading = false
    },
    // 查询
    searchHandler(params = {}) {
      this.$refs.formData.validate(valid => {
        if (!valid) return
        params = Object.assign({}, params, this.formData)
        this.loadTabledata(params)
      })
    }
  }
}
