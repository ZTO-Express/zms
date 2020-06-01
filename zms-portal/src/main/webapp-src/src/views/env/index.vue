<template>
  <welcome :env-detail-id="envId"></welcome>
</template>
<script>
import welcome from '../common/welcome'

export default {
  name: 'envHome',
  components: {
    welcome
  },
  data() {
    return {
      envId: undefined
    }
  },
  created() {
    this.envId = this.$route.params.id
  }
}
</script>
