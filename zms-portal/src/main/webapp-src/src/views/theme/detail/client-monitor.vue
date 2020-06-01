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
    <!-- 发送耗时分布（单位毫秒） -->
    <chart v-bind="GLOBAL.chartConfig" :loading="loading.distributeRate" :chartData="[distributeRate]" />
    <!-- 发送消息体大小分布（单位KB） -->
    <chart v-bind="GLOBAL.chartConfig" :loading="loading.msgSize" :chartData="[msgSize]" />
  </div>
</template>

<script>
import chart from '@/modules/components/chart'
import {
  getClientIp,
  clientCostRate,
  clientSuccessRate,
  clientFailRate,
  clientDistributeRate,
  clientMsgSize
} from '@/api/theme'
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
    envId: [String, Number]
  },
  data() {
    return {
      time: [],
      clientIps: [],
      ip: '',
      costRate: {
        title: '发送耗时指标',
        data: [],
        legend: [
          { name: 'percent95', attr: 'percent95' },
          { name: 'percent999', attr: 'percent999' }
        ]
      },
      successRate: {
        title: '发送成功速率指标',
        data: [],
        legend: [
          { name: 'mean', attr: 'mean' },
          { name: 'min1rate', attr: 'min1rate' },
          { name: 'min5rate', attr: 'min5rate' },
          { name: 'min15rate', attr: 'min15rate' }
        ]
      },
      failRate: {
        title: '发送失败速率指标',
        data: [],
        legend: [
          { name: 'mean', attr: 'mean' },
          { name: 'min1rate', attr: 'min1rate' },
          { name: 'min5rate', attr: 'min5rate' },
          { name: 'min15rate', attr: 'min15rate' }
        ]
      },
      distributeRate: {
        title: '发送耗时分布（单位毫秒）',
        data: [],
        legend: [
          { name: 'less1', attr: 'less1' },
          { name: 'less5', attr: 'less5' },
          { name: 'less10', attr: 'less10' },
          { name: 'less50', attr: 'less50' },
          { name: 'less100', attr: 'less100' },
          { name: 'less500', attr: 'less500' },
          { name: 'less1000', attr: 'less1000' },
          { name: 'more1000', attr: 'more1000' }
        ]
      },
      msgSize: {
        title: '发送消息体大小分布（单位KB）',
        data: [],
        legend: [
          { name: 'less1', attr: 'less1' },
          { name: 'less5', attr: 'less5' },
          { name: 'less10', attr: 'less10' },
          { name: 'less50', attr: 'less50' },
          { name: 'less100', attr: 'less100' },
          { name: 'less500', attr: 'less500' },
          { name: 'less1000', attr: 'less1000' },
          { name: 'more1000', attr: 'more1000' }
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
  methods: {
    // 获取时间范围
    getTimeRange(value) {
      this.time = value
    },
    getClientData(value) {
      this.ip = value
      this.loadData()
    },
    // 获取ips
    async getIp() {
      this.costRate.data = []
      this.successRate.data = []
      this.failRate.data = []
      this.clientIps = []
      this.distributeRate.data = []
      this.msgSize.data = []
      const params = {
        clientName: this.topicName,
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

      this.clientIps.length == 0 ? (this.ip = '') : (this.ip = this.clientIps[0].value)
      this.loadData()
    },
    loadData() {
      // 无ip 不请求
      if (!this.ip) return
      const params = {
        clientName: this.topicName,
        beginTime: this.time[0],
        endTime: this.time[1],
        ip: this.ip,
        envId: this.envId
      }
      this.getClientCostRate(params)
      this.getClientSuccessRate(params)
      this.getClientFailRate(params)
      this.getClientDistributeRate(params)
      this.getClientMsgSize(params)
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
    // 发送消耗指标
    async getClientCostRate(params) {
      this.loading.costRate = true
      const { result } = await clientCostRate(params)
      this.loading.costRate = false
      this.costRate.data = this.generateChartData(result)
    },
    // 发送成功速率指标
    async getClientSuccessRate(params) {
      this.loading.successRate = true
      const { result } = await clientSuccessRate(params)
      this.loading.successRate = false
      this.successRate.data = this.generateChartData(result)
    },
    // 发送失败速率
    async getClientFailRate(params) {
      this.loading.failRate = true
      const { result } = await clientFailRate(params)
      this.loading.failRate = false
      this.failRate.data = this.generateChartData(result)
    },
    // 发送耗时分布
    async getClientDistributeRate(params) {
      this.loading.distributeRate = true
      const { result } = await clientDistributeRate(params)
      this.loading.distributeRate = false
      this.distributeRate.data = this.generateChartData(result)
    },
    // 发送消息体大小分布（单位KB）
    async getClientMsgSize(params) {
      this.loading.msgSize = true
      const { result } = await clientMsgSize(params)
      this.loading.msgSize = false
      this.msgSize.data = this.generateChartData(result)
    }
  }
}
</script>
