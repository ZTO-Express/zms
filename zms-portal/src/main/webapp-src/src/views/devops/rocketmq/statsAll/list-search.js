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
import { envList, clusterList, themeListByCluster } from '@/api/theme'
import { queryApprovedConsumers } from '@/api/consumer'
export default {
  data() {
    return {
      // 搜索表单参数
      formData: {
        envId: null,
        clusterName: '',
        topicName: '', //主题
        consumerName: '' //消费组
      },
      SearchEnvId: {
        options: [],
        loading: false
      },
      SearchCluster: {
        options: [],
        loading: false
      },
      SearchTheme: {
        options: [],
        loading: false
      },
      SearchConsumerName: {
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
    topicName() {
      return this.formData.topicName
    }
  },
  watch: {
    envId() {
      this.formData.clusterName = ''
      this.formData.topicName = ''
      this.formData.consumerName = ''
    },
    clusterName() {
      this.formData.topicName = ''
      this.formData.consumerName = ''
    },
    topicName() {
      this.formData.consumerName = ''
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
    // 获取主题下拉列表
    async getThemeOptions() {
      if (this.formData.envId == null || this.formData.clusterName == null) {
        return
      }
      this.SearchTheme.loading = true
      const params = { envId: this.formData.envId, clusterType: 'ROCKETMQ', clusterName: this.formData.clusterName }
      const res = await themeListByCluster(params)
      if (res.result) {
        this.SearchTheme.options = res.result.map(item => {
          return {
            label: item.name,
            value: item.name,
            ...item
          }
        })
      }
      this.SearchTheme.loading = false
    },
    // 获取消费组下拉列表
    async getConsumerOptions() {
      if (this.formData.envId == null || this.formData.clusterName == null) {
        return
      }
      this.SearchConsumerName.loading = true
      const params = {
        envId: this.formData.envId,
        clusterName: this.formData.clusterName,
        topicName: this.formData.topicName,
        clusterType: 'ROCKETMQ'
      }
      const res = await queryApprovedConsumers(params)
      if (res.result) {
        this.SearchConsumerName.options = res.result.map(item => {
          return {
            label: item.name,
            value: item.name,
            ...item
          }
        })
        this.SearchConsumerName.loading = false
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
