<template>
  <div :class="autoheight ? 'autoheight tablewrap' : 'tablewrap'">
    <!-- <div class="table-header" v-if="menurow">
      <div class="table-header-padding clearboth">
        <slot />
      </div>
    </div> -->
    <el-table
      v-loading.lock="loading"
      ref="table"
      :data="tableData"
      :border="border"
      :size="size"
      :stripe="stripe"
      :height="height"
      :max-height="maxHeight"
      :fit="fit"
      :show-header="showHeader"
      :highlight-current-row="highlightCurrentRow"
      :current-row-key="currentRowKey"
      :row-class-name="rowClassName"
      :row-style="rowStyle"
      :row-key="rowKey"
      :empty-text="emptyText"
      :default-expand-all="defaultExpandAll"
      :expand-row-keys="expandRowKeys"
      :default-sort="defaultSort"
      :tooltip-effect="tooltipEffect"
      :show-summary="showSummary"
      :sum-text="sumText"
      :summary-method="summaryMethod"
      :style="tableStyle"
      @select="(selection, row) => emitEventHandler('select', selection, row)"
      @select-all="(selection, ref) => emitEventHandler('select-all', selection, ref)"
      @selection-change="selectionChange"
      @cell-mouse-enter="(row, column, cell, event) => emitEventHandler('cell-mouse-enter', row, column, cell, event)"
      @cell-mouse-leave="(row, column, cell, event) => emitEventHandler('cell-mouse-leave', row, column, cell, event)"
      @cell-click="(row, column, cell, event) => emitEventHandler('cell-click', row, column, cell, event)"
      @cell-dblclick="(row, column, cell, event) => emitEventHandler('cell-dblclick', row, column, cell, event)"
      @row-click="(row, event, column) => emitEventHandler('row-click', row, event, column)"
      @row-dblclick="(row, event) => emitEventHandler('row-dblclick', row, event)"
      @row-contextmenu="(row, event) => emitEventHandler('row-contextmenu', row, event)"
      @header-click="(column, event) => emitEventHandler('header-click', column, event)"
      @sort-change="args => emitEventHandler('sort-change', args)"
      @filter-change="filters => emitEventHandler('filter-change', filters)"
      @current-change="(currentRow, oldCurrentRow) => emitEventHandler('current-change', currentRow, oldCurrentRow)"
      @header-dragend="
        (newWidth, oldWidth, column, event) => emitEventHandler('header-dragend', newWidth, oldWidth, column, event)
      "
      @expand-change="(row, expanded) => emitEventHandler('expand-change', row, expanded)"
    >
      <slot name="prepend" />
      <template v-for="(column, columnIndex) in columns">
        <slot name="insert" v-if="columnIndex === insertNum" />
        <slot name="insert2" v-if="columnIndex === insertNum2" />
        <slot name="insert3" v-if="columnIndex === insertNum3" />
        <slot name="insert4" v-if="columnIndex === insertNum4" />
        <el-table-column v-bind="column" :key="columnIndex" align="center" />
      </template>
      <slot name="append" />
    </el-table>
    <div class="table-footer">
      <div class="table-footer-padding clearboth">
        <div v-if="isPagination">
          <el-pagination
            :current-page="this.pagePagination ? currentPage : pagination.pageIndex"
            background
            small
            :page-sizes="pageSizes"
            :page-size="pagination.pageSize"
            :layout="paginationLayout"
            :total="this.pagePagination ? total : total2"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
    <slot name="bottom" />
  </div>
</template>
<script>
import Vue from 'vue'
import props from './props'

export default {
  name: 'TablePagination',
  props,
  data() {
    const _this = this
    return {
      Vue,
      pagination: {
        pageIndex: 1,
        pageSize: (() => {
          const { pageSizes } = _this
          return pageSizes.length > 0 ? pageSizes[0] : 25
        })()
      },
      total2: 0,
      tableData: [],
      cacheLocalData: [],
      selections: []
    }
  },
  watch: {
    data: function(value) {
      this.loadLocalData(value)
    }
  },
  mounted() {
    this.$refs['table'].$on('expand', (row, expanded) => this.emitEventHandler('expand', row, expanded))
    const { data } = this
    this.loadLocalData(data)
  },
  methods: {
    // 页数据改变
    handleSizeChange(size) {
      this.pagination.pageSize = size
      if (this.pagePagination) {
        if (size * (this.currentPage - 1) < this.total) {
          this.$emit('changePage', { currentPage: this.currentPage, pageSize: this.pagination.pageSize })
        }
      } else {
        this.pagination.pageSize = size
        this.dataFilterHandler()
      }
    },
    // 改变当前页面
    handleCurrentChange(pageIndex) {
      if (this.pagePagination) {
        this.$emit('changePage', { currentPage: pageIndex, pageSize: this.pagination.pageSize })
      } else {
        this.pagination.pageIndex = pageIndex
        this.dataFilterHandler()
      }
    },
    dataFilterHandler() {
      const { cacheLocalData } = this
      this.total2 = cacheLocalData.length
      this.tableData = this.dataFilter(cacheLocalData)
      this.toggleSelection()
    },
    dataFilter(data) {
      const {
        isPagination,
        pagination: { pageIndex, pageSize }
      } = this
      if (!isPagination) return data
      return data.filter((v, i) => {
        return i >= (pageIndex - 1) * pageSize && i < pageIndex * pageSize
      })
    },
    dataFilterHandler2() {
      const { cacheLocalData } = this
      this.tableData = cacheLocalData
      this.toggleSelection()
    },
    emitEventHandler(event) {
      this.$emit(event, ...Array.from(arguments).slice(1))
    },
    loadLocalData(data) {
      if (!data) {
        throw new Error(`you must set attribute 'data' and 'data' must be a array.`)
      }
      this.cacheLocalData = data
      if (this.dataHandler && data) {
        this.cacheLocalData.forEach(this.dataHandler)
      }
      if (this.pagePagination) {
        this.dataFilterHandler2()
      } else {
        this.handleCurrentChange(1)
      }
    },
    toggleSelection() {
      const { multipleSelection, selections, clearSelections } = this
      if (!multipleSelection) return
      this.$nextTick(() => {
        if (selections.length) {
          selections.forEach(row => {
            this.$refs.table.toggleRowSelection(row, true)
          })
        } else {
          clearSelections()
        }
      })
    },
    // 多选回调 返回已选中的数组
    selectionChange(rows) {
      this.selections = rows
    },
    // 钩子函数 返回当前表格多选选中
    getTableSelections() {
      return this.selections
    },
    // 钩子函数 清空选中
    clearSelections() {
      this.$refs.table.clearSelection()
    },
    // 重置selections
    resetSelections() {
      this.selections = []
    }
  }
}
</script>
<style rel="stylesheet/scss" lang="scss">
.tablewrap {
  height: 100%;
}
.autoheight {
  height: auto;
  .el-table__body-wrapper {
    height: auto !important;
  }
  .app-table-space-padding {
    overflow: auto !important;
  }
}
.table-footer {
  .el-pagination {
    padding: 5px 0;
    text-align: right;
  }
  .el-pagination__sizes {
    margin: 0;
  }
  .el-input--mini .el-input__inner {
    height: 22px;
  }
}
</style>
