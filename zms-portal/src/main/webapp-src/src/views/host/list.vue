<template>
  <div class="l-page l-page--flex theme-application">
    <div class="page-nav">主机管理</div>
    <div class="l-page__content">
      <div class="content--padding">
        <div class="content__header">
          <div style="float: right">
            <el-button type="primary" size="mini" @click="handleEnabled" v-if="checkPerm(['admin'])">
              启用主机
            </el-button>
            <el-button type="primary" size="mini" @click="handleDisabled" v-if="checkPerm(['admin'])">
              停用主机
            </el-button>
            <el-button
              style="float:right;position:relative;top:2px;"
              size="mini"
              type="primary"
              @click="addHost"
              v-if="checkPerm(['admin'])"
            >
              向环境添加主机
            </el-button>
          </div>
          <div style="float: left;">
            <el-input
              style="padding:float:right;width:200px; "
              size="mini"
              placeholder="搜索"
              v-model="queryStr"
            ></el-input>
          </div>
        </div>
        <div class="content__body">
          <table-pagination v-bind="maintable" @changePage="pageHandler">
            <template slot="insert">
              <el-table-column v-bind="GLOBAL.column" label="主机名称" width="250">
                <template slot-scope="scope">
                  <color-word type="link" @click="viewDetail(scope.row)">
                    {{ scope.row.hostName }}
                  </color-word>
                </template>
              </el-table-column>
            </template>
            <template slot="insert2">
              <el-table-column v-bind="GLOBAL.column" label="服务" width="250">
                <template slot-scope="scope">
                  <div v-if="scope.row.services" class="service-column">
                    <el-popover v-if="scope.row.services.length" placement="right" width="200" trigger="click">
                      <el-card shadow="never" class="zms-host-card">
                        <div v-for="(item, key) in scope.row.services" :key="key">
                          <el-tooltip :content="item.serverType.toLowerCase()" placement="top" effect="light">
                            <svg-icon :icon-class="serviceIconClass(item.serverType)"></svg-icon>
                          </el-tooltip>
                          <span
                            class="card-item"
                            @click="goInstanceDetail(item.id)"
                            v-if="
                              checkPerm(['admin']) ||
                                item.serverType === 'KAFKA' ||
                                (item.serverType === 'ROCKETMQ' && item.instanceType === 'BROKER')
                            "
                          >
                            {{ item.instanceName }}
                          </span>
                          <span v-else style="margin-left: 4px">{{ item.instanceName }}</span>
                        </div>
                      </el-card>
                      <el-button style="border:0" slot="reference">
                        <span>{{ scope.row.services.length || 0 }} Service(s)</span>
                        <i class="el-icon-arrow-right el-icon--right"></i>
                      </el-button>
                    </el-popover>
                    <span v-else>--</span>
                  </div>
                  <div v-else>{{ scope.row.services | isEmpty }}</div>
                </template>
              </el-table-column>
            </template>
          </table-pagination>
        </div>
      </div>
      <op-message
        :dialog="dialog"
        :modalTitle="modalTitle"
        @dealDialog="dialog = false"
        :loading="loading"
        width="800px"
      >
        <template>
          <el-row style="margin-bottom: 13px">
            <span>执行安装脚本：</span>
          </el-row>
          <el-row style="margin-bottom: 13px">
            <span style="color:red">1. 在主机使用root或sudo权限的用户安装脚本。</span>
          </el-row>
          <el-row style="margin-bottom: 20px">
            <el-input style="width: 700px" v-model="formData.installUrl" :disabled="true"></el-input>
            <i
              style="margin-left: 10px; font-size:20px"
              class="el-icon-document-copy"
              @click="copy(formData.installUrl)"
            ></i>
          </el-row>
          <el-row style="margin-bottom: 13px">
            <span style="color:red">
              2. 初始化主机建议打开所需防火墙端口 [2000-9999] [10900-10999] [20910-20999] 。
            </span>
          </el-row>
        </template>
      </op-message>
    </div>
  </div>
</template>
<script>
// import _ from 'lodash'
import { common, baseStructure } from '@/mixins/index'
import { hostListQuery, hostEnabled, hostDisabled, getInstallUrl } from '@/api/host'
import OpMessage from '@/modules/modal/freight/OpMessage'

// 工具方法
import { util } from '@/utils/util'
export default {
  name: 'list',
  mixins: [common, baseStructure],
  components: {
    OpMessage
  },
  data() {
    return {
      selections: [],
      total: 0,
      loading: false,
      queryStr: '',
      envId: 0,
      hostIds: [],
      hostName: '',
      modalTitle: '',
      formData: { installUrl: '' },
      dialog: false,
      maintable: {
        ref: 'maintable',
        columns: [
          { type: 'selection', width: 50, fixed: 'left' },
          { prop: 'environmentName', label: '环境', width: 250 },
          { prop: 'hostIp', label: 'IP', width: 150 },
          { prop: 'hostStatusTxt', label: '主机状态', width: 200 },
          { prop: 'lastMonitorTime', label: '上次检测信号时间', width: 150 },
          { prop: 'totalMem', label: '内存', 'min-width': 300 },
          { prop: 'cpuRate', label: 'CPU占用率', width: 160 }
        ],
        data: [],
        insertNum: 1,
        insertNum2: 1,
        loading: false,
        pageIndex: 1,
        pageSize: 25,
        currentPage: 1,
        pagePagination: true
      }
    }
  },
  created() {
    const envId = this.$route.params.envId
    this.envId = envId
    this.pageHandler()
  },
  watch: {
    queryStr: function(n) {
      // if (!_.trim(n)) return
      this.queryStr = n
      this.pageHandler()
    }
  },
  methods: {
    copy(data) {
      let url = data
      let oInput = document.createElement('input')
      oInput.value = url
      document.body.appendChild(oInput)
      oInput.select() // 选择对象;
      document.execCommand('Copy') // 执行浏览器复制命令
      this.$message({
        message: '复制成功',
        type: 'success'
      })
      oInput.remove()
    },
    closeHandle() {
      this.dialog = false
    },
    //向环境添加新主机
    async addHost() {
      this.modalTitle = '脚本安装'
      const res = await getInstallUrl(this.envId)
      this.formData.installUrl = res.result
      this.dialog = true
    },
    //主机启用
    handleEnabled() {
      this.selections = this.$refs.maintable.getTableSelections()
      const { selections } = this
      if (selections.length === 0 || selections.length < 1) {
        let msg = '请选择数据！'
        selections.length > 1 && (msg = '请选择一条数据！')
        this.message('error', msg)
        return
      }
      this.hostId = []
      this.hostName = ''
      let i = 0
      this.selections.forEach(element => {
        this.hostIds.push(element.id)
        this.hostName += element.hostName
        if (i < this.selections.length - 1) {
          this.hostName += ';\n'
          i++
        }
      })
      const params = { hostIds: this.hostIds }
      this.isDo({
        title: `您是否确定启用主机【${this.hostName}】？`,
        cb: async () => {
          await hostEnabled(this.envId, params)
          // 提示
          this.message('success', '启用成功')
          // reload table
          this.pageHandler()
        }
      })
    },
    //停用主机
    handleDisabled() {
      this.selections = this.$refs.maintable.getTableSelections()
      const { selections } = this
      if (selections.length === 0 || selections.length < 1) {
        let msg = '请选择数据！'
        selections.length > 1 && (msg = '请选择一条数据！')
        this.message('error', msg)
        return
      }
      this.hostId = []
      this.hostName = ''
      this.selections.forEach(element => {
        this.hostIds.push(element.id)
        this.hostName += element.hostName + ';\n'
      })
      const params = { hostIds: this.hostIds }
      this.isDo({
        title: `您是否确定停用主机【${this.hostName}】？`,
        cb: async () => {
          await hostDisabled(this.envId, params)
          // 提示
          this.message('success', '停用成功')
          // reload table
          this.pageHandler()
        }
      })
    },
    // 弹窗提示
    isDo({ cb, title, catchcb = () => {} }) {
      this.$confirm(title, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(cb)
        .catch(catchcb)
    },
    // 提示
    message(type, msg) {
      this.$message({
        message: msg,
        type
      })
    },
    //实例详情
    goInstanceDetail(id) {
      this.$router.push({ name: 'instanceHome', params: { id: id } })
    },
    // 查看主机详情
    viewDetail(row) {
      this.$router.push({ name: 'hostDetail', params: { id: row.id, envId: row.environmentId } })
    },
    pageHandler(params = {}) {
      let par = this.assembleData(params)
      this.queryEvent(par)
    },
    assembleData(params = {}) {
      params.envId = this.envId
      params.hostIp = this.queryStr
      return params
    },
    queryEvent(params = {}) {
      this.maintable.loading = true
      if (!params.currentPage) {
        params.currentPage = this.maintable.currentPage
        params.pageSize = this.maintable.pageSize
      }
      hostListQuery(params)
        .then(res => {
          this.maintable.loading = false
          let { result } = res
          result.records.forEach(item => {
            const { lastMonitorTime, hostStatus } = item
            Object.assign(item, {
              lastMonitorTime: lastMonitorTime == null ? '' : util.dateToStr(new Date(lastMonitorTime), 3),
              hostStatusTxt: hostStatus === 'ENABLE' ? '可用' : '禁用'
            })
            return item
          })
          if (result) {
            this.maintable.data = result.records || []
            this.maintable.currentPage = result.currentPage
            this.maintable.pageSize = result.pageSize
            this.maintable.total = result.total
          }
        })
        .finally(() => {
          this.maintable.loading = false
        })
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
  },
  mounted() {}
}
</script>
<style lang="scss">
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
