<template>
  <div class="l-page l-page--flex consumerdelay">
    <div class="page-nav">消费迁移</div>

    <!-- <div class="l-page__search">
      <el-collapse-transition>
        <div v-show="!isCollapse">
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
                <el-form-item label="环境" prop="envId">
                  <el-select
                    v-model="formData.envId"
                    v-bind="GLOBAL.select"
                    v-loading="SearchEnv.loading"
                    @focus="getEnvOptions"
                  >
                    <el-option
                      v-for="item in SearchEnv.options"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item label="集群" prop="serviceId">
                  <el-select
                    v-model="formData.serviceId"
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
      <div class="search__collapse" @click="isCollapse = !isCollapse">
        {{ isCollapse ? '更多查询' : '收起' }}
        <svg-icon :icon-class="isCollapse ? 'arrowdown-1' : 'arrowup-1'"></svg-icon>
      </div> 
    </div> -->
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
              placeholder="搜索  ( 集群 、 消费组 )"
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
// 搜索相关
import search from './list-search'
// 迁移相关
import transfer from './list-transfer'
import OpFreight from '@/modules/modal/freight/OpFreight'
// 请求接口
import { consumerQuery } from '@/api/consumer'
// 工具方法
import { util } from '@/utils/util'

export default {
  name: 'consumerTransfer',
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
          { prop: 'name', label: '消费者', width: 250 },
          { prop: 'topicName', label: '主题名', width: 250 },
          { prop: 'broadcastTxt', label: '是否广播', width: 80 },
          { prop: 'consumerFromTxt', label: '消费位置', width: 100 },
          { prop: 'delayThreadhold', label: '积压阀值', width: 100 },
          { prop: 'applicant', label: '申请人', width: 150 },
          { prop: 'domain', label: '申请域（appId）', width: 200 },
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
      const res = await consumerQuery(params)
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
      const { modifyDate, consumerFrom, broadcast } = item
      Object.assign(item, {
        broadcastTxt: broadcast == true ? '是' : '否',
        consumerFromTxt: consumerFrom == true ? 'Earliest' : 'Latest',
        modifyDate: util.dateToStr(new Date(modifyDate), 3)
      })
      return item
    }
  }
}
</script>
