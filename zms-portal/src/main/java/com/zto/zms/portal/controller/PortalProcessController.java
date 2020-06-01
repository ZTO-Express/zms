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

package com.zto.zms.portal.controller;

import com.zto.zms.common.ServiceProcess;
import com.zto.zms.common.RunCommonConfig;
import com.zto.zms.common.ProcessStdLog;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.serve.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * <p>Class: ProcessController</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/4
 **/
@RestController
@RequestMapping("/api/process")
public class PortalProcessController {

    @Autowired
    private ProcessService processService;

    public static final Logger logger = LoggerFactory.getLogger(PortalProcessController.class);


    @GetMapping(value = "/{processId}/runCommonConfig")
    public Result<RunCommonConfig> runCommonConfig(@PathVariable Integer processId) {
        return Result.success(processService.runCommonConfig(processId));
    }

    @GetMapping(value = "/{processId}/serverConfig")
    public void uploadConfig(@PathVariable Integer processId, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=" + processService.getProgramName(processId) + ".tar.gz");
        try {
            processService.configFile(processId, response.getOutputStream());
        } catch (Exception e) {
            throw e;
        }
    }

    @GetMapping(value = "/{processId}/environmentConfig")
    @ResponseBody
    public Result<Map<String, String>> environmentConfig(@PathVariable Integer processId) {
        return Result.success(processService.environmentConfig(processId));
    }

    /**
     * 同步supervisor 服务状态
     */
    @PostMapping(value = "/runningstatus")
    @ResponseBody
    public Result<Boolean> updateRunningStatus(@RequestBody List<ServiceProcess> serviceProcesses) {
        return Result.success(processService.updateRunningStatus(serviceProcesses));
    }

    /**
     * 页面查询进程运行状态
     */
    @GetMapping(value = "/{processIds}/getRunningStatus")
    @ResponseBody
    public Result<List<com.zto.zms.dal.model.ServiceProcess>> getRunningStatus(@PathVariable List<Integer> processIds) {
        return Result.success(processService.getRunningStatus(processIds));
    }

    /**
     * 查询进程异常输出日志
     *
     * @param processId
     * @param offset
     * @param length
     * @return
     * @throws Exception
     */
    @GetMapping("/{processId}/tailProcessStderrLog")
    public Result<ProcessStdLog> tailProcessStderrLog(@PathVariable Integer processId, int offset, int length) throws Exception {
        return Result.success(processService.tailProcessStderrLog(processId, offset, length));
    }

    @GetMapping("/{processId}/tailProcessStdoutLog")
    public Result<ProcessStdLog> tailProcessStdoutLog(@PathVariable Integer processId, int offset, int length) throws Exception {
        return Result.success(processService.tailProcessStdoutLog(processId, offset, length));
    }
}

