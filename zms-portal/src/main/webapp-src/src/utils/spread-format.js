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

import GC from '@zto/spread-sheets'
import moment from 'moment'
export function UnixToDateTime() {}
export function CustomNumber() {}
UnixToDateTime.prototype = new GC.Spread.Formatter.FormatterBase()
UnixToDateTime.prototype.format = function(obj) {
  return moment(obj).format('YYYY-MM-DD HH:mm:ss')
}
CustomNumber.prototype = new GC.Spread.Formatter.FormatterBase()
CustomNumber.prototype.format = function() {}
