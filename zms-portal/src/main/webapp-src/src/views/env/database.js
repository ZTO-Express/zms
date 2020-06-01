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

// 环境配置代码
import { listByEnvIdAndServiceType, loadDatabase } from '@/api/env'

export default {
  data() {
    return {
      envDatabaseOptions: {
        visible: false,
        envName: '',
        zookeepertableData: [],
        loading: false
      },
      envId: '',
      zkServiceId: '',
      influxdbServiceId: ''
    }
  },
  methods: {
    // 打开配置弹窗
    async openDatabaseEnv(id, envName, zkServiceId, influxdbServiceId) {
      this.envId = id
      this.envDatabaseOptions.envName = envName
      this.envDatabaseOptions.diatitle = envName + '配置数据源'
      await this.getZookeepertableData(id)
      await this.getInfluxdbtableData(id)
      this.handleDia({
        options: this.envDatabaseOptions,
        cb: () => {
          this.zkServiceId = zkServiceId
          this.influxdbServiceId = influxdbServiceId
        }
      })
    },
    async getZookeepertableData(envId) {
      const params = { envId: envId, serviceType: 'ZOOKEEPER' }
      const { result } = await listByEnvIdAndServiceType(params)
      if (result) {
        this.envDatabaseOptions.zookeepertableData = result
      }
    },
    async getInfluxdbtableData(envId) {
      const params = { envId: envId, serviceType: 'INFLUXDB' }
      const { result } = await listByEnvIdAndServiceType(params)
      if (result) {
        this.envDatabaseOptions.influxdbtableData = result
      }
    },
    // 关闭弹窗
    closeEnvDatabaseDia() {
      Object.assign(this.envDatabaseOptions, {
        visible: false
      })
    },
    async saveEnvDatabaseData() {
      const data = { zkServiceId: this.zkServiceId, influxdbServiceId: this.influxdbServiceId }
      await loadDatabase(this.envId, data)
      this.closeEnvDatabaseDia()
      this.loadData()
    }
  }
}
