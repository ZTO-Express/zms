<template>
  <div class="l-page l-page--flex theme-application">
    <div class="page-nav">告警管理</div>

    <div class="l-page__content">
      <div class="content--padding">
        <div class="content__header">
          <el-button style="float: right;" v-bind="GLOBAL.button" icon="el-icon-circle-plus" @click="operateOpen">
            新增
          </el-button>
          <div style="float: left;">
            <el-input
              style="width:300px"
              size="mini"
              placeholder="搜索  ( 告警名称 、告警类型 、接收人 )"
              v-model="keyWord"
              @change="searchMaintable"
            ></el-input>
          </div>
        </div>
        <div class="content__body alert_list">
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
                        <el-button type="text" size="mini" icon="el-icon-s-open" @click="operateEdit(scope.row, 0)">
                          复制
                        </el-button>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <el-button
                          type="text"
                          size="mini"
                          icon="el-icon-edit"
                          :disabled="!(checkPerm(['admin']) || checkUser(scope.row.alertUser))"
                          @click="operateEdit(scope.row, 1)"
                        >
                          编辑
                        </el-button>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <el-button
                          type="text"
                          size="mini"
                          icon="el-icon-delete"
                          :disabled="!checkPerm(['admin'])"
                          @click="handleDelete(scope.row)"
                        >
                          删除
                        </el-button>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
              </el-table-column>
            </template>
            <template slot="insert2">
              <el-table-column v-bind="GLOBAL.column" label="环境" width="150">
                <template slot-scope="scope">
                  <div v-if="scope.row.environmentRefDtos.length" class="service-column">
                    <el-popover
                      v-for="(item, key) in scope.row.environmentRefDtos"
                      :key="key"
                      placement="right"
                      width="200"
                      trigger="hover"
                    >
                      <el-card shadow="never" class="zms-host-card">
                        <div v-for="(card, key) in scope.row.environmentRefDtos" :key="key">
                          <el-tag size="mini" :type="envType(card.environmentId)">{{ card.environmentName }}:</el-tag>
                          <el-tooltip
                            v-if="card.serverType"
                            :content="card.serverType.toLowerCase()"
                            placement="top"
                            effect="light"
                          >
                            <svg-icon :icon-class="serviceIconClass(card.serverType)"></svg-icon>
                          </el-tooltip>
                          <span class="card-item" @click="goServiceDetail(card.serviceId, 'state')">
                            {{ card.serverName }}
                          </span>
                        </div>
                      </el-card>
                      <el-tag
                        size="mini"
                        slot="reference"
                        :type="envType(item.environmentId)"
                        style="margin-left: 3px;"
                      >
                        {{ item.environmentName }}
                      </el-tag>
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
    <!-- 新增复制编辑弹窗 -->
    <op-freight
      :dialog="diaOptions.visible"
      :modalTitle="diaOptions.diatitle[diaOptions.status]"
      :loading="diaOptions.loading"
      width="54%"
      @dealDialog="closeDia"
    >
      <base-form
        :forms="diaOptions.forms"
        :ref="diaOptions.ref"
        :inline="diaOptions.inline"
        :current-form-value="diaOptions.currentFormValue"
        :label-width="diaOptions.labelWidth"
        :insert-num="diaOptions.insertNum"
      >
        <template slot="insert">
          <el-select v-model="timeUnit" placeholder="请选择">
            <el-option label="分钟" value="m"></el-option>
            <el-option label="小时" value="h"></el-option>
          </el-select>
        </template>
      </base-form>
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="preview">预览告警信息</el-button>
        <el-button v-bind="GLOBAL.button" @click="saveData">保存</el-button>
      </template>
    </op-freight>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import OpFreight from '@/modules/modal/freight/OpFreight'
// 搜索相关
import search from './list-search'
// 新增复制编辑相关
import operate from './list-operate'
// 请求接口
import { alertsQuery, deleteAlert } from '@/api/alert'

export default {
  name: 'noticeList',
  mixins: [common, baseStructure, search, operate],
  components: {
    OpFreight
  },
  data() {
    return {
      keyWord: '',
      timeUnit: 'm',
      maintable: {
        columns: [
          { type: 'index', width: 40, fixed: 'left' },
          { prop: 'name', label: '告警名称', width: 280, fixed: 'left' },
          { prop: 'type', label: '类型', width: 100 },
          { prop: 'effectTxt', label: '生效', width: 100 },
          { prop: 'field', label: '字段', width: 150 },
          { prop: 'operator', label: '阀值比较符', width: 100 },
          { prop: 'target', label: '阀值', width: 100 },
          { prop: 'scope', label: '时间间隔', width: 100 },
          { prop: 'triggerOperator', label: '次数比较符', width: 100 },
          { prop: 'triggerTimes', label: '触发次数', width: 100 },
          { prop: 'alertUser', label: '通知人', 'min-width': 150 },
          { prop: 'alertMobile', label: '电话', width: 200 }
        ],
        data: [],
        currentPage: 1,
        pageSize: 25,
        total: 0,
        pagePagination: true,
        insertNum: 1,
        insertNum2: 5,
        loading: false
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
      const res = await alertsQuery(params)
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
      const { effect, environmentRefDtos } = item
      Object.assign(item, {
        effectTxt: effect === true ? '是' : '否',
        envTxt: environmentRefDtos ? environmentRefDtos.map(item => item.environmentName).join(' ') : ''
      })
      return item
    },
    // 删除告警数据
    handleDelete(row) {
      this.isDo({
        title: `是否确定是删除告警【${row.name}】？删除后不可恢复！`,
        cb: async () => {
          this.maintable.loading = true
          await deleteAlert(row.id)
          // 提示
          this.message('success', '删除成功')
          // reload table
          this.loadTabledata()
        }
      })
    },
    envType(envId) {
      let i = envId % 5
      switch (i) {
        case 0:
          return ''
        case 1:
          return 'success'
        case 2:
          return 'info'
        case 3:
          return 'warning'
        case 4:
          return 'danger'
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
<style lang="scss">
.alert_list {
  .blue {
    color: #419dff;
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
