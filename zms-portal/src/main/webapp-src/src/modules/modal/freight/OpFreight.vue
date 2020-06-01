<template>
  <div class="c-dialog">
    <el-dialog
      v-drag-dialog
      :title="modalTitle"
      :visible.sync="dialog"
      :before-close="closeHandle"
      :width="width || '580px'"
    >
      <div v-loading="loading">
        <slot></slot>
        <div slot="footer" class="c-dialog__footer">
          <slot name="button"></slot>
          <el-button v-if="!noCancle" size="mini" @click="closeHandle">取消</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  props: ['dialog', 'modalTitle', 'callback', 'refname', 'loading', 'width', 'noCancle'],
  methods: {
    // 关闭
    closeHandle() {
      this.$emit('dealDialog')
    }
  }
}
</script>
