/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Created by Administrator on 2017/9/21 0021.
 * 封装常用工具函数,包含对原生基本类型的扩展
 */
const weeks = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
export const util = {
  getCurDate() {
    var date = new Date()
    var year = date.getFullYear()
    var month = date.getMonth() + 1
    var day = date.getDate()
    month = month.toString().length > 1 ? month : '0' + month
    day = day.toString().length > 1 ? day : '0' + day
    return { year, month, day }
  },
  getCurTime() {
    var date = new Date()

    var hours = date.getHours()
    var minutes = date.getMinutes()
    var seconds = date.getSeconds()

    hours = hours.toString().length > 1 ? hours : '0' + hours
    minutes = minutes.toString().length > 1 ? minutes : '0' + minutes
    seconds = seconds.toString().length > 1 ? seconds : '0' + seconds

    return { hours, minutes, seconds }
  },
  getWeek() {
    var date = new Date()
    var week = weeks[date.getDay()]
    return { week }
  },
  getCurDateTimeWeek() {
    var date = this.getCurDate()
    var time = this.getCurTime()
    var week = this.getWeek()

    return (
      date.year +
      '-' +
      date.month +
      '-' +
      date.day +
      ' ' +
      time.hours +
      ':' +
      time.minutes +
      ':' +
      time.seconds +
      ' ' +
      week.week
    )
  },
  dateToStr(now, flag) {
    if (!now) return ''
    if (typeof now === 'string') return now
    var year = now.getFullYear()
    var month = now.getMonth() + 1
    var date = now.getDate()
    var hour = now.getHours()
    var min = now.getMinutes()
    var sec = now.getSeconds()
    if (month < 10) {
      month = '0' + month
    }
    if (date < 10) {
      date = '0' + date
    }
    if (hour < 10) {
      hour = '0' + hour
    }
    if (min < 10) {
      min = '0' + min
    }
    if (sec < 10) {
      sec = '0' + sec
    }
    if (flag === 1) {
      return year + '-' + month + '-' + date
    } else if (flag === 2) {
      return hour + ':' + min
    } else if (flag === 3) {
      return year + '-' + month + '-' + date + ' ' + hour + ':' + min
    }
  },
  timeToStr(now, flag) {
    if (!now) return ''
    now = new Date(now)
    var year = now.getFullYear()
    var month = now.getMonth() + 1
    var date = now.getDate()
    var hour = now.getHours()
    var min = now.getMinutes()
    var sec = now.getSeconds()
    if (month < 10) {
      month = '0' + month
    }
    if (date < 10) {
      date = '0' + date
    }
    if (hour < 10) {
      hour = '0' + hour
    }
    if (min < 10) {
      min = '0' + min
    }
    if (sec < 10) {
      sec = '0' + sec
    }
    if (flag === 1) {
      return year + '-' + month + '-' + date
    } else if (flag === 2) {
      return hour + ':' + min + ':' + sec
    } else if (flag === 3) {
      return year + '-' + month + '-' + date + ' ' + hour + ':' + min + ':' + sec
    } else if (flag === 4) {
      return month + '-' + date + ' ' + hour + ':' + min + ':' + sec
    } else if (flag === 5) {
      return month + '-' + date
    }
  },
  getCurDateTime() {
    var now = new Date()
    var month = now.getMonth() + 1
    var date = now.getDate()
    var hour = now.getHours()
    var min = now.getMinutes()
    var sec = now.getSeconds()
    var time = '下午'
    if (month < 10) {
      month = '0' + month
    }
    if (date < 10) {
      date = '0' + date
    }
    if (hour < 12) {
      time = '上午'
    }
    if (hour < 10) {
      hour = '0' + hour
    }
    if (min < 10) {
      min = '0' + min
    }
    if (sec < 10) {
      sec = '0' + sec
    }
    return month + '月' + date + ' ' + hour + ':' + min + ':' + sec + ' ' + time
  },
  extend() {
    var options
    var name
    var src
    var copy
    var copyIsArray
    var clone
    var target = arguments[0] || {}
    var i = 1
    var length = arguments.length
    var deep = false

    // Handle a deep copy situation
    if (typeof target === 'boolean') {
      deep = target

      // Skip the boolean and the target
      target = arguments[i] || {}
      i++
    }

    // Handle case when target is a string or something (possible in deep copy)
    if (typeof target !== 'object' && typeof target !== 'function') {
      target = {}
    }

    // Extend jQuery itself if only one argument is passed
    if (i === length) {
      target = this
      i--
    }

    for (; i < length; i++) {
      // Only deal with non-null/undefined values
      if ((options = arguments[i]) != null) {
        // Extend the base object
        for (name in options) {
          src = target[name]
          copy = options[name]

          // Prevent never-ending loop
          if (target === copy) {
            continue
          }

          // Recurse if we're merging plain objects or arrays
          if (deep && copy && (copy.constructor === Object || (copyIsArray = Array.isArray(copy)))) {
            if (copyIsArray) {
              copyIsArray = false
              clone = src && Array.isArray(src) ? src : []
            } else {
              clone = src && src.constructor === Object ? src : {}
            }

            // Never move original objects, clone them
            target[name] = this.extend(deep, clone, copy)

            // Don't bring in undefined values
          } else if (copy !== undefined) {
            target[name] = copy
          }
        }
      }
    }
    // Return the modified object
    return target
  },
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
  isObject: input => typeof input === 'object' && !(input instanceof Array),
  isEmptyObject(e) {
    let t
    for (t in e) {
      return !1
    }
    return !0
  },
  param2Obj(url) {
    const search = url.split('?')[1]
    if (!search) {
      return {}
    }
    return JSON.parse(
      '{"' +
        decodeURIComponent(search)
          .replace(/"/g, '\\"')
          .replace(/&/g, '","')
          .replace(/=/g, '":"')
          .replace(/\+/g, ' ') +
        '"}'
    )
  },
  getLoginPath() {
    let flg = true
    const { hash, search } = window.location
    const params = flg && (hash.slice('2') || search)
    let path = '/login'
    params && (path += '?redirect=' + params)
    return path
  },
  getTimeStamp(str, accuracy, type) {
    const units = {
      ms: 1,
      s: 1000,
      min: 1000 * 60,
      h: 1000 * 60 * 60,
      d: 1000 * 60 * 60 * 24,
      mon: 1000 * 60 * 60 * 24 * 30
    }
    const operation = ['-', '+']
    // 00:00:00
    let time = new Date(new Date().toLocaleDateString()).getTime()
    if (type === 'end') {
      time += 24 * 60 * 60 * 1000 - 1
    }
    if (str.length) {
      const unit = str.slice(-1)
      if (Object.keys(units).indexOf(unit) === -1 || Object.keys(units).indexOf(accuracy) === -1) {
        throw new Error(`时间单位类型为['ms', 's', 'mim', 'h','d','mon']`)
      }
      const sign = str[0]
      if (operation.indexOf(sign) === -1) {
        throw new Error(`支持运算符类型为['-', '+']`)
      }

      const num = parseInt(str.slice(1, -1))
      if (!isNaN(num)) {
        if (sign === '-') {
          time -= num * units[unit]
        } else {
          time += num * units[unit]
        }
      }
    }
    return parseInt(time / units[accuracy])
  },
  // 数据依据time排序
  dataSort(data) {
    return data.sort((a, b) => a.timestamp - b.timestamp)
  },
  //把时间日期转成时间戳
  timestamp(time) {
    return new Date(time).getTime()
  }
}
