<template>
  <div class="service-add-instance">
    <!-- <p class="content_header">自定义 {{ serverType }} 的角色分配</p> -->
    <p
      style="margin-bottom: 25px; line-height: 1.6; background: #f0f3f7;padding: 12px 20px;border-radius: 4px;font-size: 13px;"
    >
      您可以在此处自定义服务的节点分配，但请注意，如果分配不正确（例如，分配到某个主机上的服务节点太多），性能受到影响。
    </p>
    <div v-for="(item, index) in instanceType" :key="item.id">
      <el-row :span="35">
        <el-col :span="12">
          <p style="font-size: 16px">
            {{ serviceAddInstanceForm.instanceTypeDescMap[serverType + '_' + item] }}
          </p>
        </el-col>
        <el-col :span="12">
          <el-button
            class="content_button"
            plain
            :type="serviceAddInstanceForm.instanceTypeHostMap[item].buttonClass"
            @click="openDialog(item)"
          >
            {{ serviceAddInstanceForm.instanceTypeHostMap[item].buttonTxt }}
          </el-button>
        </el-col>
      </el-row>
      <el-divider v-if="index < instanceType.length - 1"></el-divider>
    </div>
    <el-dialog title="选择主机" :visible.sync="dialogTableVisible" width="70%">
      <el-input
        size="mini"
        class="content_input"
        placeholder="输入主机名称或IP地址"
        v-model="diaForm.input"
        @input="searchGridData"
      ></el-input>
      <el-table
        ref="multipleTable"
        tooltip-effect="dark"
        :data="serviceAddInstanceForm.gridData"
        @selection-change="handleSelectionChange"
        row-key="id"
        width="700"
      >
        <el-table-column type="selection" :reserve-selection="true" width="55"></el-table-column>
        <el-table-column property="hostName" label="主机名称" width="200"></el-table-column>
        <el-table-column property="hostIp" label="IP地址" width="150"></el-table-column>
        <el-table-column label="现有服务">
          <template slot-scope="scope">
            <span v-for="item in scope.row.services" :key="item.id" style="margin-right: 20px">
              <el-tooltip :content="item.serverType.toLowerCase()" placement="top" effect="light">
                <svg-icon :icon-class="serviceIconClass(item.serverType)"></svg-icon>
              </el-tooltip>
              <span style="margin-left: 2px">{{ item.instanceName }}</span>
            </span>
          </template>
        </el-table-column>
      </el-table>
      <div class="content_mini_button">
        <el-button size="mini" @click="dialogTableVisible = false">取消</el-button>
        <el-button style="margin-left: 20px;" v-bind="GLOBAL.button" @click="saveInstanceData">
          确定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import { listHostInstanceByEnvId } from '@/api/host'

export default {
  name: 'serviceAddInstance',
  mixins: [common, baseStructure],
  props: {
    envId: [Number, String],
    serverType: String,
    instanceType: Array
  },
  data() {
    return {
      serviceAddInstanceForm: {
        currentInstanceType: '',
        allTableData: [],
        gridData: [],
        instanceTypeHostMap: {},
        instanceList: [],
        instanceType: [],
        instanceTypeDescMap: {
          Rocketmq_NAMESVR: '选择 Rocketmq 集群 namesrv 节点',
          Rocketmq_BROKER: '选择 Rocketmq 集群 broker 节点',
          Kafka_BROKER: '选择 Kafka 集群节点',
          Zookeeper_INSTANCE: '选择 Zookeeper 集群节点',
          InfluxDB_INSTANCE: '选择 InfluxDB 服务节点',
          ZMSCollector_INSTANCE: '选择 ZMSCollector 服务节点',
          ZMSAlert_INSTANCE: '选择 ZMSAlert 服务节点',
          ZMSBackupCluster_INSTANCE: '选择 ZMSBackupCluster 服务节点'
        },
        serverTypeSpanMap: {
          Rocketmq: 9,
          Kafka: 6,
          Zookeeper: 7,
          InfluxDB: 6,
          zms_collector: 8,
          zms_alert: 7
        }
      },
      dialogTableVisible: false,
      diaForm: {
        input: '',
        checkbox: []
      }
    }
  },
  created() {
    this.loadHostInstanceByEnvId()
    this.initValue()
  },
  methods: {
    async loadHostInstanceByEnvId() {
      const res = await listHostInstanceByEnvId(this.envId)
      if (res.result) {
        this.serviceAddInstanceForm.allTableData = res.result
        this.serviceAddInstanceForm.gridData = res.result
      }
    },
    initValue() {
      this.instanceType.forEach(item => {
        this.serviceAddInstanceForm.instanceTypeHostMap[item] = {
          buttonTxt: undefined,
          buttonClass: 'info',
          multiSureSelection: [],
          searchStr: ''
        }
      })
    },
    currentInstanceType() {
      return this.serviceAddInstanceForm.currentInstanceType
    },
    handleSelectionChange(val) {
      this.diaForm.checkbox = val
    },
    openDialog(val) {
      this.serviceAddInstanceForm.currentInstanceType = val
      this.serviceAddInstanceForm.instanceTypeHostMap[this.currentInstanceType()].currentInstanceType = val
      this.dialogTableVisible = true
      this.clearSelection()
      this.toggleSelection()
    },
    clearSelection() {
      if (this.diaForm.checkbox.length > 0) {
        this.$refs.multipleTable.clearSelection()
      }
    },
    toggleSelection() {
      if (this.serviceAddInstanceForm.instanceTypeHostMap[this.currentInstanceType()].multiSureSelection.length > 0) {
        this.serviceAddInstanceForm.instanceTypeHostMap[this.currentInstanceType()].multiSureSelection.forEach(row => {
          this.$refs.multipleTable.toggleRowSelection(row, true)
        })
      }
    },
    searchGridData() {
      if (!this.diaForm.input) {
        this.serviceAddInstanceForm.gridData = this.serviceAddInstanceForm.allTableData
        return
      }
      const _this = this
      const NewItems = _this.serviceAddInstanceForm.allTableData.filter(item => {
        //过滤数组元素
        return item.hostName.includes(this.diaForm.input) || item.hostIp.includes(this.diaForm.input)
      })
      this.serviceAddInstanceForm.gridData = NewItems
    },
    saveInstanceData() {
      this.serviceAddInstanceForm.instanceTypeHostMap[
        this.currentInstanceType()
      ].multiSureSelection = this.diaForm.checkbox
      if (this.serverType == 'zms_collector' || this.serverType == 'zms_alert' || this.serverType == 'influxdb') {
        if (this.serviceAddInstanceForm.instanceTypeHostMap[this.currentInstanceType()].multiSureSelection.length > 1) {
          this.message('error', '请选择单个主机！')
          return
        }
      }
      this.serviceAddInstanceForm.instanceTypeHostMap[this.currentInstanceType()].buttonTxt = undefined
      let arrs = ''
      if (this.serviceAddInstanceForm.instanceTypeHostMap[this.currentInstanceType()].multiSureSelection.length > 0) {
        for (const item of this.serviceAddInstanceForm.instanceTypeHostMap[this.currentInstanceType()]
          .multiSureSelection) {
          arrs = arrs.concat(',').concat(item.hostName)
        }
      }
      this.serviceAddInstanceForm.instanceTypeHostMap[this.currentInstanceType()].buttonTxt =
        arrs.length > 0 ? arrs.substr(1) : arrs

      this.serviceAddInstanceForm.instanceTypeHostMap[this.currentInstanceType()].buttonClass = 'info'
      this.dialogTableVisible = false
    },
    validInstanceData() {
      let params = []
      let instanceTypeArr = []
      let success = true
      for (let key in this.serviceAddInstanceForm.instanceTypeHostMap) {
        let hosts = this.serviceAddInstanceForm.instanceTypeHostMap[key].multiSureSelection
        if (hosts.length == 0) {
          this.serviceAddInstanceForm.instanceTypeHostMap[key].buttonClass = 'danger'
          this.serviceAddInstanceForm.instanceTypeHostMap = { ...this.serviceAddInstanceForm.instanceTypeHostMap }
          continue
        }
        instanceTypeArr.push(key)
        this.serviceAddInstanceForm.instanceTypeHostMap[key].multiSureSelection.forEach(item => {
          const param = {
            instanceType: key,
            hostId: item.id
          }
          params.push(param)
        })
      }
      if (params.length == 0) {
        success = false
      }
      if (success) {
        if (this.serviceAddInstanceForm.instanceType.length > 0) {
          const newItems1 = this.serviceAddInstanceForm.instanceType.filter(item => instanceTypeArr.indexOf(item) == -1)
          const newItems2 = instanceTypeArr.filter(item => this.serviceAddInstanceForm.instanceType.indexOf(item) == -1)
          if (newItems1.length > 0 || newItems2.length > 0) {
            this.serviceAddInstanceForm.instanceType = instanceTypeArr
            this.$emit('reloadServiceConfig')
          }
        }
        this.serviceAddInstanceForm.instanceList = params
        this.serviceAddInstanceForm.instanceType = instanceTypeArr
      }
      return success
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
.service-add-instance {
  margin: 0px auto;
  width: 650px;
  .content_header {
    font-size: 18px;
  }
  .content_button {
    margin-top: 6px;
    min-width: 150px;
    min-height: 38px;
  }
  .content_input {
    width: 300px;
    margin-top: 0;
    margin-bottom: 15px;
    float: right;
  }
  .el-dialog__body {
    padding: 0 20px 30px;
  }
  .content_mini_button {
    text-align: center;
    margin-top: 20px;
  }
  .errorTip {
    color: red;
    border-color: red;
  }
}
</style>
