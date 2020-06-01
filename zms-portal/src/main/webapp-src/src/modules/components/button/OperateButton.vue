<template>
  <button
    class="c-operate-button"
    @click="handleClick"
    :disabled="disabled || loading"
    type="button"
    :class="[
      type ? 'c-operate-button--' + type : '',
      {
        'is-disabled': disabled,
        'is-loading': loading
      }
    ]"
  >
    <i class="el-icon-loading" v-if="loading"></i>
    <slot></slot>
  </button>
</template>
<script>
// 按钮可选类型
const TYPE_VALUE = ['default', 'primary', 'success', 'warning', 'danger']
/**
 * 传值验证
 * @param value 当前值
 * @param typeArr  可能值集合
 */
function oneOf(value, typeArr) {
  const valid = typeArr.some(item => item === value)
  if (!valid) throw new Error(`params error:"${value}" must be one of [${typeArr.join(',')}] `)
  return valid
}
function typeValidator(val) {
  return oneOf(val, TYPE_VALUE)
}
export default {
  name: 'OperateButton',
  props: {
    type: {
      type: String,
      default: 'default',
      validator: typeValidator
    },
    loading: Boolean,
    disabled: Boolean
  },
  methods: {
    handleClick(evt) {
      if (this.disabled === true || this.loading) return
      this.$emit('click', evt)
    }
  }
}
</script>
