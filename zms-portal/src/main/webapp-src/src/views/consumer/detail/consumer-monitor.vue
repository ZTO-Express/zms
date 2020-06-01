<!-- 消费组监控 -->
<template>
  <div class="l-chart-container" v-if="initflg">
    <!-- 时间选择 -->
    <time-range @change="getTimeRange" />
    <div class="l-table-list">
      <div class="l-table-box">
        <h3>consumerProgress</h3>
        <table-pagination v-bind="maintable"></table-pagination>
      </div>
    </div>
    <chart v-bind="GLOBAL.chartConfig" :loading="loading.consumerTps" :chartData="[consumerTps]" />
    <chart v-bind="GLOBAL.chartConfig" :loading="loading.latencyMsg" :chartData="[latencyMsg]" />
    <chart v-bind="GLOBAL.chartConfig" :loading="loading.lastConsumingTime" :chartData="[lastConsumingTime]" />
  </div>
</template>

<script>
import chart from '@/modules/components/chart'
import { topicMonitorTps, consumerMonitorTps, consumerZmsRegister } from '@/api/consumer'
import { util } from '@/utils/util'
import timeRange from '@/modules/components/time-range'
import { baseStructure } from '@/mixins/index'
// import moment from 'moment'
export default {
  name: 'consumerMonitor',
  mixins: [baseStructure],
  components: {
    chart,
    timeRange
  },
  props: {
    initflg: {
      type: Boolean,
      default: false
    },
    consumerId: Number,
    topicName: String,
    clusterName: String,
    envId: [String, Number],
    consumerName: String
  },
  data() {
    return {
      // 详情table 配置
      maintable: {
        ref: 'maintable',
        isPagination: false,
        columns: [
          { type: 'index', width: 40 },
          { prop: 'zmsIP', label: 'Client IP' },
          { prop: 'instanceName', label: 'Name' },
          { prop: 'zmsVersion', label: 'Sdk Version' },
          { prop: 'startUpTime', label: 'StartUp Time' },
          { prop: 'threadLocalRandomInt', label: 'Random Num' }
        ],
        data: [],
        loading: false
      },
      // 消费组tps
      consumerTps: {
        title: 'tps',
        data: [],
        legend: [
          { name: 'topic_tps', attr: 'tps' },
          { name: 'consumer_tps', attr: 'consumetps', color: '#2fc25a' }
        ] //图例
      },
      latencyMsg: {
        title: 'latency',
        data: [],
        legend: [{ name: 'latency', attr: 'latency' }]
      },
      lastConsumingTime: {
        title: 'lastConsumingTime',
        data: [],
        legend: [{ name: 'lastConsumingTime', attr: 'lastConsumingTime' }]
      },
      loading: {
        consumerTps: false,
        latencyMsg: false,
        lastConsumingTime: false
      },
      time: []
    }
  },
  watch: {
    time() {
      this.initflg && this.loadData()
    },
    envId() {
      this.time && this.initflg && this.loadData()
    }
  },
  methods: {
    // 获取时间范围
    getTimeRange(value) {
      this.time = value
    },
    loadData() {
      const params = {
        topicName: this.topicName,
        clusterName: this.clusterName,
        beginTime: util.timestamp(this.time[0]),
        endTime: util.timestamp(this.time[1]),
        envId: this.envId,
        name: this.consumerName,
        consumerId: this.consumerId
      }
      this.getConsumerTps(params)
      this.getConsumerZmsRegister(params)
    },
    // chart data 数据生成
    generateChartData(result) {
      const data = []
      // 时间按从小到大排序、筛选数据
      util.dataSort(result).forEach(item => {
        data.push({
          name: item.name,
          x: util.timeToStr(item.timestamp * 1000, 4),
          value: item.value
        })
      })
      return data
    },
    async getConsumerZmsRegister(params) {
      this.maintable.loading = true
      const res = await consumerZmsRegister(params)
      res.result.forEach(item => {
        const { startUpTime } = item
        Object.assign(item, {
          startUpTime: startUpTime == null ? '' : util.timeToStr(new Date(startUpTime), 3)
        })
        return item
      })
      if (res.result) {
        this.maintable.data = res.result
        this.maintable.loading = false
      }
    },
    // consumer tps
    async getConsumerTps(params) {
      this.loading.consumerTps = true
      const res = await consumerMonitorTps(params)
      const result = res.result
      if (result == undefined) {
        return
      }
      let data = []
      let combineResult = []
      if (result && Array.isArray(result)) {
        combineResult = combineResult.concat(
          result
            .filter(item => item.name === 'tps')
            .map(item => {
              item.name = 'consumetps'
              return item
            })
        )
        this.latencyMsg.data = this.generateChartData(result)
        this.lastConsumingTime.data = this.generateChartData(result)
      }
      data = await this.getTopicTps(params, data, combineResult)
      this.loading.consumerTps = false
      this.consumerTps.data = data
    },
    //getConsumerTps
    async getTopicTps(params, data, combineResult) {
      const res = await topicMonitorTps(params)
      const result = res.result

      if (result && Array.isArray(result)) {
        combineResult = combineResult.concat(result)
      }
      // 时间按从小到大排序、筛选数据
      util.dataSort(combineResult).forEach(item => {
        data.push({
          name: item.name,
          x: util.timeToStr(item.timestamp * 1000, 4),
          value: item.value
        })
      })
      // this.resulthandle({
      //   result: util.dataSort(combineResult),
      //   timeunit: 1000,
      //   timeformat: 'MM/DD HH:mm:ss',
      //   yname: ['tps', 'consumetps'],
      //   obj: this.consumerTps
      // })
      return data
    }
  }
}
</script>
