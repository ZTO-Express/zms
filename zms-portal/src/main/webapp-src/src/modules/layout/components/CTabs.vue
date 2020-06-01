<template>
  <tab-menu
    class="tab-menu"
    :class="classObj"
    :panes="tabs"
    :current-name="currentName"
    :current-params="currentParams"
    :current-query="currentQuery"
    @close-other="closeOther"
    @close-all="closeAll"
    @tab-remove="removeTab"
    @tab-click="clickTab"
  ></tab-menu>
</template>

<script>
import TabMenu from '../../components/tabs/TabMenu.vue'
import { mapActions, mapGetters, mapState } from 'vuex'
// tabs 的name 等于 route name
// 在elemnt组件基础上新增加"关闭其他标签"，"关闭所有标签"可以被父元素监听！去修改store中的tabs
export default {
  name: 'CTabs',
  components: { TabMenu },
  props: {
    shadowStatus: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      currentName: '',
      currentParams: {},
      currentQuery: {}
    }
  },
  computed: {
    ...mapState({
      tagsShadow: state => state.tagsView.tagsShadow
    }),
    ...mapGetters({
      tabs: 'getVisitedViews' // 总浏览页面
    }),
    classObj() {
      return {
        'tab-menu--shadow': this.tagsShadow
      }
    }
  },
  watch: {
    $route: {
      handler: function(to, from) {
        this.addViewTags()
        this.currentName = this.$route.name
        this.currentParams = this.$route.params
        this.currentQuery = this.$route.query
        //导航菜单栏切换
        if (!from || (from && to.meta.nav !== from.meta.nav)) {
          this.setNavCur(to.meta.nav)
        }
      },
      immediate: true
    }
  },
  methods: {
    ...mapActions({
      setNavCur: 'common/setNavCur',
      addVisitedView: 'tagsView/addVisitedView',
      delVisitedView: 'tagsView/delVisitedView',
      delOthersViews: 'tagsView/delOthersViews',
      delAllViews: 'tagsView/delAllViews',
      delFrameViews: 'frame/delFrameViews',
      delOthersFrames: 'frame/delOthersFrames',
      delAllFrames: 'frame/delAllFrames'
    }),
    // 关闭其他标签
    closeOther(tab) {
      this.delOthersFrames(tab)
      this.delOthersViews(tab).then(() => {
        if (
          tab.name !== this.currentName ||
          JSON.stringify(tab.params) !== JSON.stringify(this.currentParams) ||
          JSON.stringify(tab.query) !== JSON.stringify(this.currentQuery)
        )
          this.$router.push({
            name: tab.name,
            params: tab.params,
            query: tab.query
          })
      })
    },
    // 关闭所有标签
    closeAll() {
      this.delAllFrames()
      this.delAllViews().then(() => {
        this.$router.push('/')
      })
    },
    // 关闭tab
    removeTab(tab) {
      // let _this = this
      // 删除匹配规则见状态中的mutation方法
      if (tab.iframe) this.delFrameViews(tab)
      this.delVisitedView(tab).then(views => {
        if (views.length === 0) {
          this.$router.push('/')
        } else if (
          !views.find(item => {
            return (
              item.name === this.currentName &&
              JSON.stringify(item.params) === JSON.stringify(this.currentParams) &&
              JSON.stringify(item.query) === JSON.stringify(this.currentQuery)
            )
          })
        ) {
          const latestView = views.slice(-1)[0]
          this.currentName = latestView.name
          this.currentParams = latestView.params
          this.currentQuery = latestView.query
          this.$router.push({
            name: latestView.name,
            params: latestView.params,
            query: latestView.query
          })
        }
      })
    },
    // 选中tab
    clickTab(tab) {
      //此时回调this.currentName已经变了，所以用this.$route
      if (
        tab.name !== this.currentName ||
        JSON.stringify(tab.params) !== JSON.stringify(this.currentParams) ||
        JSON.stringify(tab.query) !== JSON.stringify(this.currentQuery)
      ) {
        this.$router.push({
          name: tab.name,
          params: tab.params,
          query: tab.query
        })
      }
    },
    addViewTags() {
      if (this.$route.name) this.addVisitedView(this.$route)
    }
  }
}
</script>
