// 类型常量
const SIZE_VALUE = ['large', 'small', 'mini', 'medium']
const TYPE_VALUE = [
  'input',
  'number',
  'radio',
  'checkbox',
  'select',
  'slider',
  'date',
  'remoteselect',
  'time',
  'elautocomplete'
]
const INPUT_VALUE = ['text', 'textarea', 'email', 'password']
const SLOT_TYPE_VALUE = ['prepend', 'append']
const DATE_VALUE = ['date', 'daterange', 'datetime', 'datetimerange']

/**
 * prop传值验证
 * @param value 当前值
 * @param {Array} typeArr 可能值集合
 * @returns {Boolean}
 */
function oneOf(value, typeArr) {
  const valid = typeArr.some(item => item === value)
  if (!valid) throw new Error(`params error:"${value}" must be one of [${typeArr.join(',')}] `)
  return valid
}

/**
 * form 组合props
 */
export const formGroupProps = {
  inline: Boolean,
  forms: {
    type: Array,
    required: true
  },
  size: {
    type: String,
    default: 'small',
    validator: val => {
      return oneOf(val, SIZE_VALUE)
    }
  },
  disabled: {
    type: Boolean,
    default: false
  },
  labelWidth: {
    type: String,
    default: '150px'
  },
  // 改变值 意味着表单重绘
  currentFormValue: {
    type: Object,
    default: () => {
      return {}
    }
  },
  formrefname: {
    type: String,
    default: 'baseform'
  },
  insertNum: Number
}

/**
 * form-item props
 */
export const formItemProps = {
  // 单项成行
  inline: Boolean,
  itemType: {
    type: String,
    default: 'input',
    validator: val => {
      return oneOf(val, TYPE_VALUE)
    }
  },
  label: String,
  labelWidth: String,
  prop: {
    type: [String, Array],
    required: true
  },
  // 表单项是否渲染
  visible: {
    type: Boolean,
    default: true
  },
  disabled: {
    type: Boolean,
    default: false
  },
  rules: {
    type: Array,
    default: () => {
      return []
    }
  },
  placeholder: String,
  defaultValue: {
    type: [String, Array, Number, Boolean],
    default: undefined
  },
  current: [String, Array, Object, Number],
  parent: Object,
  // 对于循环生成多表单情况，提供formrefname 作为标识
  formrefname: String,

  /**
   * 输入框私有属性
   */
  // 类型
  inputType: {
    type: String,
    default: 'text',
    validator: val => {
      return oneOf(val, INPUT_VALUE)
    }
  },
  // slot
  slots: {
    type: Array,
    validator: arr => {
      return arr.map(item => item.type).every(vv => oneOf(vv, SLOT_TYPE_VALUE))
    }
  },
  // 文本框
  rows: Number,

  /**
   * 计数器私有属性/slider
   */
  min: Number,
  max: {
    type: Number,
    default: 2147483647
  },
  step: Number,
  stepStrictly: Boolean,

  /**
   * 选项、下拉框
   */
  options: Array,
  /**
   * slider
   */
  showInput: Boolean,
  marks: {
    type: Object,
    default: () => {
      return {}
    }
  },

  /**
   * 日期
   */
  // 类型
  dateType: {
    type: String,
    default: 'date',
    validator: val => {
      return oneOf(val, DATE_VALUE)
    }
  },
  // 输入框格式
  format: String,
  valueFormat: String,
  // 绑定值格式
  startPlaceholder: String,
  endPlaceholder: String,

  /**
   * 远程下拉框私有属性
   */
  // 接口路径
  apiUrl: String,
  // 数据位置
  resultPath: {
    type: Array,
    default: () => {
      return ['result']
    }
  },
  labelkeyname: {
    type: String,
    default: 'label'
  },
  valuekeyname: {
    type: String,
    default: 'value'
  },
  disablekeyname: String,
  disableflg: [Boolean, String, Number],
  // 静态拼接数据
  staticOptions: {
    type: Array,
    default: () => {
      return []
    }
  },
  staticFilter: {
    type: Object,
    default: () => {
      return {}
    }
  },
  // 请求参数 静态
  remoteParams: {
    type: Object,
    default: () => {
      return {}
    }
  },
  // 关联项信息 1.被关联项修改值 重置关联项 2.值作为请求的参数
  relativeProp: {
    type: Array,
    default: () => {
      return []
    }
  },
  // 载入即触发请求，并设置默认值
  autoget: {
    type: Boolean,
    default: false
  },
  // 选中回调
  change: Function,
  // 选中回调，返回选中项的所有信息（remoteselect）
  selectInfo: Function,
  pagination: {
    type: Boolean,
    default: false
  },
  // pageNum 参数名
  pageNumKey: {
    type: String,
    default: 'pageNum'
  },
  // 总页数位置
  pageCountPath: {
    type: Array,
    default: () => {
      return ['result', 'pages']
    }
  },
  // 唯一性验证
  checkApi: Object,
  itemCur: [String, Number, Boolean, Array]
}
