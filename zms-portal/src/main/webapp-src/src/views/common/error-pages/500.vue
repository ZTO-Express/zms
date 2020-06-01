<template>
  <error-page
    :bg-pic="bgPic"
    :error-pic="errorPic"
    error-msg="服务器异常，请联系管理员！"
    link-msg="返回首页"
  ></error-page>
</template>
<script>
import bgPic from '@/assets/error/bg-500.png'
import errorPic from '@/assets/error/pic-500.png'
import ErrorPage from '@/modules/components/error/ErrorPage.vue'
export default {
  name: 'ErrorPage500',
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
