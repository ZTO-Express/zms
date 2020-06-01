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

import { queryRtTimeBrokerNames, queryRtTimeMetrics } from '@/api/broker'
export default {
  data() {
    return {
      rtBrokerNameList: [],
      rtTimeMetrics: {},
      rtOrderList: [],
      rtAlreadyList: []
    }
  },
  computed: {
    joinRtAlreadyList() {
      return this.rtAlreadyList.join()
    }
  },
  watch: {
    // 监听rtAlreadyList数据变化，变化后发起请求
    joinRtAlreadyList() {
      // 判断当前已渲染数量是否为全部
      if (this.rtAlreadyList.length === this.rtBrokerNameList.length) return
      if (this.rtAlreadyList.length === 0) return
      this.loadRtTimeMetrics(this.rtBrokerNameList[this.rtAlreadyList.length])
    }
  },
  methods: {
    async initRt() {
      this.rtOrderList = []
      this.rtAlreadyList = []
      await this.getRtBrokerName()
      this.rtBrokerNameList.length && this.loadRtTimeMetrics(this.rtBrokerNameList[0])
    },
    // 获取RT节点列表
    async getRtBrokerName() {
      const { result } = await queryRtTimeBrokerNames({ envId: this.envId, clusterName: this.clusterName })
      if (this.brokerName) {
        this.rtBrokerNameList = result
          .filter(item => {
            if (this.clusterType === 'KAFKA') {
              const strs = this.hostIp.split('.')
              return this.clusterName + '_' + strs[2] + '_' + strs[3] == item.value
            } else {
              return this.brokerName == item.value
            }
          })
          .map(item => item.value)
      } else {
        this.rtBrokerNameList = result.map(item => item.value)
      }
      //初始化统计图数据
      this.rtBrokerNameList.forEach(brokerName => {
        Object.assign(this.rtTimeMetrics, {
          [brokerName]: {
            title: '',
            data: [],
            legend: [
              { name: 'result值', attr: 'result' },
              { name: 'rt值', attr: 'rt' }
            ]
          }
        })
      })
    },
    // 获取RT统计数据
    async loadRtTimeMetrics(brokerName) {
      const params = {
        envId: this.envId,
        clusterName: this.clusterName,
        beginTime: this.time[0],
        endTime: this.time[1],
        brokerName
      }
      this.getQueryRtTimeMetrics(params, brokerName)
    },
    async getQueryRtTimeMetrics(params, target) {
      const { result } = await queryRtTimeMetrics(params)
      this.rtOrderList.push(params.brokerName)
      Object.assign(this.rtTimeMetrics[target], {
        title: params.brokerName,
        data: this.generateChartData(result)
      })
    }
  }
}
