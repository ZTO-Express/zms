<template>
  <div :class="chartWrapClass" :style="{ width: realwidth }" v-loading="loading">
    <div v-bind:class="chartContainerClass">
      <div class="chart" :style="{ height }"></div>
      <div class="chart-nodata" v-if="!chartData || !chartData.length">
        <p>暂无数据</p>
      </div>
    </div>
  </div>
</template>

<script>
const COLOR = ['#2b71ff', '#13CAD2', '#FFA823', '#FF939E', '#f96760', '#a0be0c', '#99d2ff', '#ba3bb9']
import echarts from 'echarts'
import { debounce } from '@/utils'
import GLOBAL from '@/config/global'

export default {
  name: 'chart',
  props: {
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: GLOBAL.chartHeight
    },
    loading: {
      type: Boolean,
      default: false
    },
    chartData: Array,
    tooltip: Object,
    border: {
      type: Boolean,
      default: false
    },
    flexColor: {
      type: Boolean,
      default: false
    },
    autoResize: {
      type: Boolean,
      default: true
    },
    initflg: {
      type: Boolean,
      default: true
    },
    axisPointerType: {
      type: String,
      default: 'cross'
    },
    underline: {
      type: Boolean,
      default: true
    },
    type: String
  },
  data() {
    const { initflg } = this
    return {
      flg: true,
      chart: null,
      realwidth: this.setGridWidth(),
      COLOR,
      initFlg: initflg
    }
  },
  computed: {
    chartContainerClass() {
      const { border } = this
      return {
        chartContainer: true,
        border
      }
    },
    chartWrapClass() {
      return {
        underline: !this.underline,
        chartwrap: true
      }
    }
  },
  watch: {
    chartData: {
      // deep: true,
      handler(newval) {
        if (newval === undefined) {
          return
        }
        this.realwidth = this.setGridWidth()
        newval.length && this.init(newval)
      }
    },
    initflg() {
      const { initFlg, chartData } = this
      if (initFlg) return
      this.initFlg = true
      this.$nextTick(() => {
        this.initChart(chartData)
        this.flg = false
      })
    }
  },
  mounted() {
    if (this.autoResize) {
      this.__resizeHandler = debounce(() => {
        if (this.chart) {
          this.chart.resize()
        }
      }, 100)
      window.addEventListener('resize', this.__resizeHandler)
    }
    if (this.chartData.length) {
      this.init(this.chartData)
    }
  },

  methods: {
    // init操作
    init(data) {
      if (this.initFlg) {
        if (this.flg) {
          // nextTick解决：同步代码修改chart宽度，dom 元素宽度还未渲染，initchart获取到的canvas宽度还是修改前的宽度
          this.$nextTick(() => {
            this.initChart(data)
            this.flg = false
          })
          return
        }
        if (this.fmtRenderFlg) {
          this.fmtRenderFlg = false
          this.setOptions(data)
        } else if (this.fmtRenderFlg === undefined) {
          this.setOptions(data)
        }
      }
    },
    // 统计图初始化
    initChart(data) {
      this.chart = echarts.init(this.$el.children[0].children[0], 'macarons')
      this.finished()
      this.setOptions(data)
    },
    // 设置grid宽度
    setGridWidth() {
      const { chartData, width } = this
      let _width = width
      if (Array.isArray(chartData) && chartData.length > 2) {
        if (chartData.length === 2) {
          _width = chartData.length * 0.5 * 100 + '%'
        } else {
          _width = chartData.length * 0.3333 * 100 + '%'
        }
      }
      return _width
    },
    // 返回series data
    filterSeries(arr, xdata) {
      // const { flexColor } = this
      return xdata.map(item => {
        const filterItem = arr.filter(v => v.x === item)[0]
        return {
          value: filterItem ? filterItem.value : '-'
        }
      })
    },
    setOptions(opts) {
      const { filterSeries, axisPointerType } = this
      const _grid = []
      const _xAxis = []
      const _yAxis = []
      const _series = []
      const _legend = []
      const _title = []
      const _legendData = []
      const _grid_item_width = parseInt(100 / opts.length)
      opts.forEach((opt, i) => {
        const {
          type,
          data,
          legend = [],
          title,
          subtext,
          gridConfig = {},
          xAxisConfig = { type: 'category' },
          yAxisConfig = {},
          seriesConfig = {},
          legendConfig = {}
        } = opt

        _grid.push({
          show: true,
          width: _grid_item_width + '%',
          left: (_grid_item_width + 2) * i + '%',
          top: '60px',
          bottom: '30px',
          containLabel: true,
          ...gridConfig
        })

        const xdata = [...new Set(data.map(item => item.x))]

        const _xAxis_item = { gridIndex: i, boundaryGap: false }
        // 自定义x参数
        Object.assign(
          _xAxis_item,
          {
            axisLine: {
              lineStyle: {
                color: '#666666'
              }
            }
          },
          xAxisConfig
        )
        xdata.length && Object.assign(_xAxis_item, { data: xdata })
        _xAxis.push(_xAxis_item)

        const _yAxis_item = { gridIndex: i }
        // 自定义y参数
        Object.assign(
          _yAxis_item,
          {
            splitLine: {
              show: false
            },
            splitArea: {
              show: true,
              areaStyle: {
                color: ['#fefefe', '#f9f9f9']
              }
            },
            axisLine: {
              lineStyle: {
                color: '#666666'
              }
            }
          },
          yAxisConfig
        )
        _yAxis.push(_yAxis_item)

        const _seriesItem = {
          xAxisIndex: i,
          yAxisIndex: i,
          type: type || 'line',
          smooth: true,
          connectNulls: true,
          ...seriesConfig
        }

        if (legend.length) {
          legend.forEach(item => {
            _legendData.push(item.name)
            _series.push({
              name: item.name,
              data: filterSeries(
                data.filter(v => v.name === item.attr),
                xdata
              ),
              ..._seriesItem
            })
          })
        } else {
          _series.push({ ..._seriesItem, data: filterSeries(data, xdata) })
        }

        _legend.push({
          data: _legendData,
          bottom: 0,
          ...legendConfig
        })
        _title.push({
          text: title,
          subtext,
          left: 0,
          top: '15px',
          textStyle: {
            fontSize: 15,
            color: '#666666'
          }
        })
      })
      this.chart.setOption(
        {
          grid: _grid,
          xAxis: _xAxis,
          yAxis: _yAxis,
          series: _series,
          legend: _legend,
          title: _title,
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              // 坐标轴指示器，坐标轴触发有效
              type: axisPointerType, // 默认为直线，可选为：'line' | 'shadow'
              axis: 'x',
              snap: true,
              shadowStyle: {
                color: 'rgba(150,150,150,0.1)'
              },
              lineStyle: {
                type: 'dashed',
                color: '#ccc'
              }
            },
            ...this.tooltip
          },
          color: COLOR
        },
        true
      )
    },
    // 渲染结束监听
    finished() {
      if (!this.type) return
      this.chart.on('finished', () => {
        // if (this.fmtRenderFlg === false) {
        this.$emit('finished', this.type)
        // }
      })
    }
  }
}
</script>
<style lang="scss">
.chartwrap {
  width: 100%;
  padding-bottom: 25px;
  margin-bottom: 30px;
  position: relative;
  &.underline {
    padding-bottom: 10px;
    margin-bottom: 0px;
    &:before {
      display: none;
    }
    .chart-nodata {
      bottom: 40px;
    }
  }
  &:before {
    content: '';
    width: 100%;
    height: 1px;
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    border-bottom: 1px dashed #ccc;
  }
  .chartContainer {
    width: 100%;
    overflow: auto;
    box-sizing: border-box;
    .chart {
      position: static !important;
    }
  }
  .border {
    border: 1px solid #cccccc82;
    padding: 10px;
  }
  .chart-nodata {
    position: absolute;
    top: 60px;
    left: 1px;
    right: 1px;
    bottom: 56px;
    background: #ebebeb69;
    p {
      margin-top: 80px;
      text-align: center;
      font-size: 14px;
    }
  }
}
</style>
