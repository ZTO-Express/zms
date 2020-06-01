<template>
  <div class="l-page l-page--flex consumerdelay">
    <div class="page-nav">消费延迟</div>

    <div class="l-page__content">
      <div class="content--padding">
        <div class="content__header" style="padding-bottom: 15px;">
          <div style="float:right; padding-top:5px; position: relative;z-index: 1;">
            <el-radio-group size="mini" v-model="delayType" @change="changeHandler">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="rocketmq">rocketmq</el-radio-button>
              <el-radio-button label="kafka">kafka</el-radio-button>
            </el-radio-group>

            <el-switch style="margin-left:10px" size="mini" v-model="allDelay" active-text="大于积压阈值"></el-switch>
          </div>
          <div>
            <el-collapse-transition>
              <div v-show="!isCollapse">
                <!-- 查询 -->
                <el-form
                  :model="formData"
                  ref="formData"
                  size="mini"
                  class="search__form"
                  label-position="right"
                  :label-width="GLOBAL.fFormLabel"
                >
                  <el-row style="padding-top:10px;">
                    <el-col :span="3">
                      <el-form-item label="环境" prop="envId">
                        <el-select v-model="envId">
                          <el-option
                            v-for="item in environment"
                            :key="item.environmentName"
                            :label="item.environmentName"
                            :value="item.id"
                          ></el-option>
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="3">
                      <el-form-item label="集群" prop="clusterName">
                        <el-select
                          v-model="formData.clusterName"
                          v-bind="GLOBAL.select"
                          v-loading="SearchCluster.loading"
                          @focus="getClusterOptions"
                        >
                          <el-option
                            v-for="item in SearchCluster.options"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                          ></el-option>
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="4">
                      <el-form-item label="消费者" prop="name">
                        <el-select
                          v-model="formData.name"
                          v-bind="GLOBAL.select"
                          v-loading="SearchConsumer.loading"
                          @focus="getConsumerOptions"
                        >
                          <el-option
                            v-for="item in SearchConsumer.options"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                          ></el-option>
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="0">
                      <el-form-item v-model="allDelay" prop="allDelay" v-show="false"></el-form-item>
                    </el-col>
                    <el-col :span="3" style="margin-left:15px;">
                      <el-button type="primary" style="padding-top:10px;" size="mini" @click="searchHandler">
                        查询
                      </el-button>
                      <el-button type="primary" style="padding-top:10px;" size="mini" @click="onReset">
                        重置
                      </el-button>
                    </el-col>
                  </el-row>
                </el-form>
              </div>
            </el-collapse-transition>
            <!-- <div class="search__collapse" @click="isCollapse = !isCollapse">
        {{ isCollapse ? '更多查询' : '收起' }}
        <svg-icon :icon-class="isCollapse ? 'arrowdown-1' : 'arrowup-1'"></svg-icon>
      </div> -->
          </div>
        </div>
        <div class="content__body">
          <table-pagination v-bind="maintable" :data-handler="handleTableData" @changePage="pageHandler">
            <template slot="insert">
              <el-table-column v-bind="GLOBAL.column" label="消费组" width="250">
                <template slot-scope="scope">
                  <color-word
                    :disabled="scope.row.consumerId === null"
                    type="link"
                    @click="viewDetail(scope.row.consumerId)"
                  >
                    {{ scope.row.consumer }}
                  </color-word>
                </template>
              </el-table-column>
            </template>
            <template slot="insert2">
              <el-table-column v-bind="GLOBAL.column" label="延迟数" width="200">
                <template slot-scope="scope">
                  <span :class="scope.row.latencyClass">{{ formatNumber(scope.row.latency) }}</span>
                </template>
              </el-table-column>
            </template>
          </table-pagination>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
// 搜索相关
import search from './list-search'
// 请求接口
import { delayRocketQuery, delayKafkaQuery, allDelayQuery } from '@/api/delay'
import { listEnvironment } from '@/api/env'

export default {
  name: 'delayList',
  mixins: [common, baseStructure, search],
  components: {},
  data() {
    return {
      allDelay: false,
      maintable: {
        columns: [
          { type: 'index', width: 40, fixed: 'left' },
          { prop: 'tps', label: '消费速度', width: 200 },
          { prop: 'cluster', label: '集群', width: 200 },
          { prop: 'owner', label: '所有者', 'min-width': 150 }
        ],
        data: [],
        insertNum: 1,
        insertNum2: 1,
        loading: false,
        currentPage: 1,
        pageSize: 25,
        total: 0,
        pagePagination: true
      },
      delayType: 'rocketmq',
      envId: undefined,
      envs: undefined,
      environment: []
    }
  },
  watch: {
    // 监听环境变化，重置消费组选项值
    envId() {
      // this.loadTabledata()
      this.formData.envId = this.envId
    },
    allDelay() {
      this.pageHandler()
    }
  },
  created() {
    this.getListEnvironment()
  },
  methods: {
    // 获取环境下拉列表
    async getListEnvironment() {
      this.SearchEnv.loading = true
      const params = {}
      const res = await listEnvironment(params)
      if (res.result) {
        this.environment = res.result
        this.formData.envId = res.result[0].id
        this.envId = res.result[0].id
      }
      this.SearchEnv.loading = false
      this.pageHandler()
    },
    pageHandler(params = {}) {
      let par = this.assembleData(params)
      this.loadTabledata(par)
    },
    assembleData(params = {}) {
      params.allDelay = this.allDelay
      return params
    },
    changeHandler(value) {
      this.delayType = value
      this.pageHandler()
    },
    // 获取表格数据
    async loadTabledata(params = {}) {
      params.envId = this.envId
      if (!params.currentPage) {
        params.currentPage = this.maintable.currentPage
        params.pageSize = this.maintable.pageSize
      }

      if (params.envId == undefined) {
        this.$message('请先选择环境!')
        return
      }
      this.maintable.loading = true
      let res = {}
      if (this.delayType == 'rocketmq') {
        res = await delayRocketQuery(params)
      } else if (this.delayType == 'kafka') {
        res = await delayKafkaQuery(params)
      } else {
        res = await allDelayQuery(params)
      }
      if (res.result.records) {
        this.maintable.currentPage = res.result.currentPage
        this.maintable.pageSize = res.result.pageSize
        this.maintable.total = res.result.total
        this.maintable.data = res.result.records
      } else {
        this.maintable.data = []
      }
      this.maintable.loading = false
    },
    // 表格数据处理
    handleTableData(item) {
      const { latency, delayThreadhold } = item
      Object.assign(item, {
        latencyClass: delayThreadhold != null && latency > delayThreadhold ? 'over' : ''
      })
      return item
    },
    viewDetail(consumerId) {
      this.$router.push({ name: 'consumerDetail', params: { id: consumerId } })
    },
    formatNumber(num) {
      if (isNaN(num)) {
        throw new TypeError('num is not a number')
      }
      return ('' + num).replace(/(\d{1,3})(?=(\d{3})+(?:$|\.))/g, '$1,')
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss">
.over {
  color: #d41b1b;
}
</style>
