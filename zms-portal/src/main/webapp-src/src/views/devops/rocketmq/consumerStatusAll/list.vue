<template>
  <div class="l-page l-page--flex consumerdelay">
    <div class="page-nav">
      <div class="content_breadcrumb">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item>RocketMQ控制台</el-breadcrumb-item>
          <el-breadcrumb-item>
            consumerStatusAll
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
                      label="消费组"
                      prop="consumerName"
                      :rules="[{ required: true, message: '请输入消费组', trigger: 'blur' }]"
                    >
                      <el-select
                        v-model="formData.consumerName"
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
                  <el-col :span="8">
                    <div class="search__btns">
                      <el-button type="primary" size="mini" @click="searchHandler">
                        查询
                      </el-button>
                      <el-button type="primary" size="mini" @click="onReset">
                        重置
                      </el-button>
                      <el-button type="primary" size="mini" @click="handleData">
                        消息回溯
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
          <div class="l-table-list">
            <div class="l-table-box">
              <h3>consumerProgress</h3>
              <p class="subtitle">
                <span>DiffTotal:{{ diffTotal }}</span>
                <span>ConsumeTps:{{ consumeTps }}</span>
              </p>
              <table-pagination v-bind="maintable"></table-pagination>
            </div>
            <div class="l-table-box">
              <h3>consumerZmsRegister</h3>
              <table-pagination v-bind="consumerZmsRegister" />
            </div>
            <div class="l-table-box">
              <h3>consumerStatus</h3>
              <table-pagination v-bind="consumerStatus" />
            </div>
          </div>
        </div>
      </div>
    </div>
    <op-freight :dialog="diaOptions.visible" modalTitle="消息回溯" :loading="diaOptions.loading" @dealDialog="closeDia">
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
import { consumerStatusAll } from '@/api/rocketmq-cmd'

// 搜索相关
import search from './list-search'

import edit from './list-edit'

export default {
  name: 'consumerStatusAll',
  mixins: [common, baseStructure, search, edit],
  components: {
    OpFreight
  },
  data() {
    return {
      consumeTps: '',
      diffTotal: '',
      maintable: {
        columns: [
          { type: 'index', width: 60 },
          { prop: 'topic', label: 'Topic' },
          { prop: 'brokerName', label: 'Broker Name' },
          { prop: 'qid', label: 'QID' },
          { prop: 'brokerOffset', label: 'Broker Offset' },
          { prop: 'consumerOffset', label: 'Consumer Offset' },
          { prop: 'clientIP', label: 'Client IP' },
          { prop: 'diff', label: 'Diff' },
          { prop: 'lastTime', label: 'LastTime' }
        ],
        data: [],
        loading: false
      },
      consumerZmsRegister: {
        columns: [
          { type: 'index', width: 60 },
          { prop: 'zmsIP', label: 'Client IP' },
          { prop: 'instanceName', label: 'Name' },
          { prop: 'zmsVersion', label: 'Sdk Version' },
          { prop: 'startUpTime', label: 'StartUp Time' },
          { prop: 'threadLocalRandomInt', label: 'Random Num' }
        ],
        data: [],
        loading: false
      },
      consumerStatus: {
        columns: [
          { type: 'index', width: 60 },
          { prop: 'clientId', label: 'Client Id' },
          { prop: 'clientAddr', label: 'Client Addr' },
          { prop: 'language', label: 'Language' },
          { prop: 'version', label: 'Version' }
        ],
        data: [],
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
      const {
        result: { consumerProgressList, consumerStatusList, consumerZmsRegisterList, consumeTps, diffTotal }
      } = await consumerStatusAll(params)
      this.maintable.data = consumerProgressList
      this.consumerZmsRegister.data = consumerZmsRegisterList
      this.consumerStatus.data = consumerStatusList
      this.consumeTps = consumeTps
      this.diffTotal = diffTotal
      this.maintable.loading = false
    }
  }
}
</script>
