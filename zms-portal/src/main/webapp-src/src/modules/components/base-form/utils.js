export const util = {
  deepCopy(obj, cache = []) {
    function find(list, f) {
      return list.filter(f)[0]
    }
    // just return if obj is immutable value
    if (obj === null || typeof obj !== 'object' || obj instanceof RegExp) {
      return obj
    }

    // if obj is hit, it is in circular structure
    const hit = find(cache, c => c.original === obj)
    if (hit) {
      return hit.copy
    }

    const copy = Array.isArray(obj) ? [] : {}
    // put the copy into cache at first
    // because we want to refer it in recursive deepCopy
    cache.push({
      original: obj,
      copy
    })

    Object.keys(obj).forEach(key => {
      copy[key] = this.deepCopy(obj[key], cache)
    })

    return copy
  },
  /**
   * 格式化option item 数据
   * @param {*} item 项数据
   * @param {String} labelkeyname label值的索引
   * @param {String} valuekeyname value值的索引
   * @returns {Object} fmt后的option 数据
   */
  fmtOptionData(item, labelkeyname, valuekeyname) {
    return typeof item === 'object'
      ? {
          label: item[labelkeyname],
          value: item[valuekeyname],
          ...item
        }
      : {
          label: item,
          value: item
        }
  },
  /**
   * 筛选并格式化options数据
   * @param {Array} options 选项原始数据
   * @param {String} labelkeyname label值的索引
   * @param {*} valuekeyname value值的索引
   * @param {*} filterVals 筛选参数
   */
  filterOptions(options, labelkeyname, valuekeyname, filterVals) {
    const filterValsKeys = Object.keys(filterVals)
    // filterVals中存在undefined 说明有必填项无实际值，直接返回为空
    if (filterValsKeys.length && filterValsKeys.some(key => filterVals[key] === undefined)) return []
    // 此时 filterVals 可能包含了必填项，存在有效值的一般关联项
    const res = []
    options.forEach(item => {
      // item不为null &&
      // 不存在过滤条件 || 满足filterVals所有条件
      if (item !== null && (!filterValsKeys.length || filterValsKeys.every(key => item[key] === filterVals[key]))) {
        // format
        res.push(this.fmtOptionData(item, labelkeyname, valuekeyname))
      }
    })
    return res
  },
  /**
   * 参数生成（筛选参数、请求参数）
   * @param {Array} props 需要解析的关联参数信息
   * @param {Object} params 静态参数
   * @param {Object} parent 表单数据
   * @param {String} keyname 'filterkey/paramkey' 标识
   * @returns {Object} 处理后的参数
   */
  fmtParams({ props, params, parent, keyname }) {
    const _params = { ...params }
    props.length &&
      props.forEach(item => {
        const { prop, require } = item
        // params 键值
        const key = item[keyname]
        // 存入一般关联的有效值 && 强关联（key值必有，无效值为undefined）
        if ((parent[prop] !== undefined && parent[prop] !== '') || require) {
          // 必填项为 ‘’ 设置只为undefined
          parent[prop] === '' ? (_params[key] = undefined) : (_params[key] = parent[prop])
        }
      })
    return _params
  },
  // 对比两个对象的值是否完全相等 返回值 true/false
  isObjectValueEqual(a, b) {
    const aType = typeof a
    const bType = typeof b
    if (aType !== bType) return false
    if (aType === 'object') {
      // 取对象a和b的属性名
      const aProps = Object.keys(a)
      const bProps = Object.keys(b)
      // 判断属性名的length是否一致
      if (aProps.length !== bProps.length) return false
      // 判断属性名是否都能找到
      if (aProps.some(key => bProps.indexOf(key) === -1)) return false
      // 判断属性值
      return aProps.every(key => this.isObjectValueEqual(a[key], b[key]))
    } else {
      return a === b
    }
  },
  // 路径生成
  parsePath(data, path = [], defaultVal = undefined) {
    if (!path.length) return defaultVal
    let res = data
    try {
      path.forEach(key => {
        res = res[key]
      })
    } catch (e) {
      // 没返回则返回默认值
      res = defaultVal
    }
    return res
  }
}
