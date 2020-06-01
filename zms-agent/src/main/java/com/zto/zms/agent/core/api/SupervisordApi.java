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

package com.zto.zms.agent.core.api;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.zto.zms.common.ProcessStdLog;
import com.zto.zms.agent.web.dto.SupervisordProcessInfoDTO;
import com.zto.zms.agent.web.dto.SupervisordStateDTO;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static com.zto.zms.common.ZmsException.SUPERVISORD_INITIALIZATION_EXCEPTION;

/**
 * <p>Class: SupervisordApi</p>
 * <p>Description: supervisord通过xml-rpc交互</p>
 *
 * @author lidawei
 * @date 2020/2/28
 **/
@Component
public class SupervisordApi {

	public static final Logger logger = LoggerFactory.getLogger(SupervisordApi.class);


	private static final String SUPERVISORD_URL = "http://localhost:19001/RPC2";
	private static XmlRpcClient client;


	public static final int CODE_NOT_RUNNING = 70;

	public static final int CODE_BAD_NAME = 10;


	private SupervisordApi() {
		init();
	}

	static class Builder {
		private static SupervisordApi INSTANCE = new SupervisordApi();
	}

	public static SupervisordApi getInstance() {
		return Builder.INSTANCE;
	}

	private void init() {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL(SUPERVISORD_URL));
		} catch (MalformedURLException e) {
			logger.error("XmlRpcClientConfigImpl error",e);
			throw SUPERVISORD_INITIALIZATION_EXCEPTION;
		}
		client = new XmlRpcClient();
		client.setConfig(config);
	}


	/**
	 * 启动程序
	 *
	 * @param programName 程序id
	 * @return
	 * @throws XmlRpcException
	 */
	public boolean startProcess(String programName) throws XmlRpcException {
		return (boolean) client.execute("supervisor.startProcess", Lists.newArrayList(programName));
	}

	/**
	 * 获取所有进程信息
	 *
	 * @return
	 * @throws Exception
	 */
	public List<SupervisordProcessInfoDTO> getAllProcessInfo() throws Exception {
		Object map = client.execute("supervisor.getAllProcessInfo", Lists.newArrayList());
		return JSON.parseArray(JSON.toJSONString(map), SupervisordProcessInfoDTO.class);
	}

	/**
	 * 停止进程
	 *
	 * @param programName
	 * @return
	 * @throws XmlRpcException
	 */
	public boolean stopProcess(String programName) throws XmlRpcException {
		return (boolean) client.execute("supervisor.stopProcess", Lists.newArrayList(programName));
	}


	/**
	 * 从配置文件中更新正在运行的进程的配置。
	 *
	 * @param programName
	 * @return
	 * @throws XmlRpcException
	 */
	public boolean addProcessGroup(String programName) throws XmlRpcException {
		return (boolean) client.execute("supervisor.addProcessGroup", Lists.newArrayList(programName));
	}


	public boolean removeProcessGroup(String programName) throws XmlRpcException {
		return (boolean) client.execute("supervisor.removeProcessGroup", Lists.newArrayList(programName));
	}

	/**
	 * 重新加载配置。
	 * <p>
	 * 结果包含三个包含进程组名称的数组：
	 * <p>
	 * 已添加给出了已添加的流程组
	 * 已更改提供了内容已更改的流程组
	 * 删除将提供不再在配置中的进程组
	 *
	 * @return
	 * @throws XmlRpcException
	 */
	public Object reloadConfig() throws XmlRpcException {
		return client.execute("supervisor.reloadConfig", Lists.newArrayList());
	}


	/**
	 * supervisord当前状态
	 * <p>
	 * statecode	statename	Description
	 * 2			FATAL		Supervisor has experienced a serious error.
	 * 1			RUNNING		Supervisor is working normally.
	 * 0			RESTARTING	Supervisor is in the process of restarting.
	 * -1			SHUTDOWN	Supervisor is in the process of shutting down.
	 *
	 * @return
	 * @throws XmlRpcException
	 */
	public SupervisordStateDTO getState() throws XmlRpcException {
		Object result = client.execute("supervisor.getState", Lists.newArrayList());
		return JSON.parseObject(JSON.toJSONString(result), SupervisordStateDTO.class);
	}

	/**
	 * 请求(长度)字节来自(名称)的异常日志，从(偏移量)开始。
	 *
	 * 如果总的日志大小大于(偏移量+长度)，则设置溢出标志,并自动增加(偏移量)以将缓冲区定位在日志的末尾。如果可用字节小于(长度)，
	 * 则返回可用字节的最大数目。(偏移量)返回的总是log +1中的最后一个偏移量。
	 *
	 * @param programName
	 * @param offset
	 * @param length
	 * @return
	 * @throws XmlRpcException
	 */
	public ProcessStdLog tailProcessStderrLog(String programName, int offset, int length) throws XmlRpcException {
		return tailProcessStdLog(programName, offset, length, "supervisor.tailProcessStderrLog");
	}

	/**
	 *
	 * 请求(长度)字节来自(名称)的日志，从(偏移量)开始。
	 * 如果总的日志大小大于(偏移量+长度)，则设置溢出标志，并自动增加(偏移量)以将缓冲区定位在日志的末尾。
	 * 如果可用字节小于(长度)，则返回可用字节的最大数目。(偏移量)返回的总是log +1中的最后一个偏移量。
	 * @param programName
	 * @param offset
	 * @param length
	 * @return
	 * @throws XmlRpcException
	 */
	public ProcessStdLog tailProcessStdoutLog(String programName, int offset, int length) throws XmlRpcException {
		return tailProcessStdLog(programName, offset, length, "supervisor.tailProcessStdoutLog");
	}


	/**
	 * 从名称的stdout日志的偏移位置开始读取长度字节
	 * @param programName
	 * @param offset
	 * @param length
	 * @return
	 * @throws XmlRpcException
	 */
	public String readProcessStdoutLog(String programName, int offset, int length) throws XmlRpcException {
		return (String)client.execute("readProcessStdoutLog", Lists.newArrayList(programName, offset, length));
	}

	/**
	 * 从名称的stderr日志的偏移位置开始读取长度字节
	 * @param programName
	 * @param offset
	 * @param length
	 * @return
	 * @throws XmlRpcException
	 */
	public String readProcessStderrLog(String programName, int offset, int length) throws XmlRpcException {
		return (String)client.execute("readProcessStderrLog", Lists.newArrayList(programName, offset, length));
	}

	private ProcessStdLog tailProcessStdLog(String programName, int offset, int length, String s) throws XmlRpcException {
		Object[] results = (Object[]) client.execute(s, Lists.newArrayList(programName, offset, length));
		String log = (String) results[0];
		int logOffset = (Integer) results[1];
		boolean overflow = (Boolean) results[2];

		ProcessStdLog processStdLog = new ProcessStdLog();
		processStdLog.setLog(log);
		processStdLog.setLogOffset(logOffset);
		processStdLog.setOverflow(overflow);
		return processStdLog;
	}
}

