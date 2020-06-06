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

package com.zto.zms.agent.web.dto;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/2/28
 **/
public class ProcessRequestDTO {
	private Integer processId;
	private String programName;
	private String programType;
	private String lastStartProgramName;

	public Integer getProcessId() {
		return processId;
	}

	public void setProcessId(Integer processId) {
		this.processId = processId;
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public String getProgramType() {
		return programType;
	}

	public void setProgramType(String programType) {
		this.programType = programType;
	}

	public String getLastStartProgramName() {
		return lastStartProgramName;
	}

	public void setLastStartProgramName(String lastStartProgramName) {
		this.lastStartProgramName = lastStartProgramName;
	}
}

