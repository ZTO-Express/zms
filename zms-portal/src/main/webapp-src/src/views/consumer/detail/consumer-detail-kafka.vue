<template>
  <div class="l-table-list">
    <div class="l-table-box">
      <h3>消费组详情</h3>
      <table-pagination v-bind="themeDetailTable"></table-pagination>
    </div>
  </div>
</template>
<script>
import { baseStructure } from '@/mixins/index'

export default {
  name: 'consumerDetailKafka',
  mixins: [baseStructure],
  props: {
    result: Object
  },
  data() {
    return {
      // 消费组详情table 配置
      themeDetailTable: {
        isPagination: false,
        columns: [
          { type: 'index', width: 40 },
          { prop: 'topic', label: 'topic' },
          { prop: 'partition', label: 'partition' },
          { prop: 'lag', label: 'latency', sortable: true },
          { prop: 'consumerOffset', label: 'consumerOffset' },
          { prop: 'host', label: 'host' },
          { prop: 'clientId', label: 'clientId' },
          { prop: 'consumerId', label: 'consumerId' }
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
      this.themeDetailTable.data = this.result.consumerInfos
    }
  }
}
</script>
