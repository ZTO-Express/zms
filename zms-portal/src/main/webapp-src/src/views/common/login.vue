<template>
  <div class="login">
    <div class="login__bg" :style="{ backgroundImage: 'url(' + img.bg + ')' }"></div>
    <div class="login__container">
      <div class="login__wrap">
        <div class="login__header">
          <img :src="img.welcomeLogin" />
        </div>
        <el-form ref="loginForm" class="login__form" :model="loginForm" :rules="loginRules">
          <el-form-item prop="username">
            <label class="form__label">
              <img :src="img.user" />
            </label>
            <el-input
              ref="username"
              v-model="loginForm.username"
              placeholder="请输入账号"
              name="username"
              type="text"
              class="form__input"
            />
          </el-form-item>
          <el-form-item prop="password">
            <label class="form__label">
              <img :src="img.locked" />
            </label>
            <el-input
              ref="password"
              v-model="loginForm.password"
              placeholder="请输入密码"
              name="password"
              type="password"
              class="form__input"
              @keyup.enter.native="preLogin"
            />
          </el-form-item>
          <div class="form__item" @click="preLogin()">
            <el-button class="form__button">登录</el-button>
          </div>
        </el-form>
        <div class="login__footer" v-if="hasError">
          <img class="login_warning" :src="img.warning" />
          <span>账号或密码有误</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import bg from '@/assets/login/bg.png'
import welcomeLogin from '@/assets/login/welcome-login.png'
import locked from '@/assets/login/locked.png'
import user from '@/assets/login/user.png'
import warning from '@/assets/login/warning.png'
import { login } from '@/api/auth'
import { setLogin } from '@/utils/login'
export default {
  name: 'Login',
  data() {
    return {
      img: {
        bg: bg,
        welcomeLogin: welcomeLogin,
        locked: locked,
        user: user,
        warning: warning
      },
      loginRules: {
        username: [{ required: true, trigger: 'blur', message: '请输入账号' }],
        password: [{ required: true, trigger: 'blur', message: '请输入密码' }]
      },
      loginForm: {
        username: '',
        password: ''
      },
      hasError: false, // 错误提示
      reSubmit: false, // 重复提交
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  methods: {
    preLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) this.handleLogin()
      })
    },
    handleLogin() {
      if (this.reSubmit) return
      this.reSubmit = true
      login({
        userName: this.loginForm.username.trim(),
        password: this.loginForm.password.trim()
      })
        .then(
          res => {
            setLogin(res.status || true)
            this.$router.push({
              path: this.redirect || '/'
            })
          },
          () => {
            this.hasError = true
          }
        )
        .finally(() => {
          this.reSubmit = false
        })
    }
  }
}
</script>

<style lang="scss" scoped>
@media (min-height: 620px) {
  .login .login__container {
    top: 25% !important;
  }
}
.login {
  position: absolute;
  height: 100%;
  width: 100%;
  .login__bg {
    position: fixed;
    z-index: -6;
    bottom: 0;
    overflow: auto;
    width: 100%;
    height: 100%;
    background-size: cover;
    background-repeat: no-repeat;
  }
  .login__container {
    width: 485px;
    height: 385px;
    margin: 0 auto;
    position: relative;
    top: 15%;
    box-shadow: 0 0 10px rgba(171, 0, 0, 0.2);
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 10px;
    padding: 10px;
  }
  .login__wrap {
    background-color: #ffffff;
    border-radius: 10px;
    height: 385px;
  }

  .login__header {
    padding: 18px 30px 10px;
  }

  .login__form {
    box-sizing: border-box;
    padding: 0 53px;
    margin-top: 20px;
    .form__item {
      position: relative;
      margin-top: 20px;
    }
    .form__label {
      left: 0;
      top: 1px;
      position: absolute;
      width: 58px;
      height: 52px;
      line-height: 52px;
      text-align: center;
      z-index: 1;
    }

    .form__input {
      /deep/ .el-input__inner {
        height: 52px;
        -webkit-box-sizing: border-box;
        box-sizing: border-box;
        width: 100%;
        font-size: 15px;
        padding: 15px 15px 15px 58px;
        border: 1px solid #ddd;
        border-radius: 0;
        &:focus {
          border-color: rgb(104, 182, 255);
          border-style: solid;
          background-color: rgb(255, 255, 255);
          box-shadow: 0px 0px 4px 0px rgba(1, 126, 244, 0.45);
        }
      }
    }
    .form__button {
      cursor: pointer;
      width: 100%;
      height: 52px;
      background-color: #0575ff;
      color: #fff;
      font-size: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  .login__footer {
    padding: 0 53px;
    font-size: 16px;
    color: #ff4242;
    display: flex;
    align-items: center;
    margin-top: 20px;
    .login_warning {
      margin-right: 10px;
    }
  }
}
</style>
