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
 * <p>Class: ProcessCheckConfig</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/4
 **/
public class ProcessCheckCommandConfigAssemble implements CommandConfigAssemble {
	private String exitcodes;
	private String autorestart;
	private Integer startsecs;
	private Integer stopwaitsecs;
	private Boolean killasgroup;
	private Boolean stopasgroup;
	private Integer startretries;
	private String stopsignal;
	public static ProcessCheckCommandConfigAssemble newInstance(){
		return new ProcessCheckCommandConfigAssemble();
	}

	@Override
	public List<IniFileWriter.IniFileEntity> assemble(List<IniFileWriter.IniFileEntity> options) {
		options.add(new IniFileWriter.IniFileEntity("exitcodes", exitcodes));
		options.add(new IniFileWriter.IniFileEntity("autorestart", autorestart));
		options.add(new IniFileWriter.IniFileEntity("startsecs", startsecs));
		options.add(new IniFileWriter.IniFileEntity("stopwaitsecs", stopwaitsecs));
		options.add(new IniFileWriter.IniFileEntity("killasgroup", killasgroup));
		options.add(new IniFileWriter.IniFileEntity("stopasgroup", stopasgroup));
		options.add(new IniFileWriter.IniFileEntity("startretries", startretries));
		options.add(new IniFileWriter.IniFileEntity("stopsignal", stopsignal));
		return options;
	}

	public String getExitcodes() {
		return exitcodes;
	}

	public ProcessCheckCommandConfigAssemble exitcodes(String exitcodes) {
		this.exitcodes = exitcodes;
		return this;
	}

	public String getAutorestart() {
		return autorestart;
	}

	public ProcessCheckCommandConfigAssemble autorestart(String autorestart) {
		this.autorestart = autorestart;
		return this;
	}

	public Integer getStartsecs() {
		return startsecs;
	}

	public ProcessCheckCommandConfigAssemble startsecs(Integer startsecs) {
		this.startsecs = startsecs;
		return this;
	}

	public Integer getStopwaitsecs() {
		return stopwaitsecs;
	}

	public ProcessCheckCommandConfigAssemble stopwaitsecs(Integer stopwaitsecs) {
		this.stopwaitsecs = stopwaitsecs;
		return this;
	}

	public boolean isKillasgroup() {
		return killasgroup;
	}

	public ProcessCheckCommandConfigAssemble killasgroup(boolean killasgroup) {
		this.killasgroup = killasgroup;
		return this;
	}

	public Boolean getStopasgroup() {
		return stopasgroup;
	}

	public ProcessCheckCommandConfigAssemble stopasgroup(Boolean stopasgroup) {
		this.stopasgroup = stopasgroup;
		return this;
	}

	public Integer getStartretries() {
		return startretries;
	}

	public ProcessCheckCommandConfigAssemble startretries(Integer startretries) {
		this.startretries = startretries;
		return this;
	}

	public String getStopsignal() {
		return stopsignal;
	}

	public ProcessCheckCommandConfigAssemble stopsignal(String stopsignal) {
		this.stopsignal = stopsignal;
		return this;
	}
}

