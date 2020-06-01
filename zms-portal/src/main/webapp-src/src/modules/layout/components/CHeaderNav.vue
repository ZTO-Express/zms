<template>
  <header id="c-header" class="c-header-nav">
    <div class="u-flex">
      <div class="c-header-nav__logo u-flex-left">
        <!-- <img class="c-header-nav__img" :src="logo" /> -->
        ZMS管理系统
      </div>
      <!-- <el-tabs v-model="navCurrent" @tab-click="handleClick">
        <el-tab-pane
          v-for="item in navArr"
          :label="item.label"
          :name="item.prop"
          :key="item.prop"
        >
          {{ item.label }}
        </el-tab-pane>
      </el-tabs> -->
      <sidebar></sidebar>
    </div>
    <div class="u-flex">
      <el-popover placement="bottom" popper-class="c-header-nav__user" trigger="hover">
        <div class="user__info">
          <img class="user__avatar" :src="userInfo.avatar ? userInfo.avatar : avatar" />
          <div class="user__date">{{ date }}</div>
        </div>
        <div class="user__svg u-flex-center" slot="reference">
          <svg-icon icon-class="yonghu"></svg-icon>
          <div class="user__name">{{ userInfo.realName }}</div>
        </div>
      </el-popover>
      <div class="c-header-nav__line"></div>
      <div class="c-header-nav__logout u-flex-center" @click="logout">
        <svg-icon icon-class="guanbi"></svg-icon>
      </div>
    </div>
    <!-- <div class="marqueeNotice">
      <marquee>🔔日常问题联系周桥、常哲、冯横廉</marquee>
    </div> -->
  </header>
</template>
<script>
import logo from '@/assets/layout/logo.png'
import avatar from '@/assets/layout/header-avatar.jpg'
import { mapState, mapActions } from 'vuex'
import { util } from '@/utils/util'
import { clearSysSession } from '@/api/auth'
import { removeLogin } from '@/utils/login'
import Sidebar from './Sidebar'

export default {
  name: 'CHeaderNav',
  components: {
    Sidebar
  },
  data() {
    return {
      logo: logo,
      avatar: avatar,
      date: util.dateToStr(new Date(), 1),
      navCurrent: ''
    }
  },
  computed: {
    ...mapState({
      userInfo: state => state.user.userInfo,
      navArr: state => state.common.navArr,
      navCur: state => state.common.navCur
    })
  },
  watch: {
    navCur() {
      this.navCurrent = this.navCur
    }
  },
  mounted() {
    this.navCurrent = this.navCur
  },
  methods: {
    ...mapActions({
      setNavCur: 'common/setNavCur'
    }),
    handleClick(tab) {
      this.setNavCur(tab.name)
    },
    logout() {
      this.$confirm('您确定要退出吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(
        () => {
          clearSysSession().then(() => {
            removeLogin()
            window.location.href = util.getLoginPath()
            window.location.reload()
          })
        },
        () => {}
      )
    }
  }
}
</script>
<style lang="scss" scoped>
.c-header-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  .c-header-nav__img {
    width: 30px;
    margin-right: 10px;
  }
  /deep/ .el-tabs__header {
    margin: 0 0 0 40px;
    line-height: 50px;
  }
  /deep/ .el-tabs__active-bar {
    bottom: inherit;
    top: 0;
  }
  /deep/ .el-tabs__nav-wrap::after {
    bottom: inherit;
    top: 0;
  }
  /deep/ .el-tabs__item {
    font-weight: 300; // padding: 0 30px;
  }
  /deep/ .el-tabs__content {
    display: none;
  }
  .c-header-nav__line {
    height: 26px;
    width: 1px;
    margin: 0 20px;
  }
  .user__svg {
    height: 26px;
    outline: none;
    cursor: pointer;
    .svg-icon {
      font-size: 18px;
    }
  }
  .c-header-nav__logout {
    margin-right: 20px;
    cursor: pointer;
    .svg-icon {
      font-size: 16px;
    }
  }
  .marqueeNotice {
    font-size: 12px;
    padding: 12px 0 0px;
    color: #fff;
    position: absolute;
    right: 130px;
    width: calc(100% - 1000px);
    line-height: 30px;
  }

  .user__name {
    font-size: 15px;
    margin-left: 5px;
  }
}
</style>
