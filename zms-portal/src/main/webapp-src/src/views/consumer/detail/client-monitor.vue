<!-- 主题监控 -->
<template>
  <div class="l-chart-container" v-if="initflg">
    <!-- 时间选择 -->
    <time-range @change="getTimeRange" style="float:right" />
    <div class="condition">
      <label>Client IP</label>
      <el-select v-bind="GLOBAL.select" v-model="ip" placeholder="请选择" @change="getClientData">
        <el-option v-for="item in clientIps" :key="item.value" :label="item.label" :value="item.value"></el-option>
      </el-select>
    </div>
    <!-- 发送耗时指标 -->
    <chart v-bind="GLOBAL.chartConfig" :loading="loading.costRate" :chartData="[costRate]" />
    <!-- 发送时成功速率指标 -->
    <chart v-bind="GLOBAL.chartConfig" :loading="loading.successRate" :chartData="[successRate]" />
    <!-- 发送失败速率指标 -->
    <chart v-bind="GLOBAL.chartConfig" :loading="loading.failRate" :chartData="[failRate]" />
  </div>
</template>

<script>
import chart from '@/modules/components/chart'
import {
  getClientIp,
  queryConsumerUserCostTimeMs,
  queryConsumeSuccessRate,
  queryConsumeFailureRate
} from '@/api/consumer'
import { util } from '@/utils/util'
import timeRange from '@/modules/components/time-range'

export default {
  name: 'consumerMonitor',
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
    consumerName: String,
    envId: [String, Number]
  },
  data() {
    return {
      time: [],
      clientIps: [],
      ip: '',
      costRate: {
        title: '消费耗时指标',
        data: [],
        legend: [
          { name: 'percent95', attr: 'percent95' },
          { name: 'percent999', attr: 'percent999' }
        ]
      },
      successRate: {
        title: '消费成功速率指标',
        data: [],
        legend: [
          { name: 'mean', attr: 'mean' },
          { name: 'min1rate', attr: 'min1rate' },
          { name: 'min5rate', attr: 'min5rate' },
          { name: 'min15rate', attr: 'min15rate' }
        ]
      },
      failRate: {
        title: '消费失败速率指标',
        data: [],
        legend: [
          { name: 'mean', attr: 'mean' },
          { name: 'min1rate', attr: 'min1rate' },
          { name: 'min5rate', attr: 'min5rate' },
          { name: 'min15rate', attr: 'min15rate' }
        ]
      },
      loading: {
        costRate: false,
        successRate: false,
        failRate: false,
        distributeRate: false,
        msgSize: false
      }
    }
  },
  watch: {
    async time() {
      this.initflg && this.getIp()
    },
    envId() {
      this.time && this.initflg && this.getIp()
    }
  },
  // created() {
  //   this.getIp()
  //   this.loadData()
  // },
  methods: {
    getClientData(value) {
      this.ip = value
      this.loadData()
    },
    // 获取时间范围
    getTimeRange(value) {
      this.time = value
    },
    // 获取ips
    async getIp() {
      this.ip = ''
      this.costRate.data = []
      this.successRate.data = []
      this.failRate.data = []

      const params = {
        clientName: this.consumerName,
        beginTime: this.time[0],
        endTime: this.time[1],
        envId: this.envId
      }
      const { result } = await getClientIp(params)
      this.clientIps = result.map(item => {
        return {
          label: item.value,
          value: item.value
        }
      })
      this.clientIps.length && (this.ip = this.clientIps[0].value)
      this.loadData()
    },
    loadData() {
      // 无ip 不请求
      if (!this.ip) return
      const params = {
        clientName: this.consumerName,
        beginTime: util.timestamp(this.time[0]),
        endTime: util.timestamp(this.time[1]),
        ip: this.ip,
        envId: this.envId
      }
      this.getClientCostRate(params)
      this.getClientSuccessRate(params)
      this.getClientFailRate(params)
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
    // 消费消耗指标
    async getClientCostRate(params) {
      this.loading.costRate = true
      const { result } = await queryConsumerUserCostTimeMs(params)
      this.loading.costRate = false
      this.costRate.data = this.generateChartData(result)
    },
    // 发送成功速率指标
    async getClientSuccessRate(params) {
      this.loading.successRate = true
      const { result } = await queryConsumeSuccessRate(params)
      this.loading.successRate = false
      this.successRate.data = this.generateChartData(result)
    },
    // 发送失败速率
    async getClientFailRate(params) {
      this.loading.failRate = true
      const { result } = await queryConsumeFailureRate(params)
      this.loading.failRate = false
      this.failRate.data = this.generateChartData(result)
    }
  }
}
</script>
