<template>
  <!-- <span :class="disabled?`disabled z-operate-btn ${type}`:`z-operate-btn ${type}`" @click="clickHandler"> -->
  <span :class="[computedClass, type]" @click="clickHandler">
    <slot></slot>
  </span>
</template>

<script>
/**
 * 类型常量
 */
const TYPE_VALUE = ['danger', 'primary', 'warning', 'success', 'disabled', 'gray', 'normal', 'link']
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
  name: 'colorWord',
  props: {
    type: {
      type: String,
      default: 'primary',
      validator: typeValidator
    },
    disabled: {
      type: Boolean,
      default: false
    },
    cursor: {
      type: Boolean,
      default: true
    }
  },
  computed: {
    computedClass() {
      const { cursor, disabled } = this
      return {
        colorWord: true,
        cursor: cursor,
        disabled: disabled
      }
    }
  },
  methods: {
    clickHandler() {
      !this.disabled && this.$emit('click')
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss">
.cursor {
  cursor: pointer;
}
.cursor.disabled {
  cursor: not-allowed;
}
.colorWord {
  margin: 0 3px;
  padding: 0 2px;
  &.primary {
    color: #55a4e3;
  }
  &.success {
    color: #52c41a;
  }
  &.warning {
    color: #d0a23d;
  }
  &.danger {
    color: #f5222d;
  }
  &.gray {
    color: rgba(0, 0, 0, 0.35);
  }
  &.normal {
    color: #bd80cc;
  }
  &.link {
    color: #55a4e3;
    text-decoration: underline;
  }
  &.disabled {
    color: rgba(0, 0, 0, 0.45);
  }
}
</style>
