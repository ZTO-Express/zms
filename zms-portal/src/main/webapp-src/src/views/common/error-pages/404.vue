<template>
  <error-page :bg-pic="bgPic" :error-pic="errorPic" error-msg="页面走丢了！" link-msg="返回首页"></error-page>
</template>
<script>
import bgPic from '@/assets/error/bg-404.png'
import errorPic from '@/assets/error/pic-404.png'
import ErrorPage from '@/modules/components/error/ErrorPage.vue'
export default {
  name: 'ErrorPage404',
  components: {
    ErrorPage
  },
  data() {
    return {
      bgPic: bgPic,
      errorPic: errorPic
    }
  }
}
</script>
