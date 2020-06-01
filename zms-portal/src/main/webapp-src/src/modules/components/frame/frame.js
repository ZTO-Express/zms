import { mapState } from 'vuex'
export default {
  data() {
    return {
      iframeObj: {} //当前的iframe
    }
  },
  computed: {
    ...mapState({
      frameViews: state => state.frame.frameViews
    }),
    tabUrl() {
      return this.iframe + this.iframeUrl
    }
  },
  watch: {
    tabUrl: {
      handler: function() {
        this.dealObj()
        if (this.iframeUrl) this.dealJump()
        if (this.iframeObj.left || this.iframeObj.top) this.dealMargin()
      }
    }
  },
  created() {
    this.dealObj()
    if (this.iframeUrl) this.dealJump()
  },
  mounted() {
    if (this.iframeObj.left || this.iframeObj.top) this.dealMargin()
  },
  methods: {
    //赋值iframeObj，当前所处的iframe
    dealObj() {
      for (const v of this.iframeData) {
        if (this.iframe === v.name) this.iframeObj = v
      }
    },
    //处理子系统多出的菜单、导航。margin
    dealMargin() {
      const dom = document.getElementById(`pane-${this.iframe}`)
      dom.style.marginLeft = `-${this.iframeObj.left}px`
      dom.style.marginTop = `-${this.iframeObj.top}px`
      dom.style.height = `calc(100% + ${this.iframeObj.top}px)`
    },
    //同域名 iframe 缓存及复用,通知子系统跳转
    dealJump() {
      if (!window.frames[this.iframe])
        this.iframeObj.url = `${this.iframeObj.domain}${this.iframeObj.mode}${this.iframeUrl}`
      if (window.frames[this.iframe]) window.frames[this.iframe].postMessage(this.iframeUrl, this.iframeObj.domain)
    },
    //动态iframe，调接口前处理
    dealFetch() {
      let _this = this
      return new Promise(resolve => {
        const dom = document.getElementById(`pane-${this.iframe}`)
        if (!dom || (dom && !dom.innerHTML)) {
          if (this.iframeFetch.indexOf(this.$route.meta.iframe) > -1) {
            let index = -1
            for (const [i, v] of this.iframeData.entries()) {
              if (_this.iframe === v.name) index = i
            }
            if (index > -1) this.iframeData.splice(index, 1)
          }
          resolve()
        }
      })
    },
    //动态iframe，调接口后处理
    afterFetch(obj) {
      this.iframeData.push(obj)
      this.iframeObj = obj
      if (this.iframeObj.left || this.iframeObj.top) {
        this.$nextTick(() => {
          this.dealMargin()
        })
      }
    }
  }
}
