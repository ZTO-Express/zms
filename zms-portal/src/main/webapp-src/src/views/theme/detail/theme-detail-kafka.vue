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
    <div class="l-table-box">
      <h3>主题配置信息</h3>
      <table-pagination v-bind="themeConfigTable"></table-pagination>
    </div>
  </div>
</template>
<script>
import { baseStructure } from '@/mixins/index'

export default {
  name: 'themeDetailKafka',
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
          { prop: 'partition', label: 'partition' },
          { prop: 'offset', label: 'offset' },
          { prop: 'leader', label: 'leader' },
          { prop: 'isr', label: 'isr' },
          { prop: 'replicas', label: 'replicas' }
        ],
        data: [],
        loading: false
      },
      // 消费组信息table 配置
      consumerGroupTable: {
        isPagination: false,
        columns: [
          { type: 'index', width: 40 },
          { prop: 'value', label: 'consumer' }
        ],
        data: [],
        loading: false
      },
      // 主题配置信息table 配置
      themeConfigTable: {
        isPagination: false,
        columns: [
          { type: 'index', width: 40 },
          { prop: 'object1', label: 'key' },
          { prop: 'object2', label: 'value' }
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
      this.themeDetailTable.data = this.result.topicInfos
      this.consumerGroupTable.data = this.result.consumers
      this.themeConfigTable.data = this.result.topicConfigs
    }
  }
}
</script>
