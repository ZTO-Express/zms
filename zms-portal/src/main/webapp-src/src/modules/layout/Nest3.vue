<template>
  <transition name="fade" mode="out-in">
    <keep-alive :include="cachedViews">
      <router-view></router-view>
    </keep-alive>
  </transition>
</template>

<script>
export default {
  name: 'Nest3',
  computed: {
    cachedViews() {
      return this.$store.getters.getCacheViews
    }
  }
}
</script>
