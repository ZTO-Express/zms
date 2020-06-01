/*
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

package com.zto.zms.portal.assemble;

import java.util.Map;

/**
 * <p>Class: ParamOptAssemble</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/12
 **/
public interface ParamOptAssemble {
    /**
     * @param processId
     * @return
     */
    Map<String, String> assembleParamOpt(Integer processId);
}

