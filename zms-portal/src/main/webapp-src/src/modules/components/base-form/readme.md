# ele-base-form

![npm version](https://img.shields.io/npm/v/ele-base-form)
![npm download](https://img.shields.io/npm/dm/ele-base-form)

## <p id="intro">简介</p>
> ele-base-form 是一个基于element-ui组件的vue组件。  
> 项目实现效果为以简约的、配置化方式表达form表单。旨在为存在大量表单的后台管理系统开发提供便利。

## <p id="use">快速使用</p>

1. views 中使用  
在页面组件中，对form表单的变更操作主要是对formOpt的修改。修改需要符合vue数据更新监听机制。
```
<template>
  <ele-base-form v-bind="formOpt" />
</template>
<script>
  export default{
    data(){
      return {
        formOpt: {
          inline: false,
          ref: 'testform',
          formrefname: 'testform',
          forms:[
            {
              label: '基本输入框',
              prop: 'inputTest',
              slots: [{ type: 'prepend', text: 'HTTP://' }],
              defaultValue: 'www.github.com'
            }, {
              itemType: 'select',
              label: '本地下拉框',
              prop: 'select',
              options: [{ label: '数值', value: 'number' }, { label: '字符串', value: 'string' }],
              change: this.selectchange
            }, {
              itemType: 'remoteselect',
              label: '远程下拉-静态参数',
              prop: 'staticParamsRemoteSelect',
              hostName:'http://www.xxxx.com',
              apiUrl: '/api/consumer/queryApprovedConsumers',
              method: 'GET',
              remoteParams: { clusterType: 'kafka' },
              labelkeyname: 'name',
              valuekeyname: 'name',
              staticFilter: { applicant: '王强' }
              autoget: true
            }
          ]
        }
      }
    },
    methods:{
      selectchange(val, formrefname){
        console.log(val,formrefname)
      }
    }
  }
</script>
```


## <p id="api">API</p>
### Props
| 参数 | 说明 | 类型 | 可选值 | 默认值 |
| --- |------|:----:|:-----:|:-----:|
| inline | 表单项排列规则 | Boolean | - | false |
| size | 表单size | String | medium，small，mini | small |
| disabled | 表单整体禁用,优先级高于item disabled | Boolean | - | false |
| labelWidth | 表单统一label宽度 | String | - | '150px' |
| hostName | 表单中请求通用域名 | String | - | - |
| currentFormValue | 表单当前值。从外部对表单进行赋值，currentFormValue中属性名需和表单的prop对应 | Object | - | {} |
| ref | 当前组件的引用信息，注册在父组件$refs对象上 | String | - | baseform |
| formrefname | 添加在form item上的表单ref。常用场景：动态生成多个表单项，操作表单item时，item回调返回formrefname,以该参数作为找到当前表单的标志 | String | - | baseform |
| forms | 表单配置数组 | Array | - | - |

### Events

| 事件名称 | 说明 | 回调参数 |
| --- |------|:----:|
| paramsChange | 监听表单params变化 | params |

### Methods

| 事件名称 | 说明 | 回调参数 |
| --- |------|:----:|
| handleFormValidate | 根据rules验证form表单，访问方式为 this.$refs[refname].handleFormValidate | Function 接收valid,value两个参数 |
| reset | 重置表单数据 | - |
| clear | 清空表单数据 | - |


### forms数组元素（对象）规则

> 通用配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
| --- |------|:----:|-----|:-----:|
| itemType | item 类型 | String | [input](#inputType), number, radio, checkbox, select, slider, date,time, remoteselect | input |
| label | item描述 | String | - | - |
| labelWidth | 当前项label 宽度 | String | - | - |
| prop | item key,必填，表单数据以它为key | String | - | - |
| visible | 表单项是否渲染 | Boolean | - | true |
| disabled | item 禁用 | Boolean | - | false |
| inline | 是否自成一行 | Boolean | - | false |
| rules | 表单项验证规则 | Array | - | - |  |
| placeholder | placeholder | String | - | 输入框默认：请输入内容，下拉框：请选择 |
| defaultValue | item 默认值 | String, Array, Number,Boolean | - | - |
| change | 选中


> <span id="inputType">input类型额外配置</span>

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
| --- |------|:----:|-----|:-----:|
| inputType | 输入框延伸类型 | String | text, textarea, email, password | text |
| slots | 支持append、prepend插入 | Array | {type:'append',text:''},{type:'prepend',text:''} | - |
| rows | textarea行数 | Number | - | - |

> number类型额外配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
| --- |------|:----:|-----|:-----:|
| slots | 支持append、prepend插入 | Array | {type:'append',text:''},{type:'prepend',text:''} | - |
| min | 最小值 | Number | - | - |
| max | 最大值 | Number | - | 2147483647 |
| step | 增减粒度 | Number | - | 1 |
| stepStrictly | 是否严格控制递增递减step | Boolean | - | false |

> silder类型额外配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
| --- |------|:----:|-----|:-----:|
| showInput | 是否显示silder输入框 | Boolean | - | - |
| marks | 滑块坐标标记 | Object | - | - |

> radio/checkbox/select类型额外配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
| --- |------|:----:|-----|:-----:|
| options | 选项信息,option单项中设置disabled可以禁用当前选项 | Array | ['example1','example2']或者[{label:'example1',value:'value1',name:'cc'}] | - |
| labelkeyname | 展示label的key值 | String | - | 'label' |
| valuekeyname | 展示value的key值 | String | - | 'value' |
| change | 选中回调 | Function | - | - |


> select类型额外配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
| --- |------|:----:|-----|:-----:|
| filterable | 是否可搜索 | Boolean | - | false |

> date类型额外配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
| --- |------|:----:|-----|:-----:|
| dateType | 延伸的日期类型 | String | date, daterange, datetime, datetimerange | date |
| format | 日期输入框格式 | String | timestamp 、yyyy/MM/dd/HH/mm/ss | - |
| valueFormat | 日期输入框值绑定格式 | String | timestamp 、 yyyy/MM/dd/HH/mm/ss | - |
| startPlaceholder | dateType:'daterange/datetimerange' 第一项placeholder | String | - | - |
| endPlaceholder | dateType:'daterange/datetimerange' 第二项placeholder | String | - | - |

> time类型额外配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
| --- |------|:----:|-----|:-----:|
| format | 日期输入框格式 | String | HH:mm | - |
| valueFormat | 日期输入框值绑定格式 | String | HH:mm | - |

> remoteselect类型额外配置

| 参数 | 说明 | 类型 | 可选值 | 默认值 |
| --- |------|:----:|-----|:-----:|
| hostName | 请求域名，优先级高于form设置的hostName | String | - | - |
| apiUrl | 请求API路径 | String | - | - |
| method | 请求类型 | String | 'GET', 'POST', 'PUT', 'PATCH', 'DELETE' | 'GET' | 
| resultPath | 数据解析路径 | Array | - | ['result'] | 
| labelkeyname | 展示label的key值 | String | - | 'label' |
| valuekeyname | 展示value的key值 | String | - | 'value' |
| disablekeyname | 数据展示禁用key | String | label,value.... | - |
| disableflg | 数据禁用flg | Boolean,String | - | - |
| staticOptions | 远程下拉框数据中添加的静态数据 | Array | - | - |
| remoteParams | 请求静态参数 | Object | - | - |
| staticFilter | 下拉框静态筛选参数 | Object | - | {} |
| pagination | 下拉框滚动到底部，分页请求,根据需要，配合传参remoteParams:{pageSize}| Boolean | - | false |
| pageNumKey | 分页pageNum 参数key值 | String | - | pageNum |
| pagePath | 总页数所在res位置 | Array | - | ['result','pages'] |
| <span id="relativeProp">relativeProp</span> | [表单关联信息，通过prop搭建关系](#relativePropDetail)，[{prop:'',paramkey:'',filterkey:'']| Array | - | - |
| autoget | 自动请求下拉列表数据 | Boolean | - | false |
| change | 选中回调 | Function | - | - |
| selectInfo | 选中回调,返回选中项的所有信息 | Function | - | - |

### <span id="relativePropDetail">relativeProp详细介绍</span>  
以A作为B的依赖为例子  
1. A值被修改后，B值被重置为默认值
2. 依赖关系
    1. A值作为B的请求参数，使用键paramkey，值为A的prop
    2. A值作为B的筛选参数，使用filterkey


### 附常见操作场景介绍
1. 表格数据的编辑。将rows Object赋值给currentFormValue，即可完成表单赋值。（注意rows Object key值需要与forms中的prop值对应）
2. 动态生成的表单建议添加formrefname参数。进行内部关联操作时（下拉框选中回调），change回调会返回formrefname，界面可以通过this.$refs[formrefname]找到操作项。（ref属性和formrefname需要值一致）
3. form item关联。提供部分简单关联入口见[relativeProp](#relativeProp)，复杂关联提供change/selectInfo 方法回调操作。
