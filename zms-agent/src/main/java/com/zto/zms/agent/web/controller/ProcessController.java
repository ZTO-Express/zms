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

package com.zto.zms.agent.web.controller;

import com.zto.zms.utils.Assert;
import com.zto.zms.agent.web.dto.ProcessRequestDTO;
import com.zto.zms.common.ProcessStdLog;
import com.zto.zms.common.Result;
import com.zto.zms.agent.core.manager.ProcessManager;
import com.zto.zms.utils.HMacVerifyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.zto.zms.common.ZmsConst.ZMS_TOKEN;

/**
 * <p>Class: ProcessController</p>
 * <p>Description: 进程处理</p>
 *
 * @author lidawei
 * @date 2020/2/24
 **/
@RestController
@RequestMapping("/api/agent/process")
public class ProcessController {
	@Autowired
	private ProcessManager processManager;
	private String token = System.getProperty(ZMS_TOKEN);

	/**
	 * 启动服务进程
	 *
	 * @param processRequestDto
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/start")
	public Result<Boolean> start(@Validated @RequestBody ProcessRequestDTO processRequestDto, String sign) throws Exception {
		Assert.that(HMacVerifyUtil.verifySignature(sign, processRequestDto, token), "验证签名不通过");
		boolean success = processManager.start(processRequestDto);
		if (success) {
			return Result.success(null);
		}
		return Result.error("401", "启动失败");
	}

	/**
	 * 停止服务进程
	 *
	 * @param processRequestDto
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/stop")
	public Result<Boolean> stop(@RequestBody ProcessRequestDTO processRequestDto, String sign) throws Exception {
		Assert.that(HMacVerifyUtil.verifySignature(sign, processRequestDto, token), "验证签名不通过");
		boolean success = processManager.stop(processRequestDto.getProgramName());
		if (success) {
			return Result.success(null);
		}
		return Result.error("401", "停止服务进程失败");
	}

	@GetMapping("/readProcessStdoutLog")
	public Result<String> readProcessStdoutLog(String programName, int offset, int length) throws Exception {
		return Result.success(processManager.readProcessStdoutLog(programName, offset, length));
	}

	@GetMapping("/readProcessStderrLog")
	public Result<String> readProcessStderrLog(String programName, int offset, int length) throws Exception {
		return Result.success(processManager.readProcessStderrLog(programName, offset, length));
	}

	@GetMapping("/tailProcessStdoutLog")
	public Result<ProcessStdLog> tailProcessStdoutLog(String programName, int offset, int length) throws Exception {
		return Result.success(processManager.tailProcessStdoutLog(programName, offset, length));
	}

	@GetMapping("/tailProcessStderrLog")
	public Result<ProcessStdLog> tailProcessStderrLog(String programName, int offset, int length) throws Exception {
		return Result.success(processManager.tailProcessStderrLog(programName, offset, length));
	}
}

