<template>
  <div :class="'l-wrapper skin-' + skin">
    <c-header v-if="!navOpen"></c-header>
    <c-header-nav v-if="navOpen"></c-header-nav>
    <div class="c-container" :class="classObj">
      <div class="c-container__content">
        <!--主要内容-->
        <c-main></c-main>
        <c-help></c-help>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex'
import CHeader from './components/CHeader'
import CHeaderNav from './components/CHeaderNav'
import CMain from './components/CMain'
import CHelp from './components/CHelp'

export default {
  name: 'Layout',
  components: {
    CMain,
    CHeader,
    CHeaderNav,
    CHelp
  },
  data() {
    return {}
  },
  computed: {
    ...mapState({
      navOpen: state => state.common.navOpen,
      skin: state => state.common.skin,
      userInfo: state => state.user.userInfo,
      sidebar: state => state.common.sidebar
    }),
    classObj() {
      return {
        'c-sidebar--hide': !this.sidebar.opened
      }
    }
  },
  mounted() {
    // 初始化水印代码。不需要水印,则注释掉
    // this.initWater()
    // 关闭当前标签事件
    this.Event.$on('closeTab', () => {
      const tab = {
        name: this.$route.name,
        params: this.$route.params,
        query: this.$route.query
      }
      this.$refs.cTabs.removeTab(tab)
    })
  }
}
</script>

<style lang="scss" scoped>
.c-container {
  .c-container__content {
    transition: margin 0.2s linear 0.01s;
  }
}
</style>
