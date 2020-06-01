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

// 消息详情
export default {
  data() {
    return {
      diaOptions: {
        inline: true,
        visible: false,
        ref: 'diaform',
        diatitle: '消息详情',
        forms: [
          {
            prop: 'msgId',
            label: '消息Id',
            disabled: true,
            defaultValue: ' '
          },
          {
            prop: 'key',
            label: 'Key',
            disabled: true,
            defaultValue: ' '
          },
          {
            prop: 'tag',
            label: 'Tag',
            disabled: true,
            defaultValue: ' '
          },
          {
            prop: 'storeSize',
            label: '消息大小',
            disabled: true,
            defaultValue: ' '
          },
          {
            prop: 'queueId',
            label: '分区',
            disabled: true,
            defaultValue: ' '
          },
          {
            prop: 'queueOffset',
            label: '偏移量',
            disabled: true,
            defaultValue: ' '
          },
          {
            prop: 'bornHost',
            label: '发送端IP',
            disabled: true,
            defaultValue: ' '
          },
          {
            prop: 'bornTimestamp',
            label: '发送时间',
            disabled: true,
            defaultValue: ' '
          },
          {
            prop: 'storeHost',
            label: '存储IP',
            disabled: true,
            defaultValue: ' '
          },
          {
            prop: 'storeTimestamp',
            label: '存储时间',
            disabled: true,
            defaultValue: ' '
          },
          {
            prop: 'messageBody',
            label: '消息内容',
            inputType: 'textarea',
            rows: 7,
            disabled: true,
            inline: false,
            defaultValue: ' '
          }
        ],
        currentFormValue: {},
        loading: false,
        heightSize: '168px'
      }
    }
  },
  methods: {
    handleDetail(row) {
      let _row = row
      if (row.properties) {
        _row.key = row.properties.KEYS
        _row.tag = row.properties.TAGS
      }
      this.handleDia({
        options: this.diaOptions,
        row: _row,
        cb: () => {
          // if (!_row.messageTrackList) {
          //   this.diaOptions.heightSize = '68px'
          // } else if (_row.messageTrackList.length >= 3) {
          //   this.diaOptions.heightSize = '168px'
          // } else if (_row.messageTrackList.length == 2) {
          //   this.diaOptions.heightSize = '256px'
          // }
        }
      })
    },
    // 关闭弹窗
    closeDia() {
      Object.assign(this.diaOptions, {
        visible: false
      })
    }
  }
}
