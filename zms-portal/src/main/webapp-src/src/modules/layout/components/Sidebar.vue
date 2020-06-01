<template>
  <div class="c-sidebar" :class="{ 'c-sidebar--collapse': isCollapse, 'c-sidebar--nav': navOpen }">
    <div class="c-sidebar__header u-flex-center" v-if="!navOpen">
      <img class="c-sidebar__logo" :src="logo" v-if="!isCollapse" />
      <svg-icon icon-class="branch-1" v-else></svg-icon>
    </div>
    <div class="c-sidebar__body">
      <c-menu
        :menus="userInfo.menus[navCur]"
        :isCollapse="isCollapse"
        :defaultOpeneds="defaultOpeneds"
        :defaultActive="defaultActive"
        :uniqueOpened="uniqueOpened"
      ></c-menu>
    </div>
    <!-- <div class="c-sidebar__footer u-flex-center" @click="toggleMenu">
      <svg-icon
        :icon-class="isCollapse ? 'arrowright-2' : 'arrowleft-2'"
      ></svg-icon>
    </div> -->
  </div>
</template>
<script>
import { mapState, mapGetters } from 'vuex'
import logo from '@/assets/layout/logo.png'
import CMenu from '@/modules/components/menu/Menu'
export default {
  name: 'Sidebar',
  // 必须严格按照此格式，
  components: {
    CMenu: CMenu
  },
  data() {
    return {
      uniqueOpened: false, // 是否允许只能打开一个菜单
      logo: logo, // 侧边栏展开状态下的logo
      // isCollapse: false, // 侧边栏是否默认收起
      defaultActive: '', // 默认激活的导航
      defaultOpeneds: [] // 默认打开的菜单
    }
  },
  computed: {
    ...mapGetters(['sidebar']),
    ...mapState({
      userInfo: state => state.user.userInfo,
      navOpen: state => state.common.navOpen,
      navCur: state => state.common.navCur
    }),
    // 侧边栏是否默认收起
    isCollapse() {
      return !this.sidebar.opened
    }
  },
  watch: {
    $route: {
      handler: function() {
        this.defaultActive = this.$route.name
      },
      immediate: true
    }
  },
  methods: {
    toggleMenu() {
      this.$store.dispatch('common/toggleSideBar')
    }
  }
}
</script>
<style lang="scss">
.c-sidebar {
  .el-submenu__title:focus,
  .el-submenu__title:hover {
    background: transparent;
  }
  .el-menu--item {
    float: left;
    margin-right: 10px;
  }
  :focus {
    outline: 0;
  }
  .el-menu.el-menu--horizontal {
    border-bottom: none;
  }
  .c-sidebar__body {
    padding-top: 9px;
    .el-menu-item,
    .el-submenu__title {
      font-size: 15px;
    }
  }
  .el-submenu__icon-arrow {
    right: 5px;
    margin-top: -4px;
  }
  ::-webkit-scrollbar {
    position: absolute;
    width: 10px;
    margin-left: -10px;
    -webkit-appearance: none;
  }
  ::-webkit-scrollbar-thumb {
    height: 50px;
    background-clip: content-box;
    border-color: transparent;
    border-style: solid;
    border-width: 1px 2px;
  }
}
.el-menu--horizontal .el-menu .el-menu-item.is-active,
.el-menu--horizontal .el-menu .el-submenu.is-active > .el-submenu__title,
.el-menu--horizontal .el-menu-item:not(.is-disabled):focus,
.el-menu--horizontal .el-menu-item:not(.is-disabled):hover {
  // color: #47a6ff;
  color: #2b71ff;
}
.el-menu--horizontal .el-menu .el-menu-item,
.el-menu--horizontal .el-menu .el-submenu__title {
  // color: #3693ff;
  color: #2b71ff;
}
</style>
