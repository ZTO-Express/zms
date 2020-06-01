<template>
  <header id="c-header" class="c-header">
    <div class="u-flex">
      <div class="c-header__date u-flex-center 11">{{ date }}</div>
      <div class="c-header__line"></div>
      <div class="c-header__content u-flex-center">
        {{ userInfo.realName }}
      </div>
    </div>
    <div class="u-flex">
      <div class="c-header__logout" @click="logout">
        <i class="fa fa-sign-out" style="font-size:14px"></i>
        退出
      </div>
    </div>
  </header>
</template>

<script>
import { mapState } from 'vuex'
import { util } from '@/utils/util'
import { clearSysSession } from '@/api/auth'
import { removeLogin } from '@/utils/login'
export default {
  name: 'CHeader',
  data() {
    return {
      date: ''
    }
  },
  computed: {
    ...mapState({
      userInfo: state => state.user.userInfo
    })
  },
  created() {
    this.date = util.getCurDateTimeWeek()
    setInterval(() => {
      this.date = util.getCurDateTimeWeek()
    }, 1000)
  },
  methods: {
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
.c-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  .c-header__line {
    width: 1px;
    height: 16px;
  }
  .c-header__content {
    padding-left: 20px;
  }
  .c-header__logout {
    cursor: pointer;
    padding-right: 20px;
  }
}
</style>
