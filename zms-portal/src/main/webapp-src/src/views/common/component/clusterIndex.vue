<template>
  <div
    class="l-chart-container cluster-index"
    style="background:#fff;box-shadow:0 0 12px #ccc;padding:1px 20px 20px;margin-top:15px;"
  >
    <el-tabs v-model="activeName" @tab-click="handleTabClick">
      <el-tab-pane label="集群指标" name="metric">
        <el-select v-model="selectEnvId" size="mini" style="width:150px">
          <el-option v-for="item in envOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
        </el-select>
        <!-- 时间选择 -->
        <time-range
          :time-str="currentTime1"
          :quick-num="quickNum1"
          @change="getTimeRange1"
          @changeQuick="changeQuick1"
        />
        <div class="cluster-chart l-chart-box">
          <div v-if="showCluster.length">
            <div
              class="box"
              v-for="item in showCluster"
              :key="item"
              v-loading="!metricOrder.some(order => order === item)"
            >
              <chart
                v-if="metricOrder.some(order => order === item)"
                v-bind="GLOBAL.chartConfig"
                :chartData="[metricCharts[item]]"
                :underline="false"
                :type="item"
                @finished="
                  value => {
                    triggerFinish(value, 'metricAlreadylist')
                  }
                "
              />
              <div class="position" v-else></div>
            </div>
          </div>
          <div v-if="showCluster.length < 3">
            <div class="box cluster-box">
              <div class="position"></div>
              <div class="position" v-if="showCluster.length < 1"></div>
            </div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="日消息量" name="offset">
        <el-select v-model="selectEnvId" size="mini" style="width:150px">
          <el-option v-for="item in envOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
        </el-select>
        <!-- 时间选择 -->
        <time-range
          :time-str="currentTime2"
          :quick-num="quickNum2"
          @change="getTimeRange2"
          @changeQuick="changeQuick2"
        />

        <div class="cluster-chart l-chart-box">
          <div v-if="showCluster.length">
            <div
              class="box"
              v-for="item in showCluster"
              :key="item"
              v-loading="!offsetOrder.some(order => order === item)"
            >
              <chart
                v-if="offsetOrder.some(order => order === item)"
                v-bind="GLOBAL.chartConfig"
                :chartData="[offsetCharts[item]]"
                :underline="false"
                :type="item"
                @finished="
                  value => {
                    triggerFinish(value, 'offsetAlreadylist')
                  }
                "
              />
              <div class="position" v-else></div>
            </div>
          </div>
          <div v-if="showCluster.length < 3">
            <div class="box cluster-box">
              <div class="position"></div>
              <div class="position" v-if="showCluster.length < 1"></div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { common, baseStructure } from '@/mixins/index'
import chart from '@/modules/components/chart'
import timeRange from '@/modules/components/time-range'
import { getclusterMetric, queryClusterOffset } from '@/api/home'
import { util } from '@/utils/util'

export default {
  name: 'ClusterIndex',
  mixins: [common, baseStructure],
  components: {
    chart,
    timeRange
    // OpFreight
  },
  props: {
    clusterList: Array
  },
  data() {
    return {
      activeName: 'metric',
      metricInit: true,
      offsetInit: false,
      envOptions: [],
      selectEnvId: '',
      time: [],
      currentTime1: '',
      currentTime2: '',
      quickNum1: 0,
      quickNum2: 0,
      allCluster: {},
      showCluster: [],
      metricCharts: [],
      metricOrder: [],
      metricAlreadylist: [],
      offsetCharts: [],
      offsetOrder: [],
      offsetAlreadylist: [],
      validlist: []
    }
  },
  watch: {
    clusterList(newval) {
      this.setRenderCluster(newval)
    },
    selectEnvId() {
      this.changeInit()
      this.changeEnvId()
      this.resetClusterIndexData()
      this.loadClusterIndexData(this.showCluster[0], this.activeName)
    },
    time() {
      if (!this.selectEnvId || this.showCluster.length == 0) {
        return
      }
      this.changeInit()
      this.resetClusterIndexData()
      this.loadClusterIndexData(this.showCluster[0], this.activeName)
    }
  },
  methods: {
    handleTabClick(tab) {
      if (!this.metricInit && tab.name == 'metric') {
        this.metricInit = true
        this.loadClusterIndexData(this.showCluster[0], tab.name)
      } else if (!this.offsetInit && tab.name == 'offset') {
        this.offsetInit = true
        this.loadClusterIndexData(this.showCluster[0], tab.name)
      }
    },
    // 获取时间范围
    getTimeRange1(value) {
      if (this.activeName === 'metric') {
        this.time = value
        this.currentTime2 = this.time[0] + ',' + this.time[1]
      }
    },
    getTimeRange2(value) {
      if (this.activeName === 'offset') {
        this.time = value
        this.currentTime1 = this.time[0] + ',' + this.time[1]
      }
    },
    changeQuick1(value) {
      if (this.activeName === 'metric') {
        this.quickNum2 = value
      } else {
        this.quickNum2 = -1
      }
    },
    changeQuick2(value) {
      if (this.activeName === 'offset') {
        this.quickNum1 = value
      } else {
        this.quickNum1 = -1
      }
    },
    // 筛选渲染的集群
    setRenderCluster(arr) {
      if (arr.length) {
        for (const env of arr) {
          this.envOptions.push({ label: env.environmentName, value: env.id })
          let envCluster = []
          if (env.services && env.services.length) {
            for (const item of env.services) {
              if (item.serverType === 'ROCKETMQ') {
                envCluster.push(item.serverName)
              }
            }
            for (const item of env.services) {
              if (item.serverType === 'KAFKA') {
                envCluster.push(item.serverName)
              }
            }
          }
          this.allCluster = Object.assign({}, this.allCluster, { [env.id]: envCluster })
        }
        this.showCluster = this.allCluster[arr[0].id]
        this.selectEnvId = arr[0].id
      }
    },
    changeInit() {
      if (this.activeName === 'metric') {
        this.offsetInit = false
      } else {
        this.metricInit = false
      }
    },
    changeEnvId() {
      this.metricCharts = {}
      this.offsetCharts = {}
      this.showCluster = this.allCluster[this.selectEnvId]
      if (this.showCluster && this.showCluster.length) {
        this.showCluster.forEach(item => {
          this.metricCharts = Object.assign({}, this.metricCharts, {
            [item]: {
              title: item,
              data: [],
              legend: [
                { name: 'totalTps', attr: 'totalTps' },
                { name: 'clusterNums', attr: 'clusterNums' }
              ]
            }
          })
          this.offsetCharts = Object.assign({}, this.offsetCharts, {
            [item]: {
              title: item,
              data: [],
              legend: [{ name: 'offsets daily increasement', attr: 'offsets_daily_increasement' }]
            }
          })
        })
      }
    },
    // chart data 数据生成
    generateChartData(result) {
      const data = []
      // 时间按从小到大排序、筛选数据
      util.dataSort(result).forEach(item => {
        data.push({
          name: item.name,
          x: util.timeToStr(item.timestamp * 1000, 4),
          value: item.value
        })
      })
      return data
    },
    resetClusterIndexData() {
      this.metricAlreadylist = []
      this.offsetAlreadylist = []
      this.metricOrder = []
      this.offsetOrder = []
      this.validlist = []
    },
    // 获取集群指标和集群日消息量 统计图数据
    async loadClusterIndexData(clusterName, type) {
      if (!clusterName) {
        return
      }
      const params = {
        clusterName,
        beginTime: this.time[0],
        endTime: this.time[1],
        envId: this.selectEnvId
      }
      if (type === 'metric') {
        const res = await getclusterMetric(params)
        this.metricOrder.push(clusterName)
        this.metricCharts[clusterName].data = this.generateChartData(res.result)
      } else {
        const res = await queryClusterOffset(params)
        this.offsetOrder.push(clusterName)
        this.offsetCharts[clusterName].data = this.generateChartData(res.result)
      }
    },
    // 单个统计图渲染完成回调
    triggerFinish(value, alreadyList) {
      const valid = value + '---' + alreadyList
      if (this.validlist.some(item => item === valid)) {
        return
      }
      this.validlist.push(valid)
      this[alreadyList] = [...new Set([...this[alreadyList], value])]
      if (this[alreadyList].length === this.showCluster.length) return
      const next = this.showCluster[this[alreadyList].length]
      if (alreadyList === 'metricAlreadylist') {
        this.loadClusterIndexData(next, 'metric')
      } else {
        this.loadClusterIndexData(next, 'offset')
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.l-chart-box2 {
  border-top: 1px dashed #ccc;
}
.cluster-index {
  .l-time-range {
    display: inline-block;
    margin-top: 0;
    margin-left: 20px;
    vertical-align: -60%;
    .time-range {
      float: left;
    }
  }
  .cluster-chart {
    margin-top: 20px;
  }
  .cluster-box {
    border: none;
  }
}
</style>
