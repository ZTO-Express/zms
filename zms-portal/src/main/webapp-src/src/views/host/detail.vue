<template>
  <div class="l-page detail-page">
    <div class="l-page__search" v-loading="environment.loading">
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane name="first">
          <span slot="label" @click="hostList(envId)">
            主机列表
            <i class="el-icon-arrow-right" />
          </span>

          <span slot="label">主机 : {{ form.hostName }}</span>
          <ul class="hostinfo">
            <li>主机IP: {{ form.hostIp }}</li>
            <li>主机状态: {{ form.hostStatusTxt }}</li>
            <li>上次监测时间: {{ form.lastMonitorTime }}</li>
          </ul>
        </el-tab-pane>
      </el-tabs>
      <div class="l-table-box" style="height:300px">
        <h3>服务实例</h3>
        <table-pagination v-bind="consumerProgressTable">
          <template slot="insert">
            <el-table-column v-bind="GLOBAL.column" label="服务" width="600">
              <template slot-scope="scope">
                <svg-icon :icon-class="serviceIconClass(scope.row.serverType)"></svg-icon>

                {{ scope.row.serverType }}
              </template>
            </el-table-column>
          </template>
        </table-pagination>
      </div>
    </div>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import { hostDetail } from '@/api/host'
import OpFreight from '@/modules/modal/freight/OpFreight'
// 工具方法
import { util } from '@/utils/util'
export default {
  name: 'hostDetail',
  mixins: [baseStructure, common, OpFreight],

  data() {
    return {
      envId: 0,
      activeName: 'first',
      environment: {
        list: [],
        loading: false
      },
      env: undefined,
      detail: {
        type: '',
        loading: false,
        result: {}
      },
      form: {
        id: '',
        hostIp: '',
        hostUser: ''
      },
      consumerProgressTable: {
        insertNum: 1,
        isPagination: false,
        columns: [
          { type: 'index', width: 40 },
          { prop: 'instanceName', label: '实例名称_实例类型' }
        ],
        data: [],
        loading: false
      }
    }
  },
  watch: {
    $route() {
      this.loadDetail()
    }
  },
  created() {
    this.loadDetail()
  },
  methods: {
    hostList(envId) {
      this.$router.push({ name: 'hostList', params: { envId: envId } })
    },
    async loadDetail() {
      this.envId = this.$route.params.envId

      this.getId()
      // this.loadEnv(id)
    },
    // 获取id
    async getId() {
      let envId = this.$route.params.envId
      let hostId = this.$route.params.id
      const res = await hostDetail(envId, hostId)
      if (res.result) {
        const { lastMonitorTime, hostStatus } = res.result

        res.result.lastMonitorTime = lastMonitorTime == null ? '' : util.dateToStr(new Date(lastMonitorTime), 3)
        res.result.hostStatusTxt = hostStatus === 'ENABLE' ? '可用' : '禁用'
        this.form = res.result
        res.result.services.forEach(item => {
          const { instanceName, instanceType } = item
          Object.assign(item, {
            // lastMonitorTime: lastMonitorTime == null ? '' : (new Date().getTime() - lastMonitorTime) / 1000 + 's'
            instanceName: instanceName + '_' + instanceType
          })
          return item
        })
        this.consumerProgressTable.data = res.result.services
      }
    },
    serviceIconClass(serviceType) {
      switch (serviceType) {
        case 'ZOOKEEPER':
          return 'Zookeeper'
        case 'INFLUXDB':
          return 'influxDb'
        case 'ZMS_ALERT':
          return 'gaojing'
        case 'ZMS_COLLECTOR':
          return 'caiji'
        case 'ROCKETMQ':
          return 'RocketMQ'
        case 'KAFKA':
          return 'Kafka'
        case 'ZMS_BACKUP_CLUSTER':
          return 'backup'
        default:
          return ''
      }
    }
  }
}
</script>
<style lang="scss">
.detail-page .l-page__search {
  height: calc(100% - 30px);
  overflow: auto;
}
.l-pane-content {
  min-height: 400px;
}
.hostinfo {
  font-size: 14px;
  li {
    margin-bottom: 10px;
    margin-right: 50px;
    display: inline-block;
  }
}
</style>
