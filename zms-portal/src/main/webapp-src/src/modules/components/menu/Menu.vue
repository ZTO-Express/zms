<template>
  <div>
    <el-menu
      ref="menu"
      mode="horizontal"
      :collapse="isCollapse"
      :unique-opened="uniqueOpened"
      class="el-menu-vertical c-menu"
      :default-active="defaultActive"
      :default-openeds="defaultOpeneds"
    >
      <sub-menu v-for="(item, i) in menus" :menu-item="item" :key="i" />
    </el-menu>
  </div>
</template>
<script>
import SubMenu from './SubMenu'
export default {
  components: {
    SubMenu
  },
  props: {
    uniqueOpened: {
      type: Boolean,
      default: false
    },
    isCollapse: {
      type: Boolean,
      default: false
    },
    defaultActive: {
      type: String,
      default: ''
    },
    defaultOpeneds: {
      type: Array,
      default: function() {
        return []
      }
    },
    menus: {
      type: Array,
      default: function() {
        return []
      }
    }
  }
}
</script>
<style lang="scss">
.c-menu {
  border: none;
  .el-menu-item {
    overflow: hidden;
    min-width: auto;
  }
}
.c-sidebar--collapse {
  .c-menu {
    .i-icon {
      margin-right: 0;
    }
    .el-tooltip {
      text-align: center;
      padding: 0 !important;
    }
    > div > .el-menu--nochildren {
      padding: 0 10px !important;
      margin-right: 0;
      span {
        display: none;
      }
    }
    .el-submenu__title {
      padding: 0 !important;
      text-align: center;
      span {
        display: none;
      }
      .el-submenu__icon-arrow,
      .el-icon-arrow-right {
        display: none;
      }
    }
  }
}
</style>
