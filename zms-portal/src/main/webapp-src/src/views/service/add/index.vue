<template>
  <div class="l-page l-page--flex">
    <div class="l-page__content">
      <div class="content--padding">
        <div class="content__body">
          <div class="service_add_header">添加{{ serverType }}服务到{{ envName }}</div>
          <add-option
            ref="addOption"
            v-show="activeTabName == 'option'"
            @changeNextDisable="changeNextDisable"
          ></add-option>
          <add-name ref="addName" v-if="addNameFlg" v-show="activeTabName == 'name'" :env-id="envId"></add-name>
          <add-instance
            ref="addInstance"
            v-if="addInstanceFlg"
            v-show="activeTabName == 'instance'"
            :env-id="envId"
            :server-type="serverType"
            :instance-type="instanceType"
          ></add-instance>
          <add-service
            ref="addService"
            v-if="addServiceFlg"
            v-show="activeTabName == 'service'"
            :env-id="envId"
            :server-type="serverType"
          ></add-service>
          <add-run
            ref="addRun"
            v-if="addRunFlg"
            v-show="activeTabName == 'run'"
            :env-id="envId"
            :server-type="serverType"
            :server-name="serverName"
            :property-list="propertyList"
            :instance-list="instanceList"
            @runComplete="runComplete"
          ></add-run>
          <add-complete ref="addComplete" v-if="addCompleteFlg" v-show="activeTabName == 'complete'"></add-complete>
        </div>
      </div>
    </div>
    <div class="service_add_bottom">
      <el-button :disabled="activeTabName == 'run'" size="small" @click="backService">
        返回
      </el-button>
      <el-button :disabled="nextDisable" size="small" type="primary" @click="nextService">
        {{ this.activeTabName == 'run' ? '完成' : '下一步' }}
      </el-button>
    </div>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import { validServerName } from '@/api/service'
// 添加服务选项
import addOption from './add-option'
// 添加服务名称
import addName from './add-name'
// 添加服务实例
import addInstance from './add-instance'
// 添加服务配置
import addService from './add-service'
// 运行服务实例
import addRun from './add-run'
// 添加服务实例完成
import addComplete from './add-complete'

export default {
  name: 'serviceAddHome',
  mixins: [common, baseStructure],

  components: {
    addOption,
    addName,
    addService,
    addInstance,
    addRun,
    addComplete
  },
  data() {
    return {
      envId: '',
      envName: '',
      nextDisable: true,
      activeTabName: 'option',
      addNameFlg: false,
      addServiceFlg: false,
      addInstanceFlg: false,
      addRunFlg: false,
      addCompleteFlg: false,
      serverType: '',
      serverName: '',
      propertyList: [],
      instanceList: [],
      instanceType: []
    }
  },
  created() {
    // 首页跳转
    this.envId = this.$route.params.envId
    this.envName = this.$route.params.envName
  },
  methods: {
    changeNextDisable(instanceType) {
      this.nextDisable = false
      this.addNameFlg = false
      this.addInstanceFlg = false
      this.addServiceFlg = false
      this.addRunFlg = false
      this.addCompleteFlg = false
      this.instanceType = instanceType
    },
    runComplete() {
      this.nextDisable = false
    },
    async nextService() {
      if (this.activeTabName === 'option') {
        this.serverType = this.$refs.addOption.serviceAddOptions.selectServerType
        this.addNameFlg = true
        this.activeTabName = 'name'
      } else if (this.activeTabName === 'name') {
        let validateFlg = this.$refs.addName.validateServerName()
        if (!validateFlg) {
          return
        }
        validateFlg = await this.$refs.addName.checkServerName()
        if (!validateFlg) {
          return
        }
        this.serverName = this.$refs.addName.serviceAddNameForm.serverName

        // if (await this.checkServerName()) {
        this.addInstanceFlg = true
        this.activeTabName = 'instance'
        // }
      } else if (this.activeTabName === 'instance') {
        const validateFlg = this.$refs.addInstance.validInstanceData()
        if (!validateFlg) {
          return
        }
        this.instanceList = this.$refs.addInstance.serviceAddInstanceForm.instanceList
        this.addServiceFlg = true
        this.activeTabName = 'service'
      } else if (this.activeTabName === 'service') {
        const validateFlg = this.$refs.addService.serviceInstanceConfigSave()
        if (!validateFlg) {
          return
        }
        this.propertyList = this.$refs.addService.serviceConfigData
        this.addRunFlg = true
        this.activeTabName = 'run'
        this.nextDisable = true
      } else if (this.activeTabName === 'run') {
        // this.addCompleteFlg = true
        // this.activeTabName = 'complete'
        this.$router.push({ name: 'Welcome' })
      } else if (this.activeTabName === 'complete') {
        this.$router.push({ name: 'Welcome' })
      }
    },
    backService() {
      if (this.activeTabName === 'option') {
        this.$router.push({ name: 'Welcome' })
      } else if (this.activeTabName === 'name') {
        this.activeTabName = 'option'
      } else if (this.activeTabName === 'instance') {
        this.activeTabName = 'name'
      } else if (this.activeTabName === 'service') {
        this.activeTabName = 'instance'
      } else if (this.activeTabName === 'run') {
        this.activeTabName = 'service'
      } else if (this.activeTabName === 'complete') {
        this.activeTabName = 'run'
      }
    },
    async checkServerName() {
      const params = {
        envId: this.envId,
        serverName: this.serverName
      }
      const res = await validServerName(params)
      if (!res.result) {
        this.message('error', '服务名称已存在')
        return false
      }
      return true
    }
  }
}
</script>
<style lang="scss">
.service_add_header {
  font-size: 20px;
  text-align: center;
  background: #fff;
  padding: 15px 20px 10px;
  margin-top: 35px;
}
.service_add_bottom {
  text-align: right;
  background: WhiteSmoke;
  padding: 6px 20px;
}
</style>
