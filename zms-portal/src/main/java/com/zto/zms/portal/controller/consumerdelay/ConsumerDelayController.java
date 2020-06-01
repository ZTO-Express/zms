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

package com.zto.zms.portal.controller.consumerdelay;

import com.zto.zms.service.domain.page.PageResult;
import com.zto.zms.portal.dto.consumer.ConsumerQueryDTO;
import com.zto.zms.portal.result.ConsumerSummary;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.monitor.MonitorServiceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuhao.zhang
 * @description
 * @date 2020/4/26
 */
@RestController
@RequestMapping("/api/consumer/delay")
public class ConsumerDelayController {

    @Autowired
    private MonitorServiceManager monitorServiceManager;

    /**
     * 所有 消费延迟列表
     *
     * @param consumerQueryDto
     * @return
     */
    @RequestMapping(value = "/consumerLatenciesByPage", method = RequestMethod.GET)
    public Result<PageResult<ConsumerSummary>> consumerLatenciesByPage(ConsumerQueryDTO consumerQueryDto) {
        return Result.success(monitorServiceManager.getConsumerSummariesByPage(null, consumerQueryDto.getClusterName(),
                consumerQueryDto.getName(), consumerQueryDto.getEnvId(), consumerQueryDto.getCurrentPage(),
                consumerQueryDto.getPageSize(), consumerQueryDto.getAllDelay()));
    }
}

