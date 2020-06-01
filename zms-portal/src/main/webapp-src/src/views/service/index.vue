<template>
  <div class="l-page l-page--flex">
    <div class="l-page__search">
      <div class="zms-service-head">
        <index-header
          v-if="headerInitflg"
          :server-id="serviceData.id"
          :server-name="serviceData.serverName"
          :env-id="serviceData.environmentId"
          :env-name="serviceData.environmentName"
        />
        <div>
          <el-tabs v-model="activeTabName" @tab-click="handleTabClick">
            <el-tab-pane label="状态" name="state" v-if="showStateTab">
              <state-monitor
                v-if="stateMonitorflg"
                :cluster-name="serviceData.serverName"
                :cluster-type="serviceData.serverType"
                :env-id="serviceData.environmentId"
              />
            </el-tab-pane>
            <el-tab-pane label="实例" name="instance"></el-tab-pane>
            <el-tab-pane label="配置" name="config" v-if="checkPerm(['admin'])"></el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
    <div class="l-page__content">
      <div class="content--padding">
        <instance-list
          v-show="activeTabName == 'instance'"
          v-if="instanceListflg"
          :server-id="serviceData.id"
          :server-name="serviceData.serverName"
          :server-type="serviceData.serverType"
          :env-id="serviceData.environmentId"
        />
        <config v-show="activeTabName == 'config'" v-if="serviceConfigflg" :server-id="serviceData.id" />
      </div>
    </div>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import indexHeader from './header/index-header'
import stateMonitor from './monitor/state-monitor'
import instanceList from './instance-list'
import config from './instance/config'
import { getServiceAndEnvById } from '@/api/service'

export default {
  name: 'serviceHome',
  mixins: [common, baseStructure],
  components: {
    indexHeader,
    stateMonitor,
    instanceList,
    config
  },
  data() {
    return {
      activeTabName: 'state',
      showStateTab: false,
      serviceData: {},
      // 状态监控
      stateMonitorflg: false,
      headerInitflg: false,
      instanceListflg: false,
      serviceConfigflg: false
    }
  },
  created() {
    this.activeTabName = this.getTab()
    // 首页跳转
    this.createdByService()
  },
  methods: {
    // 获取id
    getId() {
      return this.$route.params.id
    },
    getTab() {
      return this.$route.params.tab
    },
    async createdByService() {
      // 获取服务信息
      await this.loadServiceAndEnvById(this.getId())
      // 等待数据获取后再初始化
      this.headerInitflg = true

      this.showStateTab = this.serviceData.serverType == 'ROCKETMQ' || this.serviceData.serverType == 'KAFKA'

      if (this.activeTabName === 'state' && this.showStateTab) {
        this.stateMonitorflg = true
      } else if (this.activeTabName === 'state') {
        // 从首页点击服务跳转，不是下拉菜单的实例或配置
        this.activeTabName = 'instance'
      }
      if (this.activeTabName === 'instance') {
        this.instanceListflg = true
      }
      if (this.activeTabName === 'config') {
        this.serviceConfigflg = true
      }
    },
    async loadServiceAndEnvById(id) {
      const params = { serviceId: id }
      const res = await getServiceAndEnvById(params)
      if (res.result) {
        this.serviceData = res.result
      }
    },
    handleTabClick(tab) {
      if (tab.name === 'state') {
        this.stateMonitorflg = true
      } else if (this.activeTabName === 'instance') {
        this.instanceListflg = true
      } else {
        this.serviceConfigflg = true
      }
    }
  }
}
</script>
