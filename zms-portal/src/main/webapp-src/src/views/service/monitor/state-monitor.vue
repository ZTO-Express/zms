<template>
  <div class="l-chart-container">
    <!-- 时间选择 -->
    <time-range @change="getTimeRange" style="float: right" />
    <div class="condition">
      <el-button :plain="buttonName !== 'rt'" type="primary" size="mini" @click="handleButtonClick('rt')">
        RT指标
      </el-button>
      <el-button :plain="buttonName !== 'broker'" type="primary" size="mini" @click="handleButtonClick('broker')">
        节点详情
      </el-button>
      <el-button
        v-if="clusterType == 'KAFKA'"
        :plain="buttonName !== 'jvm'"
        type="primary"
        size="mini"
        @click="handleButtonClick('jvm')"
      >
        JVM
      </el-button>
    </div>
    <!-- RT监控 -->
    <div v-if="buttonName == 'rt'">
      <div class="l-chart-box">
        <div
          class="box"
          v-for="brokerName in rtBrokerNameList"
          :key="brokerName"
          v-loading="!rtOrderList.some(item => item === brokerName)"
        >
          <chart
            v-if="rtOrderList.some(item => item === brokerName)"
            :type="brokerName"
            @finished="
              value => {
                triggerFinish(value, 'rtAlreadyList', 'rtBrokerNameList')
              }
            "
            v-bind="GLOBAL.chartConfig"
            :chartData="[rtTimeMetrics[brokerName]]"
            :underline="false"
          />
          <div class="position" v-else></div>
        </div>
      </div>
    </div>
    <div v-if="buttonName == 'broker' && clusterType == 'ROCKETMQ' && mqBrokerNameList.length">
      <div class="l-chart-box">
        <div
          class="box"
          v-for="brokerName in mqBrokerNameList"
          :key="brokerName"
          v-loading="!mqOrderList.some(item => item === brokerName)"
        >
          <chart
            v-if="mqOrderList.some(item => item === brokerName)"
            :type="brokerName"
            @finished="
              value => {
                triggerFinish(value, 'mqAlreadyList', 'mqBrokerNameList')
              }
            "
            v-bind="GLOBAL.chartConfig"
            :chartData="[mqMetricChart[brokerName]]"
          />
          <div class="position" v-else></div>
        </div>
      </div>
    </div>
    <div v-if="buttonName == 'broker' && clusterType == 'KAFKA' && kafkaBorkerNameList.length">
      <div class="l-chart-box">
        <div
          class="box select-box"
          v-for="brokerName in loadedKafkaList"
          :key="brokerName"
          v-loading="!kafkaOrderList.some(item => item === brokerName)"
        >
          <el-select
            v-model="kafkaBrokerFilterValue[brokerName]"
            :filterable="true"
            size="mini"
            @change="
              val => {
                handleChange(val, brokerName)
              }
            "
          >
            <el-option v-for="(item, i) in kafkaBrokerFilterOptions[brokerName]" :key="i" :label="item" :value="item" />
          </el-select>
          <chart
            v-if="kafkaOrderList.some(item => item === brokerName)"
            :type="brokerName"
            @finished="
              value => {
                triggerFinish(value, 'kafkaAlreadyList', 'requestkafkaList')
              }
            "
            v-bind="GLOBAL.chartConfig"
            :chartData="[kafkaMetricChart[brokerName]]"
          />
          <div class="position" v-else></div>
        </div>
      </div>
      <div class="bottom-button" v-if="kafkaList.length > viewMoreTimes * 4">
        <el-button plain type="primary" size="small" @click="viewKafkaBrokerMore()">
          查看更多
        </el-button>
      </div>
    </div>
    <div v-if="buttonName == 'jvm' && kafkaJvmBrokerNameList.length">
      <div class="l-chart-box">
        <div
          class="box select-box"
          v-for="brokerName in loadedkafkaJvmList"
          :key="brokerName"
          v-loading="!kafkaJvmOrderList.some(item => item === brokerName)"
        >
          <el-select
            v-model="kafkaJvmFilterValue[brokerName]"
            :filterable="true"
            size="mini"
            @change="
              val => {
                handleJvmChange(val, brokerName)
              }
            "
          >
            <el-option v-for="(item, i) in kafkaJvmFilterOptions[brokerName]" :key="i" :label="item" :value="item" />
          </el-select>
          <chart
            v-if="kafkaJvmOrderList.some(item => item === brokerName)"
            :type="brokerName"
            @finished="
              value => {
                triggerFinish(value, 'kafkaJvmAlreadyList', 'requestkafkaJvmList')
              }
            "
            v-bind="GLOBAL.chartConfig"
            :chartData="[kafkaJvmMetricChart[brokerName]]"
          />
          <div class="position" v-else></div>
        </div>
      </div>
      <div class="bottom-button" v-if="kafkaJvmList.length > viewJvmMoreTimes * 4">
        <el-button plain type="primary" size="small" @click="viewKafkaJvmMore()">
          查看更多
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import chart from '@/modules/components/chart'
import rt from './rt'
import mqBroker from './mq-broker'
import kafkaBroker from './kafka-broker'
import kafkaJvm from './kafka-jvm'
import { util } from '@/utils/util'
import timeRange from '@/modules/components/time-range'

export default {
  name: 'stateMonitor',
  mixins: [rt, mqBroker, kafkaBroker, kafkaJvm],
  components: {
    chart,
    timeRange
  },
  props: {
    clusterName: String,
    clusterType: String,
    envId: [String, Number],
    brokerName: String,
    hostIp: String
  },
  data() {
    return {
      buttonName: 'rt',
      time: []
    }
  },
  watch: {
    async time() {
      this.loadData()
    }
  },
  methods: {
    // 获取时间范围
    getTimeRange(value) {
      this.time = value
    },
    handleButtonClick(name) {
      this.buttonName = name
      this.loadData()
    },
    async loadData() {
      if (this.buttonName === 'rt') {
        this.initRt()
      } else if (this.buttonName === 'jvm') {
        this.initKafkaJvm()
      } else if (this.clusterType === 'ROCKETMQ') {
        this.initMqMetrics()
      } else {
        this.initKafkaMetrics()
      }
    },
    // RT指标和MQ节点详情  chart data 数据生成
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
    // Kafka节点详情和JVM  chart data 数据生成
    generateKafkaChartData(result, filterName) {
      const data = []
      // 时间按从小到大排序、筛选数据
      util.dataSort(result).forEach(item => {
        const _vv = {
          name: item.name,
          x: util.timeToStr(item.timestamp * 1000, 4),
          value: item.value
        }
        if (filterName) {
          _vv[filterName] = item[filterName]
        }
        data.push(_vv)
      })
      return data
    },
    /**
     * 单个统计图渲染完成回调
     * @param value string 当前渲染完成项
     * @param alreadyList string 已完成渲染项data 名
     * */
    triggerFinish(value, alreadyList, requesList) {
      if (this[requesList].some(item => item === value)) {
        // 已渲染项添加
        this[alreadyList] = [...new Set([...this[alreadyList], value])]
      }
    }
  }
}
</script>
<style>
.bottom-button {
  text-align: center;
}
</style>
