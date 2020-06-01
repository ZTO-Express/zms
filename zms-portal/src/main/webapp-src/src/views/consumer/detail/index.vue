<template>
  <div class="l-page detail-page">
    <div class="l-page__search" v-loading="environment.loading">
      <div class="content_breadcrumb">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/consumer/list' }">{{ '消费组管理' }}</el-breadcrumb-item>

          <el-breadcrumb-item>
            {{ consumerName }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="title env">
        <!-- <h3>消费组名：{{ consumerName }}</h3> -->
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
          <span slot="label">消费组详情</span>
          <div class="l-pane-content" v-loading="detail.loading">
            <consumer-detail-rocketmq v-if="detail.type === 'rocketmq'" :result="detail.result" />
            <consumer-detail-kafka v-if="detail.type === 'kafka'" :result="detail.result" />
          </div>
        </el-tab-pane>
        <el-tab-pane label="消费组监控" name="second">
          <consumer-monitor
            :initflg="consumerMonitor.flg"
            :topic-name="topicName"
            :consumer-name="consumerName"
            :env-id="env"
            :cluster-name="clusterName"
            :consumer-id="consumerId"
          />
        </el-tab-pane>
        <el-tab-pane label="客户端监控" name="third">
          <client-monitor :initflg="clientMonitor.flg" :consumer-name="consumerName" :env-id="env" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
import { getConsumerKafkaDetail, getConsumerRocketDetail, consumerQuery } from '@/api/consumer'
import consumerDetailRocketmq from './consumer-detail-rocketmq'
import consumerDetailKafka from './consumer-detail-kafka'
import consumerMonitor from './consumer-monitor'
import clientMonitor from './client-monitor'

export default {
  name: 'consumerDetail',
  components: {
    consumerDetailRocketmq,
    consumerDetailKafka,
    consumerMonitor,
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
      consumerName: '',
      topicName: '',
      clusterName: '',
      consumerId: Number,
      // 主题监控
      consumerMonitor: {
        // 是否渲染标志
        flg: false
      },
      // 客户端监控
      clientMonitor: {
        flg: false
      },
      clusterType: 'rocketmq'
    }
  },
  watch: {
    $route() {
      this.loadDetail()
    },
    // 监听环境值发生变化，请求消费组详情已经监控信息
    env(newval) {
      // if (oldval !== undefined) {
      //   this.consumerMonitor.flg = true
      // }
      this.clusterType = this.environment.list.filter(item => Number(item.value) === Number(newval))[0].clusterType
      this.clusterName = this.environment.list.filter(item => Number(item.value) === Number(newval))[0].serverName
      this.loadConsumerDetail(this.getId())
    }
  },
  created() {
    this.loadDetail()
  },
  methods: {
    async loadDetail() {
      const id = this.getId()
      this.loadEnv(id)
    },
    // 获取id
    getId() {
      const id = this.$route.params.id
      this.consumerId = Number(this.$route.params.id)
      // 地址栏id为空，返回主题列表页
      if (id === undefined) {
        this.$router.push({ name: 'List' })
        return
      } else {
        return id
      }
    },
    // 环境数据请求
    async loadEnv(id) {
      this.environment.loading = true
      const consumerlistRes = await consumerQuery({ id: id })
      const consumerdata = consumerlistRes.result.records.filter(item => Number(item.id) === Number(id))[0]
      let environmentList = consumerdata.consumerEnvironmentRefVos
      environmentList = environmentList.map(item => {
        return {
          ...item,
          label: item.environmentName,
          value: item.environmentId
        }
      })
      this.topicName = consumerdata.topicName
      this.consumerName = consumerdata.name
      this.consumerId = consumerdata.id
      this.environment.loading = false
      this.environment.list = environmentList
      this.env = environmentList[0].value
    },
    // 消费组详情
    async loadConsumerDetail(id) {
      this.detail.loading = true
      let res = {}
      const params = { envId: this.env, consumerId: id }

      if (this.clusterType === 'KAFKA') {
        res = await getConsumerKafkaDetail(params)
        this.detail.type = 'kafka'
      } else {
        res = await getConsumerRocketDetail(params)
        this.detail.type = 'rocketmq'
      }
      this.detail.loading = false
      this.detail.result = res.result
    },
    handleClick(tab) {
      if (tab.name === 'second') {
        this.consumerMonitor.flg = true
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
