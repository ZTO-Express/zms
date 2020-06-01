<template>
  <div class="l-page l-page--flex consumerdelay">
    <div class="page-nav">
      <div class="content_breadcrumb">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item>RocketMQ控制台</el-breadcrumb-item>
          <el-breadcrumb-item>
            clusterList
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
                      :rules="[{ required: true, message: '请输入环境', trigger: 'blur' }]"
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
          <table-pagination v-bind="maintable" :data-handler="handleTableData"></table-pagination>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { common, baseStructure } from '@/mixins/index'

// import OpFreight from '@/modules/modal/freight/OpFreight'
// 工具方法
import { clusterList } from '@/api/rocketmq-cmd'
// 搜索相关
import search from './list-search'

export default {
  name: 'clusterList',
  mixins: [common, baseStructure, search],
  components: {
    // OpFreight
  },
  data() {
    return {
      maintable: {
        columns: [
          { type: 'index', width: 60 },
          { prop: 'clusterName', label: 'Cluster Name', width: 180 },
          { prop: 'brokerName', label: 'Broker Name', width: 180 },
          { prop: 'bid', label: 'BID', width: 180 },
          { prop: 'addr', label: 'Addr', width: 180 },
          { prop: 'version', label: 'Version', width: 180 },
          { prop: 'inTps', label: 'InTps', width: 180 },
          { prop: 'outTps', label: 'OutTps', width: 180 },
          { prop: 'pcWait', label: 'PCWait', width: 180 },
          { prop: 'hour', label: 'Hour', width: 180 },
          { prop: 'space', label: 'Space', width: 180 },
          { prop: 'inTotalYest', label: 'InTotalYest', width: 180 },
          { prop: 'outTotalYest', label: 'OutTotalYest', width: 180 },
          { prop: 'inTotalToday', label: 'InTotalToday', width: 180 },
          { prop: 'outTotalToday', label: 'OutTotalToday', width: 180 }
        ],
        data: [],
        insertNum: 1,
        insertNum2: 1,
        insertNum3: 1,
        loading: false
      }
    }
  },

  created() {
    // this.loadTabledata()
  },
  methods: {
    // 获取表格数据
    async loadTabledata(params = {}) {
      this.maintable.loading = true
      const res = await clusterList(params)
      if (res.result) {
        this.maintable.data = res.result
        this.maintable.loading = false
      }
    },
    // 表格数据处理
    handleTableData(item) {
      return item
    }
  }
}
</script>
