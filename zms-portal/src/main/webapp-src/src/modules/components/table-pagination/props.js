const props = {
  height: {
    type: String,
    default: '100%'
  },
  maxHeight: [String, Number],
  size: {
    type: String,
    default: 'mini'
  },
  stripe: Boolean,
  border: {
    type: Boolean,
    default: true
  },
  fit: {
    type: Boolean,
    default: true
  },
  fixed: String,
  showHeader: {
    type: Boolean,
    default: true
  },
  highlightCurrentRow: Boolean,
  currentRowKey: [String, Number],
  className: String,
  rowClassName: [String, Function],
  rowStyle: [String, Function],
  rowKey: [String, Function],
  emptyText: String,
  defaultExpandAll: Boolean,
  expandRowKeys: Array,
  defaultSort: Object,
  tooltipEffect: String,
  showSummary: Boolean,
  sumText: String,
  summaryMethod: Function,
  // custom attributes
  tableStyle: {
    type: String,
    default: 'width:100%;'
  },
  url: {
    type: [String, Array]
  },
  listField: {
    type: String,
    default: 'data.list'
  },
  totalField: {
    type: String,
    default: 'data.rowCount'
  },
  data: {
    type: Array
  },
  currentPage: {
    type: Number,
    default: 1
  },
  total: {
    type: Number,
    default: 0
  },
  dataHandler: {
    type: Function
  },
  columns: {
    type: Array,
    required: true
  },
  isPagination: {
    type: Boolean,
    default: true
  },
  pagePagination: {
    type: Boolean,
    default: false
  },
  pageSizes: {
    type: Array,
    default: () => {
      return [25, 50, 100]
    }
  },
  paginationLayout: {
    type: String,
    default: 'total, prev, pager, next, jumper, sizes'
  },
  pageIndexKey: {
    type: String,
    default: 'pageNo'
  },
  pageSizeKey: {
    type: String,
    default: 'pageSize'
  },
  editableTable: {
    type: Boolean,
    default: false
  },
  editableTableInfo: Object,
  menurow: {
    type: Boolean,
    default: true
  },
  insertNum: {
    type: Number
  },
  insertNum2: {
    type: Number
  },
  insertNum3: {
    type: Number
  },
  insertNum4: {
    type: Number
  },
  loading: Boolean,
  autoheight: {
    type: Boolean,
    default: false
  },
  sortable: {
    type: Boolean,
    default: false
  },
  multipleSelection: {
    type: Boolean,
    default: false
  }
}

export default props
