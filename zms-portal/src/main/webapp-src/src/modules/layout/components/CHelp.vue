<template>
  <div class="c-container__tooltip">
    <transition name="el-zoom-in-center">
      <div v-show="!iconfont_isCollapse" class="transition-box">
        <div class="iconfont__tooltip">
          <el-tooltip effect="light" placement="left">
            <div slot="content">
              <span>使用帮助</span>
              <br />
              <el-link
                href="https://github.com/ZTO-Express/zms/wiki/zms%E8%AE%BE%E8%AE%A1%E6%96%87%E6%A1%A3"
                target="_blank"
                type="primary"
                :underline="false"
                style="font-size:12px"
              >
                点此跳转
              </el-link>
            </div>
            <i class="iconfont icon-help iconfont-help"></i>
          </el-tooltip>
        </div>
        <div style="margin-top: 5px" class="iconfont__tooltip">
          <el-tooltip effect="light" placement="left">
            <div slot="content">
              <span>问题反馈</span>
              <br />
              <el-link
                href="https://github.com/ZTO-Express/zms/issues"
                target="_blank"
                type="primary"
                :underline="false"
                style="font-size:12px"
              >
                点此跳转
              </el-link>
            </div>
            <i class="iconfont icon-issue iconfont-issue"></i>
          </el-tooltip>
        </div>
      </div>
    </transition>
    <div class="iconfont_isCollapse">
      <i
        :class="!iconfont_isCollapse ? 'el-icon-d-arrow-right' : 'el-icon-d-arrow-left'"
        @click="iconfont_isCollapse = !iconfont_isCollapse"
        class="iconfont_isCollapse-size"
      ></i>
    </div>
  </div>
</template>
<script>
export default {
  name: 'CHelp',
  data() {
    return {
      iconfont_isCollapse: false
    }
  }
}
</script>
<style lang="scss">
.el-tooltip__popper.is-light {
  border: solid #e6e5e6 1px;
}
.c-container__tooltip {
  position: fixed;
  bottom: 60px;
  right: 50px;
  z-index: 1000000;
  .iconfont_isCollapse {
    position: relative;
    bottom: 46px;
    left: 30px;
  }
  .iconfont__tooltip {
    border: solid #edf1f7 1px;
    background-color: #fff;
    padding: 5px;
    &:hover {
      background-color: #edf1f7;
      color: #2b71ff;
    }
  }
  .iconfont_isCollapse-size {
    font-size: 27px;
    // color: #2b71ff;
  }
  .iconfont-help {
    font-size: 22px;
  }
  .iconfont-issue {
    font-size: 20px;
  }
}
</style>
