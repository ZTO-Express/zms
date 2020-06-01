<template>
  <div class="content_restart">
    <div v-for="item in propertyList" :key="item.instanceId">
      <p class="content_instanceNamer">{{ item.instanceName }}</p>
      <div v-for="file in item.fileList" :key="file.fileName" style="margin-bottom: 20px;">
        <div class="content_fileName">
          <span style="margin-left: 10px;">文件：{{ file.fileName }}</span>
          <span style="margin-left: 10px;">
            （-{{ file.reduceNum }}，{{ file.processNum }}，+{{ file.addNum }}，{{ file.currentNum }}）
          </span>
        </div>
        <div v-for="val in file.compareList" :key="val.id" style="height: 20px">
          <div class="content_num content_left_num">{{ val.leftNum }}</div>
          <div class="content_num content_right_num">{{ val.rightNum }}</div>
          <div :class="'+' == val.mark ? content_add : '-' == val.mark ? content_reduce : ''" class="content_mark">
            {{ val.mark == '' ? '&nbsp;' : val.mark }}
            {{ val.value }}
          </div>
        </div>
        <div class="content_bottom"></div>
      </div>
    </div>
    <div class="content_checkbox">
      <el-checkbox v-model="restartChecked">
        <b :class="content_red">
          重新部署客户端配置，群集中使用过期配置运行的所有服务及其依赖关系需要重启才能生效
        </b>
      </el-checkbox>
    </div>
    <div class="content_state" v-if="runStateFlg">
      <run-state ref="runState" type="restart" loading-text="正在重启"></run-state>
    </div>
    <div class="content_button">
      <el-button v-bind="GLOBAL.button" :disabled="runStateFlg" @click="confirmRestart">重启</el-button>
      <el-button size="mini" @click="cancelRestart">关闭</el-button>
    </div>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import { compareServiceProperty, restartServiceInstance } from '@/api/service'
import runState from './run-state'

export default {
  name: 'instanceReStart',
  mixins: [common, baseStructure],
  components: {
    runState
  },
  props: {
    serverId: Number,
    instanceIds: Array
  },
  data() {
    return {
      propertyList: [],
      restartChecked: false,
      runStateFlg: false,
      content_add: 'content_add',
      content_reduce: 'content_reduce',
      content_red: ''
    }
  },
  created() {
    this.loadServiceProperty()
  },
  watch: {
    restartChecked() {
      if (this.restartChecked) {
        this.content_red = ''
      }
    }
  },
  methods: {
    async loadServiceProperty() {
      const res = await compareServiceProperty(this.serverId, this.instanceIds)
      if (res.result) {
        this.propertyList = res.result
      }
    },
    async confirmRestart() {
      if (!this.restartChecked) {
        this.content_red = 'content_red'
        return
      }
      this.content_red = ''
      let instanceIds = []
      this.propertyList.forEach(item => {
        instanceIds.push(item.instanceId)
      })
      this.runStateFlg = true
      const res = await restartServiceInstance(instanceIds)
      this.$refs.runState.checkProcessState(res.result)
    },
    cancelRestart() {
      this.$emit('cancelRestart')
    }
  }
}
</script>
<style lang="scss">
.content_restart {
  .content_instanceNamer {
    font-size: 18px;
    font-weight: bold;
    text-align: center;
  }
  .content_fileName {
    height: 40px;
    line-height: 40px;
    background-color: #eff0f8;
    border: 1px solid #ccc;
  }
  .content_num {
    width: 30px;
    background-color: #eff0f8;
    display: inline-block;
    height: 20px;
    text-align: center;
    vertical-align: text-top;
  }
  .content_bottom {
    border-bottom: 1px solid #ccc;
  }
  .content_left_num {
    border-left: 1px solid #ccc;
  }
  .content_right_num {
    border-right: 1px solid #ccc;
  }
  .content_mark {
    width: calc(100% - 63px);
    white-space: pre;
    display: inline-block;
    height: 20px;
    border-right: 1px solid #ccc;
  }
  .content_reduce {
    background-color: #ffcccc;
  }
  .content_add {
    background-color: #99ff99;
  }
  .el-dialog__body {
    padding: 0 20px 30px;
  }
  .content_checkbox {
    margin-top: 30px;
  }
  .content_red {
    color: red;
  }
  .content_button {
    margin-top: 10px;
    text-align: center;
  }
  .content_state {
    margin-top: 30px;
  }
}
</style>
