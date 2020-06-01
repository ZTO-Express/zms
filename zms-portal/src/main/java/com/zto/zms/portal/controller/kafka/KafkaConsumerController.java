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

package com.zto.zms.portal.controller.kafka;


import com.zto.zms.common.BrokerType;
import com.zto.zms.service.domain.page.PageResult;
import com.zto.zms.dal.mapper.ConsumerMapper;
import com.zto.zms.portal.dto.consumer.ConsumerQueryDTO;
import com.zto.zms.portal.result.ConsumerSummary;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.monitor.MonitorServiceManager;
import com.zto.zms.service.influx.InfluxdbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka/consumer")
public class KafkaConsumerController {

    public static final Logger logger = LoggerFactory.getLogger(KafkaConsumerController.class);

    @Autowired
    InfluxdbClient influxdbClient;

    @Autowired
    ConsumerMapper consumerMapper;

    @Autowired
    private MonitorServiceManager monitorServiceManager;

    /**
     * kafka延迟分页列表
     *
     * @param consumerQueryDto
     * @return
     */
    @RequestMapping(value = "/consumerLatenciesByPage", method = RequestMethod.GET)
    public Result<PageResult<ConsumerSummary>> consumerLatenciesByPage(ConsumerQueryDTO consumerQueryDto) {
        return Result.success(monitorServiceManager.getConsumerSummariesByPage(BrokerType.KAFKA.getName(),
                consumerQueryDto.getClusterName(),
                consumerQueryDto.getName(), consumerQueryDto.getEnvId(),
                consumerQueryDto.getCurrentPage(), consumerQueryDto.getPageSize(), consumerQueryDto.getAllDelay()));
    }

}

