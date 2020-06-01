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

package com.zto.zms.agent.core.config;

import com.zto.zms.writer.IniFileWriter;

import java.util.List;

/**
 * <p> Description: </p>
 * 
 * @author lidawei
 * @version 1.0
 * @date 2020/3/4
 */
public interface CommandConfigAssemble {

	/**
	 * 组装配置
	 * @param options
	 * @return
	 */
	List<IniFileWriter.IniFileEntity> assemble(List<IniFileWriter.IniFileEntity> options);
}

