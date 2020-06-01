<template>
  <run-state ref="runState" type="stop" loading-text="正在停止"></run-state>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import { stopServiceInstance } from '@/api/service'
import runState from './run-state'

export default {
  name: 'instanceStop',
  mixins: [common, baseStructure],
  components: {
    runState
  },
  props: {
    instanceIds: Array
  },
  created() {
    this.stopInstance()
  },
  methods: {
    async stopInstance() {
      const res = await stopServiceInstance(this.instanceIds)
      this.$refs.runState.checkProcessState(res.result)
    }
  }
}
</script>
