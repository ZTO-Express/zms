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

package com.zto.zms.agent.core.supervisor;

import com.google.common.collect.Lists;
import com.zto.zms.writer.IniFileWriter;
import com.zto.zms.agent.core.config.CommandConfigAssemble;

import java.util.List;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/5
 **/
public class SupervisordConfigAssemble {
	private static final String SECTION_NAME = "program";
	private String section;
	private List<IniFileWriter.IniFileEntity> options = Lists.newArrayList();


	public SupervisordConfigAssemble(String section) {
		this.section = section;
	}

	public static SupervisordConfigAssemble newInstance(String programName) {
		return new SupervisordConfigAssemble(SECTION_NAME + ":" + programName);
	}



	public void accept(CommandConfigAssemble commandConfigAssemble) {
		commandConfigAssemble.assemble(options);
	}


	public String getSection() {
		return section;
	}

	private void setSection(String section) {
		this.section = section;
	}

	public List<IniFileWriter.IniFileEntity> getOptions() {
		return options;
	}
}

