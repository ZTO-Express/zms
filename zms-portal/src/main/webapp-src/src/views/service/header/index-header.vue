<template>
  <div>
    <div class="zms-service-title">
      <div class="content_breadcrumb">
        <el-breadcrumb separator-class="el-icon-arrow-right">
          <el-breadcrumb-item :to="{ path: '/env/' + envId }">{{ envName }}</el-breadcrumb-item>
          <el-breadcrumb-item>{{ serviceName }}</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <!-- <div style="margin-top:3px;" v-if="checkPerm(['admin'])">
        <el-dropdown trigger="click">
          <operate-button style="color:#048cff">
            操作
            <i class="el-icon-arrow-down el-icon--right"></i>
          </operate-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item disabled>启动</el-dropdown-item>
            <el-dropdown-item disabled>停止</el-dropdown-item>
            <el-dropdown-item disabled>重启</el-dropdown-item>
            <el-dropdown-item disabled>滚动重启</el-dropdown-item>
            <el-dropdown-item @click.native="openRenameService(serverId, serviceName, 1)">
              重命名
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div> -->
    </div>
    <el-divider></el-divider>
    <!-- 重命名环境弹框 -->
    <op-freight
      :dialog="serviceRenameOptions.visible"
      :modalTitle="serviceRenameOptions.diatitle"
      :loading="serviceRenameOptions.loading"
      width="400px"
      @dealDialog="closeServiceRenameDia"
    >
      <base-form
        :forms="serviceRenameOptions.forms"
        :ref="serviceRenameOptions.ref"
        :current-form-value="serviceRenameOptions.currentFormValue"
        label-width="80px"
      ></base-form>
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="saveServiceRenameData">保存</el-button>
      </template>
    </op-freight>
  </div>
</template>

<script>
import { common, baseStructure } from '@/mixins/index'
import OpFreight from '@/modules/modal/freight/OpFreight'
// 重命名环境
import serviceRename from './rename'

export default {
  name: 'indexHeader',
  mixins: [common, baseStructure, serviceRename],
  components: {
    OpFreight
  },
  props: {
    serverId: Number,
    serverName: String,
    envId: [String, Number],
    envName: String
  },
  data() {
    return {
      serviceName: ''
    }
  },
  created() {
    this.serviceName = this.serverName
  },
  methods: {
    resetServerName(name) {
      this.serviceName = name
    }
  }
}
</script>

<style lang="scss">
.zms-service-head {
  .content_breadcrumb {
    float: left;
    margin-right: 20px;
  }
  .zms-service-title {
    overflow: hidden;
  }
  .el-tabs__header {
    margin: 0;
  }
  .el-divider {
    margin-top: 10px;
    margin-bottom: 10px;
  }
}
</style>
