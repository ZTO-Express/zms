<template>
  <div class="l-page l-page--flex consumerdelay">
    <div class="page-nav">集群迁移</div>

    <div class="l-page__content">
      <div class="content--padding">
        <div class="content__header">
          <el-button
            style="float: right;margin-top:5px;"
            v-bind="GLOBAL.button"
            icon="el-icon-refresh"
            @click="transferOpen"
            size="mini"
          >
            开始迁移
          </el-button>
          <div>
            环境
            <el-select
              @change="envChange"
              v-model="envId"
              v-bind="GLOBAL.select"
              size="mini"
              style="margin-right:15px;width:100px;margin-left:5px;"
            >
              <el-option
                v-for="item in SearchEnv.options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
            <el-input
              style="width:300px"
              size="mini"
              placeholder="搜索  ( 集群 )"
              v-model="keyWord"
              @change="searchMaintable"
            ></el-input>
          </div>
        </div>
        <div class="content__body">
          <table-pagination v-bind="maintable"></table-pagination>
        </div>
      </div>
    </div>
    <!-- 开始迁移弹窗 -->
    <op-freight
      :dialog="diaOptions.visible"
      :modalTitle="diaOptions.diatitle"
      :loading="diaOptions.loading"
      @dealDialog="closeDia"
    >
      <base-form
        :forms="diaOptions.forms"
        :ref="diaOptions.ref"
        :current-form-value="diaOptions.currentFormValue"
        label-width="90px"
      />
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="beginTransfer">执行迁移</el-button>
      </template>
    </op-freight>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import OpFreight from '@/modules/modal/freight/OpFreight'
// 搜索相关
import search from './list-search'
// 迁移相关
import transfer from './list-transfer'
import { queryClusters } from '@/api/transfer'

export default {
  name: 'clusterTransfer',
  mixins: [common, baseStructure, search, transfer],
  components: {
    OpFreight
  },
  data() {
    return {
      envId: '',
      keyWord: '',
      maintable: {
        ref: 'maintable',
        columns: [
          { type: 'selection', width: 50, fixed: 'left' },
          { prop: 'environmentName', label: '环境', width: 100 },
          { prop: 'serverName', label: '集群名称', width: 180 },
          { prop: 'serverType', label: '集群类型', width: 150 },
          { prop: 'startAddress', label: '启动地址', width: 300 },
          { prop: 'serviceAddress', label: '服务地址', width: 300 },
          { prop: 'version', label: '版本' }
        ],
        data: [],
        loading: false
      }
    }
  },
  created() {
    this.getEnvOptions()
    this.loadTabledata()
  },
  methods: {
    searchMaintable() {
      let params = this.assembleData()
      this.loadTabledata(params)
    },
    assembleData(params = {}) {
      params.envId = this.envId
      params.keyWord = this.keyWord
      return params
    },
    envChange(value) {
      this.status = value
      let params = this.assembleData()
      this.loadTabledata(params)
    },
    // 获取表格数据
    async loadTabledata(params = {}) {
      this.maintable.loading = true
      const res = await queryClusters(params)
      if (res.result) {
        this.maintable.data = res.result
        this.maintable.loading = false
      }
    }
  }
}
</script>
