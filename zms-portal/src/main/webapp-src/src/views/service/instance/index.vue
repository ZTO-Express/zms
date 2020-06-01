<template>
  <div class="l-page l-page--flex">
    <div class="l-page__search">
      <div class="zms-intance-head">
        <div class="content_breadcrumb">
          <el-breadcrumb separator-class="el-icon-arrow-right">
            <el-breadcrumb-item :to="{ path: '/env/' + instanceData.environmentId }">
              {{ instanceData.environmentName }}
            </el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: '/service/' + instanceData.serviceId + '/instance' }">
              {{ instanceData.serverName }}
            </el-breadcrumb-item>
            <el-breadcrumb-item>{{ instanceData.instanceName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <el-divider></el-divider>
        <div>
          <el-tabs v-model="activeTabName" @tab-click="handleTabClick">
            <el-tab-pane label="状态" name="state" v-if="showStateTab">
              <state-monitor
                :initflg="stateMonitor.flg"
                :cluster-name="instanceData.serverName"
                :cluster-type="instanceData.serverType"
                :env-id="instanceData.environmentId"
                :broker-name="instanceData.instanceName"
                :host-ip="instanceData.hostIp"
              />
            </el-tab-pane>
            <el-tab-pane label="配置" name="config" v-if="checkPerm(['admin'])"></el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
    <div class="l-page__content">
      <div class="content--padding">
        <config
          v-show="activeTabName == 'config'"
          v-if="serviceConfigflg"
          :server-id="instanceData.serviceId"
          :instance-id="instanceData.id"
        />
      </div>
    </div>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import stateMonitor from '../monitor/state-monitor'
import { getServiceAndEnvByInstanceId } from '@/api/service'
// 实例配置
import config from './config'

export default {
  name: 'instanceHome',
  mixins: [common, baseStructure],
  components: {
    stateMonitor,
    config
  },
  data() {
    return {
      activeTabName: 'state',
      showStateTab: false,
      instanceData: {},
      // 状态监控
      stateMonitor: {
        flg: false
      },
      headerInitflg: false,
      serviceConfigflg: false
    }
  },
  created() {
    //服务页面点击实例名称跳转过来
    this.createdByInstance()
  },
  methods: {
    // 获取实例id
    getId() {
      return this.$route.params.id
    },
    async createdByInstance() {
      // 获取服务信息
      await this.loadServiceAndEnvByInstanceId(this.getId())
      // 等待数据获取后再初始化
      this.headerInitflg = true
      if (
        (this.instanceData.serverType === 'ROCKETMQ' && this.instanceData.instanceType === 'BROKER') ||
        this.instanceData.serverType === 'KAFKA'
      ) {
        this.showStateTab = true
        // 集群类型，初始化监控数据
        this.stateMonitor.flg = true
      } else {
        this.activeTabName = 'config'
        this.serviceConfigflg = true
      }
    },
    async loadServiceAndEnvByInstanceId(instanceId) {
      const params = { instanceId: instanceId }
      const res = await getServiceAndEnvByInstanceId(params)
      if (res.result) {
        this.instanceData = res.result
      }
    },
    handleTabClick(tab) {
      if (tab.name === 'config') {
        this.serviceConfigflg = true
      }
    }
  }
}
</script>

<style lang="scss">
.zms-intance-head {
  .content_breadcrumb {
    margin-top: 5px;
    margin-bottom: 18px;
  }
  .el-divider {
    margin-top: 10px;
    margin-bottom: 10px;
  }
  .el-tabs__header {
    margin-bottom: 0;
  }
}
</style>
