<template>
  <div class="content__wrap">
    <div class="content__header">
      <div>
        <div style="display: inline-block" v-if="checkPerm(['admin'])">
          <el-dropdown trigger="click">
            <el-button v-bind="GLOBAL.button">
              已选定的操作
              <i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="handleStart">启动</el-dropdown-item>
              <el-dropdown-item @click.native="handleStop">停止</el-dropdown-item>
              <el-dropdown-item @click.native="handleReStart">重启</el-dropdown-item>
              <el-dropdown-item divided @click.native="handleDelete">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-button v-if="addInstanceFlg" v-bind="GLOBAL.button" style="margin-left: 10px" @click="addInstance()">
            添加服务节点
          </el-button>
        </div>
        <div style="float: right">
          <el-input
            size="mini"
            style="width: 200px;"
            placeholder="搜索"
            v-model="searchStr"
            @input="searchMaintable"
          ></el-input>
        </div>
      </div>
    </div>
    <div class="content__body">
      <table-pagination v-bind="maintable" :data-handler="handleTableData">
        <template slot="insert">
          <el-table-column v-bind="GLOBAL.column" label="节点名称" width="350">
            <template slot-scope="scope">
              <color-word @click="goInstanceDetail(scope.row.id)" v-if="scope.row.instanceDetail">
                {{ scope.row.instanceName }}
              </color-word>
              <span v-else>{{ scope.row.instanceName }}</span>
            </template>
          </el-table-column>
        </template>
        <template slot="insert2">
          <el-table-column v-bind="GLOBAL.column" label="节点状态" width="250">
            <template slot-scope="scope">
              {{ scope.row.instanceStatusTxt }}
              <span style="color: #55a4e3">
                {{ scope.row.publishDescTxt }}
              </span>
            </template>
          </el-table-column>
        </template>
        <template slot="insert3">
          <el-table-column v-bind="GLOBAL.column" label="主机" width="350">
            <template slot-scope="scope">
              <color-word @click="goHostDetail(scope.row.hostId)">
                {{ scope.row.hostName }}
              </color-word>
            </template>
          </el-table-column>
        </template>
      </table-pagination>
    </div>
    <el-dialog title="停止进程" :visible.sync="dialogStopTableVisible" :before-close="closeStopDialog" width="800px">
      <instance-stop ref="instanceStop" v-if="instanceStopFlg" :instance-ids="instanceIds"></instance-stop>
    </el-dialog>
    <el-dialog title="启动进程" :visible.sync="dialogStartTableVisible" :before-close="closeStartDialog" width="800px">
      <instance-start ref="instanceStart" v-if="instanceStartFlg" :instance-ids="instanceIds"></instance-start>
      <div class="content_mini_button"></div>
    </el-dialog>
    <el-dialog
      title="重启进程"
      :visible.sync="dialogReStartTableVisible"
      :before-close="closeReStartDialog"
      width="800px"
    >
      <instance-restart
        v-if="instanceReStartFlg"
        ref="instanceReStart"
        :server-id="serverId"
        :instance-ids="instanceIds"
        @cancelRestart="closeReStartDialog"
      ></instance-restart>
    </el-dialog>
  </div>
</template>

<script>
import { common, baseStructure } from '@/mixins/index'
import instanceStop from './instance/instance-stop'
import instanceStart from './instance/instance-start'
import instanceRestart from './instance/instance-restart'
import { queryHostServiceInstance, deleteServiceInstance } from '@/api/service'
// 工具方法
import { util } from '@/utils/util'
export default {
  name: 'instanceList',
  mixins: [common, baseStructure],
  components: {
    instanceStop,
    instanceStart,
    instanceRestart
  },
  props: {
    serverId: Number,
    envId: Number,
    serverName: String,
    serverType: String
  },
  data() {
    return {
      // instanceStopFlg,
      dialogStopTableVisible: false,
      dialogStartTableVisible: false,
      dialogReStartTableVisible: false,
      instanceReStartFlg: false,
      instanceStartFlg: false,
      instanceStopFlg: false,
      searchStr: '',
      allTableData: [],
      maintable: {
        ref: 'maintable',
        columns: [
          { type: 'selection', width: 50 },
          { prop: 'hostStatusTxt', label: '主机状态' },
          { prop: 'lastMonitorTime', label: '服务检测时间' }
        ],
        data: [],
        insertNum: 1,
        insertNum2: 1,
        insertNum3: 1,
        loading: false
      },
      instanceIds: Array,
      addInstanceFlg: false,
      instanceStatus: {
        START: '已启动',
        STOP: '已停止',
        STARTING: '启动中'
      }
    }
  },
  created() {
    this.addInstanceFlg =
      this.serverType === 'ZOOKEEPER' || this.serverType === 'KAFKA' || this.serverType === 'ROCKETMQ'

    this.loadTabledata(this.serverId)
  },
  methods: {
    // 获取表格数据
    async loadTabledata(id) {
      const params = { serviceId: id }
      this.maintable.loading = true
      const res = await queryHostServiceInstance(params)
      if (res.result) {
        this.allTableData = res.result
        this.maintable.data = res.result
        this.maintable.loading = false
      }
    },
    handleTableData(item) {
      const { instanceStatus, hostStatus, publishDesc, instanceType, lastMonitorTime } = item
      const isAdmin = this.checkPerm(['admin'])
      Object.assign(item, {
        instanceStatusTxt: this.instanceStatus[instanceStatus],
        publishDescTxt: publishDesc ? ' ('.concat(publishDesc).concat(')') : '',
        hostStatusTxt: hostStatus === 'ENABLE' ? '可用' : '禁用',
        lastMonitorTime: lastMonitorTime == null ? '' : util.dateToStr(new Date(lastMonitorTime), 3),
        instanceDetail:
          isAdmin || this.serverType === 'KAFKA' || (this.serverType === 'ROCKETMQ' && instanceType === 'BROKER')
      })
      return item
    },
    searchMaintable() {
      if (!this.searchStr) {
        this.maintable.data = this.allTableData
        return
      }
      const _this = this
      const NewItems = _this.allTableData.filter(item => {
        //过滤数组元素
        return item.instanceName.includes(this.searchStr)
      })
      this.maintable.data = NewItems
    },
    async handleDelete() {
      this.selections = this.$refs.maintable.getTableSelections()
      const { selections } = this
      if (selections.length === 0) {
        this.message('error', '请选择数据！')
        return
      }
      let ids = []
      const NewItems = selections.filter(item => {
        ids.push(item.id)
        return item.instanceStatus === 'START'
      })
      if (NewItems.length > 0) {
        this.message('error', NewItems.map(item => item.instanceName).join() + '服务节点不是停止状态！')
        return
      }
      this.isDo({
        title: `是否确定删除服务节点？`,
        cb: async () => {
          await deleteServiceInstance(ids)
          this.message('success', '删除成功')
          this.loadTabledata(this.serverId)
        }
      })
    },

    handleStart() {
      this.selections = this.$refs.maintable.getTableSelections()
      const { selections } = this
      if (selections.length === 0) {
        this.message('error', '请选择数据！')
        return
      }
      let ids = []
      const NewItems = selections.filter(item => {
        ids.push(item.id)
        return item.instanceStatus === 'START' || item.instanceStatus === 'STARTING'
      })
      if (NewItems.length > 0) {
        this.message('error', '[' + NewItems.map(item => item.instanceName).join() + ']' + '已经存在启动的服务节点！')
        return
      }
      this.instanceIds = ids
      this.isDo({
        title: `是否确定启动服务节点？`,
        cb: async () => {
          this.dialogStartTableVisible = true
          this.instanceStartFlg = true
        }
      })
    },

    handleStop() {
      this.selections = this.$refs.maintable.getTableSelections()
      const { selections } = this
      if (selections.length === 0) {
        this.message('error', '请选择数据！')
        return
      }
      let ids = []
      const NewItems = selections.filter(item => {
        ids.push(item.id)
        return item.instanceStatus === 'STOP'
      })
      if (NewItems.length > 0) {
        this.message('error', NewItems.map(item => item.instanceName).join() + '服务节点不是启动状态！')
        return
      }
      this.instanceIds = ids
      this.isDo({
        title: `是否确定停止服务节点？`,
        cb: async () => {
          this.dialogStopTableVisible = true
          this.instanceStopFlg = true
        }
      })
    },
    handleReStart() {
      this.selections = this.$refs.maintable.getTableSelections()
      const { selections } = this
      if (selections.length === 0) {
        this.message('error', '请选择数据！')
        return
      }
      let ids = []
      const NewItems = selections.filter(item => {
        ids.push(item.id)
        return item.instanceStatus != 'START'
      })
      if (NewItems.length > 0) {
        this.message('error', NewItems.map(item => item.instanceName).join() + '服务节点不是启动状态！')
        return
      }
      this.instanceIds = ids
      this.isDo({
        title: `是否确定重启服务节点？`,
        cb: async () => {
          this.dialogReStartTableVisible = true
          this.instanceReStartFlg = true
        }
      })
    },
    closeStopDialog() {
      this.dialogStopTableVisible = false
      this.instanceStopFlg = false
      this.loadTabledata(this.serverId)
    },
    closeStartDialog() {
      this.dialogStartTableVisible = false
      this.instanceStartFlg = false
      this.loadTabledata(this.serverId)
    },
    closeReStartDialog() {
      this.dialogReStartTableVisible = false
      this.instanceReStartFlg = false
      this.loadTabledata(this.serverId)
    },
    goInstanceDetail(id) {
      this.$router.push({ name: 'instanceHome', params: { id: id } })
    },
    goHostDetail(hostId) {
      this.$router.push({ name: 'hostDetail', params: { id: hostId, envId: this.envId } })
    },
    checkServerType() {
      let serverType = this.serverType.toLowerCase()
      switch (serverType) {
        case 'kafka':
          return 'Kafka'
        case 'rocketmq':
          return 'Rocketmq'
        case 'zookeeper':
          return 'Zookeeper'
        default:
          return serverType
      }
    },
    addInstance() {
      let serverType = this.checkServerType()
      this.$router.push({
        name: 'instanceAddHome',
        params: {
          envId: this.envId,
          serverId: this.serverId,
          serverName: this.serverName,
          serverType: serverType
        }
      })
    }
  }
}
</script>
