<template>
  <div class="l-table-list">
    <div class="l-table-box">
      <h3>consumerProgress</h3>
      <span style="margin-left: 5px" v-if="diffTotal">DiffTotal:{{ diffTotal }}</span>
      <span style="margin-left: 20px" v-if="diffTotal && consumeTps">ConsumeTps:{{ consumeTps }}</span>
      <span style="margin-left: 5px" v-if="!diffTotal && consumeTps">ConsumeTps:{{ consumeTps }}</span>
      <table-pagination v-bind="consumerProgressTable"></table-pagination>
    </div>
    <div class="l-table-box">
      <h3>consumerZmsRegister</h3>
      <table-pagination v-bind="consumerZmsRegisterTable"></table-pagination>
    </div>
    <div class="l-table-box">
      <h3>consumerStatus</h3>
      <table-pagination v-bind="consumerStatusTable"></table-pagination>
    </div>
  </div>
</template>
<script>
import { baseStructure } from '@/mixins/index'

export default {
  name: 'consumerDetailRocketmq',
  mixins: [baseStructure],
  props: {
    result: Object
  },
  data() {
    return {
      // 主题详情table 配置
      consumerProgressTable: {
        isPagination: false,
        columns: [
          { type: 'index', width: 40 },
          { prop: 'topic', label: 'Topic' },
          { prop: 'brokerName', label: 'Broker Name' },
          { prop: 'qid', label: 'QID' },
          { prop: 'brokerOffset', label: 'Broker Offset' },
          { prop: 'consumerOffset', label: 'Consumer Offset' },
          { prop: 'clientIP', label: 'Client IP' },
          { prop: 'diff', label: 'Diff' },
          { prop: 'lastTime', label: 'LastTime' }
        ],
        data: [],
        loading: false
      },
      consumerZmsRegisterTable: {
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
      consumerStatusTable: {
        isPagination: false,
        columns: [
          { type: 'index', width: 40 },
          { prop: 'clientId', label: 'Client Id' },
          { prop: 'clientAddr', label: 'Client Addr' },
          { prop: 'language', label: 'Language' },
          { prop: 'version', label: 'Version' }
        ],
        data: [],
        loading: false
      },
      diffTotal: 0,
      consumeTps: 0
    }
  },
  watch: {
    result: {
      deep: true,
      handler() {
        this.generateTableData()
      }
    }
  },
  created() {
    this.generateTableData()
  },
  methods: {
    generateTableData() {
      this.diffTotal = this.result.diffTotal
      this.consumeTps = this.result.consumeTps
      this.consumerProgressTable.data = this.result.consumerProgressList
      this.consumerZmsRegisterTable.data = this.result.consumerZmsRegisterList
      this.consumerStatusTable.data = this.result.consumerStatusList
    }
  }
}
</script>
