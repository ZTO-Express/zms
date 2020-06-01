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

package com.zto.zms.portal.controller.statistics;

import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.MqBrokerInfoService;
import com.zto.zms.service.domain.InfluxTagDTO;
import com.zto.zms.service.domain.rocketmq.MqBrokerInfoDTO;
import com.zto.zms.service.domain.rocketmq.MqBrokerInfoQueryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * RocketMQ节点详情
 * Created by liangyong on 2018/9/26.
 */
@RestController
@RequestMapping("/api/statistics")
public class MqBrokerInfoController {

    @Autowired
    MqBrokerInfoService mqBrokerInfoService;

    @GetMapping(value = "/queryMqBrokerList")
    public Result<List<InfluxTagDTO>> queryKafkaBrokerList(MqBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName())) {
            return Result.error("-1", "clusterName required");
        }
        return Result.success(this.mqBrokerInfoService.queryMqBrokerTagInfo("brokerName", queryVo.getClusterName()));
    }


    @GetMapping(value = "/queryMqBrokerMetrics")
    public Result<List<MqBrokerInfoDTO>> queryMqBrokerMetrics(MqBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerName())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        return Result.success(mqBrokerInfoService.queryMqBrokerMetrics(queryVo));
    }


}

