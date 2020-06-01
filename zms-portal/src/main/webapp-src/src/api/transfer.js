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

import request from '@/utils/request'
import * as env from '@/config/env.js'
const HostName = env.HostName

// 环境下拉列表
export function environmentList(params) {
  return request({
    url: `${HostName}/api/env/listEnvironment`,
    method: 'get',
    params: params
  })
}

// 集群下拉列表
export function clusterList(params) {
  return request({
    url: `${HostName}/api/service/listClusters`,
    method: 'get',
    params: params
  })
}

// 集群查询列表
export function queryClusters(params) {
  return request({
    url: `${HostName}/api/service/queryClusters`,
    method: 'get',
    params: params
  })
}

// 集群迁移topic
export function migrateClusterTopicZk(params) {
  return request({
    url: `${HostName}/api/migrate/migrateClusterTopicZk`,
    method: 'get',
    params: params
  })
}

// 集群迁移consumer
export function migrateClusterConsumerZk(params) {
  return request({
    url: `${HostName}/api/migrate/migrateClusterConsumerZk`,
    method: 'get',
    params: params
  })
}

// 主题迁移/消费迁移
export function migrateZk(params) {
  return request({
    url: `${HostName}/api/migrate/migrateZk`,
    method: 'get',
    params: params
  })
}

// 集群重建
export function clusterRecover(params) {
  return request({
    url: `${HostName}/api/recover/clusterRecover`,
    method: 'get',
    params: params
  })
}
