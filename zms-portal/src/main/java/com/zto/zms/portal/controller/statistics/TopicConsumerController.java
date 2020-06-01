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
import com.zto.zms.portal.service.StatisticTopicConsumerService;
import com.zto.zms.service.domain.InfluxTagDTO;
import com.zto.zms.service.domain.topic.StatisticTopicConsumerDTO;
import com.zto.zms.service.domain.topic.StatisticTopicConsumerQueryVO;
import com.zto.zms.service.manager.ZmsContextManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyong on 2018/10/12.
 */

@RestController
@RequestMapping("/api/statistics")
public class TopicConsumerController {

    @Autowired
    StatisticTopicConsumerService statisticTopicConsumerService;

    /**
     * 获取消费组客户端
     *
     * @param queryVo
     * @return
     */
    @GetMapping(value = "/queryConsumerClients")
    public Result<List<InfluxTagDTO>> queryConsumerClients(StatisticTopicConsumerQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClientName())) {
            return Result.error("-1", "clientName required");
        }
        //根据消费组名称查询
        return Result.success(this.statisticTopicConsumerService.queryTopicConsumerTagInfo("ip", queryVo.getClientName()));
    }

    @RequestMapping(value = "/queryConsumeSuccessRate", method = {RequestMethod.GET})
    public Result<List<StatisticTopicConsumerDTO>> queryConsumeSuccessRate(StatisticTopicConsumerQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClientName())) {
            return Result.error("-1", "clientName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        if (0 == queryVo.getEnvId()) {
            return Result.error("-1", "envId required");
        }
        ZmsContextManager.setEnv(queryVo.getEnvId());
        queryVo.setType("consumeSuccessRate");
        List<StatisticTopicConsumerDTO> resultLst = getConsumerRateMetrics(queryVo);
        return Result.success(resultLst);
    }

    @GetMapping(value = "/queryConsumeFailureRate")
    public Result<List<StatisticTopicConsumerDTO>> queryConsumeFailureRate(StatisticTopicConsumerQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClientName())) {
            return Result.error("-1", "clientName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setType("consumeFailureRate");
        List<StatisticTopicConsumerDTO> resultLst = getConsumerRateMetrics(queryVo);
        return Result.success(resultLst);
    }

    private List<StatisticTopicConsumerDTO> getConsumerRateMetrics(StatisticTopicConsumerQueryVO queryVo) {
        List<StatisticTopicConsumerDTO> resultLst = new ArrayList<>();

        queryVo.setName("mean");
        resultLst.addAll(this.statisticTopicConsumerService.queryMaxStatisticTopicConsumerInfo(queryVo));
        resultLst.addAll(this.statisticTopicConsumerService.queryLatestStatisticTopicConsumerInfo(queryVo));

        queryVo.setName("min1rate");
        resultLst.addAll(this.statisticTopicConsumerService.queryMaxStatisticTopicConsumerInfo(queryVo));
        resultLst.addAll(this.statisticTopicConsumerService.queryLatestStatisticTopicConsumerInfo(queryVo));

        queryVo.setName("min5rate");
        resultLst.addAll(this.statisticTopicConsumerService.queryMaxStatisticTopicConsumerInfo(queryVo));
        resultLst.addAll(this.statisticTopicConsumerService.queryLatestStatisticTopicConsumerInfo(queryVo));

        queryVo.setName("min15rate");
        resultLst.addAll(this.statisticTopicConsumerService.queryMaxStatisticTopicConsumerInfo(queryVo));
        resultLst.addAll(this.statisticTopicConsumerService.queryLatestStatisticTopicConsumerInfo(queryVo));
        return resultLst;
    }

    @GetMapping(value = "/queryConsumerUserCostTimeMs")
    public Result<List<StatisticTopicConsumerDTO>> queryConsumerUserCostTimeMs(StatisticTopicConsumerQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClientName())) {
            return Result.error("-1", "clientName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setType("userCostTimeMs");
        List<StatisticTopicConsumerDTO> resultLst = new ArrayList<>();

        queryVo.setName("percent99");
        resultLst.addAll(this.statisticTopicConsumerService.queryMaxStatisticTopicConsumerInfo(queryVo));
        resultLst.addAll(this.statisticTopicConsumerService.queryLatestStatisticTopicConsumerInfo(queryVo));

        queryVo.setName("percent999");
        resultLst.addAll(this.statisticTopicConsumerService.queryMaxStatisticTopicConsumerInfo(queryVo));
        resultLst.addAll(this.statisticTopicConsumerService.queryLatestStatisticTopicConsumerInfo(queryVo));

        return Result.success(resultLst);
    }

}

