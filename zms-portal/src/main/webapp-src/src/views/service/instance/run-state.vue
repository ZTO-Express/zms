<template>
  <div class="content_run_state">
    <p class="content_header" v-if="type == 'firstRun'">{{ headerTitle }}</p>
    <div class="content_state">
      <span>状态</span>
      <div
        class="content_icon"
        v-if="runStatus == 0"
        v-loading="runLoading"
        :element-loading-text="loadingText"
        element-loading-spinner="el-icon-loading"
      ></div>
      <b v-else-if="runStatus == 1" icon="el-icon-date" style="margin-left: 10px;color: green">
        <i class="el-icon-success"></i>
        已完成
      </b>
      <b v-else style="margin-left: 10px;color: red">
        <i class="el-icon-warning"></i>
        失败
      </b>
      <i style="margin-left: 20px" class="el-icon-date"></i>
      <span style="margin-left: 10px">{{ runTime }}</span>
      <i style="margin-left: 20px" class="el-icon-time"></i>
      <span style="margin-left: 10px">{{ runningTimeStr }}</span>
    </div>
    <div class="content_result">
      <el-collapse v-for="item in runResult" :key="item.processId">
        <el-collapse-item :class="processResultClass(item.status)">
          <template slot="title">
            <i :class="processResultClass(item.status) === 'success' ? 'el-icon-success' : 'el-icon-warning'"></i>
            {{ item.instanceName }}
          </template>
          <el-tabs v-model="logActiveName">
            <el-tab-pane v-if="item.stdoutLog" label="stdout" name="stdout">
              <div class="stdlog">
                {{ item.stdoutLog }}
              </div>
            </el-tab-pane>
            <el-tab-pane v-if="item.stderrLog" label="stderr" name="stderr">
              <div class="stdlog">
                {{ item.stderrLog }}
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>
<script>
import { util } from '@/utils/util'
import { getRunningStatus, tailProcessStderrLog, tailProcessStdoutLog } from '@/api/process'

export default {
  name: 'serviceRunState',
  props: {
    headerTitle: String,
    type: String,
    loadingText: String
  },
  data() {
    return {
      runningTimeStr: '0s',
      runningTimer: '',
      runningTime: 0,
      runTime: '',
      runStatus: 0,
      runLoading: true,
      runResult: [],
      instanceParam: {},
      runningStatusTimer: '',
      processIds: [],
      logActiveName: 'stdout'
    }
  },
  created() {
    this.runTime = util.getCurDateTime()
    this.runningTimer = setInterval(() => {
      this.runningTime = this.runningTime + 1
      this.runningTimeStr = this.runningTime + 's'
    }, 1000)
  },
  //销毁定时器
  beforeDestroy() {
    if (this.runningTimer) {
      clearInterval(this.runningTimer)
    }
    if (this.runningStatusTimer) {
      clearInterval(this.runningStatusTimer)
    }
  },
  methods: {
    checkProcessFailState() {
      this.runStatus = 2
      this.runLoading = false
      clearInterval(this.runningTimer)
    },
    checkProcessState(result) {
      if (!result) {
        this.checkProcessFailState()
        return
      }
      this.instanceParam = new Map()
      for (const item of result) {
        this.processIds.push(item.processId)
        this.instanceParam.set(item.processId, item.instanceName + '（' + item.hostName + '）')
      }
      this.runningStatusTimer = setInterval(() => {
        this.getProcessRunningStatus()
      }, 2000)
    },
    async getProcessRunningStatus() {
      if (this.processIds.length < 1) {
        return
      }
      const res = await getRunningStatus(this.processIds)
      if (res.result) {
        for (const item of res.result) {
          if (this.type === 'stop') {
            // 停止进程判断逻辑
            if (item.runningStatus !== 'STOPPING' && item.runningStatus !== 'RUNNING') {
              const list = this.processIds.filter(id => id != item.id)
              this.processIds = list

              let stderrLog = ''
              let stdoutLog = ''
              if (item.runningStatus === 'FATAL') {
                stderrLog = this.tailErrLog(item.id)
                stdoutLog = this.tailOutLog(item.id)
              }
              let result = {
                processId: item.id,
                instanceName: this.instanceParam.get(item.id),
                status: item.runningStatus === 'STOPPED' ? true : false,
                stderrLog: stderrLog,
                stdoutLog: stdoutLog
              }
              this.runResult.push(result)
            }
          } else {
            // 首次运行、启动、重启进程逻辑判断
            if (
              (item.status === 'SUCCESS' && item.runningStatus !== 'STARTING' && item.runningStatus !== 'STOPPED') ||
              item.status === 'FAILURE'
            ) {
              const list = this.processIds.filter(id => id != item.id)
              this.processIds = list

              let stderrLog = ''
              let stdoutLog = ''
              if (item.status === 'SUCCESS') {
                stderrLog = await this.tailErrLog(item.id)
                stdoutLog = await this.tailOutLog(item.id)
              }

              let result = {
                processId: item.id,
                instanceName: this.instanceParam.get(item.id),
                status: item.runningStatus === 'RUNNING' ? true : false,
                stderrLog: stderrLog,
                stdoutLog: stdoutLog
              }
              this.runResult.push(result)
            }
          }
        }
      }
      if (this.processIds.length < 1) {
        this.runStatus = 1
        this.runLoading = false
        clearInterval(this.runningStatusTimer)
        clearInterval(this.runningTimer)
        this.$emit('runComplete')
      }
    },
    processResultClass(status) {
      if (status) {
        return 'success'
      }
      return 'failure'
    },
    async tailErrLog(processId) {
      let param = { offset: 0, length: 10000 }
      const resErr = await tailProcessStderrLog(processId, param)
      if (resErr.result) {
        return resErr.result.log
      }
    },
    async tailOutLog(processId) {
      let param = { offset: 0, length: 10000 }
      const resOut = await tailProcessStdoutLog(processId, param)
      if (resOut.result) {
        return resOut.result.log
      }
    }
  }
}
</script>
<style lang="scss">
.content_run_state {
  margin: 0 auto;
  width: 80%;
  .content_header {
    font-size: 16px;
    color: #008cf6;
    margin: 25px 0;
  }
  .content_state {
    font-size: 14px;
    .content_icon {
      margin-left: 10px;
      display: inline-block;
      width: 75px;
      top: 5px;
      i {
        font-size: 16px;
        float: left;
        position: relative;
        top: 3px;
      }
    }
  }
  .content_result {
    font-size: 13px;
    margin-top: 15px;
    line-height: 20px;
  }
  .success {
    .el-collapse-item__header {
      color: green;
    }
    i {
      margin-right: 8px;
      font-size: 18px;
    }
  }
  .failure {
    .el-collapse-item__header {
      color: red;
    }
    i {
      margin-right: 8px;
      font-size: 18px;
    }
  }
  .stdlog {
    white-space: pre;
    background-color: beige;
    height: 500px;
    overflow-y: scroll;
  }
}
</style>
