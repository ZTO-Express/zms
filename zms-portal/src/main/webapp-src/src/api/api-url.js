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

import * as env from '@/config/env.js'
const HostName = env.HostName

const theme = {
  validCode: { url: HostName + '/api/topic/uniqueCheck', method: 'get' },
  applicant: { url: HostName + '/api/userinfo/queryApplicants', method: 'get' },
  getTopicsByClusterId: { url: HostName + '/api/topic/getTopicsByClusterId', method: 'get' },
  getTopicsByCluster: { url: HostName + '/api/topic/getTopicsByCluster', method: 'get' }
}
const environment = {
  list: { url: HostName + '/api/env/listEnvironment', method: 'get' }
}
const cluster = {
  list: { url: HostName + '/api/service/listClusters', method: 'get' }
}

const consumer = {
  list: { url: HostName + '/api/consumer/queryApprovedConsumers', method: 'get' },
  getApproveTopicList: { url: HostName + '/api/topic/getTopicsByClusterId', method: 'get' },
  getConsumersByClusterId: { url: HostName + '/api/consumer/getConsumersByClusterId', method: 'get' },
  validCode: { url: HostName + '/api/consumer/uniqueCheck', method: 'post' }
}

export default {
  theme,
  cluster,
  consumer,
  environment
}
