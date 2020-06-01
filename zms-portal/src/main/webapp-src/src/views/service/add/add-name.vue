<template>
  <div class="service-add-name">
    <el-form size="mini" :model="serviceAddNameForm" ref="serviceAddNameForm" label-width="50px">
      <el-form-item
        label="名称"
        prop="serverName"
        :rules="[
          {
            required: true,
            message: '请输入服务名称',
            trigger: 'blur'
          }
        ]"
      >
        <el-input v-model="serviceAddNameForm.serverName"></el-input>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
import { validServerName } from '@/api/service'
import { common, baseStructure } from '@/mixins/index'
export default {
  name: 'serviceAddName',
  mixins: [common, baseStructure],
  props: {
    envId: [Number, String]
  },
  data() {
    return {
      serviceAddNameForm: {
        serverName: ''
      }
    }
  },
  methods: {
    async checkServerName() {
      let validateFlg = true
      const params = {
        envId: this.envId,
        serverName: this.serviceAddNameForm.serverName
      }
      const res = await validServerName(params)
      if (!res.result) {
        this.message('error', '服务名称已存在')
        validateFlg = false
      }
      return validateFlg
    },
    validateServerName() {
      let validateFlg = false
      this.$refs['serviceAddNameForm'].validate(valid => {
        if (valid) {
          validateFlg = true
        } else {
          validateFlg = false
        }
      })
      return validateFlg
    }
  }
}
</script>
<style lang="scss">
.service-add-name {
  margin: 50px auto;
  width: 500px;
}
</style>
