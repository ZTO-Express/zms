<template>
  <div class="l-page l-page--flex theme-application">
    <div class="page-nav">消费组管理</div>
    <div class="l-page__content">
      <div class="content--padding">
        <div class="content__header">
          <div style="float: left;">
            状态
            <el-select @change="statusChange" v-model="status" v-bind="GLOBAL.select" size="mini">
              <el-option
                v-for="item in SearchStatus.options"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
            <el-input
              style="margin-left:15px;width:300px;"
              size="mini"
              placeholder="搜索  ( 消费组 、主题名 、申请人 )"
              v-model="keyWord"
              @change="searchMaintable"
            ></el-input>
          </div>
          <div style="float: right;">
            <el-button v-bind="GLOBAL.button" icon="el-icon-circle-plus" @click="operateOpen">
              新增
            </el-button>
            <el-button
              v-bind="GLOBAL.button"
              type=""
              icon="el-icon-refresh"
              @click="syncOpen"
              v-if="checkPerm(['admin'])"
            >
              多环境同步
            </el-button>
          </div>
        </div>

        <div class="content__body">
          <table-pagination v-bind="maintable" :data-handler="handleTableData" @changePage="pageHandler">
            <template slot="insert">
              <el-table-column label="操作" fixed="right" width="55" align="center">
                <template slot-scope="scope">
                  <el-dropdown trigger="click" size="medium">
                    <span class="el-dropdown-link">
                      <i class="el-icon-menu el-icon--right"></i>
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item>
                        <el-button
                          type="text"
                          size="mini"
                          icon="el-icon-edit"
                          :disabled="!(checkPerm(['admin']) || scope.row.applicant === userInfo.realName)"
                          @click="operateEdit(scope.row)"
                        >
                          编辑
                        </el-button>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <el-button
                          type="text"
                          size="mini"
                          icon="el-icon-delete"
                          :disabled="!(checkPerm(['admin']) || scope.row.applicant === userInfo.realName)"
                          @click="handleDelete(scope.row)"
                        >
                          删除
                        </el-button>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <el-button
                          type="text"
                          size="mini"
                          icon="el-icon-s-check"
                          :disabled="!checkPerm(['admin'])"
                          @click="approvalConsumer(scope.row)"
                        >
                          审批
                        </el-button>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <el-button
                          type="text"
                          size="mini"
                          icon="el-icon-setting"
                          :disabled="!(checkPerm(['admin']) && (scope.row.status === 1 || scope.row.status === 3))"
                          @click="handleConfig(scope.row)"
                        >
                          配置
                        </el-button>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
              </el-table-column>
            </template>
            <template slot="insert2">
              <el-table-column v-bind="GLOBAL.column" label="消费组" fixed="left" width="250">
                <template slot-scope="scope">
                  <color-word
                    type="link"
                    :disabled="scope.row.status === 0 || scope.row.status === 2"
                    @click="viewDetail(scope.row)"
                  >
                    {{ scope.row.name }}
                  </color-word>
                </template>
              </el-table-column>
            </template>
            <template slot="insert3">
              <el-table-column v-bind="GLOBAL.column" label="状态" width="100">
                <template slot-scope="scope">
                  <span :class="scope.row.statusClass">{{ scope.row.statusTxt }}</span>
                </template>
              </el-table-column>
            </template>
            <template slot="insert4">
              <el-table-column v-bind="GLOBAL.column" label="环境" width="200">
                <template slot-scope="scope">
                  <div v-if="scope.row.consumerEnvironmentRefVos.length" class="service-column">
                    <el-popover
                      v-for="(item, key) in scope.row.consumerEnvironmentRefVos"
                      placement="right"
                      width="200"
                      trigger="hover"
                      :key="key"
                    >
                      <el-card shadow="never" class="zms-host-card">
                        <div v-for="(card, key) in scope.row.consumerEnvironmentRefVos" :key="key">
                          <span>{{ card.environmentName + ' : ' }}</span>
                          <el-tooltip
                            v-if="card.clusterType"
                            :content="card.clusterType.toLowerCase()"
                            placement="top"
                            effect="light"
                          >
                            <svg-icon :icon-class="serviceIconClass(card.clusterType)"></svg-icon>
                          </el-tooltip>
                          <span class="card-item" @click="goServiceDetail(card.serviceId, 'state')">
                            {{ card.clusterName }}
                          </span>
                        </div>
                      </el-card>
                      <span slot="reference" class="content_envTxt" :class="envType(item.environmentId)">
                        {{ item.environmentName }}
                      </span>
                    </el-popover>
                  </div>
                  <div v-else>{{ scope.row.envTxt | isEmpty }}</div>
                </template>
              </el-table-column>
            </template>
          </table-pagination>
        </div>
      </div>
    </div>
    <!-- 新增编辑弹窗 -->
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
        <el-button v-bind="GLOBAL.button" @click="saveData">保存</el-button>
      </template>
    </op-freight>
    <!-- 审批弹窗 -->
    <op-freight
      :dialog="approvalOptions.visible"
      :modalTitle="approvalOptions.diatitle"
      :loading="approvalOptions.loading"
      @dealDialog="closeApprovalDia"
    >
      <base-form
        :forms="approvalOptions.forms"
        :ref="approvalOptions.ref"
        :current-form-value="approvalOptions.currentFormValue"
        label-width="120px"
      />
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="saveApprovalData">保存</el-button>
      </template>
    </op-freight>
    <!-- 配置 -->
    <op-freight
      :dialog="configOptions.visible"
      :modalTitle="configOptions.diatitle"
      :loading="configOptions.loading"
      @dealDialog="closeConfigOptions"
    >
      <base-form
        :forms="configOptions.forms"
        :ref="configOptions.ref"
        :current-form-value="configOptions.currentFormValue"
        label-width="120px"
      />
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="saveConfigData()">保存</el-button>
      </template>
    </op-freight>
    <!-- 开始同步弹窗 -->
    <op-freight
      :dialog="diaSyncOptions.visible"
      :modalTitle="diaSyncOptions.diatitle"
      :loading="diaSyncOptions.loading"
      @dealDialog="closeSyncDia"
    >
      <base-form
        :forms="diaSyncOptions.forms"
        :ref="diaSyncOptions.ref"
        :current-form-value="diaSyncOptions.currentFormValue"
        label-width="90px"
      />
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="beginSync">开始同步</el-button>
      </template>
    </op-freight>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
// 搜索相关
import search from './list-search'
// 新增编辑相关
import operate from './list-operate'
// 审批相关
import approval from './list-approval'
// 审批相关
import handleConfig from './list-config'

import syncEnv from './list-sync'

import OpFreight from '@/modules/modal/freight/OpFreight'
// 请求接口
import { consumerQuery } from '@/api/consumer'
// 工具方法
import { util } from '@/utils/util'

export default {
  name: 'consumerList',
  mixins: [common, baseStructure, search, operate, approval, handleConfig, syncEnv],
  components: {
    OpFreight
  },
  data() {
    return {
      keyWord: '',
      status: '',
      maintable: {
        ref: 'maintable',
        columns: [
          { type: 'selection', width: 50, fixed: 'left' },
          { prop: 'broadcastTxt', label: '是否广播', width: 80 },
          { prop: 'topicName', label: '主题名', width: 250 },
          { prop: 'applicant', label: '申请人', width: 150 },
          { prop: 'domain', label: '申请域(appId)', width: 200 },
          { prop: 'delayThreadhold', label: '积压阈值', width: 150 },
          { prop: 'memo', label: '备注', 'min-width': 300 },
          { prop: 'modifyDate', label: '修改时间', width: 160 }
        ],
        data: [],
        insertNum: 1,
        insertNum2: 1,
        insertNum3: 1,
        insertNum4: 5,
        loading: false,
        pageIndex: 1,
        pageSize: 25,
        currentPage: 1,
        pagePagination: true
      }
    }
  },

  created() {
    this.loadTabledata()
  },
  methods: {
    goServiceDetail(id, tab) {
      this.$router.push({ name: 'serviceHome', params: { id: id, tab: tab } })
    },
    pageHandler(params = {}) {
      let par = this.assembleData(params)
      this.loadTabledata(par)
    },
    assembleData(params = {}) {
      params.status = this.status
      params.keyWord = this.keyWord
      return params
    },
    statusChange(value) {
      this.status = value
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
      const { modifyDate, status, broadcast, consumerEnvironmentRefVos } = item
      Object.assign(item, {
        statusTxt: status === 0 || status === 2 ? '待审批' : '已审批',
        statusClass: status === 0 || status === 2 ? 'wait' : 'done',
        broadcastTxt: broadcast == true ? '是' : '否',
        modifyDate: util.dateToStr(new Date(modifyDate), 3),
        envTxt: consumerEnvironmentRefVos ? consumerEnvironmentRefVos.map(item => item.environmentName).join(' ') : ''
      })
      return item
    },
    // 查看消费组详情
    viewDetail(row) {
      this.$router.push({ name: 'consumerDetail', params: { id: row.id } })
    },
    envType(envId) {
      let i = envId % 5
      switch (i) {
        case 0:
          return 'content_envTxt_color_0'
        case 1:
          return 'content_envTxt_color_1'
        case 2:
          return 'content_envTxt_color_2'
        case 3:
          return 'content_envTxt_color_3'
        case 4:
          return 'content_envTxt_color_4'
        default:
          return ''
      }
    },
    serviceIconClass(serviceType) {
      switch (serviceType) {
        case 'ZOOKEEPER':
          return 'Zookeeper'
        case 'INFLUXDB':
          return 'influxDb'
        case 'ZMS_ALERT':
          return 'gaojing'
        case 'ZMS_COLLECTOR':
          return 'caiji'
        case 'ROCKETMQ':
          return 'RocketMQ'
        case 'KAFKA':
          return 'Kafka'
        case 'ZMS_BACKUP_CLUSTER':
          return 'backup'
        default:
          return ''
      }
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss">
.theme-application {
  .approvaled {
    color: #ccc;
  }
  .underApproval {
    color: #000;
  }
  .wait {
    color: #ff7900;
  }
  .done {
    color: #909399;
  }
  .blue {
    color: #419dff;
  }
  .content_envTxt {
    margin-left: 4px;
    padding: 3px;
    border-radius: 4px;
  }
  .content_envTxt_color_0 {
    border: 1px solid #07e2b8;
    color: #07e2b8;
  }
  .content_envTxt_color_1 {
    border: 1px solid #006aff;
    color: #006aff;
  }
  .content_envTxt_color_2 {
    border: 1px solid #ffb000;
    color: #ffb000;
  }
  .content_envTxt_color_3 {
    border: 1px solid #0bbc0b;
    color: #0bbc0b;
  }
  .content_envTxt_color_4 {
    border: 1px solid #8400ff;
    color: #8400ff;
  }
}
.zms-host {
  height: 100%;
  .zms-title,
  .zms-filter {
    min-height: 45px;
    padding: 0px 20px 0 1px;
    .zms-operate {
      float: right;
      .el-button {
        padding: 10px 15px;
      }
    }
  }
  .zms-title {
    line-height: 45px;
    -webkit-box-shadow: inset 0 -1px #dcdcdc;
    box-shadow: inset 0 -1px #dcdcdc;
    background-color: #f8f8f8;
    h3 {
      // float: left;
      margin-left: 15px;
      font-size: 16px;
      font-weight: normal;
    }
    .zms-operate {
      float: right;
      .el-button {
        padding: 6px;
      }
    }
  }
  .zms-filter {
    background: #f3f9fc;
    .el-form-item {
      margin: 0;
    }
    .el-textarea {
      padding: 12px 0;
    }
  }
  .zms-host-body {
    padding: 5px 15px;
    background: rgb(255, 255, 255);
    height: calc(100% - 45px);
    box-sizing: border-box;
    .service-column {
      .el-button {
        border: 0;
      }
    }
    .el-table__body tr {
      &:hover {
        td {
          .el-button {
            background: #f5f7fa;
          }
        }
      }
    }
    .zms-selected-operate {
      margin-bottom: 10px;
      .el-button {
        padding: 6px;
      }
    }
    .content__header {
      padding: 5px 0;
      height: 35px;
      line-height: 35px;
      // border-bottom: 1px dotted rgba(0, 0, 0, 0.2);
      // margin-left: 10px;
    }
  }
  .zms-pagination {
    padding: 10px 0;
    text-align: right;
  }
}
.zms-host-card {
  border: none;
  margin: -5px;
  .el-card__body {
    padding: 5px;
    line-height: 26px;
  }
  .card-item {
    color: #0b7fad;
    cursor: pointer;
    &:hover {
      color: #186485;
    }
  }
}
</style>
