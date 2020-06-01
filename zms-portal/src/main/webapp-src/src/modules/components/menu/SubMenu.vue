<template>
  <div class="el-menu--item" v-if="!menuItem.hidden">
    <template v-if="menuItem.children && menuItem.children.length > 0">
      <!-- 存在子级 -->
      <el-submenu :index="menuItem.path">
        <template slot="title">
          <c-icon :icon-class="menuItem.icon" class-name="i-icon"></c-icon>
          <!-- span 隐藏 -->
          <span>{{ menuItem.label }}</span>
        </template>
        <sub-menu v-for="item in menuItem.children" :key="item.name" :menu-item="item" :index="item.name" />
      </el-submenu>
    </template>
    <template v-else>
      <el-menu-item :index="menuItem.name" @click.native="executeRouter(menuItem)" class="el-menu--nochildren">
        <c-icon :icon-class="menuItem.icon" class-name="i-icon"></c-icon>
        <span slot="title">{{ menuItem.label }}</span>
      </el-menu-item>
    </template>
  </div>
</template>
<script>
import CIcon from '@/modules/components/icon/CIcon'
export default {
  name: 'SubMenu',
  components: { CIcon },
  props: {
    menuItem: {
      type: Object,
      default: function() {
        return {}
      }
    }
  },
  methods: {
    executeRouter(menu) {
      if (this.$route.name !== menu.name) {
        this.$router.push({ name: menu.name })
      }
    }
  }
}
</script>
