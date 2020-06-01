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
import { getConsumersByClusterId } from '@/api/consumer'
import { clusterList, applicantList } from '@/api/theme'

export default {
  data() {
    return {
      // 搜索表单参数
      formData: {
        clusterName: '', // 集群
        clusterId: '',
        name: '',
        envId: undefined,
        allDelay: false
      },
      SearchCluster: {
        options: [],
        loading: false
      },
      SearchTheme: {
        options: [],
        loading: false
      },

      SearchConsumer: {
        options: [],
        loading: false
      },
      SearchEnv: {
        options: [],
        loading: false
      },
      SearchApplicant: {
        options: [],
        loading: false
      },
      SearchStatus: {
        options: [
          { label: '待审批', value: 0 },
          { label: '已审批', value: 1 }
        ]
      }
    }
  },
  computed: {
    clusterName() {
      return this.formData.clusterName
    }
  },
  watch: {
    // 监听集群值变化，重置消费组选项值
    clusterName() {
      this.formData.name = ''
    }
  },
  methods: {
    // 获取集群下拉列表
    async getClusterOptions() {
      this.SearchCluster.loading = true
      const params = { envId: this.formData.envId }
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
    // 获取消费组下拉列表
    async getConsumerOptions() {
      this.SearchConsumer.loading = true
      const params = { clusterId: this.formData.clusterId }
      const res = await getConsumersByClusterId(params)
      if (res.result) {
        this.SearchConsumer.options = res.result.map(item => {
          return {
            label: item.name,
            value: item.name,
            ...item
          }
        })
      }
      this.SearchConsumer.loading = false
    },

    // 获取主题下拉列表
    // async getThemeOptions() {
    //   this.SearchTheme.loading = true
    //   const params = { clusterName: this.formData.clusterName }
    //   const res = await themeListByCluster(params)
    //   if (res.result) {
    //     this.SearchTheme.options = res.result.map(item => {
    //       return {
    //         label: item.name,
    //         value: item.name,
    //         ...item
    //       }
    //     })
    //   }
    //   this.SearchTheme.loading = false
    // },
    // 获取申请人下列表
    async getApplicantOptions() {
      this.SearchApplicant.loading = true
      const res = await applicantList()
      if (res.result) {
        this.SearchApplicant.options = res.result.map(item => {
          return {
            label: item,
            value: item
          }
        })
      }
      this.SearchApplicant.loading = false
    },
    // 查询
    searchHandler(params = {}) {
      this.$refs.formData.validate(valid => {
        if (!valid) return
        this.formData.envId = this.envId
        params = Object.assign({}, params, this.formData)
        this.loadTabledata(params)
      })
    }
  }
}
