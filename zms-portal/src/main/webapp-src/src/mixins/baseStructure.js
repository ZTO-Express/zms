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

import tablePagination from '@/modules/components/table-pagination'
import OperateButton from '@/modules/components/button/OperateButton'
import baseForm from '@/modules/components/base-form/form'
import colorWord from '@/modules/components/colorWord'
import { mapGetters } from 'vuex'
import { checkPerm, checkUser } from '@/utils/permission'

export default {
  components: {
    tablePagination,
    OperateButton,
    baseForm,
    colorWord
  },
  computed: {
    ...mapGetters['userInfo']
  },
  methods: {
    checkPerm,
    checkUser,
    /**
     * 打开弹窗后执行表单相关的回调
     * @param {object} row 表单值当前数据，一般在编辑状态，由表格行数据传递过来
     * @param {object} options 表单配置对象
     * @param {function} cb 打开弹窗其他操作
     * @param {function} nextTickCb 弹窗DOM渲染后需要执行的操作
     *
     */
    handleDia({ row = {}, options, cb, nextTickCb }) {
      options = Object.assign(options, { visible: true, currentFormValue: row })
      cb && cb()
      this.$nextTick(() => {
        nextTickCb && nextTickCb()
        options.ref && this.$refs[options.ref].clear()
      })
    },
    // form 提交验证
    handleValidate({ refname, cb }) {
      // nextTick 解决mouted中默认值搜索获不到值的问题
      this.$nextTick(() => {
        this.$refs[refname].handleFormValidate(params => {
          cb && cb(params)
        })
      })
    },
    // 弹窗提示
    isDo({ cb, title, catchcb = () => {} }) {
      this.$confirm(title, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(cb)
        .catch(catchcb)
    },
    // 跳转
    go(params) {
      this.$router.push(params)
    },
    // 提示
    message(type, msg) {
      this.$message({
        message: msg,
        type
      })
    }
  }
}
