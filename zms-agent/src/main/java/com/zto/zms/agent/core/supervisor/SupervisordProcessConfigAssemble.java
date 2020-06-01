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

import com.zto.zms.common.RunCommonConfig;
import com.zto.zms.agent.core.config.EnvironmentCommandConfigAssemble;
import com.zto.zms.agent.core.config.ExecuteCommandConfigAssemble;
import com.zto.zms.agent.core.config.LogCommandConfigAssemble;
import com.zto.zms.agent.core.config.ProcessCheckCommandConfigAssemble;
import com.zto.zms.agent.core.constant.EnvironmentOptConstant;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

import static com.zto.zms.agent.core.constant.EnvironmentOptConstant.CONF_DIR;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/11
 **/
@Component
public class SupervisordProcessConfigAssemble implements ProcessConfigAssemble {
	@Override
	public SupervisordConfigAssemble assemble(RunCommonConfig runCommonConfig) throws Exception {
		String serviceLibDir = System.getProperty(EnvironmentOptConstant.SERVICE_LIB_DIR);
		String serviceDir = System.getProperty(EnvironmentOptConstant.SERVICE_DIR);
		String processDir = System.getProperty(EnvironmentOptConstant.PROCESS_DIR);

		String processRunDirectory = MessageFormat.format("{0}/{1}", processDir, runCommonConfig.getProgramName());
		String command = MessageFormat.format("{0}/{1}", serviceDir, runCommonConfig.getCommand());
		String serverLibHome = MessageFormat.format("{0}/{1}", serviceLibDir, runCommonConfig.getLibDir());
		String serverLibHomeEnv = MessageFormat.format("{0}_HOME", runCommonConfig.getLibDir().toUpperCase());

		//supervisor配置
		SupervisordConfigAssemble configAssemble = SupervisordConfigAssemble.newInstance(runCommonConfig.getProgramName());
		//启动配置
		configAssemble.accept(ExecuteCommandConfigAssemble.newInstance()
				.command(command, runCommonConfig.getArgs())
				.directory(processRunDirectory)
				.user(runCommonConfig.getUser()));
		//日志配置
		configAssemble.accept(LogCommandConfigAssemble.newInstance()
				.processRunDirectory(processRunDirectory)
				.stderrLogfile(runCommonConfig.getStderrLogfile())
				.stderrLogfileBackups(runCommonConfig.getStderrLogfileBackups())
				.stderrLogfileMaxbytes(runCommonConfig.getStderrLogfileMaxBytes())
				.stderrCaptureMaxBytes(runCommonConfig.getStderrCaptureMaxBytes())
				.stdoutCaptureMaxBytes(runCommonConfig.getStdoutCaptureMaxBytes())
				.stderrEventsEnabled(runCommonConfig.getStderrEventsEnabled())
				.stdoutEventsEnabled(runCommonConfig.getStdoutEventsEnabled())
				.stdoutSyslog(runCommonConfig.getStdoutSyslog())
				.stdoutLogfileMaxbytes(runCommonConfig.getStdoutLogfileMaxBytes())
				.stdoutLogfileBackups(runCommonConfig.getStdoutLogfileBackups())
				.stdoutLogfile(runCommonConfig.getStdoutLogfile())
				.stderrLogfile(runCommonConfig.getStderrLogfile())
				.stderrSyslog(runCommonConfig.getStderrSyslog())
				.redirectStderr(runCommonConfig.getRedirectStderr()));
		//环境变量配置
		configAssemble.accept(EnvironmentCommandConfigAssemble.newInstance()
				.addEnvironment(CONF_DIR, processRunDirectory)
				.addEnvironment(serverLibHomeEnv, serverLibHome)
				.addAllEnvironment(runCommonConfig.getEnvironment()));
		//启动检查配置
		configAssemble.accept(ProcessCheckCommandConfigAssemble.newInstance()
				.autorestart(runCommonConfig.getAutoRestart())
				.exitcodes(runCommonConfig.getExitCodes())
				.startsecs(runCommonConfig.getStartSecs())
				.stopwaitsecs(runCommonConfig.getStopWaitSecs())
				.killasgroup(runCommonConfig.getKillAsGroup())
				.stopasgroup(runCommonConfig.getStopAsGroup())
				.stopsignal(runCommonConfig.getStopSignal())
				.startretries(runCommonConfig.getStartRetries()));
		return configAssemble;
	}
}

