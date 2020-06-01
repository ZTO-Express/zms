<template>
  <div class="content_form" v-if="serviceConfigData.length">
    <!-- <p class="content_header">审核更改</p> -->

    <el-form :model="serviceConfigForm" ref="serviceConfigForm" size="mini">
      <div v-for="item in serviceConfigData" :key="item.id" class="content_row">
        <div class="content_left">
          <h3>
            <span class="asterisk" v-if="item.isRequired == 1">*</span>
            {{ item.propertyName }}
          </h3>
          <span>{{ item.description }}</span>
        </div>
        <div class="content_right">
          <el-form-item
            :prop="item.id"
            :rules="[
              {
                required: item.isRequired == 1 && item.chooseType != 'CLUSTER_MAP',
                message: '请输入' + item.propertyName,
                trigger: 'blur'
              },
              { validator: checkValue, trigger: 'blur' }
            ]"
          >
            <el-input v-model.trim="serviceConfigForm[item.id]" v-if="item.chooseType == 'TEXT'"></el-input>

            <el-checkbox-group v-model="serviceConfigForm[item.id]" v-if="item.chooseType == 'CHECKBOX'">
              <div v-for="property in item.propertyValueList" :key="property.id" class="content_checkbox">
                <el-checkbox :label="property.propertyValue">
                  {{ property.displayValue }}
                </el-checkbox>
              </div>
            </el-checkbox-group>

            <el-radio-group
              v-model="serviceConfigForm[item.id]"
              v-if="item.chooseType == 'RADIO'"
              @change="radioChange(item)"
            >
              <div v-for="property in item.propertyValueList" :key="property.id" class="content_radio">
                <el-radio :label="property.propertyValue">
                  {{ property.displayValue }}
                </el-radio>
              </div>
            </el-radio-group>

            <el-select v-model="serviceConfigForm[item.id]" v-if="item.chooseType == 'LIST'">
              <el-option
                v-for="property in item.propertyValueList"
                :key="property.id"
                :label="property.displayValue"
                :value="property.propertyValue"
              ></el-option>
            </el-select>
            <div v-if="item.chooseType == 'CLUSTER_MAP'">
              <div v-for="(property, index) in clusterMapData" :key="index" class="content_checkbox">
                <div v-if="clusterMapData[index]['delete'] === 0">
                  <el-row style="margin-bottom: 10px;">
                    <el-col :span="10">
                      <el-select v-model="clusterMapData[index]['originCluster']" placeholder="备份集群" size="small">
                        <el-option
                          v-for="cluster in originClusters"
                          :key="cluster.id"
                          :label="cluster.serverName"
                          :value="cluster.serverName"
                        ></el-option>
                      </el-select>
                    </el-col>
                    <el-col :span="10">
                      <el-select v-model="clusterMapData[index]['currentCluster']" placeholder="目标集群" size="small">
                        <el-option
                          v-for="cluster in currentClusters"
                          :key="cluster.id"
                          :label="cluster.serverName"
                          :value="cluster.serverName"
                        ></el-option>
                      </el-select>
                    </el-col>
                    <el-col :span="4">
                      <i
                        class="el-icon-remove-outline icon_cluster"
                        style="color: red"
                        @click="removeClusterMap(index)"
                      />
                    </el-col>
                  </el-row>
                </div>
              </div>
              <el-row>
                <i class="el-icon-circle-plus-outline icon_cluster" @click="addClusterMap()" />
              </el-row>
            </div>
          </el-form-item>
        </div>
      </div>
    </el-form>
  </div>
</template>

<script>
import { common, baseStructure } from '@/mixins/index'
import { getAddServiceInstanceConfig, clusterList } from '@/api/service'

export default {
  name: 'serviceAddConfig',
  mixins: [common, baseStructure],
  props: {
    envId: [Number, String],
    serverType: String,
    instanceType: Array
  },
  data() {
    return {
      serviceConfigForm: {},
      serviceConfigData: [],
      currentClusters: [],
      originClusters: [],
      clusterMapData: [],
      clusterMapProperties: []
    }
  },
  created() {
    this.loadServiceInstanceConfig()
  },
  methods: {
    checkServerType() {
      let serverType = this.serverType
      switch (serverType) {
        case 'ZMSCollector':
          return 'zms_collector'
        case 'ZMSAlert':
          return 'zms_alert'
        case 'Kafka':
          return 'kafka'
        case 'Rocketmq':
          return 'rocketmq'
        case 'Zookeeper':
          return 'zookeeper'
        case 'InfluxDB':
          return 'influxdb'
        case 'ZMSBackupCluster':
          return 'zms_backup_cluster'
        default:
          return serverType
      }
    },
    async loadServiceInstanceConfig(instanceType) {
      let serverType = this.checkServerType()
      this.serviceConfigForm = {}
      this.serviceConfigData = []
      const res = await getAddServiceInstanceConfig(
        this.envId,
        serverType,
        instanceType ? instanceType : this.instanceType ? this.instanceType : ''
      )
      if (res.result) {
        this.assignCurrentValue(res.result)
      }
      this.getServiceAndEnvById()
    },

    async getServiceAndEnvById() {
      let needQueryClusters = false
      for (const item of this.serviceConfigData) {
        if (item.chooseType == 'CLUSTER_MAP') {
          needQueryClusters = true
          break
        }
      }
      if (needQueryClusters) {
        this.currentClusters = await this.listClusterList(this.envId)
        if (this.clusterMapData.length == 0) {
          this.addClusterMap()
        }
      }
    },

    clusterMap(item) {
      if (item.chooseType == 'CLUSTER_MAP') {
        const currentValue = this.serviceConfigForm[item.id]
        if (null == currentValue) {
          return
        }
        this.clusterMapData = []
        for (var value of currentValue.split(',')) {
          var clusterMap = value.split(':')
          var originCluster = clusterMap[0]
          var currentCluster = clusterMap[1]
          this.clusterMapData.push({ originCluster: originCluster, currentCluster: currentCluster, delete: 0 })
        }
      }
    },
    removeClusterMap(index) {
      console.log('clusterMapData', this.clusterMapData)
      this.clusterMapData[index]['delete'] = 1
    },
    addClusterMap() {
      console.log('clusterMapData', this.clusterMapData)
      this.clusterMapData.push({ delete: 0 })
    },
    async radioChange(item) {
      if (item.propertyName === 'envId') {
        console.log('clusterMapData', this.clusterMapData)
        const envId = this.serviceConfigForm[item.id]

        this.originClusters = await this.listClusterList(envId)
        for (const item of this.clusterMapData) {
          if (item['originCluster']) {
            item['originCluster'] = null
          }
        }
      }
    },
    async listClusterList(envId) {
      const resClusterList = await clusterList({ envId: envId })
      if (resClusterList.result) {
        return resClusterList.result
      }
    },
    assignCurrentValue(result) {
      for (const item of result) {
        //number转string
        item.id = item.id.toString()
        item.currentValue = item.defaultValue
        if (item.chooseType === 'CHECKBOX') {
          if (!item.currentValue) {
            item.currentValue = []
          } else {
            item.currentValue = item.currentValue.split(',')
          }
        }
      }
      this.serviceConfigData = result
      // 初始化form表单的值
      this.resetForm()
    },
    resetForm() {
      for (const item of this.serviceConfigData) {
        this.serviceConfigForm = { ...this.serviceConfigForm, [item.id]: item.currentValue }
        this.clusterMap(item)
      }
    },
    serviceInstanceConfigSave() {
      let validateFlg = false
      this.$refs['serviceConfigForm'].validate(async valid => {
        if (valid) {
          for (const item of this.serviceConfigData) {
            item.currentValue = this.serviceConfigForm[item.id]
            if (item.chooseType === 'CHECKBOX') {
              let arrs = ''
              for (const arr of item.currentValue) {
                arrs = arrs.concat(',').concat(arr)
              }
              item.currentValue = arrs.length > 0 ? arrs.substr(1) : arrs
            }
            if (item.chooseType == 'CLUSTER_MAP') {
              let arrs = ''
              for (const clusterMap of this.clusterMapData) {
                if (clusterMap['delete'] == 1) {
                  continue
                }
                let originCluster = clusterMap['originCluster']
                let currentCluster = clusterMap['currentCluster']
                arrs = arrs.concat(',').concat(originCluster + ':' + currentCluster)
              }
              item.currentValue = arrs.length > 0 ? arrs.substr(1) : arrs
            }
          }
          validateFlg = true
        } else {
          validateFlg = false
        }
      })
      return validateFlg
    },
    checkClusterMap(rule, value, callback) {
      const count = this.clusterMapData.filter(item => {
        return item['delete'] == 1
      })
      if (count === 0) {
        callback(new Error('设置backup.cluster.map'))
      }
      let clusterMapData = this.clusterMapData.filter(item => {
        return item['delete'] == 0
      })
      let clusterSet = new Set()
      clusterMapData.forEach(element => {
        if (element['delete'] === 0) {
          if (!(element['originCluster'] && element['currentCluster'])) {
            callback(new Error('集群不能为空'))
          }
          clusterSet.add(element['originCluster'])
        }
      })
      if (clusterSet.size != clusterMapData.length) {
        callback(new Error('源集群不能重复配置'))
        return
      }
      callback()
    },
    checkValue(rule, value, callback) {
      const data = this.serviceConfigData.filter(item => rule.field == item.id)[0]
      if (value) {
        if (data.chooseType === 'TEXT') {
          const errMsg = this.checkTextValue(data, value)
          if (errMsg) {
            callback(new Error(errMsg))
          }
        }
      }

      if (data && data.chooseType === 'CLUSTER_MAP') {
        this.checkClusterMap(rule, value, callback)
      }
      callback()
    },
    checkTextValue(data, value) {
      if (data.valueType === 'STRING') {
        // 校验字符长度
        if (data.minLen && data.maxLen && (value.length < data.minLen || value.length > data.maxLen)) {
          return '长度应介于'
            .concat(data.minLen)
            .concat('和')
            .concat(data.maxLen)
            .concat('之间')
        } else if (data.minLen && value.length < data.minLen) {
          return '长度应大于等于'.concat(data.minLen)
        } else if (data.maxLen && value.length > data.maxLen) {
          return '长度应小于等于'.concat(data.maxLen)
        }
      } else if (data.valueType === 'NUMBER') {
        if (!this.isNumber(value)) {
          return '请输入整数类型'
        } else if (
          data.minValue &&
          data.maxValue &&
          (parseInt(value) < data.minValue || parseInt(value) > data.maxValue)
        ) {
          return '请输入介于'
            .concat(data.minValue)
            .concat('和')
            .concat(data.maxValue)
            .concat('之间的整数')
        } else if (data.minValue && parseInt(value) < data.minValue) {
          return '请输入不小于'.concat(data.minValue).concat('的整数')
        } else if (data.maxValue && parseInt(value) > data.maxValue) {
          return '请输入不大于'.concat(data.maxValue).concat('的整数')
        }
      } else if (data.valueType === 'DOUBLE') {
        if (!this.isDouble(value)) {
          return '请输入数值类型'
        } else if (
          data.minValue &&
          data.maxValue &&
          (parseFloat(value) < data.minValue || parseFloat(value) > data.maxValue)
        ) {
          return '请输入介于'
            .concat(data.minValue)
            .concat('和')
            .concat(data.maxValue)
            .concat('之间的值')
        } else if (data.minValue && parseFloat(value) < data.minValue) {
          return '请输入不小于'.concat(data.minValue).concat('的值')
        } else if (data.maxValue && parseFloat(value) > data.maxValue) {
          return '请输入不大于'.concat(data.maxValue).concat('的值')
        }
      }
      return ''
    },
    isNumber(value) {
      const pattern = /^[0-9]*$/
      if (pattern.test(value)) {
        return true
      }
      return false
    },
    isDouble(value) {
      const pattern = /^[0-9,.]*$/
      if (pattern.test(value)) {
        return true
      }
      return false
    }
  }
}
</script>
<style lang="scss">
.content_form {
  width: 1000px;
  margin: 0 auto;
  .content_header {
    font-size: 18px;
    margin-left: 150px;
  }
  .content_row {
    width: inherit;
    margin-top: 20px;
  }
  .content_left {
    display: inline-block;
    width: 300px;
    text-align: right;
    margin-right: 50px;
    .asterisk {
      color: red;
      font-size: 12px;
    }
    h3 {
      margin-top: 0;
      margin-bottom: 6px;
    }
  }
  .content_right {
    display: inline-block;
    width: 600px;
    vertical-align: top;
  }
  .content_radio {
    line-height: 26px;
    .el-radio__label {
      padding-left: 3px;
    }
  }
  .content_checkbox {
    line-height: 22px;
    .el-checkbox__label {
      padding-left: 8px;
    }
  }
  .icon_cluster {
    font-size: 30px;
    position: relative;
    right: 80px;
    float: right;
  }
}
</style>
