<template>
  <div class="l-table-list">
    <div class="l-table-box">
      <h3>主题详情</h3>
      <table-pagination v-bind="themeDetailTable"></table-pagination>
    </div>
    <div class="l-table-box">
      <h3>消费组信息</h3>
      <table-pagination v-bind="consumerGroupTable"></table-pagination>
    </div>
  </div>
</template>
<script>
import { baseStructure } from '@/mixins/index'

export default {
  name: 'themeDetailRocketmq',
  mixins: [baseStructure],
  props: {
    result: Object
  },
  data() {
    return {
      // 主题详情table 配置
      themeDetailTable: {
        isPagination: false,
        columns: [
          { type: 'index', width: 40 },
          { prop: 'brokerName', label: 'Broker Name' },
          { prop: 'qid', label: 'QID' },
          { prop: 'minOffset', label: 'MinOffset' },
          { prop: 'maxOffset', label: 'MaxOffset' },
          { prop: 'lastUpdated', label: 'LastUpdate' }
        ],
        data: [],
        loading: false
      },
      consumerGroupTable: {
        isPagination: false,
        columns: [
          { type: 'index', width: 40 },
          { prop: 'topic', label: 'topic' },
          { prop: 'consumerGroup', label: 'consumerGroup' },
          { prop: 'accumulation', label: 'accumulation' },
          { prop: 'inTPS', label: 'inTPS' },
          { prop: 'outTPS', label: 'outTPS' },
          { prop: 'inMsg24Hour', label: 'inMsg24Hour' },
          { prop: 'outMsg24Hour', label: 'outMsg24Hour' }
        ],
        data: [],
        loading: false
      }
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
      this.themeDetailTable.data = this.result.topicStatusDtoList
      this.consumerGroupTable.data = this.result.statsAllResultDtoList
    }
  }
}
</script>
