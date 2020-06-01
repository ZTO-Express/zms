<template>
  <run-state ref="runState" type="start" loading-text="正在启动"></run-state>
</template>
<script>
import { common, baseStructure } from '@/mixins/index'
import { startServiceInstance } from '@/api/service'
import runState from './run-state'

export default {
  name: 'instanceStart',
  mixins: [common, baseStructure],
  components: {
    runState
  },
  props: {
    instanceIds: Array
  },
  created() {
    this.startInstance()
  },
  methods: {
    async startInstance() {
      const res = await startServiceInstance(this.instanceIds)
      this.$refs.runState.checkProcessState(res.result)
    }
  }
}
</script>
