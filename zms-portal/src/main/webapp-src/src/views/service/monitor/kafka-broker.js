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

import {
  queryKafkaBrokerList,
  queryBytesInPerSec,
  queryBytesOutPerSec,
  queryMessagesInPerSec,
  queryLeaderElectionRateAndTimeMs,
  queryProduceRequestsPerSec,
  queryFetchConsumerRequestsPerSec,
  queryActiveControllerCount,
  queryOfflinePartitionsCount,
  queryLeaderCount,
  queryPartitionCount,
  queryFetchConsumerTotalTimeMs,
  queryProduceTotalTimeMs,
  queryProduceRequestQueueTimeMs,
  queryFetchConsumerRequestQueueTimeMs,
  queryFailedFetchRequestsPerSec,
  queryFailedProduceRequestsPerSec,
  queryFetchFollowerRequestQueueTimeMs,
  queryFetchFollowerRequestsPerSec,
  queryFetchFollowerTotalTimeMs,
  queryIsrShrinksPerSec,
  queryBytesRejectedPerSec,
  queryLogFlushRateAndTimeMs,
  queryUncleanLeaderElectionsPerSec,
  queryUnderReplicatedPartitions,
  queryRequestHandlerAvgIdlePercent
} from '@/api/broker'
export default {
  data() {
    return {
      kafkaOrderList: [],
      kafkaAlreadyList: [],
      kafkaBrokerFilterOptions: {},
      kafkaBrokerFilterValue: {},
      kafkaBorkerNameList: [],
      kafkaBorkerOriginData: {},
      kafkaMetricChart: {},
      defaultViewMoreTimes: 1,
      defaultRequestkafkaList: ['BytesInPerSec', 'BytesOutPerSec', 'MessagesInPerSec', 'LeaderElectionRateAndTimeMs'],
      viewMoreTimes: 1,
      requestkafkaList: [],
      kafkaList: [
        'BytesInPerSec',
        'BytesOutPerSec',
        'MessagesInPerSec',
        'LeaderElectionRateAndTimeMs',
        'ProduceRequestsPerSec',
        'FetchConsumerRequestsPerSec',
        'ActiveControllerCount',
        'OfflinePartitionsCount',
        'LeaderCount',
        'PartitionCount',
        'FetchConsumerTotalTimeMs',
        'ProduceTotalTimeMs',
        'ProduceRequestQueueTimeMs',
        'FetchConsumerRequestQueueTimeMs',
        'FailedFetchRequestsPerSec',
        'FailedProduceRequestsPerSec',
        'FetchFollowerRequestQueueTimeMs',
        'FetchFollowerRequestsPerSec',
        'FetchFollowerTotalTimeMs',
        'IsrShrinksPerSec',
        'BytesRejectedPerSec',
        'LogFlushRateAndTimeMs',
        'UncleanLeaderElectionsPerSec',
        'UnderReplicatedPartitions',
        'RequestHandlerAvgIdlePercent'
      ],
      funlist: {
        BytesInPerSec: queryBytesInPerSec,
        BytesOutPerSec: queryBytesOutPerSec,
        MessagesInPerSec: queryMessagesInPerSec,
        LeaderElectionRateAndTimeMs: queryLeaderElectionRateAndTimeMs,
        ProduceRequestsPerSec: queryProduceRequestsPerSec,
        FetchConsumerRequestsPerSec: queryFetchConsumerRequestsPerSec,
        ActiveControllerCount: queryActiveControllerCount,
        OfflinePartitionsCount: queryOfflinePartitionsCount,
        LeaderCount: queryLeaderCount,
        PartitionCount: queryPartitionCount,
        FetchConsumerTotalTimeMs: queryFetchConsumerTotalTimeMs,
        ProduceTotalTimeMs: queryProduceTotalTimeMs,
        ProduceRequestQueueTimeMs: queryProduceRequestQueueTimeMs,
        FetchConsumerRequestQueueTimeMs: queryFetchConsumerRequestQueueTimeMs,
        FailedFetchRequestsPerSec: queryFailedFetchRequestsPerSec,
        FailedProduceRequestsPerSec: queryFailedProduceRequestsPerSec,
        FetchFollowerRequestQueueTimeMs: queryFetchFollowerRequestQueueTimeMs,
        FetchFollowerRequestsPerSec: queryFetchFollowerRequestsPerSec,
        FetchFollowerTotalTimeMs: queryFetchFollowerTotalTimeMs,
        IsrShrinksPerSec: queryIsrShrinksPerSec,
        BytesRejectedPerSec: queryBytesRejectedPerSec,
        LogFlushRateAndTimeMs: queryLogFlushRateAndTimeMs,
        UncleanLeaderElectionsPerSec: queryUncleanLeaderElectionsPerSec,
        UnderReplicatedPartitions: queryUnderReplicatedPartitions,
        RequestHandlerAvgIdlePercent: queryRequestHandlerAvgIdlePercent
      }
    }
  },
  computed: {
    loadedKafkaList() {
      return this.kafkaList.slice(0, this.viewMoreTimes * 4)
    },
    joinKafkaAlreadyList() {
      return this.kafkaAlreadyList.join()
    }
  },
  watch: {
    // 监听kafkaAlreadyList数据变化，变化后发起请求
    joinKafkaAlreadyList() {
      // 判断当前已渲染数量是否为全部
      if (this.kafkaAlreadyList.length === this.requestkafkaList.length) return
      if (this.kafkaAlreadyList.length === 0) return
      this.loadKafkaMetrics(this.requestkafkaList[this.kafkaAlreadyList.length])
    }
  },
  methods: {
    async initKafkaMetrics() {
      this.viewMoreTimes = this.defaultViewMoreTimes
      this.kafkaOrderList = []
      this.kafkaAlreadyList = []
      await this.getKafkaBrokerName()
      this.requestkafkaList = this.defaultRequestkafkaList
      this.kafkaBorkerNameList.length && this.loadKafkaMetrics(this.requestkafkaList[0])
    },
    // 获取kafka节点
    async getKafkaBrokerName() {
      const { result } = await queryKafkaBrokerList({ envId: this.envId, clusterName: this.clusterName })
      if (this.brokerName) {
        this.kafkaBorkerNameList = result
          .filter(item => {
            if (this.clusterType === 'KAFKA') {
              return this.hostIp == item.value
            } else {
              return this.brokerName == item.value
            }
          })
          .map(item => item.value)
      } else {
        this.kafkaBorkerNameList = result.map(item => item.value)
      }
      //初始化统计图数据
      const _legend = this.kafkaBorkerNameList.map(item => {
        return {
          name: item,
          attr: item
        }
      })
      this.kafkaList.forEach(item => {
        this.kafkaMetricChart = Object.assign({}, this.kafkaMetricChart, {
          [item]: {
            title: item,
            data: [],
            legend: _legend
          }
        })
      })
    },
    async loadKafkaMetrics(nowRequest) {
      const params = {
        envId: this.envId,
        clusterName: this.clusterName,
        beginTime: this.time[0],
        endTime: this.time[1],
        brokerNames: this.kafkaBorkerNameList.join(',')
      }
      this.getQuerykafkaBrokerMetrics(params, nowRequest)
    },

    async getQuerykafkaBrokerMetrics(params, target) {
      if (!target) {
        return
      }
      const { result } = await this.funlist[target](params)
      this.kafkaOrderList.push(target)
      result.forEach(item => {
        item.filterName = item.name
        item.name = item.brokerName
      })
      result.filter(item => item.filterName)
      const options = [...new Set(result.map(item => item.filterName))]
      this.kafkaBrokerFilterOptions = Object.assign({}, this.kafkaBrokerFilterOptions, {
        [target]: options
      })
      this.kafkaBorkerOriginData[target] = this.kafkaMetricChart[target].data.concat(
        this.generateKafkaChartData(result, 'filterName')
      )
      if (options.length) {
        this.kafkaBrokerFilterValue = Object.assign({}, this.kafkaBrokerFilterValue, {
          [target]: options[0]
        })
        Object.assign(this.kafkaMetricChart[target], {
          data: this.kafkaBorkerOriginData[target].filter(vv => vv.filterName === options[0])
        })
      } else {
        Object.assign(this.kafkaMetricChart[target], {
          data: this.kafkaBorkerOriginData[target]
        })
      }
    },
    // 下拉框选中
    handleChange(val, target) {
      Object.assign(this.kafkaMetricChart[target], {
        data: this.kafkaBorkerOriginData[target].filter(vv => vv.filterName === val)
      })
    },
    viewKafkaBrokerMore() {
      if (this.kafkaAlreadyList.length > 0 && this.kafkaAlreadyList.length < this.requestkafkaList.length) {
        return
      }
      this.loadViewMoreKafkaData()
    },
    loadViewMoreKafkaData() {
      let len = 4
      if (this.viewMoreTimes * 4 > this.kafkaList.length) {
        len = (this.viewMoreTimes * 4) % this.kafkaList.length
      }
      this.requestkafkaList = this.kafkaList.slice(this.viewMoreTimes * 4, this.viewMoreTimes * 4 + len)
      this.kafkaAlreadyList = []
      this.viewMoreTimes++
      this.loadKafkaMetrics(this.requestkafkaList[0])
    }
  }
}
