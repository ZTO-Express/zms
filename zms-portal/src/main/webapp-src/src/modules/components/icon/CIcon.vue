<template>
  <i :class="curClass" v-if="isWhite"></i>
  <svg-icon :icon-class="iconClass" :class-name="className" v-else-if="iconClass"></svg-icon>
</template>

<script>
export default {
  name: 'CIcon',
  props: {
    iconClass: {
      type: String,
      default: ''
    },
    className: {
      type: String
    }
  },
  data() {
    return {
      whiteList: ['fa', 'el'] //白名单内的走i标签
    }
  },
  computed: {
    curClass() {
      return `${this.iconClass} ${this.className}`
    },
    isWhite() {
      let _this = this
      return (
        this.whiteList.find(item => {
          return _this.iconClass.startsWith(item)
        }) || ''
      )
    }
  }
}
</script>
