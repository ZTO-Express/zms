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

import com.zto.zms.utils.Assert;
import com.zto.zms.writer.IniFileWriter;

import java.io.File;
import java.util.List;

/**
 *
 * @author lidawei
 * @date 2020/3/4
 **/
public class LogCommandConfigAssemble implements CommandConfigAssemble {

	private String stdoutLogfileMaxbytes = "10MB";
	private String stdoutLogfileBackups = "10";
	private String stderrLogfileMaxbytes = "10MB";
	private String stderrLogfileBackups = "10";
	private String stdoutLogfile;
	private String stderrLogfile;
	private String processRunDirectory;
	private Boolean redirectStderr;
	private Integer stdoutCaptureMaxBytes;
	private Boolean stdoutEventsEnabled;
	private Boolean stdoutSyslog;
	private Integer stderrCaptureMaxBytes;
	private Boolean stderrEventsEnabled;
	private Boolean stderrSyslog;


	public static LogCommandConfigAssemble newInstance() {
		return new LogCommandConfigAssemble();
	}

	@Override
	public List<IniFileWriter.IniFileEntity> assemble(List<IniFileWriter.IniFileEntity> options) {
		this.stdoutLogfile = null == stdoutLogfile ? processRunDirectory + "/logs/stdout.log" : stdoutLogfile;
		this.stderrLogfile = null == stderrLogfile ? processRunDirectory + "/logs/stderr.log" : stderrLogfile;
		makeDir(stdoutLogfile);
		makeDir(stderrLogfile);
		options.add(new IniFileWriter.IniFileEntity("stdout_logfile", stdoutLogfile));
		options.add(new IniFileWriter.IniFileEntity("stderr_logfile", stderrLogfile));
		options.add(new IniFileWriter.IniFileEntity("stdout_logfile_maxbytes", stdoutLogfileMaxbytes));
		options.add(new IniFileWriter.IniFileEntity("stdout_logfile_backups", stdoutLogfileBackups));
		options.add(new IniFileWriter.IniFileEntity("stderr_logfile_maxbytes", stderrLogfileMaxbytes));
		options.add(new IniFileWriter.IniFileEntity("stderr_logfile_backups", stderrLogfileBackups));
		options.add(new IniFileWriter.IniFileEntity("redirect_stderr", redirectStderr));
		options.add(new IniFileWriter.IniFileEntity("stdout_capture_maxbytes", stdoutCaptureMaxBytes));
		options.add(new IniFileWriter.IniFileEntity("stdout_events_enabled", stdoutEventsEnabled));
		options.add(new IniFileWriter.IniFileEntity("stdout_syslog", stdoutSyslog));
		options.add(new IniFileWriter.IniFileEntity("stderr_capture_maxbytes", stderrCaptureMaxBytes));
		options.add(new IniFileWriter.IniFileEntity("stderr_events_enabled", stderrEventsEnabled));
		options.add(new IniFileWriter.IniFileEntity("stderr_syslog", stderrSyslog));
		return options;
	}

	private void makeDir(String filePath) {
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			boolean result = file.getParentFile().mkdirs();
			Assert.that(result, "创建目录失败:" + filePath);
		}
	}

	public String getStdoutLogfileMaxbytes() {
		return stdoutLogfileMaxbytes;
	}

	public LogCommandConfigAssemble stdoutLogfileMaxbytes(String stdoutLogfileMaxbytes) {
		this.stdoutLogfileMaxbytes = stdoutLogfileMaxbytes;
		return this;
	}

	public String getStdoutLogfileBackups() {
		return stdoutLogfileBackups;
	}

	public LogCommandConfigAssemble stdoutLogfileBackups(String stdoutLogfileBackups) {
		this.stdoutLogfileBackups = stdoutLogfileBackups;
		return this;
	}

	public String getStderrLogfileMaxbytes() {
		return stderrLogfileMaxbytes;
	}

	public LogCommandConfigAssemble stderrLogfileMaxbytes(String stderrLogfileMaxbytes) {
		this.stderrLogfileMaxbytes = stderrLogfileMaxbytes;
		return this;
	}

	public String getStderrLogfileBackups() {
		return stderrLogfileBackups;
	}

	public LogCommandConfigAssemble stderrLogfileBackups(String stderrLogfileBackups) {
		this.stderrLogfileBackups = stderrLogfileBackups;
		return this;
	}

	public String getStdoutLogfile() {
		return stdoutLogfile;
	}

	public LogCommandConfigAssemble stdoutLogfile(String stdoutLogfile) {
		this.stdoutLogfile = stdoutLogfile;
		return this;
	}

	public String getStderrLogfile() {
		return stderrLogfile;
	}

	public LogCommandConfigAssemble stderrLogfile(String stderrLogfile) {
		this.stderrLogfile = stderrLogfile;
		return this;
	}

	public String getProcessRunDirectory() {
		return processRunDirectory;
	}

	public LogCommandConfigAssemble processRunDirectory(String processRunDirectory) {
		this.processRunDirectory = processRunDirectory;
		return this;
	}

	public Boolean getRedirectStderr() {
		return redirectStderr;
	}

	public LogCommandConfigAssemble redirectStderr(Boolean redirectStderr) {
		this.redirectStderr = redirectStderr;
		return this;
	}

	public Integer getStdoutCaptureMaxBytes() {
		return stdoutCaptureMaxBytes;
	}

	public LogCommandConfigAssemble stdoutCaptureMaxBytes(Integer stdoutCaptureMaxBytes) {
		this.stdoutCaptureMaxBytes = stdoutCaptureMaxBytes;
		return this;
	}

	public Boolean getStdoutEventsEnabled() {
		return stdoutEventsEnabled;
	}

	public LogCommandConfigAssemble stdoutEventsEnabled(Boolean stdoutEventsEnabled) {
		this.stdoutEventsEnabled = stdoutEventsEnabled;
		return this;
	}

	public Boolean getStdoutSyslog() {
		return stdoutSyslog;
	}

	public LogCommandConfigAssemble stdoutSyslog(Boolean stdoutSyslog) {
		this.stdoutSyslog = stdoutSyslog;
		return this;
	}

	public Integer getStderrCaptureMaxBytes() {
		return stderrCaptureMaxBytes;
	}

	public LogCommandConfigAssemble stderrCaptureMaxBytes(Integer stderrCaptureMaxBytes) {
		this.stderrCaptureMaxBytes = stderrCaptureMaxBytes;
		return this;
	}

	public Boolean getStderrEventsEnabled() {
		return stderrEventsEnabled;
	}

	public LogCommandConfigAssemble stderrEventsEnabled(Boolean stderrEventsEnabled) {
		this.stderrEventsEnabled = stderrEventsEnabled;
		return this;
	}

	public Boolean getStderrSyslog() {
		return stderrSyslog;
	}

	public LogCommandConfigAssemble stderrSyslog(Boolean stderrSyslog) {
		this.stderrSyslog = stderrSyslog;
		return this;
	}
}

