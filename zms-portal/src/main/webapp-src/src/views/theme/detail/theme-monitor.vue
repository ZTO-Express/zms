<!-- 主题监控 -->
<template>
  <div class="l-chart-container" v-if="initflg">
    <!-- 时间选择 -->
    <time-range @change="getTimeRange" />
    <chart v-bind="GLOBAL.chartConfig" :loading="loading.topicTps" :chartData="[topicTps]" />
    <chart v-bind="GLOBAL.chartConfig" :loading="loading.dailyMsg" :chartData="[dailyMsg]" />
  </div>
</template>

<script>
import chart from '@/modules/components/chart'
import { themeMonitorTps, themeMonitorDailyMsg } from '@/api/theme'
import { util } from '@/utils/util'
import timeRange from '@/modules/components/time-range'

export default {
  name: 'themeMonitor',
  components: {
    chart,
    timeRange
  },
  props: {
    initflg: {
      type: Boolean,
      default: false
    },
    topicName: String,
    clusterName: String,
    envId: [String, Number]
  },
  data() {
    return {
      // 主题发送tps
      topicTps: {
        title: 'Topic发送tps',
        data: [],
        legend: [{ name: 'tps', attr: 'tps' }] //图例
      },
      dailyMsg: {
        title: 'Topic日消息量',
        data: [],
        legend: [{ name: 'offsets daily increasement', attr: 'offsets_daily_increasement' }]
      },
      loading: {
        topicTps: false,
        dailyMsg: false
      },
      time: []
    }
  },
  watch: {
    time() {
      this.initflg && this.loadData()
    },
    envId() {
      this.initflg && this.time && this.loadData()
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
        beginTime: this.time[0],
        endTime: this.time[1],
        envId: this.envId
      }
      this.getTopicTps(params)
      this.getDailyMsg(params)
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
    // topic tps
    async getTopicTps(params) {
      this.loading.topicTps = true
      const { result } = await themeMonitorTps(params)
      this.loading.topicTps = false
      this.topicTps.data = this.generateChartData(result)
    },
    // daily msg
    async getDailyMsg(params) {
      this.loading.dailyMsg = true
      const { result } = await themeMonitorDailyMsg(params)
      this.loading.dailyMsg = false
      this.dailyMsg.data = this.generateChartData(result)
    }
  }
}
</script>
