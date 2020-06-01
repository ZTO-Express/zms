<template>
  <div class="service-add-option">
    <el-table :data="serviceAddOptions.tableData" size="medium" border>
      <el-table-column label="服务类型" width="240" header-align="center">
        <template slot-scope="scope">
          <el-radio
            :label="scope.row.serverType"
            v-model="serviceAddOptions.selectServerType"
            @change.native="changeSelectServerType(scope.row.instanceType)"
          >
            <svg-icon :icon-class="serviceIconClass(scope.row.serverType)"></svg-icon>
            {{ scope.row.serverType }}
          </el-radio>
        </template>
      </el-table-column>
      <el-table-column prop="serverDesc" label="说明" header-align="center"></el-table-column>
    </el-table>
  </div>
</template>
<script>
export default {
  name: 'serviceAddOption',
  data() {
    return {
      serviceAddOptions: {
        selectServerType: false,
        tableData: [
          {
            serverType: 'ZMSCollector',
            serverDesc: 'zms监控指标采集',
            instanceType: ['INSTANCE']
          },
          {
            serverType: 'ZMSAlert',
            serverDesc: 'zms主题、消费组告警',
            instanceType: ['INSTANCE']
          },
          {
            serverType: 'Kafka',
            serverDesc: '高吞吐量的分布式发布订阅消息系统',
            instanceType: ['BROKER']
          },
          {
            serverType: 'Rocketmq',
            serverDesc: '分布式消息中间件，具有低延迟、高吞吐量、高可用性',
            instanceType: ['NAMESVR', 'BROKER']
          },
          {
            serverType: 'Zookeeper',
            serverDesc: 'zms集群、主题、消费组、kafka集群元数据管理',
            instanceType: ['INSTANCE']
          },
          {
            serverType: 'InfluxDB',
            serverDesc: 'zms集群、主题、消费组监控数据存储数据库',
            instanceType: ['INSTANCE']
          },
          {
            serverType: 'ZMSBackupCluster',
            serverDesc: '用于同步不同环境集群元数据',
            instanceType: ['INSTANCE']
          }
        ]
      }
    }
  },
  methods: {
    changeSelectServerType(val) {
      this.$emit('changeNextDisable', val)
    },
    serviceIconClass(serviceType) {
      switch (serviceType) {
        case 'Zookeeper':
          return 'Zookeeper'
        case 'InfluxDB':
          return 'influxDb'
        case 'ZMSAlert':
          return 'gaojing'
        case 'ZMSCollector':
          return 'caiji'
        case 'Rocketmq':
          return 'RocketMQ'
        case 'Kafka':
          return 'Kafka'
        case 'ZMSBackupCluster':
          return 'backup'
        default:
          return ''
      }
    }
  }
}
</script>
<style lang="scss">
.service-add-option {
  margin: 20px auto;
  width: 620px;
  .el-table .cell {
    font-size: 13px;
    padding: 0 20px !important;
  }
  .el-table__header th {
    padding: 8px 0;
    font-weight: bold;
  }
}
</style>
