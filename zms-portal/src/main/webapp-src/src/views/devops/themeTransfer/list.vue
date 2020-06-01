<template>
  <div class="l-page l-page--flex consumerdelay">
    <div class="page-nav">主题迁移</div>

    <div class="l-page__content">
      <div class="content--padding">
        <div class="content__header">
          <el-button
            style="float: right;margin-top:5px;"
            v-bind="GLOBAL.button"
            icon="el-icon-refresh"
            size="mini"
            @click="transferOpen"
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
              placeholder="搜索  ( 集群 、 主题名 )"
              v-model="keyWord"
              @change="searchMaintable"
            ></el-input>
          </div>
        </div>
        <div class="content__body">
          <table-pagination
            v-bind="maintable"
            :data-handler="handleTableData"
            @changePage="pageHandler"
          ></table-pagination>
        </div>
      </div>
    </div>
    <!-- 迁移弹窗 -->
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
// 工具方法
import { util } from '@/utils/util'
// 搜索相关
import search from './list-search'
// 迁移相关
import transfer from './list-transfer'
// 请求接口
import { themeQuery } from '@/api/theme'

export default {
  name: 'themeTransfer',
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
          { prop: 'name', label: '主题名', width: 250 },
          { prop: 'partitions', label: '分片数', width: 80 },
          { prop: 'applicant', label: '申请人', width: 150 },
          { prop: 'alertEmails', label: '邮箱通知列表', width: 250 },
          { prop: 'domain', label: '申请域(appId)', width: 200 },
          { prop: 'tps', label: '发送速度', width: 100 },
          { prop: 'msgszie', label: '消息体', width: 100 },
          { prop: 'memo', label: '备注', 'min-width': 300 },
          { prop: 'modifyDate', label: '修改时间', width: 160 }
        ],
        data: [],
        loading: false,
        currentPage: 1,
        pageSize: 25,
        total: 0,
        pagePagination: true
      }
    }
  },

  created() {
    this.getEnvOptions()

    this.loadTabledata()
  },
  methods: {
    pageHandler(params = {}) {
      let par = this.assembleData(params)
      this.loadTabledata(par)
    },
    assembleData(params = {}) {
      params.envId = this.envId
      params.keyWord = this.keyWord
      return params
    },
    envChange(value) {
      this.envId = value
      let params = this.assembleData()
      this.loadTabledata(params)
    },
    searchMaintable() {
      let params = this.assembleData()
      this.loadTabledata(params)
    },
    // 获取表格数据
    async loadTabledata(params = {}) {
      this.maintable.loading = true
      if (!params.currentPage) {
        params.currentPage = this.maintable.currentPage
        params.pageSize = this.maintable.pageSize
      }
      const res = await themeQuery(params)
      if (res.result) {
        this.maintable.data = res.result.records
        this.maintable.currentPage = res.result.currentPage
        this.maintable.pageSize = res.result.pageSize
        this.maintable.total = res.result.total
        this.maintable.loading = false
      }
    },
    // 表格数据处理
    handleTableData(item) {
      const { modifyDate } = item
      Object.assign(item, {
        modifyDate: util.dateToStr(new Date(modifyDate), 3)
      })
      return item
    }
  }
}
</script>
