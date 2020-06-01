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

import Layout from '@/modules/layout/Layout'
import Nest2 from '@/modules/layout/Nest2'

export default {
  path: '/devops',
  name: 'devops',
  redirect: 'noredirect',
  component: Layout,
  meta: {
    label: '运维管理'
  },
  children: [
    {
      path: '',
      component: Nest2,
      children: [
        {
          path: 'cluster-transfer',
          name: 'clusterTransfer',
          component: () => import('@/views/devops/clusterTransfer/list'),
          meta: {
            label: '集群迁移', // 标题
            cache: true, // 是否缓存
            role: 'admin',
            closable: true // 是否标签可关闭，
          }
        },
        {
          path: 'theme-transfer',
          name: 'themeTransfer',
          component: () => import('@/views/devops/themeTransfer/list'),
          meta: {
            label: '主题迁移', // 标题
            cache: true, // 是否缓存
            role: 'admin',
            closable: true // 是否标签可关闭，
          }
        },
        {
          path: 'consumer-transfer',
          name: 'consumerTransfer',
          component: () => import('@/views/devops/consumerTransfer/list'),
          meta: {
            label: '消费迁移', // 标题
            cache: true, // 是否缓存
            role: 'admin',
            closable: true // 是否标签可关闭，
          }
        },
        {
          path: 'message-search',
          name: 'messageSearch',
          component: () => import('@/views/devops/messageSearch/list'),
          meta: {
            label: 'MQ消息检索', // 标题
            cache: true, // 是否缓存
            closable: true // 是否标签可关闭，
          }
        },
        {
          path: 'rocketmqcmd',
          name: 'rocketmqcmd',
          redirect: 'noredirect',
          component: Nest2,
          meta: {
            label: 'RocketMQ控制台',
            role: 'me_rocketmq_node'
          },
          children: [
            {
              path: 'statsAll',
              name: 'statsAll',
              component: () => import('@/views/devops/rocketmq/statsAll/list'),
              meta: {
                label: 'statsAll', // 标题
                role: 'admin',
                closable: true // 是否标签可关闭，
              }
            },
            {
              path: 'updatePerm',
              name: 'updatePerm',
              component: () => import('@/views/devops/rocketmq/updatePerm/list'),
              meta: {
                label: 'updatePerm', // 标题
                role: 'admin',
                closable: true // 是否标签可关闭，
              }
            },
            {
              path: 'getBrokerConfig',
              name: 'getBrokerConfig',
              component: () => import('@/views/devops/rocketmq/getBrokerConfig/list'),
              meta: {
                label: 'getBrokerConfig', // 标题
                role: 'admin',
                closable: true // 是否标签可关闭，
              }
            },
            {
              path: 'consumerStatusAll',
              name: 'consumerStatusAll',
              component: () => import('@/views/devops/rocketmq/consumerStatusAll/list'),
              meta: {
                label: 'consumerStatusAll', // 标题
                role: 'admin',
                closable: true // 是否标签可关闭，
              }
            },
            {
              path: 'clusterList',
              name: 'clusterList',
              component: () => import('@/views/devops/rocketmq/clusterList/list'),
              meta: {
                label: 'clusterList', // 标题
                role: 'admin',
                closable: true // 是否标签可关闭，
              }
            },
            {
              path: 'topicStatus',
              name: 'topicStatus',
              component: () => import('@/views/devops/rocketmq/topicStatus/list'),
              meta: {
                label: 'topicStatus', // 标题
                role: 'admin',
                closable: true // 是否标签可关闭，
              }
            },
            {
              path: 'InMsg24HourTop',
              name: 'InMsg24HourTop',
              component: () => import('@/views/devops/rocketmq/topicInMsgTop/list'),
              meta: {
                label: 'InMsg24HourTop', // 标题
                role: 'admin',
                closable: true // 是否标签可关闭，
              }
            }
          ]
        },
        {
          path: 'kafkacmd',
          name: 'kafkacmd',
          redirect: 'noredirect',
          component: Nest2,
          meta: {
            label: 'Kafka控制台',
            role: 'me_kafka_node'
          },
          children: [
            {
              path: 'ConsumerSummary',
              name: 'ConsumerSummary',
              component: () => import('@/views/devops/kafka/ConsumerSummary/list'),
              meta: {
                label: 'ConsumerSummary', // 标题
                role: 'admin',
                closable: true // 是否标签可关闭，
              }
            },
            {
              path: 'TopicSummary',
              name: 'TopicSummary',
              component: () => import('@/views/devops/kafka/topicSummary/list'),
              meta: {
                label: 'TopicSummary', // 标题
                role: 'admin',
                closable: true // 是否标签可关闭，
              }
            }
          ]
        }
      ]
    }
  ]
}
