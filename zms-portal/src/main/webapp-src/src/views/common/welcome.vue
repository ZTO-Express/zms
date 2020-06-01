<template>
  <div class="zms-home welcome">
    <div class="envlist" v-if="envList.length">
      <div class="env-box" v-for="env in envList" :key="env.id">
        <el-menu class="zms-menu" default-active="1">
          <el-menu-item>
            <!-- 环境名称 -->
            <template slot="title">
              <i
                :class="isCollapse[env.id] ? 'el-icon-arrow-down' : 'el-icon-arrow-up'"
                @click="isCollapse[env.id] = !isCollapse[env.id]"
              ></i>
              <b class="link" @click="goEnvDetail(env.id)" slot="title">
                {{ env.environmentName }}
              </b>
              <div
                class="health_circle"
                :class="
                  env.healthState == 0 ? 'health_success' : env.healthState == 1 ? 'health_warning' : 'health_danger'
                "
              ></div>
            </template>
            <!-- 对应下拉操作 -->
            <div class="zms-dropdown-wrap" v-if="checkPerm(['admin'])">
              <el-dropdown trigger="click">
                <span class="el-dropdown-link content_border">
                  <svg-icon icon-class="drop-down"></svg-icon>
                </span>

                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item @click.native="goServiceAddHome(env.id, env.environmentName)">
                    添加服务
                  </el-dropdown-item>
                  <el-dropdown-item @click.native="addHost(env.id)">添加主机</el-dropdown-item>
                  <el-dropdown-item @click.native="openRenameEnv(env.id, env.environmentName)">
                    重命名环境
                  </el-dropdown-item>
                  <el-dropdown-item
                    @click.native="openDatabaseEnv(env.id, env.environmentName, env.zkServiceId, env.influxdbServiceId)"
                  >
                    配置数据源
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </el-menu-item>
          <el-collapse-transition>
            <div v-show="!isCollapse[env.id]">
              <div class="content-host">
                <!-- 主机 -->
                <i class="el-icon-setting"></i>
                <span class="hostlink" @click="hostList(env.id)">主机 -- {{ env.hostCount }}</span>
              </div>
              <div style="overflow:hidden" v-if="moreServices[env.id].length > 0">
                <!-- 服务 -->
                <div class="host-link-line "></div>
                <el-menu-item v-for="item in moreServices[env.id]" :key="item.id">
                  <template>
                    <el-tooltip :content="item.serverType.toLowerCase()" placement="top" effect="light">
                      <svg-icon :icon-class="serviceIconClass(item.serverType)"></svg-icon>
                    </el-tooltip>
                    <span class="link" style="padding-left: 10px;" @click="goServiceDetail(item.id, 'state')">
                      {{ item.serverName }}
                    </span>
                    <div
                      class="health_circle"
                      :class="
                        item.healthState == 0
                          ? 'health_success'
                          : item.healthState == 1
                          ? 'health_warning'
                          : 'health_danger'
                      "
                    ></div>
                  </template>
                  <div class="zms-dropdown-wrap">
                    <el-dropdown trigger="click">
                      <span class="el-dropdown-link content_border">
                        <svg-icon icon-class="drop-down"></svg-icon>
                      </span>
                      <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item @click.native="goServiceDetail(item.id, 'instance')">实例</el-dropdown-item>
                        <div v-if="checkPerm(['admin'])">
                          <el-dropdown-item @click.native="goServiceDetail(item.id, 'config')">配置</el-dropdown-item>
                          <el-dropdown-item
                            @click.native="addInstance(item)"
                            v-if="
                              item.serverType === 'ZOOKEEPER' ||
                                item.serverType === 'KAFKA' ||
                                item.serverType === 'ROCKETMQ'
                            "
                          >
                            添加服务节点
                          </el-dropdown-item>
                          <el-dropdown-item @click.native="handleDeleteService(item.id)">删除</el-dropdown-item>
                        </div>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </div>
                </el-menu-item>
                <div v-if="moreServices[env.id].length < moreServicesCount[env.id]">
                  <div class="link-line "></div>
                  <div class="content_service_button">
                    <small @click="viewMore(env.id)">加载全部</small>
                  </div>
                </div>
                <!-- <div style="text-align: center" v-if="moreServices[env.id].length == moreServicesCount[env.id]">
                  <small>没有更多啦 ~</small>
                </div> -->
              </div>
            </div>
          </el-collapse-transition>
        </el-menu>
      </div>
    </div>
    <!-- add button -->
    <el-tooltip v-if="checkPerm(['admin'])" class="item" effect="dark" content="添加环境" placement="right">
      <el-button
        v-bind="GLOBAL.button"
        size="large"
        class="envAdd"
        icon="el-icon-plus"
        circle
        @click="openAddEnv"
      ></el-button>
    </el-tooltip>

    <div class="welcome__right">
      <cluster-index :cluster-list="envList" />
    </div>
    <!-- 重命名环境弹框 -->
    <op-freight
      :dialog="envRenameOptions.visible"
      :modalTitle="envRenameOptions.diatitle"
      :loading="envRenameOptions.loading"
      width="400px"
      @dealDialog="closeEnvRenameDia"
    >
      <base-form
        :forms="envRenameOptions.forms"
        :ref="envRenameOptions.ref"
        :current-form-value="envRenameOptions.currentFormValue"
        label-width="80px"
      ></base-form>
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="saveEnvRenameData">保存</el-button>
      </template>
    </op-freight>
    <!-- 配置database -->
    <op-freight
      :dialog="envDatabaseOptions.visible"
      :modalTitle="envDatabaseOptions.diatitle"
      :loading="envDatabaseOptions.loading"
      @dealDialog="closeEnvDatabaseDia"
    >
      <template>
        <!-- <th>为{{ envDatabaseOptions.envName }}环境配置数据源</th> -->
        <br />
        <el-row style="margin-top: 10px">
          <el-col :span="10" style="font-size:14">
            <h3>
              zookeeper
            </h3>
            <span>用于存储集群的元数据。如果没有安装，zms消息集群功能不能使用</span>
          </el-col>
          <el-col :span="14">
            <el-select v-model="zkServiceId" clearable placeholder="请选择">
              <el-option
                v-for="item in envDatabaseOptions.zookeepertableData"
                :key="item.id"
                :label="item.serverName"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-col>
        </el-row>
        <br />
        <br />
        <el-row>
          <el-col :span="10">
            <h3>
              influxdb
            </h3>
            <span>用于存储收集集群的指标数据</span>
          </el-col>
          <el-col :span="14">
            <el-select v-model="influxdbServiceId" clearable placeholder="请选择">
              <el-option
                v-for="item in envDatabaseOptions.influxdbtableData"
                :key="item.id"
                :label="item.serverName"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-col>
        </el-row>
        <!-- <el-select v-model="influxdbServiceId" placeholder="请选择">
          <el-option
            v-for="influxdb in envDatabaseOptions.influxdbtableData"
            :key="influxdb.id"
            :label="influxdb.serverName"
            :value="influxdb.id"
          ></el-option>
        </el-select> -->
      </template>
      <br />
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="saveEnvDatabaseData">保存</el-button>
      </template>
    </op-freight>
    <!-- 添加主机弹框 -->
    <op-message :dialog="dialog" :modalTitle="modalTitle" @dealDialog="dialog = false" :loading="loading" width="800px">
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
          <span style="color:red">2. 初始化主机建议打开所需防火墙端口 [2000-9999] [10900-10999] [20910-20999] 。</span>
        </el-row>
      </template>
    </op-message>
    <!-- 添加环境弹框 -->
    <op-freight
      :dialog="envAddOptions.visible"
      :modalTitle="envAddOptions.diatitle"
      :loading="envAddOptions.loading"
      width="400px"
      @dealDialog="closeEnvAddDia"
    >
      <base-form
        :forms="envAddOptions.forms"
        :ref="envAddOptions.ref"
        :current-form-value="envAddOptions.currentFormValue"
        label-width="80px"
      ></base-form>
      <template slot="button">
        <el-button v-bind="GLOBAL.button" @click="saveEnvAddData">保存</el-button>
      </template>
    </op-freight>
  </div>
</template>
<script>
import { getListService } from '@/api/home'
import clusterIndex from './component/clusterIndex'
import { common, baseStructure } from '@/mixins/index'
import OpFreight from '@/modules/modal/freight/OpFreight'
import OpMessage from '@/modules/modal/freight/OpMessage'
import { deleteService } from '@/api/service'
// 重命名环境
import envRename from '../env/rename'
// 配置database
import envDatabase from '../env/database'
import { getInstallUrl } from '@/api/host'
// 添加环境
import envAdd from '../env/add'

export default {
  name: 'Welcome',
  mixins: [common, baseStructure, envRename, envDatabase, envAdd],
  components: {
    OpMessage,
    OpFreight,
    clusterIndex
  },
  props: {
    envDetailId: undefined
  },
  data() {
    return {
      envList: [],
      isCollapse: [],
      modalTitle: '',
      formData: { installUrl: '' },
      dialog: false,
      moreServices: [],
      moreServicesCount: []
    }
  },
  created() {
    this.loadData()
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
    //向环境添加新主机
    async addHost(envId) {
      this.modalTitle = '脚本安装'
      const res = await getInstallUrl(envId)
      this.formData.installUrl = res.result
      this.dialog = true
    },
    async loadData() {
      let params = {}
      if (this.envDetailId) {
        params.envId = this.envDetailId
      }
      this.envList = []
      this.moreServices = []
      this.moreServicesCount = []
      const { result } = await getListService(params)
      if (result.length) {
        for (const item of result) {
          this.isCollapse = Object.assign({}, this.isCollapse, { [item.id]: false })
          this.moreServicesCount = Object.assign({}, this.moreServicesCount, { [item.id]: item.services.length })
          if (item.services.length < 6) {
            this.moreServices = Object.assign({}, this.moreServices, { [item.id]: item.services })
          } else {
            this.moreServices = Object.assign({}, this.moreServices, { [item.id]: item.services.slice(0, 5) })
          }
        }
      }
      this.envList = result
    },
    viewMore(envId) {
      for (const item of this.envList) {
        if (item.id === envId) {
          this.moreServices = Object.assign({}, this.moreServices, { [item.id]: item.services })
        }
      }
    },
    // 环境详情
    goEnvDetail(id) {
      this.$router.push({ name: 'envHome', params: { id: id } })
    },
    goServiceDetail(id, tab) {
      this.$router.push({ name: 'serviceHome', params: { id: id, tab: tab } })
    },
    goServiceAddHome(envId, envName) {
      this.$router.push({ name: 'serviceAddHome', params: { envId: envId, envName: envName } })
    },
    // 查看消费组详情
    hostList(envId) {
      this.$router.push({ name: 'hostList', params: { envId: envId } })
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
    },
    addInstance(item) {
      this.$router.push({
        name: 'instanceAddHome',
        params: {
          envId: item.environmentId,
          serverId: item.id,
          serverName: item.serverName,
          serverType: this.checkServerType(item)
        }
      })
    },
    checkServerType(item) {
      let serverType = item.serverType.toLowerCase()
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
    async handleDeleteService(id) {
      this.isDo({
        title: `是否确定删除服务？`,
        cb: async () => {
          await deleteService(id)
          this.message('success', '删除成功')
          this.loadData()
        }
      })
    }
  }
}
</script>
<style lang="scss">
.zms-home {
  height: 100%;
  .envlist {
    width: 328px;
    float: left;
    border-right: 1px solid #e6e5e6;
    box-sizing: border-box;
    height: 100%;
    overflow: auto;
    padding: 15px;
    .env-box {
      margin-bottom: 10px;
      font-size: 14px;
      box-shadow: 0 0 12px #ccc;
      .el-menu-item {
        padding: 0px 10px;
      }
      .link {
        color: #303133;
        margin: 0;
        padding: 0;
        cursor: pointer;
        width: 70%;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        display: inline-block;
        &:hover {
          color: #2b71ff;
          // text-decoration: underline;
        }
      }
      .content-host {
        padding-left: 20px;
        margin: 6px 0 8px 0;
        color: #2b71ff;
      }
      .hostlink {
        margin-left: 10px;
        cursor: pointer;
        &:hover {
          text-decoration: underline;
        }
      }
      .el-dropdown {
        padding-top: 2px;
      }
      .content_border:hover {
        background-color: #dcdfe6;
      }
      .content_icon {
        border-width: 5px;
        border-style: solid;
        border-color: #409eff transparent transparent transparent;
      }
      .health_circle {
        float: right;
        margin-top: 16px;
        margin-right: 35px;
        width: 8px;
        height: 8px;
        border-radius: 50px;
      }
      .health_success {
        background: #07e2b8;
      }
      .health_warning {
        background: #e6a23c;
      }
      .health_danger {
        background: #f56c6c;
      }
      .content_service_button {
        text-align: center;
        small {
          cursor: pointer;
          font-style: italic;
          &:hover {
            color: #2b71ff;
          }
        }
      }
    }
  }
  .welcome__right {
    position: absolute;
    top: 0px;
    left: 327px;
    right: 0px;
    bottom: 0;
    padding: 0px 15px;
    border-radius: 4px;
    overflow-y: auto;
    overflow-x: hidden;
  }
  .zms-menu {
    border-radius: 4px;
    // margin-bottom: 15px;
    padding-bottom: 10px;
    border-right: none;
    .el-dropdown {
      float: right;
    }
    .zms-dropdown-wrap {
      position: absolute;
      top: -3px;
      right: 22px;
    }
    .el-menu-item:focus,
    .el-menu-item:hover {
      background: transparent;
    }
    > li {
      padding-left: 13px !important;
      cursor: default;
      // margin-top: 10px;
      .link {
        font-size: 18px !important;
      }
      i {
        cursor: pointer;
      }
    }
    > div {
      > li {
        padding-left: 15px !important;
      }
      > div {
        li {
          padding-left: 20px !important;
        }
      }
    }
  }
  .el-divider {
    margin: 10px 0;
  }
  /*中间的过度的横线*/
  .link-line {
    // margin: 10px 10px 5px 10px;
    margin: 5px 0;
    border-top: solid #edf1f7 1px;
  }
  /*中间的过度的横线*/
  .host-link-line {
    margin-top: 5px;
    border-top: solid #edf1f7 1px;
  }
  .el-menu-item,
  .el-submenu__title,
  .el-submenu .el-menu-item {
    height: 36px;
  }
}
.border {
  border-top: 1px solid #cccccc82;
}
.item {
  margin: 4px;
}

h3 {
  margin-top: 0;
  margin-bottom: 6px;
}
.envAdd {
  position: fixed;
  bottom: 35px;
  left: 25px;
  box-shadow: 1px 3px 12px #c1c1c1;
  i {
    font-weight: bold;
    font-size: 20px;
  }
}
</style>
