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
import { envList, clusterList } from '@/api/theme'
import { queryClusters } from '@/api/cluster'
export default {
  data() {
    return {
      // 搜索表单参数
      formData: {
        envId: null,
        clusterName: '',
        brokerAddr: ''
      },
      SearchEnvId: {
        options: [],
        loading: false
      },
      SearchCluster: {
        options: [],
        loading: false
      },
      SearchBrokerAddr: {
        options: [],
        loading: false
      }
    }
  },
  computed: {
    envId() {
      return this.formData.envId
    },
    clusterName() {
      return this.formData.clusterName
    },
    brokerAddr() {
      return this.formData.brokerAddr
    }
  },
  watch: {
    envId() {
      this.formData.clusterName = ''
      this.formData.brokerAddr = ''
    },
    clusterName() {
      this.formData.brokerAddr = ''
    }
  },
  methods: {
    // 获取环境列表
    async getEnvListOptions() {
      this.SearchEnvId.loading = true
      const res = await envList()
      if (res.result) {
        this.SearchEnvId.options = res.result.map(item => {
          return {
            label: item.environmentName,
            value: item.id,
            ...item
          }
        })
        this.SearchEnvId.loading = false
      }
    },
    // 获取集群下拉列表
    async getClusterOptions() {
      if (this.formData.envId == null) {
        return
      }
      this.SearchCluster.loading = true
      const params = { envId: this.formData.envId, serviceType: 'ROCKETMQ' }
      const res = await clusterList(params)
      if (res.result) {
        this.SearchCluster.options = res.result.map(item => {
          return {
            label: item.serverName,
            value: item.serverName,
            ...item
          }
        })
        this.SearchCluster.loading = false
      }
    },
    async getBrokerAddr() {
      if (this.formData.envId == null || this.formData.clusterName == null) {
        return
      }

      this.SearchBrokerAddr.loading = true
      const params = { envId: this.formData.envId, serviceName: this.formData.clusterName, serviceType: 'ROCKETMQ' }
      const res = await queryClusters(params)
      if (res.result) {
        const options = res.result[0].serviceAddress.split(';')
        this.SearchBrokerAddr.options = options.map(item => {
          return {
            label: item,
            value: item,
            ...item
          }
        })
        this.SearchBrokerAddr.loading = false
      }
    },
    // 查询
    searchHandler() {
      this.$refs.formData.validate(valid => {
        if (!valid) return
        this.loadTabledata(this.formData)
      })
    }
  }
}
