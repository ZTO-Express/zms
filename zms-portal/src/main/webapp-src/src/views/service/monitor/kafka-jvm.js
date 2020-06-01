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
  queryHeapMemoryUsage,
  queryNonHeapMemoryUsage,
  queryG1OldGen,
  queryG1OldGenPeak,
  queryG1EdenSpace,
  queryG1EdenSpacePeak,
  queryG1OldGeneration,
  queryG1YoungGeneration,
  queryNetworkProcessorAvgIdlePercent,
  queryOperatingSystem,
  queryMetaspace,
  queryMetaspacePeak,
  queryGV1SurvivorSpace,
  queryG1SurvivorSpacePeak,
  queryCompressedClassSpace,
  queryCompressedClassSpacePeak,
  queryCodecache,
  queryCodecachePeek,
  queryThreadInfo,
  queryDirectInfo,
  queryMappedInfo
} from '@/api/broker'
export default {
  data() {
    return {
      kafkaJvmOrderList: [],
      kafkaJvmAlreadyList: [],
      kafkaJvmFilterOptions: {},
      kafkaJvmFilterValue: {},
      kafkaJvmBrokerNameList: [],
      kafkaJvmBrokerOriginData: {},
      kafkaJvmMetricChart: {},
      defaultViewJvmMoreTimes: 1,
      defaultRequestkafkaJvmList: ['HeapMemoryUsage', 'NonHeapMemoryUsage', 'G1OldGen', 'G1OldGenPeak'],
      viewJvmMoreTimes: 1,
      requestkafkaJvmList: [],
      kafkaJvmList: [
        'HeapMemoryUsage',
        'NonHeapMemoryUsage',
        'G1OldGen',
        'G1OldGenPeak',
        'G1EdenSpace',
        'G1EdenSpacePeak',
        'G1OldGeneration',
        'G1YoungGeneration',
        'NetworkProcessorAvgIdlePercent',
        'OperatingSystem',
        'Metaspace',
        'MetaspacePeak',
        'GV1SurvivorSpace',
        'G1SurvivorSpacePeak',
        'CompressedClassSpace',
        'CompressedClassSpacePeak',
        'Codecache',
        'CodecachePeek',
        'ThreadInfo',
        'DirectInfo',
        'MappedInfo'
      ],
      jvmFunlist: {
        HeapMemoryUsage: queryHeapMemoryUsage,
        NonHeapMemoryUsage: queryNonHeapMemoryUsage,
        G1OldGen: queryG1OldGen,
        G1OldGenPeak: queryG1OldGenPeak,
        G1EdenSpace: queryG1EdenSpace,
        G1EdenSpacePeak: queryG1EdenSpacePeak,
        G1OldGeneration: queryG1OldGeneration,
        G1YoungGeneration: queryG1YoungGeneration,
        NetworkProcessorAvgIdlePercent: queryNetworkProcessorAvgIdlePercent,
        OperatingSystem: queryOperatingSystem,
        Metaspace: queryMetaspace,
        MetaspacePeak: queryMetaspacePeak,
        GV1SurvivorSpace: queryGV1SurvivorSpace,
        G1SurvivorSpacePeak: queryG1SurvivorSpacePeak,
        CompressedClassSpace: queryCompressedClassSpace,
        CompressedClassSpacePeak: queryCompressedClassSpacePeak,
        Codecache: queryCodecache,
        CodecachePeek: queryCodecachePeek,
        ThreadInfo: queryThreadInfo,
        DirectInfo: queryDirectInfo,
        MappedInfo: queryMappedInfo
      }
    }
  },
  computed: {
    loadedkafkaJvmList() {
      return this.kafkaJvmList.slice(0, this.viewJvmMoreTimes * 4)
    },
    joinKafkaJvmAlreadyList() {
      return this.kafkaJvmAlreadyList.join()
    }
  },
  watch: {
    // 监听kafkaJvmAlreadyList数据变化，变化后发起请求
    joinKafkaJvmAlreadyList() {
      // 判断当前已渲染数量是否为全部
      if (this.kafkaJvmAlreadyList.length === this.requestkafkaJvmList.length && this.requestkafkaJvmList.length > 0) {
        this.requestkafkaJvmList = []
        return
      }
      if (this.kafkaJvmAlreadyList.length === 0) return
      this.loadKafkaJvm(this.requestkafkaJvmList[this.kafkaJvmAlreadyList.length])
    }
  },
  methods: {
    async initKafkaJvm() {
      this.viewJvmMoreTimes = this.defaultViewJvmMoreTimes
      this.kafkaJvmOrderList = []
      this.kafkaJvmAlreadyList = []
      await this.getKafkaJvmName()
      this.requestkafkaJvmList = this.defaultRequestkafkaJvmList
      this.kafkaJvmBrokerNameList.length && this.loadKafkaJvm(this.requestkafkaJvmList[0])
    },
    // 获取kafka节点
    async getKafkaJvmName() {
      const { result } = await queryKafkaBrokerList({ envId: this.envId, clusterName: this.clusterName })
      if (this.brokerName) {
        this.kafkaJvmBrokerNameList = result
          .filter(item => {
            if (this.clusterType === 'KAFKA') {
              return this.hostIp == item.value
            } else {
              return this.brokerName == item.value
            }
          })
          .map(item => item.value)
      } else {
        this.kafkaJvmBrokerNameList = result.map(item => item.value)
      }
      //初始化统计图数据
      const _legend = this.kafkaJvmBrokerNameList.map(item => {
        return {
          name: item,
          attr: item
        }
      })
      this.kafkaJvmList.forEach(item => {
        this.kafkaJvmMetricChart = Object.assign({}, this.kafkaJvmMetricChart, {
          [item]: {
            title: item,
            data: [],
            legend: _legend
          }
        })
      })
    },
    async loadKafkaJvm(nowRequest) {
      const params = {
        envId: this.envId,
        clusterName: this.clusterName,
        beginTime: this.time[0],
        endTime: this.time[1],
        brokerNames: this.kafkaJvmBrokerNameList.join(',')
      }
      this.getQuerykafkaJvmMetrics(params, nowRequest)
    },

    async getQuerykafkaJvmMetrics(params, target) {
      if (!target) {
        return
      }
      const { result } = await this.jvmFunlist[target](params)
      this.kafkaJvmOrderList.push(target)
      result.forEach(item => {
        item.filterName = item.name
        item.name = item.brokerName
      })
      // result.filter(item => item.filterName)
      const options = [...new Set(result.map(item => item.filterName))]
      this.kafkaJvmFilterOptions = Object.assign({}, this.kafkaJvmFilterOptions, {
        [target]: options
      })
      this.kafkaJvmBrokerOriginData[target] = this.kafkaJvmMetricChart[target].data.concat(
        this.generateKafkaChartData(result, 'filterName')
      )
      if (options.length) {
        this.kafkaJvmFilterValue = Object.assign({}, this.kafkaJvmFilterValue, {
          [target]: options[0]
        })
        Object.assign(this.kafkaJvmMetricChart[target], {
          data: this.kafkaJvmBrokerOriginData[target].filter(vv => vv.filterName === options[0])
        })
      } else {
        Object.assign(this.kafkaJvmMetricChart[target], {
          data: this.kafkaJvmBrokerOriginData[target]
        })
      }
    },
    // 下拉框选中
    handleJvmChange(val, target) {
      Object.assign(this.kafkaJvmMetricChart[target], {
        data: this.kafkaJvmBrokerOriginData[target].filter(vv => vv.filterName === val)
      })
    },
    viewKafkaJvmMore() {
      if (this.requestkafkaJvmList.length > 0) {
        return
      }
      this.loadViewMoreKafkaJvmData()
    },
    loadViewMoreKafkaJvmData() {
      let len = 4
      if (this.viewJvmMoreTimes * 4 > this.kafkaJvmList.length) {
        len = (this.viewJvmMoreTimes * 4) % this.kafkaJvmList.length
      }
      this.requestkafkaJvmList = this.kafkaJvmList.slice(this.viewJvmMoreTimes * 4, this.viewJvmMoreTimes * 4 + len)
      this.kafkaJvmAlreadyList = []
      this.viewJvmMoreTimes++
      this.loadKafkaJvm(this.requestkafkaJvmList[0])
    }
  }
}
