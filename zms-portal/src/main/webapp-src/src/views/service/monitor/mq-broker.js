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

import { queryMqBrokerList, queryMqBrokerMetrics } from '@/api/broker'
export default {
  data() {
    return {
      mqBrokerNameList: [],
      mqMetricChart: {},
      mqOrderList: [],
      mqAlreadyList: []
    }
  },
  computed: {
    joinMqAlreadyList() {
      return this.mqAlreadyList.join()
    }
  },
  watch: {
    // 监听mqAlreadyList数据变化，变化后发起请求
    joinMqAlreadyList() {
      // 判断当前已渲染数量是否为全部
      if (this.mqAlreadyList.length === this.mqBrokerNameList.length) return
      if (this.mqAlreadyList.length === 0) return
      this.loadMqMetrics(this.mqBrokerNameList[this.mqAlreadyList.length])
    }
  },
  methods: {
    async initMqMetrics() {
      this.mqOrderList = []
      this.mqAlreadyList = []
      await this.getMqBrokerName()
      this.mqBrokerNameList.length && this.loadMqMetrics(this.mqBrokerNameList[0])
    },
    // 获取MQ节点列表
    async getMqBrokerName() {
      const { result } = await queryMqBrokerList({ envId: this.envId, clusterName: this.clusterName })
      if (this.brokerName) {
        this.mqBrokerNameList = result
          .filter(item => {
            return this.brokerName == item.value
          })
          .map(item => item.value)
      } else {
        this.mqBrokerNameList = result.map(item => item.value)
      }
      //初始化统计图数据
      this.mqBrokerNameList.forEach(brokerName => {
        Object.assign(this.mqMetricChart, {
          [brokerName]: {
            title: '',
            data: [],
            legend: [
              { name: 'putTps', attr: 'putTps' },
              { name: 'getTotalTps', attr: 'getTotalTps' }
            ]
          }
        })
      })
    },
    // 获取MQ统计数据
    async loadMqMetrics(brokerName) {
      const params = {
        envId: this.envId,
        clusterName: this.clusterName,
        beginTime: this.time[0],
        endTime: this.time[1],
        brokerName
      }
      this.getQueryMqMetricChart(params, brokerName)
    },
    async getQueryMqMetricChart(params, target) {
      const { result } = await queryMqBrokerMetrics(params)
      this.mqOrderList.push(params.brokerName)
      Object.assign(this.mqMetricChart[target], {
        title: params.brokerName,
        data: this.generateChartData(result)
      })
    }
  }
}
