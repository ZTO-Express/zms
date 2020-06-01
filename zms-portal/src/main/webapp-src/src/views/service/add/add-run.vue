<template>
  <div class="add_run_state">
    <run-state
      ref="runState"
      header-title="运行结果"
      type="firstRun"
      loading-text="正在运行"
      @runComplete="runComplete"
    ></run-state>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import { addService, addServiceInstance, startServiceInstance } from '@/api/service'
import runState from '../instance/run-state'

export default {
  name: 'serviceAddRun',
  mixins: [common, baseStructure],
  components: {
    runState
  },
  props: {
    envId: [Number, String],
    serverType: String,
    serverName: String,
    propertyList: Array,
    instanceList: Array,
    serverId: Number
  },
  created() {
    this.saveService()
  },
  methods: {
    checkServerType() {
      let serverType = this.serverType
      switch (serverType) {
        case 'ZMSCollector':
          return 'zms_collector'
        case 'ZMSAlert':
          return 'zms_alert'
        case 'Kafka':
          return 'kafka'
        case 'Rocketmq':
          return 'rocketmq'
        case 'Zookeeper':
          return 'zookeeper'
        case 'InfluxDB':
          return 'influxdb'
        case 'ZMSBackupCluster':
          return 'zms_backup_cluster'
        default:
          return serverType
      }
    },
    async saveService() {
      let serverType = this.checkServerType()
      const service = {
        environmentId: this.envId,
        serverName: this.serverName,
        serverType: serverType,
        id: this.serverId
      }
      const params = {
        service: service,
        instanceList: this.instanceList,
        propertyList: this.propertyList
      }
      let res
      if (this.serverId) {
        res = await addServiceInstance(params)
      } else {
        res = await addService(params)
      }
      if (!res.result) {
        this.$refs.runState.checkProcessFailState()
        return
      }
      const res1 = await startServiceInstance(res.result)
      this.$refs.runState.checkProcessState(res1.result)
    },
    runComplete() {
      this.$emit('runComplete')
    }
  }
}
</script>
<style lang="scss">
.add_run_state {
  margin: 0 auto;
  width: 800px;
}
</style>
