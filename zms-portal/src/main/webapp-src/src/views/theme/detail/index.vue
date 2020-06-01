<template>
  <div class="l-page detail-page">
    <div class="l-page__search" v-loading="environment.loading">
      <div class="content_breadcrumb ">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/theme/list' }">{{ '主题管理' }}</el-breadcrumb-item>

          <el-breadcrumb-item>
            {{ topicName }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="title env">
        <!-- <h3>主题名：{{ topicName }}</h3> -->
        <!-- 环境下拉框 -->
        <label>环境 ：</label>
        <el-select v-model="env" :clearable="false" v-bind="GLOBAL.select" class="l-page__env">
          <el-option
            v-for="item in environment.list"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          ></el-option>
        </el-select>
      </div>

      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane name="first">
          <span slot="label">主题详情</span>
          <div class="l-pane-content" v-loading="detail.loading">
            <theme-detail-rocketmq v-if="detail.type === 'rocketmq'" :result="detail.result" />
            <theme-detail-kafka v-if="detail.type === 'kafka'" :result="detail.result" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="topic监控" name="second">
          <theme-monitor
            :initflg="themeMonitor.flg"
            :topic-name="topicName"
            :env-id="env"
            :cluster-name="clusterName"
          />
        </el-tab-pane>
        <el-tab-pane label="客户端监控" name="third">
          <client-monitor :initflg="clientMonitor.flg" :topic-name="topicName" :env-id="env" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
import { getThemeDetail, themeQuery } from '@/api/theme'
import themeDetailRocketmq from './theme-detail-rocketmq'
import themeDetailKafka from './theme-detail-kafka'
import themeMonitor from './theme-monitor'
import clientMonitor from './client-monitor'

export default {
  name: 'themeDetail',
  components: {
    themeDetailRocketmq,
    themeDetailKafka,
    themeMonitor,
    clientMonitor
  },
  data() {
    return {
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
      topicName: '',
      clusterName: '',
      // 主题监控
      themeMonitor: {
        // 是否渲染标志
        flg: false
      },
      // 客户端监控
      clientMonitor: {
        flg: false
      }
    }
  },
  watch: {
    $route() {
      this.loadDetail()
    },
    // 监听环境值发生变化，请求主题详情
    env(newval) {
      // console.info('oldval' + oldval)
      // if (oldval !== undefined) {
      //   this.themeMonitor.flg = true
      // }
      this.loadThemeDetail(this.getId())
      this.clusterName = this.environment.list.filter(item => Number(item.value) === Number(newval))[0].serverName
    }
  },
  created() {
    this.loadDetail()
  },
  methods: {
    viewDetail() {
      const id = this.getId()
      this.$router.push({ name: 'themeDetail', params: { id: id } })
    },
    async loadDetail() {
      const id = this.getId()
      this.loadEnv(id)
    },
    // 获取id
    getId() {
      const id = this.$route.params.id
      // 地址栏id为空，返回主题列表页
      if (id === undefined) {
        this.$router.push({ name: 'themeList' })
        return
      } else {
        return id
      }
    },
    // 环境数据请求
    async loadEnv(id) {
      this.environment.loading = true
      const themelistRes = await themeQuery({ id: id })
      const themedata = themelistRes.result.records.filter(item => Number(item.id) === Number(id))[0]
      let environmentList = themedata.environments
      environmentList = environmentList.map(item => {
        return {
          ...item,
          label: item.environmentName,
          value: item.environmentId
        }
      })
      this.topicName = themedata.name
      this.environment.loading = false
      this.environment.list = environmentList
      this.env = environmentList[0].value
    },
    // 主题详情
    async loadThemeDetail(id) {
      this.detail.loading = true
      const res = await getThemeDetail(id, { envId: this.env })
      this.detail.loading = false
      this.detail.result = res.result
      if (res.result.topicConfigs) {
        this.detail.type = 'kafka'
      } else {
        this.detail.type = 'rocketmq'
      }
    },
    handleClick(tab) {
      if (tab.name === 'second') {
        this.themeMonitor.flg = true
      }
      if (tab.name === 'third') {
        this.clientMonitor.flg = true
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
</style>
