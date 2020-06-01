<template>
  <div class="l-page l-page--flex consumerdelay">
    <div class="page-nav">MQ消息检索</div>

    <!-- <div class="l-page__search">
      <el-collapse-transition>
        <div v-show="!isCollapse">
          
          <el-form
            :model="formData"
            ref="formData"
            size="mini"
            class="search__form"
            label-position="right"
            :label-width="GLOBAL.fFormLabel"
          >
            <el-row :gutter="0">
              <el-col :span="5">
                <el-form-item
                  label="环境"
                  prop="envId"
                  :rules="[{ required: true, message: '请选择环境', trigger: 'blur' }]"
                >
                  <el-select
                    v-model="formData.envId"
                    v-bind="GLOBAL.select"
                    v-loading="SearchEnv.loading"
                    @focus="getEnvOptions"
                  >
                    <el-option
                      v-for="item in SearchEnv.options"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item
                  label="主题名"
                  prop="topicName"
                  :rules="[{ required: true, message: '请选择主题', trigger: 'blur' }]"
                >
                  <el-select
                    v-model="formData.topicName"
                    v-bind="GLOBAL.select"
                    v-loading="SearchTheme.loading"
                    @focus="getThemeOptions"
                  >
                    <el-option
                      v-for="item in SearchTheme.options"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item
                  label="检索类型"
                  prop="type"
                  :rules="[{ required: true, message: '请选择检索类型', trigger: 'blur' }]"
                >
                  <el-select v-model="formData.type" v-bind="GLOBAL.select">
                    <el-option
                      v-for="item in SearchType.options"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item
                  label="检索值"
                  prop="searchText"
                  :rules="[{ required: true, message: '请输入检索值', trigger: 'blur' }]"
                >
                  <el-input v-model="formData.searchText"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <div class="search__btns">
                  <el-button type="primary" size="mini" @click="searchHandler">
                    查询
                  </el-button>
                  <el-button type="primary" size="mini" @click="onReset">
                    重置
                  </el-button>
                </div>
              </el-col>
            </el-row>
          </el-form>
        </div>
      </el-collapse-transition>
       <div class="search__collapse" @click="isCollapse = !isCollapse">
        {{ isCollapse ? '更多查询' : '收起' }}
        <svg-icon :icon-class="isCollapse ? 'arrowdown-1' : 'arrowup-1'"></svg-icon>
      </div> 
    </div> -->
    <div class="l-page__content">
      <div class="content--padding">
        <div class="content__header">
          <el-form
            :model="formData"
            ref="formData"
            size="mini"
            class="search__form"
            label-position="right"
            :label-width="GLOBAL.fFormLabel"
          >
            <el-row :gutter="0">
              <el-col :span="3">
                <el-form-item
                  label="环境"
                  prop="envId"
                  :rules="[{ required: true, message: '请选择环境', trigger: 'blur' }]"
                >
                  <el-select
                    v-model="formData.envId"
                    v-bind="GLOBAL.select"
                    v-loading="SearchEnv.loading"
                    @focus="getEnvOptions"
                  >
                    <el-option
                      v-for="item in SearchEnv.options"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="5">
                <el-form-item
                  label="主题名"
                  prop="topicName"
                  :rules="[{ required: true, message: '请选择主题', trigger: 'blur' }]"
                >
                  <el-select
                    v-model="formData.topicName"
                    v-bind="GLOBAL.select"
                    v-loading="SearchTheme.loading"
                    @focus="getThemeOptions"
                  >
                    <el-option
                      v-for="item in SearchTheme.options"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item
                  label="检索类型"
                  prop="type"
                  :rules="[{ required: true, message: '请选择检索类型', trigger: 'blur' }]"
                >
                  <el-select v-model="formData.type" v-bind="GLOBAL.select">
                    <el-option
                      v-for="item in SearchType.options"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-form-item
                  label="检索值"
                  prop="searchText"
                  :rules="[{ required: true, message: '请输入检索值', trigger: 'blur' }]"
                >
                  <el-input v-model="formData.searchText"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <div class="search__btns" style="margin-left:15px;">
                  <el-button type="primary" style="padding-top:10px;" size="mini" @click="searchHandler">
                    查询
                  </el-button>
                  <el-button type="primary" style="padding-top:10px;" size="mini" @click="onReset">
                    重置
                  </el-button>
                </div>
              </el-col>
            </el-row>
          </el-form>
        </div>
        <div class="content__body">
          <table-pagination v-bind="maintable" :data-handler="handleTableData">
            <template slot="insert">
              <el-table-column label="操作" fixed="left" :width="90" align="center">
                <template slot-scope="scope">
                  <operate-button type="success" @click="handleDetail(scope.row)">详情</operate-button>
                </template>
              </el-table-column>
            </template>
          </table-pagination>
        </div>
      </div>
    </div>
    <!-- 详情弹窗 -->
    <op-message
      :dialog="diaOptions.visible"
      :modalTitle="diaOptions.diatitle"
      :loading="diaOptions.loading"
      width="54%"
      @dealDialog="closeDia"
    >
      <base-form
        :forms="diaOptions.forms"
        :ref="diaOptions.ref"
        :inline="diaOptions.inline"
        :current-form-value="diaOptions.currentFormValue"
        label-width="120px"
      />
      <template slot="table">
        <el-form label-width="120px">
          <el-form-item label="消费情况">
            <div class="message-table">
              <el-table :data="diaOptions.currentFormValue.messageTrackList" border :height="diaOptions.heightSize">
                <el-table-column label="序号" width="40px" align="left">
                  <template slot-scope="scope">
                    <span>{{ scope.$index + 1 }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="Consumer Group" prop="consumerGroup" align="left"></el-table-column>
                <el-table-column label="TrackType" prop="trackType" align="left"></el-table-column>
              </el-table>
            </div>
          </el-form-item>
        </el-form>
      </template>
    </op-message>
  </div>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import OpMessage from '@/modules/modal/freight/OpMessage'
// 工具方法
import { util } from '@/utils/util'
// 搜索相关
import search from './list-search'
// 详情相关
import detail from './list-detail'
// 请求接口
import { viewByKey, viewByMsgId } from '@/api/message'

export default {
  name: 'messageSearch',
  mixins: [common, baseStructure, search, detail],
  components: {
    OpMessage
  },
  data() {
    return {
      maintable: {
        ref: 'maintable',
        columns: [
          { type: 'index', width: 40, fixed: 'left' },
          { prop: 'msgId', label: '消息Id', width: 300 },
          { prop: 'properties.TAGS', label: 'Tag', width: 150 },
          { prop: 'properties.KEYS', label: 'Key', width: 360 },
          { prop: 'bornHost', label: '发送端IP', width: 188 },
          { prop: 'bornTimestamp', label: '发送时间', 'min-width': 180 }
        ],
        data: [],
        insertNum: 1,
        loading: false
      }
    }
  },
  methods: {
    // 获取表格数据
    async loadTabledata(params = {}) {
      this.maintable.loading = true
      let res = ''
      let searchText = params.searchText
      if (params.type === 'key') {
        params.key = searchText
        res = await viewByKey(params)
      } else {
        params.msgId = searchText
        res = await viewByMsgId(params)
      }
      if (res.result) {
        this.maintable.data = res.result
        this.maintable.loading = false
      }
    },
    // 表格数据处理
    handleTableData(item) {
      const { bornTimestamp, storeTimestamp } = item
      Object.assign(item, {
        bornTimestamp: util.dateToStr(new Date(bornTimestamp), 3),
        storeTimestamp: util.dateToStr(new Date(storeTimestamp), 3)
      })
      return item
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss">
.message-table .el-table__header th {
  background-color: #f5f7fa;
}
.message-table .has-gutter th {
  line-height: 25px;
}
</style>
