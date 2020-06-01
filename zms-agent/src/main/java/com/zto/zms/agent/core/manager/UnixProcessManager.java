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

package com.zto.zms.agent.core.manager;

import com.google.common.collect.Lists;
import com.zto.zms.common.RunCommonConfig;
import com.zto.zms.utils.Assert;
import com.zto.zms.writer.IniFileWriter;
import com.zto.zms.agent.core.api.SupervisordApi;
import com.zto.zms.agent.web.api.ZmsPortalApi;
import com.zto.zms.agent.web.dto.ProcessRequestDTO;
import com.zto.zms.common.ProcessStdLog;
import com.zto.zms.agent.core.supervisor.ProcessConfigAssemble;
import com.zto.zms.agent.core.supervisor.SupervisordConfigAssemble;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

import static com.zto.zms.agent.core.api.SupervisordApi.CODE_BAD_NAME;
import static com.zto.zms.agent.core.api.SupervisordApi.CODE_NOT_RUNNING;
import static com.zto.zms.agent.core.constant.EnvironmentOptConstant.SUPERVISOR_DIR;

/**
 * <p>Class: UnixProcessManagerImpl</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/2/28
 **/
@Service("supervisord")
public class UnixProcessManager implements ProcessManager {
	private static Logger logger = LoggerFactory.getLogger(UnixProcessManager.class);

	@Autowired
	private ProcessConfigAssemble processConfigAssemble;
	@Autowired
	private ZmsPortalApi zmsPortalApi;
	@Autowired
	private SupervisordApi supervisordApi;


	@Override
	public boolean start(ProcessRequestDTO processRequestDto) throws Exception {
		//查询上次启动成的进程
		Integer processId = processRequestDto.getProcessId();
		String programName = processRequestDto.getProgramName();
		String lastStartProgramName = processRequestDto.getLastStartProgramName();
		//根据服务processId下载服务配置
		//下载并解压服务配置文件
		zmsPortalApi.uploadConfigFile(processId, programName);
		//supervisor配置文件
		uploadSupervisorConfig(processId, programName, lastStartProgramName);
		supervisordApi.reloadConfig();
		return supervisordApi.addProcessGroup(programName);
	}

	@Override
	public boolean stop(String programName) throws XmlRpcException {
		try {
			boolean stop = supervisordApi.stopProcess(programName);
			Assert.that(stop, "停止服务失败");
			boolean remove = supervisordApi.removeProcessGroup(programName);
			Assert.that(remove, "移除服务失败");
		} catch (XmlRpcException e) {
			if (!Lists.newArrayList(CODE_NOT_RUNNING, CODE_BAD_NAME).contains(e.code)) {
				throw e;
			}
		}
		//删除supervisor配置文件
		removeSupervisorConfig(programName);
		return true;
	}

	@Override
	public String readProcessStdoutLog(String programName, int offset, int length) throws XmlRpcException {
		return supervisordApi.readProcessStdoutLog(programName, offset, length);
	}

	@Override
	public String readProcessStderrLog(String programName, int offset, int length) throws XmlRpcException {
		return supervisordApi.readProcessStderrLog(programName, offset, length);
	}

	@Override
	public ProcessStdLog tailProcessStdoutLog(String programName, int offset, int length) throws XmlRpcException {
		return supervisordApi.tailProcessStdoutLog(programName, offset, length);
	}

	@Override
	public ProcessStdLog tailProcessStderrLog(String programName, int offset, int length) throws XmlRpcException {
		return supervisordApi.tailProcessStderrLog(programName, offset, length);
	}

	/**
	 * 生成supervisor配置文件
	 *
	 * @param processId
	 * @param programName
	 * @param lastStartProgramName
	 * @throws IOException
	 */
	private void uploadSupervisorConfig(Integer processId, String programName, String lastStartProgramName) throws Exception {
		//服务环境配置参数
		RunCommonConfig runCommonConfig = zmsPortalApi.runCommon(processId);
		SupervisordConfigAssemble supervisordConfigAssemble = processConfigAssemble.assemble(runCommonConfig);
		//supervisor配置目录
		String supervisorDir = System.getProperty(SUPERVISOR_DIR);
		String outDir = MessageFormat.format("{0}/include/{1}.conf", supervisorDir, programName);
		File entryFile = new File(outDir);
		if (!entryFile.getParentFile().exists()) {
			boolean result = entryFile.getParentFile().mkdirs();
			Assert.that(result, "创建目录失败:" + outDir);
		}
		//生成supervisor配置文件
		try (FileOutputStream outputStream = new FileOutputStream(entryFile)) {
			IniFileWriter iniFileWriter = IniFileWriter.newInstance();
			iniFileWriter.add(supervisordConfigAssemble.getSection(), supervisordConfigAssemble.getOptions());
			iniFileWriter.store(outputStream);
		}
		//删除上次配置
		if (StringUtils.isNotBlank(lastStartProgramName) && !programName.equals(lastStartProgramName)) {
			removeSupervisorConfig(lastStartProgramName);
		}
		logger.info("Supervisor  config file :{}", outDir);
	}

	private void removeSupervisorConfig(String lastStartProgramName) {
		String supervisorDir = System.getProperty(SUPERVISOR_DIR);
		String lastStartProgramFile = MessageFormat.format("{0}/include/{1}.conf", supervisorDir, lastStartProgramName);
		File deleteEntryFile = new File(lastStartProgramFile);
		if (deleteEntryFile.exists() && deleteEntryFile.isFile()) {
			boolean delete = deleteEntryFile.delete();
			if (!delete) {
				logger.error(MessageFormat.format("Delete Supervisor config file failure:{0}", lastStartProgramFile));
				return;
			}
			logger.info("removeSupervisorConfig:{}", lastStartProgramFile);
		}
	}


}

