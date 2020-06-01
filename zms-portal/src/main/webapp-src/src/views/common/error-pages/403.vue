<template>
  <error-page
    :bg-pic="bgPic"
    :error-pic="errorPic"
    error-msg="权限不足，请联系管理员开通！"
    link-msg="返回首页"
  ></error-page>
</template>
<script>
import bgPic from '@/assets/error/bg-403.png'
import errorPic from '@/assets/error/pic-403.png'
import ErrorPage from '@/modules/components/error/ErrorPage.vue'
export default {
  name: 'ErrorPage403',
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
