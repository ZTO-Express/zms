<template>
  <section class="c-main" ref="cmain" :style="{ height: pageHeight }" @scroll="scrollThrottle()">
    <div class="c-main__inner">
      <transition name="fade" mode="out-in">
        <!-- <keep-alive :include="cachedViews"> -->
        <router-view></router-view>
        <!-- </keep-alive> -->
      </transition>
      <frame-view v-show="!viewType" :iframe="iframe" :iframe-url="iframeUrl"></frame-view>
    </div>
  </section>
</template>
<script>
import { throttle } from 'throttle-debounce'
import FrameView from '@/modules/components/frame/FrameView.vue'
import { mapActions, mapState } from 'vuex'
export default {
  name: 'CMain',
  components: {
    FrameView
  },
  data() {
    return {
      pageHeight: 'auto',
      iframe: '',
      iframeUrl: '',
      redHeight: ''
    }
  },
  computed: {
    ...mapState({
      viewType: state => state.frame.viewType
    }),
    cachedViews() {
      return this.$store.getters.getCacheViews
    }
  },
  watch: {
    $route(to) {
      this.catView(to)
    }
  },
  created() {
    this.catView(this.$route)
    this.scrollThrottle = throttle(300, this.mainScroll)
    this.resizeThrottle = throttle(500, this.computedHeight)
    window.addEventListener('resize', () => {
      this.resizeThrottle()
    })
  },
  mounted() {
    this.computedHeight()
  },
  methods: {
    ...mapActions({
      catViewType: 'frame/catViewType',
      addFrameViews: 'frame/addFrameViews',
      setTagsShadow: 'tagsView/setTagsShadow'
    }),
    computedHeight() {
      const cHeader = document.getElementById('c-header')
      if (cHeader) {
        this.redHeight = cHeader.clientHeight
        this.pageHeight = document.documentElement.clientHeight - this.redHeight + 'px'
      }
    },
    catView(to) {
      if (to.meta.iframe) {
        this.iframe = to.meta.iframe
        this.iframeUrl = to.meta.iframeUrl
        this.catViewType(false)
        this.addFrameViews(to)
      } else {
        this.catViewType(true)
      }
    },
    mainScroll() {
      let shadowStatus = this.$refs.cmain.scrollTop > 0 ? true : false
      this.setTagsShadow(shadowStatus)
    }
  }
}
</script>
<style lang="scss" scoped>
.c-main {
  overflow: auto;
  position: relative;
  width: 100%;
  background-color: #f0f2f5;
  font-size: 0;
  .c-main__inner {
    display: inline-block;
    min-width: 1000px;
    width: 100%;
    height: 100%;
    font-size: 12px;
  }
}
</style>
