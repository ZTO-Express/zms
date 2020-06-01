<template>
  <div class="l-page l-page--flex consumerdelay">
    <div class="page-nav">
      <div class="content_breadcrumb">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item>RocketMQ控制台</el-breadcrumb-item>
          <el-breadcrumb-item>
            updatePerm
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
          <!-- <div class="search__collapse" @click="isCollapse = !isCollapse">
        {{ isCollapse ? '更多查询' : '收起' }}
        <svg-icon :icon-class="isCollapse ? 'arrowdown-1' : 'arrowup-1'"></svg-icon>
      </div> -->
        </div>
        <div class="content__body">
          <table-pagination v-bind="maintable" :data-handler="handleTableData">
            <template slot="insert">
              <el-table-column v-bind="GLOBAL.column" label="操作" width="240">
                <template slot-scope="scope">
                  <operate-button type="primary" @click="handleData(scope.row)">编辑</operate-button>
                </template>
              </el-table-column>
            </template>
          </table-pagination>
        </div>
      </div>
    </div>
    <op-freight
      :dialog="diaOptions.visible"
      :modalTitle="diaOptions.diatitle[diaOptions.status]"
      :loading="diaOptions.loading"
      @dealDialog="closeDia"
    >
      <base-form
        :forms="diaOptions.forms"
        :ref="diaOptions.ref"
        :current-form-value="diaOptions.currentFormValue"
        label-width="120px"
      />
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="saveData">确认</el-button>
      </template>
    </op-freight>
  </div>
</template>

<script>
import { common, baseStructure } from '@/mixins/index'

import OpFreight from '@/modules/modal/freight/OpFreight'
// 工具方法
import { getPerm } from '@/api/rocketmq-cmd'
// 搜索相关
import search from './list-search'

import edit from './list-edit'

export default {
  name: 'updatePerm',
  mixins: [common, baseStructure, search, edit],
  components: {
    OpFreight
  },
  data() {
    return {
      maintable: {
        columns: [
          { type: 'index', width: 60 },
          { prop: 'clusterName', label: 'cluster' },
          { prop: 'topicName', label: 'topic' },
          { prop: 'broker', label: 'broker' },
          { prop: 'perm', label: 'perm' }
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
      const res = await getPerm(params)
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
