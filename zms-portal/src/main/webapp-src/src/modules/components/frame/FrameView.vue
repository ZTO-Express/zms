<template>
  <!--iframe页面展示区域-->
  <div class="frame-view">
    <el-tabs :value="iframe" type="card">
      <el-tab-pane v-for="item in iframeData" :key="item.name" :label="item.name" :name="item.name">
        <iframe
          :src="item.url"
          :key="item.name"
          :name="item.name"
          frameborder="0"
          v-if="frameViews.indexOf(item.name) > -1"
        ></iframe>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script>
import iframeMixins from './frame'
export default {
  name: 'FrameView',
  mixins: [iframeMixins],
  props: {
    iframe: {
      type: String,
      default: ''
    },
    iframeUrl: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      //注册静态的iframe
      iframeData: [],
      iframeFetch: [] //注册动态的iframe，存iframe的name
    }
  },
  watch: {
    $route: {
      handler: function() {
        // 在此处、根据业务调相应接口
      },
      immediate: true
    }
  }
}
</script>
<style lang="scss">
.frame-view {
  position: absolute;
  left: 0;
  right: 0;
  top: 0px;
  bottom: 0;
  z-index: 1000 !important;
  .el-tabs {
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    bottom: 0;
  }
  .el-tabs__header {
    display: none;
  }
  .el-tabs__content {
    width: 100%;
    height: 100%;
  }
  .el-tab-pane {
    height: 100%;
    iframe {
      height: 100%;
      width: 100%;
    }
  }
}
</style>
