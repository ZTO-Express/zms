<template>
  <div class="l-page l-page--flex">
    <div class="service_add_header">添加服务节点到{{ serverName }}</div>
    <div class="l-page__content">
      <div class="content--padding">
        <div class="content__body">
          <add-instance
            ref="addInstance"
            v-show="activeTabName == 'instance'"
            :env-id="envId"
            :server-type="serverType"
            :instance-type="serverInstanceTypeMap[serverType]"
            @reloadServiceConfig="reloadServiceConfig"
          ></add-instance>
          <add-service
            ref="addService"
            v-if="addServiceFlg"
            v-show="activeTabName == 'service'"
            :env-id="envId"
            :server-type="serverType"
            :instance-type="instanceType"
          ></add-service>
          <add-run
            ref="addRun"
            v-if="addRunFlg"
            v-show="activeTabName == 'run'"
            :env-id="envId"
            :server-id="serverId"
            :server-type="serverType"
            :server-name="serverName"
            :property-list="propertyList"
            :instance-list="instanceList"
            @runComplete="runComplete"
          ></add-run>
          <add-complete ref="addComplete" v-if="addCompleteFlg" v-show="activeTabName == 'complete'"></add-complete>
        </div>
        <div class="left-button">
          <el-button style="width: 68px;" :disabled="activeTabName == 'run'" size="small" @click="backService">
            返回
          </el-button>
          <el-button
            style="margin-left: 525px;"
            :disabled="nextDisable"
            size="small"
            type="primary"
            @click="nextService"
          >
            {{ this.activeTabName == 'run' ? '完成' : '下一步' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
// 添加服务实例
import addInstance from '../add/add-instance'
// 添加服务配置
import addService from '../add/add-service'
// 运行服务实例
import addRun from '../add/add-run'
// 添加服务实例完成
import addComplete from '../add/add-complete'

export default {
  name: 'serviceAddHome',
  mixins: [common, baseStructure],
  components: {
    addService,
    addInstance,
    addRun,
    addComplete
  },
  data() {
    return {
      envId: '',
      serviceId: '',
      nextDisable: false,
      activeTabName: 'instance',
      addServiceFlg: false,
      addRunFlg: false,
      addCompleteFlg: false,
      serverType: '',
      serverName: '',
      propertyList: [],
      instanceList: [],
      instanceType: [],
      serverInstanceTypeMap: {
        ZMSCollector: ['INSTANCE'],
        ZMSAlert: ['INSTANCE'],
        Kafka: ['BROKER'],
        Rocketmq: ['NAMESVR', 'BROKER'],
        Zookeeper: ['INSTANCE'],
        InfluxDB: ['INSTANCE'],
        ZMSBackupCluster: ['INSTANCE']
      }
    }
  },
  created() {
    // 首页跳转
    this.envId = this.$route.params.envId
    this.serverName = this.$route.params.serverName
    this.serverId = this.$route.params.serverId
    this.serverType = this.$route.params.serverType
  },
  methods: {
    reloadServiceConfig() {
      if (this.addServiceFlg) {
        this.$refs.addService.loadServiceInstanceConfig(this.$refs.addInstance.serviceAddInstanceForm.instanceType)
      }
    },
    runComplete() {
      this.nextDisable = false
    },
    nextService() {
      if (this.activeTabName === 'instance') {
        const validateFlg = this.$refs.addInstance.validInstanceData()
        if (!validateFlg) {
          return
        }
        this.instanceList = this.$refs.addInstance.serviceAddInstanceForm.instanceList
        this.instanceType = this.$refs.addInstance.serviceAddInstanceForm.instanceType
        this.addServiceFlg = true
        this.activeTabName = 'service'
      } else if (this.activeTabName === 'service') {
        const validateFlg = this.$refs.addService.serviceInstanceConfigSave()
        if (!validateFlg) {
          return
        }
        this.propertyList = this.$refs.addService.serviceConfigData
        this.addRunFlg = true
        this.activeTabName = 'run'
        this.nextDisable = true
      } else if (this.activeTabName === 'run') {
        // this.addCompleteFlg = true
        // this.activeTabName = 'complete'
        this.$router.push({ name: 'serviceHome', params: { id: this.serverId, tab: 'instance' } })
      } else if (this.activeTabName === 'complete') {
        this.$router.push({ name: 'serviceHome', params: { id: this.serverId, tab: 'instance' } })
      }
    },
    backService() {
      if (this.activeTabName === 'instance') {
        this.$router.go(-1)
      } else if (this.activeTabName === 'service') {
        this.activeTabName = 'instance'
      } else if (this.activeTabName === 'run') {
        this.activeTabName = 'service'
      } else if (this.activeTabName === 'complete') {
        this.activeTabName = 'run'
      }
    }
  }
}
</script>
<style lang="scss">
.service_add_header {
  font-size: 20px;
  text-align: center;
  background: #fff;
  padding: 15px 20px 10px;
  margin: 20px;
}
.left-button {
  margin-left: 405px;
}
.service_add_bottom {
  background: WhiteSmoke;
  padding: 6px 20px;
}
</style>
