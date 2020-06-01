<template>
  <div class="l-page l-page--flex theme-detail consumerdelay">
    <div class="page-nav">
      <div class="content_breadcrumb">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item>Kafka控制台</el-breadcrumb-item>
          <el-breadcrumb-item>
            topicSummary
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
    </div>
    <div class="l-page__content">
      <div class="content--padding">
        <div class="content__header">
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
                <el-row :gutter="0">
                  <el-col :span="5">
                    <el-form-item
                      label="环境"
                      prop="envId"
                      :rules="[{ required: true, message: '请输入消费组', trigger: 'blur' }]"
                    >
                      <el-select
                        v-model="formData.envId"
                        v-bind="GLOBAL.select"
                        v-loading="SearchEnvId.loading"
                        @focus="getEnvListOptions"
                      >
                        <el-option
                          v-for="item in SearchEnvId.options"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="5">
                    <el-form-item
                      label="集群"
                      prop="clusterName"
                      :rules="[{ required: true, message: '请输入集群', trigger: 'blur' }]"
                    >
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
                  <el-col :span="5">
                    <el-form-item
                      label="主题"
                      prop="topicName"
                      :rules="[{ required: true, message: '请输入主题', trigger: 'blur' }]"
                    >
                      <el-select
                        v-model="formData.topicName"
                        v-bind="GLOBAL.select"
                        v-loading="SearchTheme.loading"
                        @focus="getThemeOptions"
                      >
                        <el-option
                          v-for="item in SearchTheme.options"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                        ></el-option>
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="4">
                    <div class="search__btns">
                      <el-button type="primary" size="mini" @click="searchHandler">
                        查询
                      </el-button>
                      <el-button type="primary" size="mini" @click="onReset">
                        重置
                      </el-button>
                    </div>
                  </el-col>
                </el-row>
              </el-form>
            </div>
          </el-collapse-transition>
        </div>
        <div class="content__body">
          <div class="l-table-list">
            <div class="l-table-box">
              <h3>主题详情</h3>
              <table-pagination v-bind="topicInfostable"></table-pagination>
            </div>
            <div class="l-table-box">
              <h3>消费组信息</h3>
              <table-pagination v-bind="consumertable">
                <template slot="insert">
                  <el-table-column v-bind="GLOBAL.column" label="操作" width="240">
                    <template slot-scope="scope">
                      <operate-button type="primary" @click="handleData(scope.row)">查看</operate-button>
                    </template>
                  </el-table-column>
                </template>
              </table-pagination>
            </div>
            <div class="l-table-box">
              <h3>主题配置信息</h3>
              <table-pagination v-bind="topicConfigstable"></table-pagination>
            </div>
          </div>
        </div>
      </div>
    </div>
    <op-freight
      class="topicSunmmary"
      :dialog="diaOptions.visible"
      :modalTitle="diaOptions.diatitle"
      @dealDialog="closeDia"
      :no-cancle="true"
      width="80%"
    >
      <div class="content--padding">
        <div class="content__body">
          <div class="l-table-box">
            <table-pagination v-bind="topicSunmmary" style="paddingBottom:40px">
              <p style="marginTop:0">总延迟:{{ totalLag }}</p>
            </table-pagination>
          </div>
        </div>
      </div>
    </op-freight>
  </div>
</template>

<script>
import { common, baseStructure } from '@/mixins/index'

import OpFreight from '@/modules/modal/freight/OpFreight'
// 工具方法
import { topicSummary, consumerSummary } from '@/api/kafka-cmd'
// 搜索相关
import search from './list-search'

const diaOptions = {
  visible: false,
  ref: 'diaform',
  diatitle: '查看消费信息',
  clusterName: '',
  envId: ''
}

export default {
  name: 'topicSummary',
  mixins: [common, baseStructure, search],
  components: {
    OpFreight
  },
  data() {
    return {
      diaOptions,
      totalLag: '',
      topicInfostable: {
        columns: [
          { type: 'index', width: 40 },
          { prop: 'partition', label: 'partition' },
          { prop: 'offset', label: 'offset' },
          { prop: 'leader', label: 'leader' },
          { prop: 'isr', label: 'isr' },
          { prop: 'replicas', label: 'replicas' }
        ],
        data: [],
        loading: false
        // isPagination: false
      },
      consumertable: {
        columns: [
          { type: 'index', width: 60 },
          { prop: 'value', label: 'value' }
        ],
        data: [],
        insertNum: 1,
        loading: false
        // isPagination: false
      },
      topicConfigstable: {
        columns: [
          { type: 'index', width: 40 },
          { prop: 'object1', label: 'label' },
          { prop: 'object2', label: 'value' }
        ],
        data: [],
        loading: false
        // isPagination: false
      },
      topicSunmmary: {
        columns: [
          { type: 'index', width: 40 },
          { prop: 'topic', label: 'topic', width: 200 },
          { prop: 'partition', label: 'partition', width: 150 },
          { prop: 'lag', label: 'latency', sortable: true, width: 150 },
          { prop: 'consumerOffset', label: 'consumerOffset', width: 150 },
          { prop: 'host', label: 'host', width: 150 },
          { prop: 'clientId', label: 'clientId', width: 150 },
          { prop: 'consumerId', label: 'consumerId', width: 350 }
        ],
        data: []
        // loading: false
      }
    }
  },

  created() {
    // this.loadTabledata()
  },
  methods: {
    // 获取表格数据
    async loadTabledata(params = {}) {
      this.topicInfostable.loading = true
      this.consumertable.loading = true
      this.topicConfigstable.loading = true
      const {
        result: { topicInfos, topicConfigs, consumers, cluster, envId }
      } = await topicSummary(params)
      this.topicInfostable.data = topicInfos
      this.topicConfigstable.data = topicConfigs
      this.consumertable.data = consumers
      this.diaOptions.envId = envId
      this.diaOptions.clusterName = cluster

      this.topicInfostable.loading = false
      this.consumertable.loading = false
      this.topicConfigstable.loading = false
    },
    // 打开弹窗
    async handleData(row) {
      console.info('row', row)
      const params = { envId: this.diaOptions.envId, clusterName: this.diaOptions.clusterName, consumerName: row.value }
      diaOptions.visible = true
      const {
        result: { totalLag, consumerInfos }
      } = await consumerSummary(params)
      this.topicSunmmary.data = consumerInfos
      this.totalLag = totalLag
    },

    closeDia() {
      Object.assign(this.diaOptions, {
        visible: false
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
.theme-detail {
  .title {
    font-size: 14px;
    margin: 20px 35px 0px;
    font-weight: bold;
  }
  .line {
    border-bottom: 1px dashed #ccc;
  }
  .subtitle {
    margin: 20px 35px 0px;
    span {
      margin-right: 20px;
    }
  }
  .c-dialog .el-dialog {
    width: 80%;
  }
  .app-table-space-padding {
    overflow: auto !important;
  }
}
</style>
