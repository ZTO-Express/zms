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
 * @date 2020/3/6
 **/
public class SupervisordProcessInfoDTO {

	private String stderr_logfile;
	private String logfile;
	private Long start;
	private String description;
	private Long pid;
	private String statename;
	private String spawnerr;
	private Long stop;
	private Long now;
	private String name;
	private String exitstatus;
	private String state;
	private String stdout_logfile;
	private String group;

	public String getStderr_logfile() {
		return stderr_logfile;
	}

	public void setStderr_logfile(String stderr_logfile) {
		this.stderr_logfile = stderr_logfile;
	}

	public String getLogfile() {
		return logfile;
	}

	public void setLogfile(String logfile) {
		this.logfile = logfile;
	}

	public Long getStart() {
		return start;
	}

	public void setStart(Long start) {
		this.start = start;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public String getStatename() {
		return statename;
	}

	public void setStatename(String statename) {
		this.statename = statename;
	}

	public String getSpawnerr() {
		return spawnerr;
	}

	public void setSpawnerr(String spawnerr) {
		this.spawnerr = spawnerr;
	}

	public Long getStop() {
		return stop;
	}

	public void setStop(Long stop) {
		this.stop = stop;
	}

	public Long getNow() {
		return now;
	}

	public void setNow(Long now) {
		this.now = now;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExitstatus() {
		return exitstatus;
	}

	public void setExitstatus(String exitstatus) {
		this.exitstatus = exitstatus;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getStdout_logfile() {
		return stdout_logfile;
	}

	public void setStdout_logfile(String stdout_logfile) {
		this.stdout_logfile = stdout_logfile;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
}

