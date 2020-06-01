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

package com.zto.zms.portal.service.agent;

import com.google.common.collect.Maps;
import com.zto.zms.utils.HttpClient;
import com.zto.zms.portal.dto.ProcessStartDTO;
import com.zto.zms.portal.result.Result;
import com.zto.zms.utils.HMacVerifyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

/**
 * <p>Class: AgentApiService</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/6
 **/
@Service
public class AgentApiService {

    private static final String START_PROCESS = "http://{0}:18080/api/agent/process/start?sign={1}";

    private static final String STOP_PROCESS = "http://{0}:18080/api/agent/process/stop?sign={1}";

    private static final String READ_PROCESS_STDOUT_LOG = "http://{0}:18080/api/agent/process/readProcessStdoutLog";

    private static final String READ_PROCESS_STDERR_LOG = "http://{0}:18080/api/agent/process/readProcessStderrLog";

    private static final String TAIL_PROCESS_STDOUT_LOG = "http://{0}:18080/api/agent/process/tailProcessStdoutLog";

    private static final String TAIL_PROCESS_STDERR_LOG = "http://{0}:18080/api/agent/process/tailProcessStderrLog";

    @Autowired
    private HttpClient httpClientHelper;

    /**
     * 通过agent启动服务进程
     *
     * @param hostIp
     * @param processId
     * @param programName
     * @param programType
     * @param lastProgramName
     * @param token
     * @return
     * @throws IOException
     */
    public Result startProcess(String hostIp, Integer processId, String programName, String programType, String lastProgramName, String token) throws IOException {
        ProcessStartDTO processStartDto = new ProcessStartDTO();
        processStartDto.setLastStartProgramName(lastProgramName);
        processStartDto.setProcessId(processId);
        processStartDto.setProgramName(programName);
        processStartDto.setProgramType(programType);
        String sign = HMacVerifyUtil.generateSign(processStartDto, token);
        String url = MessageFormat.format(START_PROCESS, hostIp, sign);
        return httpClientHelper.post(url, processStartDto, Result.class);
    }

    /**
     * 通过agent停止服务进程
     *
     * @param hostIp
     * @param programName
     * @param token
     * @return
     * @throws IOException
     */
    public Result stopProcess(String hostIp, String programName, String token) throws IOException {
        ProcessStartDTO processStartDto = new ProcessStartDTO();
        processStartDto.setProgramName(programName);
        String sign = HMacVerifyUtil.generateSign(processStartDto, token);
        String url = MessageFormat.format(STOP_PROCESS, hostIp, sign);
        return httpClientHelper.post(url, processStartDto, Result.class);
    }

    /**
     * 读取进程日志
     *
     * @param hostIp
     * @param programName
     * @param offset
     * @param length
     * @return
     * @throws IOException
     */
    public Result readProcessStdoutLog(String hostIp, String programName, int offset, int length) throws IOException {
        return processStdLog(hostIp, programName, offset, length, READ_PROCESS_STDOUT_LOG);
    }

    /**
     * 读取进程错误日志
     *
     * @param hostIp
     * @param programName
     * @param offset
     * @param length
     * @return
     * @throws IOException
     */
    public Result readProcessStderrLog(String hostIp, String programName, int offset, int length) throws IOException {
        return processStdLog(hostIp, programName, offset, length, READ_PROCESS_STDERR_LOG);
    }

    /**
     * 从尾部读取进程日志
     *
     * @param hostIp
     * @param programName
     * @param offset
     * @param length
     * @return
     * @throws IOException
     */
    public Result tailProcessStdoutLog(String hostIp, String programName, int offset, int length) throws IOException {
        return processStdLog(hostIp, programName, offset, length, TAIL_PROCESS_STDOUT_LOG);
    }

    /**
     * 从尾部读取进程错误日志
     *
     * @param hostIp
     * @param programName
     * @param offset
     * @param length
     * @return
     * @throws IOException
     */
    public Result tailProcessStderrLog(String hostIp, String programName, int offset, int length) throws IOException {
        return processStdLog(hostIp, programName, offset, length, TAIL_PROCESS_STDERR_LOG);
    }

    private Result processStdLog(String hostIp, String programName, int offset, int length, String readProcessStdoutLog) throws IOException {
        Map<String, String> params = Maps.newHashMap();
        params.put("programName", programName);
        params.put("offset", String.valueOf(offset));
        params.put("length", String.valueOf(length));
        String url = MessageFormat.format(readProcessStdoutLog, hostIp);
        return httpClientHelper.get(url, params, Result.class);
    }

}

