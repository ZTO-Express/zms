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
 * Created by Administrator
 */

/**
* name:'router-name'             the name is used by <keep-alive> (must set!!!),对所有带二级菜单的路由来说必填项
* icon: 'svg-name'               路由前缀图标。可不传
* redirect                       重定向位置，非必填参数
* meta : {
    label:      string           标题（不存在则不显示在菜单列表中，取代旧版的noMe）
    cache:      boolean          是否缓存标签数据。true缓存标签
    closable:   boolean          标签是否可关闭。true可关闭标签
    role:       string           访问当前路由需要的权限。参数不存在时，不需要权限皆可访问
    hidden:     boolean          路由是否显示在菜单栏。true不显示  当父级设置不显示时，子菜单皆不显示。
    nav         string           路由对应的导航栏菜单，不传为默认导航菜单
    iframe      string           iframe引用页面,自定义key。同域名复用时可以使用一样的key
    iframeUrl   string           iframeUrl引用页面的子路径（例如 /xxx 。同域名复用时使用，不复用时不传这个参数）
  }
**/
import Vue from 'vue'
import Router from 'vue-router'
import { constantRouterMap } from './constant'

//vue-router3.1.0 以上版本，push和replace方法会返回一个promise。捕获异常
const originalPush = Router.prototype.push
Router.prototype.push = function push(location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalPush.call(this, location, onResolve, onReject)
  }
  return originalPush.call(this, location).catch(err => err)
}
Vue.use(Router)
export default new Router({
  // mode: 'history', // require service support
  // scrollBehavior: () => ({ y: 0 }),
  routes: constantRouterMap
})
