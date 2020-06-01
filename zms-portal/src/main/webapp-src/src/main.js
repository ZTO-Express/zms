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

import Vue from 'vue'
// import 'normalize.css/normalize.css' // 解决游览器之间的差异

import Element from 'element-ui' // 引入element ui
import 'element-ui/lib/theme-chalk/index.css' // 引入 element ui 样式

import '@/styles/index.scss' // 自定义样式
import '@/modules/directive' // 自定义指令
import '@/utils/components' // 自定义组件
import '@/utils/filters' // 自定义过滤器

import '@/assets/iconfont/iconfont.js' // 阿里icon
import '@/assets/iconfont/iconfont.css' // 阿里icon
import '@/vendor/iconfont-local' // 阿里icon
import 'font-awesome/css/font-awesome.css' // font-awesome图标

import './permission' // permission control

import App from './App.vue'
import router from './router/index'
import store from './store/index'
import Global from './config/global' // 全局变量
Object.defineProperty(Vue.prototype, 'GLOBAL', { value: Global })

Vue.prototype.Event = new Vue() // event bus
Vue.use(Element, {
  // size: 'mini' // set element-ui default size
})
Vue.config.productionTip = false

new Vue({
  el: '#app',
  router,
  store,
  methods: {
    hashChangeHandler: function() {
      const target = window.location.hash
      this.$router.push(target.substring(1, target.length))
    }
  },
  mounted: function() {
    //  fix for IE 11
    if (!!window.MSInputMethodContext && !!document.documentMode) {
      window.addEventListener('hashchange', this.hashChangeHandler)
    }
  },
  destroyed: function() {
    // fix for IE 11
    if (!!window.MSInputMethodContext && !!document.documentMode) {
      window.removeEventListener('hashchange', this.hashChangeHandler)
    }
  },
  render: h => h(App)
})
