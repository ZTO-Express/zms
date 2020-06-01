<template>
  <div class="l-page l-page--flex">
    <div class="page-nav">集群重建</div>
    <div class="l-page__content">
      <div class="content--padding">
        <div class="content__body">
          <div class="specialrebuild">
            <base-form v-bind="formOptions" label-width="80px"></base-form>
            <div class="content_button">
              <el-button v-bind="GLOBAL.button" @click="handleRebuild">执行重建</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import { clusterRecover } from '@/api/transfer'

export default {
  mixins: [common, baseStructure],
  name: 'clusterRebuild',
  data() {
    return {
      formOptions: {
        ref: 'diaform',
        forms: [
          {
            prop: 'envId',
            label: '环境',
            itemType: 'remoteselect',
            apiUrl: 'environment_list',
            labelkeyname: 'environmentName',
            valuekeyname: 'id',
            change: this.changeCluster
          },
          {
            prop: 'serviceType',
            label: '集群类型',
            itemType: 'select',
            options: [
              { label: 'ROCKETMQ', value: 'ROCKETMQ' },
              { label: 'KAFKA', value: 'KAFKA' }
            ],
            change: this.changeCluster
          },
          {
            prop: 'srcClusterId',
            label: '源集群',
            itemType: 'remoteselect',
            apiUrl: 'cluster_list',
            relativeProp: [
              { prop: 'envId', paramkey: 'envId' },
              { prop: 'serviceType', paramkey: 'serviceType' }
            ],
            labelkeyname: 'serverName',
            valuekeyname: 'id',
            selectInfo: this.selectSrcCluster,
            rules: [{ required: true, message: '请选择源集群', trigger: 'change' }]
          },
          {
            prop: 'targetClusterId',
            label: '目标集群',
            itemType: 'remoteselect',
            relativeProp: [
              { prop: 'envId', paramkey: 'envId' },
              { prop: 'serviceType', paramkey: 'serviceType' }
            ],
            apiUrl: 'cluster_list',
            labelkeyname: 'serverName',
            valuekeyname: 'id',
            selectInfo: this.selectTargetSrcCluster,
            rules: [{ required: true, message: '请选择目标集群', trigger: 'change' }]
          }
        ]
      },
      srcEnv: '',
      targetEnv: '',
      srcServerType: '',
      targetServerType: ''
    }
  },
  methods: {
    changeCluster() {
      this.$refs.diaform.specialSet({ prop: 'srcClusterId', val: undefined })
      this.$refs.diaform.specialSet({ prop: 'targetClusterId', val: undefined })
    },
    selectSrcCluster(val) {
      this.srcEnv = val.environmentId
      this.srcServerType = val.serverType
    },
    selectTargetSrcCluster(val) {
      this.targetEnv = val.environmentId
      this.targetServerType = val.serverType
    },
    handleRebuild() {
      this.handleValidate({
        refname: 'diaform',
        cb: async value => {
          if (
            value.srcClusterId === value.targetClusterId ||
            this.srcEnv !== this.targetEnv ||
            this.srcServerType !== this.targetServerType
          ) {
            this.message('error', '目标集群和源集群不能相同，必须同一环境且集群类型一致')
            return
          }
          const { message, status } = await clusterRecover(value)
          this.message(status ? 'success' : 'error', message)
          // 重置
          this.$refs.diaform.reset('clear')
        }
      })
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss">
.l-page .l-page__content {
  padding: 0;
  .specialrebuild {
    width: 450px;
    margin: 40px auto;
    .content_button {
      margin-left: 195px;
      margin-top: 25px;
    }
  }
}
</style>
