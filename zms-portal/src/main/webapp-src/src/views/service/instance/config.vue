<template>
  <div class="content__wrap">
    <div class="content__body">
      <div class="service_specialconfig" v-if="serviceConfigData.length">
        <el-form :model="serviceConfigForm" ref="serviceConfigForm" size="mini">
          <div v-for="prop in serviceConfigData" :key="prop.instanceId" class="content_row">
            <h3 class="content_instanceName" v-if="!instanceId && prop.instanceName">
              {{ prop.instanceName }}
            </h3>
            <div v-for="item in prop.propertyList" :key="item.id">
              <div class="content_left">
                <h3>
                  <span class="asterisk" v-if="item.isRequired == 1">*</span>
                  {{ item.propertyName }}
                </h3>
                <span>{{ item.description }}</span>
              </div>
              <div class="content_right">
                <el-form-item
                  :prop="prop.instanceId + '-' + item.id"
                  :rules="[
                    {
                      required: item.isRequired == 1 && item.chooseType != 'CLUSTER_MAP',
                      message: '请输入' + item.propertyName,
                      trigger: 'blur'
                    },
                    { validator: checkValue, trigger: 'blur' }
                  ]"
                >
                  <el-input
                    v-model.trim="serviceConfigForm[prop.instanceId + '-' + item.id]"
                    :disabled="item.isReadOnly == 1"
                    v-if="item.chooseType == 'TEXT'"
                  ></el-input>

                  <el-checkbox-group
                    v-model="serviceConfigForm[prop.instanceId + '-' + item.id]"
                    :disabled="item.isReadOnly == 1"
                    v-if="item.chooseType == 'CHECKBOX'"
                  >
                    <div v-for="property in item.propertyValueList" :key="property.id" class="content_checkbox">
                      <el-checkbox :label="property.propertyValue">
                        {{ property.displayValue }}
                      </el-checkbox>
                    </div>
                  </el-checkbox-group>

                  <el-radio-group
                    v-model="serviceConfigForm[prop.instanceId + '-' + item.id]"
                    :disabled="item.isReadOnly == 1"
                    v-if="item.chooseType == 'RADIO'"
                    @change="radioChange(prop.instanceId, item)"
                  >
                    <div v-for="property in item.propertyValueList" :key="property.id" class="content_radio">
                      <el-radio :label="property.propertyValue">
                        {{ property.displayValue }}
                      </el-radio>
                    </div>
                  </el-radio-group>

                  <el-select
                    v-model="serviceConfigForm[prop.instanceId + '-' + item.id]"
                    :disabled="item.isReadOnly == 1"
                    v-if="item.chooseType == 'LIST'"
                  >
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
                            <el-select v-model="clusterMapData[index]['originCluster']" placeholder="备份集群">
                              <el-option
                                v-for="cluster in originClusters"
                                :key="cluster.id"
                                :label="cluster.serverName"
                                :value="cluster.serverName"
                              ></el-option>
                            </el-select>
                          </el-col>
                          <el-col :span="10">
                            <el-select v-model="clusterMapData[index]['currentCluster']" placeholder="目标集群">
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
          </div>
          <div class="content_left"></div>
          <div class="content_button">
            <el-form-item>
              <el-button type="primary" @click="serviceInstanceConfigSave">
                保存
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { common, baseStructure } from '@/mixins/index'
import { getServiceInstanceConfig, saveServiceInstanceConfig, getServiceAndEnvById, clusterList } from '@/api/service'
export default {
  name: 'serviceInstanceConfig',
  mixins: [common, baseStructure],
  props: {
    serverId: Number,
    instanceId: Number
  },
  data() {
    return {
      serviceConfigForm: {},
      serviceConfigData: [],
      envId: null,
      currentClusters: [],
      originClusters: [],
      clusterMapData: [],
      clusterMapProperties: []
    }
  },
  created() {
    this.loadConfigData()
  },
  methods: {
    loadConfigData() {
      if (this.instanceId) {
        // 实例页面配置
        this.loadInstanceConfig()
      } else {
        // 服务页面配置
        this.loadServiceConfig()
      }
    },

    async getServiceAndEnvById() {
      let needQueryClusters = false
      let currentEnvId = ''
      for (const prop of this.serviceConfigData) {
        for (const item of prop.propertyList) {
          if (item.chooseType == 'CLUSTER_MAP') {
            needQueryClusters = true
            if (this.clusterMapData.length == 0) {
              this.clusterMapData.push({ delete: 0 })
            }
          }
          if (item.propertyName == 'envId') {
            currentEnvId = item.currentValue
          }
        }
      }
      if (needQueryClusters) {
        const res = await getServiceAndEnvById({ serviceId: this.serverId })
        if (res.result) {
          this.envId = res.result.envId
        }
        this.currentClusters = await this.listClusterList(this.envId)
        //currentEnvId
        if (currentEnvId) {
          this.originClusters = await this.listClusterList(currentEnvId)
        }
      }
    },

    clusterMap(instanceId, item) {
      if (item.chooseType == 'CLUSTER_MAP') {
        const currentValue = this.serviceConfigForm[instanceId + '-' + item.id]
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
      console.log('this.clusterMapData', this.clusterMapData)
    },
    removeClusterMap(index) {
      this.clusterMapData[index]['delete'] = 1
    },
    addClusterMap() {
      this.clusterMapData.push({ delete: 0 })
    },
    async radioChange(instanceId, item) {
      if (item.propertyName === 'envId') {
        const envId = this.serviceConfigForm[instanceId + '-' + item.id]
        this.originClusters = await this.listClusterList(envId)

        for (const item of this.clusterMapData) {
          item['originCluster'] = null
        }
      }
    },
    async listClusterList(envId) {
      const resClusterList = await clusterList({ envId: envId })
      if (resClusterList.result) {
        return resClusterList.result
      }
    },
    async loadServiceConfig() {
      const res = await getServiceInstanceConfig({ serviceId: this.serverId })
      if (res.result) {
        this.assignCurrentValue(res.result)
        this.getServiceAndEnvById()
      }
    },
    async loadInstanceConfig() {
      const res = await getServiceInstanceConfig({ instanceId: this.instanceId })
      if (res.result) {
        this.assignCurrentValue(res.result)
        this.getServiceAndEnvById()
      }
    },
    assignCurrentValue(result) {
      for (const prop of result) {
        if (!prop.instanceId) {
          prop.instanceId = 0
        }
        for (const item of prop.propertyList) {
          //number转string
          item.id = item.id.toString()
          if (item.chooseType === 'CHECKBOX') {
            if (!item.currentValue) {
              item.currentValue = []
            } else {
              item.currentValue = item.currentValue.split(',')
            }
          }
        }
      }
      this.serviceConfigData = result
      // 初始化form表单的值
      this.resetForm()
    },
    serviceInstanceConfigSave() {
      this.$refs['serviceConfigForm'].validate(async valid => {
        if (valid) {
          for (const prop of this.serviceConfigData) {
            for (const item of prop.propertyList) {
              item.currentValue = this.serviceConfigForm[prop.instanceId + '-' + item.id]
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
            if (prop.instanceId == 0) {
              delete prop.instanceId
            }
          }
          const params = {
            serviceId: this.serverId,
            propertyQueryList: this.serviceConfigData
          }
          await saveServiceInstanceConfig(params)
          // 重新查询
          this.loadConfigData()
          this.message('success', '保存成功')
        }
      })
    },
    resetForm() {
      for (const prop of this.serviceConfigData) {
        for (const item of prop.propertyList) {
          this.serviceConfigForm = { ...this.serviceConfigForm, [prop.instanceId + '-' + item.id]: item.currentValue }
          this.clusterMap(prop.instanceId, item)
        }
      }
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
      if (value) {
        const arr = rule.field.split('-')
        for (const prop of this.serviceConfigData) {
          if (prop.instanceId == arr[0]) {
            const data = prop.propertyList.filter(data => arr[1] == data.id)[0]
            if (data.chooseType === 'TEXT') {
              const errMsg = this.checkTextValue(data, value)
              if (errMsg) {
                callback(new Error(errMsg))
              }
            }
            if (data.chooseType === 'CLUSTER_MAP') {
              this.checkClusterMap(rule, value, callback)
            }
          }
        }
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
.service_specialconfig {
  width: 1000px;
  margin: 0 auto;
  .el-form {
    margin-left: -100px;
  }
  .content_instanceName {
    text-align: center;
    margin-top: 0;
    margin-left: 195px;
    margin-bottom: 20px;
  }
  .content_row {
    width: inherit;
    margin-top: 10px;
    > div {
      margin-top: 15px;
    }
  }
  .content_left {
    display: inline-block;
    width: 310px;
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
  .content_button {
    margin-top: 10px;
    text-align: center;
    margin-left: 180px;
  }
  .icon_cluster {
    font-size: 30px;
    position: relative;
    right: 80px;
    float: right;
  }
}
</style>
