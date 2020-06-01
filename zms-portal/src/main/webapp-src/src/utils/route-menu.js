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
 *
 * 菜单，路由工具类
 */
import Vue from 'vue'
import { util } from '@/utils/util'
import Layout from '@/modules/layout/Layout'
import Nest2 from '@/modules/layout/Nest2'
import Nest3 from '@/modules/layout/Nest3'

export default class RouteMenu {
  constructor(originRouteArr, permissions) {
    this.originRouteArr = originRouteArr ? util.deepCopy(originRouteArr) : []
    this.permissions = permissions || [] // 菜单、操作、父菜单权限集
    this.routeId = 0 // 路由Id
    this.routeArr = [] // 所有路由数组
    this.addRouters = [] // 需要添加的路由
  }

  // 非网关
  // 整理所有的路由 prop
  preRouteArr(routes = this.originRouteArr, pId = 0) {
    for (const v of routes) {
      this.routeId++
      this.routeArr.push({
        id: this.routeId,
        pId: pId,
        role: v.meta && v.meta.role
      })
      if (v.children && v.children.length > 0) {
        this.preRouteArr(v.children, this.routeId)
      }
    }
  }

  // 递归向上，统计所有菜单权限
  fillPerms(role, routeArr = this.routeArr) {
    this.permissions.push(role)
    for (const v of routeArr) {
      if (role === v.role) {
        for (const v2 of routeArr) {
          if (v.pId === v2.id) {
            if (v2.role) {
              this.permissions.push(v2.role)
              this.fillPerms(v2.role, routeArr)
            }
          }
        }
      }
    }
  }

  // 返回所有权限
  getPermission() {
    this.permissions = Array.from(new Set([...this.permissions]))
    return this.permissions
  }

  // 生成菜单树
  menuTree(prop, navDefault, data = this.originRouteArr) {
    let _menu = []
    data.forEach(item => {
      const { meta = {}, name, children, icon, path } = item
      const { nav, role, hidden, label } = meta
      // 当前菜单tab对应的所有路由
      if (prop === nav || (!nav && prop === navDefault)) {
        // 筛选菜单栏显示项
        if (!hidden && (!role || (role && this.permissions.indexOf(role) > -1))) {
          if (label === undefined) {
            children && Array.isArray(children) && (_menu = _menu.concat(this.menuTree(prop, navDefault, children)))
          } else {
            const menuitem = { ...meta, icon, path, name }
            children &&
              Array.isArray(children) &&
              Object.assign(menuitem, {
                children: this.menuTree(prop, navDefault, children)
              })
            _menu.push(menuitem)
          }
        }
      }
    })
    return _menu
  }

  // 权限路由
  hasPermission(roles, route) {
    if (route.meta && route.meta.role) {
      const _role = route.meta.role
      return roles.some(role => _role === role)
    }
    return true
  }

  // 筛选权限路由
  filterAsyncRouter(asyncRouterMap = this.originRouteArr, roles = this.permissions) {
    const accessedRouters = asyncRouterMap.filter(route => {
      if (this.hasPermission(roles, route)) {
        if (route.children && route.children.length) {
          route.children = this.filterAsyncRouter(route.children, roles)
        }
        return true
      }
      return false
    })
    return accessedRouters
  }

  // 传递,深层级数
  getFloor(treeData, reference) {
    let max = 0
    const floor = 0
    treeData = reference ? treeData : util.deepCopy(treeData)
    function each(data, floor) {
      data.forEach(e => {
        e.floor = floor
        if (floor > max) {
          max = floor
        }
        if (e.children && e.children.length > 0) {
          each(e.children, floor + 1)
        }
      })
    }
    each(treeData, floor + 1)
    return max
  }

  // 添加详情页路由
  pageDetail(params, obj, index, routeDeep) {
    obj.children = []
    if (index === routeDeep) {
      const Detail = params.component
      const Component = Vue.component(params.name, {
        render() {
          return <Detail />
        },
        components: { Detail }
      })
      obj.path = params.path
      obj.name = params.name
      obj.component = Component
      obj.meta = params.meta
    } else {
      obj.path = ''
      if (index === 1) obj.component = Layout
      if (index === 2) obj.component = Nest2
      if (index === 3) obj.component = Nest3
      const objSub = {}
      obj.children.push(this.pageDetail(params, objSub, index + 1, routeDeep))
    }
    return obj
  }
}
