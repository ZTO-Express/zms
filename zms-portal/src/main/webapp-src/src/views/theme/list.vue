<template>
  <div class="l-page l-page--flex theme-application">
    <div class="page-nav">主题管理</div>
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
              style="margin-left:10px;width:300px"
              size="mini"
              placeholder="搜索  ( 主题 、申请人 )"
              v-model="keyWord"
              @change="searchMaintable"
            ></el-input>
          </div>
          <div style="float: right;">
            <el-button v-bind="GLOBAL.button" icon="el-icon-circle-plus" @click="operateOpen">
              新增
            </el-button>
            <el-button v-bind="GLOBAL.button" type="" icon="el-icon-bell" @click="handleSendMsg">
              发消息
            </el-button>
          </div>
          <!-- <el-button v-bind="GLOBAL.button" icon="el-icon-sort" @click="handleSynchronization">
            同步主题
          </el-button> -->
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
                          :disabled="!(checkPerm(['admin']) || scope.row.applicant === userInfo.realName)"
                          type="text"
                          size="mini"
                          icon="el-icon-edit"
                          @click="operateEdit(scope.row)"
                        >
                          <span>编辑</span>
                        </el-button>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <el-button
                          :disabled="!(checkPerm(['admin']) || scope.row.applicant === userInfo.realName)"
                          type="text"
                          size="mini"
                          icon="el-icon-delete"
                          @click="handleDelete(scope.row)"
                        >
                          <span>删除</span>
                        </el-button>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <el-button
                          :disabled="!checkPerm(['admin'])"
                          type="text"
                          size="mini"
                          icon="el-icon-s-check"
                          @click="approval(scope.row)"
                        >
                          <span>审批</span>
                        </el-button>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <el-button
                          :disabled="!(checkPerm(['admin']) && scope.row.status === 1)"
                          type="text"
                          size="mini"
                          icon="el-icon-setting"
                          @click="configure(scope.row)"
                        >
                          <span>配置</span>
                        </el-button>
                      </el-dropdown-item>
                      <el-dropdown-item>
                        <el-button
                          :disabled="!(checkPerm(['admin']) && scope.row.status === 1)"
                          type="text"
                          size="mini"
                          icon="el-icon-sort"
                          @click="handleSynchronization(scope.row)"
                        >
                          <span>同步</span>
                        </el-button>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
              </el-table-column>
            </template>
            <template slot="insert2">
              <el-table-column v-bind="GLOBAL.column" fixed="left" label="主题名" width="250">
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
                  <div v-if="scope.row.environments" class="service-column">
                    <el-popover
                      v-for="(item, key) in scope.row.environments"
                      :key="key"
                      placement="right"
                      width="200"
                      trigger="hover"
                    >
                      <el-card shadow="never" class="zms-host-card">
                        <div v-for="(card, key) in scope.row.environments" :key="key">
                          <span>{{ card.environmentName + ' : ' }}</span>
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
    <!-- 配置弹窗 -->
    <op-freight
      :dialog="configOptions.visible"
      :modalTitle="configOptions.diatitle"
      :loading="configOptions.loading"
      @dealDialog="closeConfigDia"
    >
      <base-form
        :forms="configOptions.forms"
        :ref="configOptions.ref"
        :current-form-value="configOptions.currentFormValue"
        label-width="120px"
      />
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="saveConfigData">保存</el-button>
      </template>
    </op-freight>
    <!-- 发消息 -->
    <op-freight
      :dialog="sendMsgOptions.visible"
      :modalTitle="sendMsgOptions.diatitle"
      :loading="sendMsgOptions.loading"
      @dealDialog="closeMsgDia"
    >
      <base-form
        :forms="sendMsgOptions.forms"
        :ref="sendMsgOptions.ref"
        :current-form-value="sendMsgOptions.currentFormValue"
        label-width="120px"
      />
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="saveMsgData">保存</el-button>
      </template>
    </op-freight>
    <!-- 同步 -->
    <op-freight
      :dialog="synchronizationOptions.visible"
      :modalTitle="synchronizationOptions.diatitle"
      :loading="synchronizationOptions.loading"
      @dealDialog="closeSynchronizationOptionsDia"
    >
      <base-form
        :forms="synchronizationOptions.forms"
        :ref="synchronizationOptions.ref"
        :current-form-value="synchronizationOptions.currentFormValue"
        label-width="120px"
      />
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="saveSynchronizationData">保存</el-button>
      </template>
    </op-freight>
  </div>
</template>
<script>
import { mapState, mapActions } from 'vuex'
import { common, baseStructure } from '@/mixins/index'
// 搜索相关
import search from './list-search'
// 新增编辑相关
import operate from './list-operate'
// 审批相关
import approval from './list-approval'
// 配置相关
import configure from './list-configure'
// 发消息
import sendMsg from './send-msg'

import OpFreight from '@/modules/modal/freight/OpFreight'
// 请求接口
import { themeQuery, deleteTheme, syncTheme } from '@/api/theme'
// 工具方法
import { util } from '@/utils/util'

export default {
  name: 'themeList',
  mixins: [common, baseStructure, search, operate, approval, configure, sendMsg],
  components: {
    OpFreight
  },
  data() {
    return {
      keyWord: '',
      status: '',
      maintable: {
        columns: [
          { type: 'index', width: 40, fixed: 'left' },
          { prop: 'applicant', label: '申请人', width: 150 },
          { prop: 'alertEmails', label: '邮箱通知列表', width: 250 },
          { prop: 'domain', label: '申请域(appId)', width: 200 },
          { prop: 'tps', label: '发送速度', width: 100 },
          { prop: 'msgszie', label: '消息体', width: 100 },
          { prop: 'partitions', label: '分片数', width: 80 },
          { prop: 'memo', label: '备注', 'min-width': 300 },
          { prop: 'modifyDate', label: '修改时间', width: 160 }
        ],
        data: [],
        insertNum: 1,
        insertNum2: 1,
        insertNum3: 1,
        insertNum4: 4,
        loading: false,
        currentPage: 1,
        pageSize: 25,
        pagePagination: true
      },
      synchronizationForms: [
        {
          prop: 'environments',
          label: '环境',
          itemType: 'checkbox',
          options: []
        }
      ],
      // 主题同步表单
      synchronizationOptions: {
        visible: false,
        ref: 'synchronizationForm',
        diatitle: '将主题同步到其他环境',
        forms: [],
        currentFormValue: {},
        loading: false
      }
    }
  },
  computed: {
    ...mapState({
      envListData: state => state.theme.envListData
    })
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
    ...mapActions({
      getEnvList: 'theme/getEnvList'
    }),

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
      const { modifyDate, status, environments } = item
      Object.assign(item, {
        statusTxt: status === 0 || status === 2 ? '待审批' : '已审批',
        statusClass: status === 0 || status === 2 ? 'wait' : 'done',
        modifyDate: util.dateToStr(new Date(modifyDate), 3),
        envTxt: environments ? environments.map(item => item.environmentName).join(' ') : ''
      })
      return item
    },
    // 查看主题详情
    viewDetail(row) {
      this.$router.push({ name: 'themeDetail', params: { id: row.id } })
    },
    // 删除主题
    handleDelete(row) {
      this.isDo({
        title: `是否确定是删除主题【${row.name}】？`,
        cb: async () => {
          this.maintable.loading = true
          await deleteTheme(row.id)
          // 提示
          this.message('success', '删除成功')
          // reload table
          this.loadTabledata()
        }
      })
    },
    // 获取环境信息
    async synchronizationEnvList() {
      let _envListData = this.envListData
      this.synchronizationOptions.loading = true
      if (_envListData === undefined) {
        await this.getEnvList()
      }
      this.synchronizationOptions.loading = false
    },
    // 同步主题
    async handleSynchronization(row) {
      await this.synchronizationEnvList()
      // 编辑表单的当前值
      let _row = { ...row }
      if (Array.isArray(_row.environments)) {
        _row.originEnvironments = util.deepCopy(row.environments)
        _row.environments = row.environments.map(item => {
          return item.environmentId
        })
      }
      this.synchronizationOptions.forms = [...this.synchronizationForms]
      this.handleDia({
        options: this.synchronizationOptions,
        row: _row,
        cb: () => {
          // 环境列表
          Object.assign(this.synchronizationOptions.forms[0], {
            options: this.envListData.map(item => {
              return {
                label: item.environmentName,
                value: item.id,
                disabled: _row.environments.indexOf(item.id) > -1,
                ...item
              }
            }),
            change: this.setCluster
          })
          // 集群列表
          const _clusterForm = this.envListData.map(item => {
            const index = _row.environments.indexOf(item.id)
            // 经过审批的环境
            if (index > -1) {
              return {
                prop: 'originCluster_' + item.id,
                label: item.environmentName + '集群',
                defaultValue: _row.originEnvironments[index].serverName,
                disabled: true
              }
            } else {
              return {
                prop: 'cluster_' + item.id,
                label: item.environmentName + '集群',
                apiUrl: 'cluster_list',
                itemType: 'remoteselect',
                labelkeyname: 'serverName',
                valuekeyname: 'id',
                visible: false,
                remoteParams: { envId: item.id },
                rules: [{ required: true, message: '请选择' }]
              }
            }
          })
          this.synchronizationOptions.forms.push(..._clusterForm)
        }
      })
    },
    // 同步环境选中-添加集群表单
    setCluster(ids) {
      const _ids = ids.map(id => 'cluster_' + id)
      const _proplist = this.synchronizationOptions.forms.map(item => item.prop)
      _proplist.forEach((item, i) => {
        if (item.indexOf('cluster_') > -1) {
          this.synchronizationOptions.forms[i].visible = false
          if (_ids.indexOf(item) > -1) {
            this.synchronizationOptions.forms[i].visible = true
          }
        }
      })
    },
    // 关闭弹窗
    closeSynchronizationOptionsDia() {
      Object.assign(this.synchronizationOptions, {
        visible: false
      })
    },
    saveSynchronizationData() {
      this.handleValidate({
        refname: 'synchronizationForm',
        cb: async value => {
          const _value = []
          const data = Object.keys(value).filter(item => item.indexOf('cluster_') > -1)
          if (data.length) {
            data.map(item => {
              const _id = item.split('_')[1]
              _value.push({ environmentId: _id, serviceId: value[item] })
            })
            this.synchronizationOptions.loading = true
            const id = this.synchronizationOptions.currentFormValue.id
            await syncTheme(id, _value)
            this.synchronizationOptions.loading = false
            // 重新load表格数据
            this.loadTabledata()
          }
          this.message('success', '保存成功')
          // 关闭弹窗
          this.closeSynchronizationOptionsDia()
        }
      })
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
