<template>
  <div class="c-error">
    <div class="c-error__bg"><img :src="bgPic" alt="错误背景图" /></div>
    <div class="c-error__body">
      <img :src="errorPic" alt="错误图" />
    </div>
    <div class="c-error__footer">
      <h3 class="c-error__tip">{{ errorMsg }}</h3>
      <router-link :to="link">
        <el-button type="primary">{{ linkMsg }}</el-button>
      </router-link>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    bgPic: {
      type: String,
      default: ''
    },
    errorPic: {
      type: String,
      default: ''
    },
    errorMsg: {
      type: String,
      default: ''
    },
    link: {
      type: String,
      default: '/'
    },
    linkMsg: {
      type: String,
      default: ''
    }
  }
}
</script>
