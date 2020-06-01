<template>
  <div class="l-time-range">
    <div class="time-range">
      <div class="time-range--quick">
        <ul class="list">
          <li
            v-for="item in quickEntry"
            :key="item.value"
            :class="item.value === quick ? 'active' : ''"
            @click="quickHandle(item.value)"
          >
            {{ item.label }}
          </li>
        </ul>
      </div>
      <div class="time-range-exact">
        <el-date-picker size="mini" :clearable="false" v-model="date" type="datetimerange" @change="exactChange" />
      </div>
    </div>
  </div>
</template>

<script>
function oneOf(value, typeArr) {
  const valid = typeArr.some(item => item === value)
  if (!valid) throw new Error(`params error:"${value}" must be one of [${typeArr.join(',')}] `)
  return valid
}
export default {
  name: 'timeRange',
  props: {
    defaultQuick: {
      type: String,
      default: '30分钟',
      validator: val => {
        return oneOf(val, ['30分钟', '1小时', '2小时', '6小时', '12小时', '1天', '7天', '30天'])
      }
    },
    timeStr: String,
    quickNum: Number
  },
  data() {
    return {
      quickEntry: [
        { label: '30分钟', value: 30 },
        { label: '1小时', value: 60 },
        { label: '2小时', value: 2 * 60 },
        { label: '6小时', value: 6 * 60 },
        { label: '12小时', value: 12 * 60 },
        { label: '1天', value: 24 * 60 },
        { label: '7天', value: 7 * 24 * 60 },
        { label: '30天', value: 30 * 24 * 60 }
      ],
      date: [],
      quick: 0
    }
  },
  watch: {
    timeStr() {
      if (!this.timeStr) {
        return
      }
      const times = this.timeStr.split(',')
      this.date = [new Date(Number(times[0])), new Date(Number(times[1]))]
      this.changeDate()
    },
    quickNum() {
      if (this.quickNum > -1) {
        this.quick = this.quickNum
      }
    }
  },
  created() {
    this.quick = this.getQuickValue(this.$props.defaultQuick)
    this.transQuickToExact(this.quick)
  },
  methods: {
    // 根据prop 返回默认时间值
    getQuickValue(label) {
      return this.quickEntry.filter(item => item.label === label)[0].value
    },
    // 将快捷时间转换为日期框中的精确时间
    transQuickToExact(value) {
      const timestampRange = value * 60 * 1000
      const now = new Date().getTime()
      this.date = [now - timestampRange, now]
      this.changeDate()
    },
    changeDate() {
      const time1 = new Date(this.date[0]).getTime()
      const time2 = new Date(this.date[1]).getTime()
      this.date = [time1, time2]
      this.$emit('change', this.date)
      this.$emit('changeQuick', this.quick)
    },
    // 快捷点击
    quickHandle(value) {
      this.quick = value
      this.transQuickToExact(value)
    },
    // 精确时间选择
    exactChange() {
      this.quick = 0
      this.changeDate()
    }
  }
}
</script>
<style lang="scss">
.l-time-range {
  overflow: hidden;
  margin-top: 20px;
  .time-range {
    float: right;
    > div {
      display: inline-block;
    }
    .time-range--quick {
      .list {
        li {
          display: inline-block;
          margin: 0 7px;
          cursor: pointer;
          transition: all 0.3s ease;
          &.active,
          &:hover {
            color: #409eff;
          }
          &.active {
            text-decoration: underline;
            font-weight: bold;
          }
        }
      }
    }
    .time-range-exact {
      .el-date-editor {
        width: 350px;
        margin-left: 10px;
      }
    }
  }
}
</style>
