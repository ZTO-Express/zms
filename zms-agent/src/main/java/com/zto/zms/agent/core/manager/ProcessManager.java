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


import com.zto.zms.agent.web.dto.ProcessRequestDTO;
import com.zto.zms.common.ProcessStdLog;
import org.apache.xmlrpc.XmlRpcException;

/**
 * <p>Class: LocalProcessManager</p>
 * <p> Description: 本地进程管理</p>
 *
 * @author lidawei
 * @version 1.0
 * @date 2020/2/24
 */
public interface ProcessManager {


	/**
	 * processRequestDto
	 *
	 * @param processRequestDto
	 * @return
	 * @throws Exception
	 */
	boolean start(ProcessRequestDTO processRequestDto) throws Exception;

	/**
	 * 停止
	 *
	 * @param programName
	 * @return
	 * @throws Exception
	 */
	boolean stop(String programName) throws Exception;


	/**
	 * 读取Stdout日志
	 *
	 * @param programName
	 * @param offset
	 * @param length
	 * @return
	 */
	String readProcessStdoutLog(String programName, int offset, int length) throws Exception;


	/**
	 * 从名称的stderr日志的偏移位置开始读取长度字节
	 *
	 * @param programName
	 * @param offset
	 * @param length
	 * @return
	 * @throws XmlRpcException
	 */
	String readProcessStderrLog(String programName, int offset, int length) throws Exception;

	/**
	 * 请求(长度)字节来自(名称)的日志，从(偏移量)开始。
	 *
	 * @param programName
	 * @param offset
	 * @param length
	 * @return
	 * @throws XmlRpcException
	 */
	public ProcessStdLog tailProcessStdoutLog(String programName, int offset, int length) throws Exception;

	/**
	 * 请求(长度)字节来自(名称)的异常日志，从(偏移量)开始。
	 *
	 * @param programName
	 * @param offset
	 * @param length
	 * @return
	 * @throws XmlRpcException
	 */
	public ProcessStdLog tailProcessStderrLog(String programName, int offset, int length) throws Exception;

}

