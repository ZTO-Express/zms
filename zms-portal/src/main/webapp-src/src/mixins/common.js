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

// 通用表格，页面。包括reSubmit重复提交参数，loading加载，modal弹框,mulSelection多选框
import GLOBAL from '@/config/global'
export const common = {
  data() {
    return {
      isCollapse: false,
      reSubmit: false,
      loading: false,
      opTitle: '',
      opType: '',
      opKey: 1,
      opDialog: false,
      clearFk: 1, // 清空模糊查询参数
      mulSelection: [] // 所选择的行
    }
  },
  methods: {
    // 通用编辑
    commonEdit(row) {
      this.opType = 'edit'
      this.modalData = row
      this.opKey++
      setTimeout(() => {
        this.opDialog = true
      }, GLOBAL.modalTime)
    },
    // 通用新增
    commonAdd() {
      this.opType = 'add'
      this.opKey++
      setTimeout(() => {
        this.opDialog = true
      }, GLOBAL.modalTime)
    },
    // 模态框回调
    callbackModal() {
      this.fetchData()
    },
    // 重置表单
    onReset() {
      this.$refs['formData'].resetFields()
      this.clearFk++
    },
    // 多选框
    handleSelectionChange(val) {
      this.mulSelection = val
    },
    // 批量删除
    preDeleteBatch(str) {
      const items = []
      this.mulSelection.map(v => {
        items.push(v[str])
      })
      this.handleDelete(items)
    },
    // 删除当前row
    preDelete(row, str, e) {
      if (e) e.stopPropagation()
      const items = [row[str]]
      this.handleDelete(items)
    }
  }
}
