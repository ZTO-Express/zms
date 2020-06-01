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
import com.zto.zms.portal.service.StatisticTopicProducerService;
import com.zto.zms.portal.service.topic.TopicStatisticsService;
import com.zto.zms.service.domain.InfluxTagDTO;
import com.zto.zms.service.domain.rocketmq.MqTopicInfoQueryVO;
import com.zto.zms.service.domain.topic.StatisticTopicProducerDTO;
import com.zto.zms.service.domain.topic.StatisticTopicProducerQueryVO;
import com.zto.zms.service.domain.topic.TopicInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyong on 2018/10/12.
 */

@RestController
@RequestMapping("/api/statistics")
public class TopicProducerController {

    @Autowired
    private StatisticTopicProducerService statisticTopicProducerService;

    @Autowired
    private TopicStatisticsService topicStatisticsService;

    /**
     * topic 指标
     *
     * @param queryVo
     * @return
     */
    @GetMapping(value = "/queryTopicMetrics")
    public Result<List<TopicInfoDTO>> queryTopicMetrics(MqTopicInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getTopicName())) {
            return Result.error("-1", "clusterName and topicName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }

        List<TopicInfoDTO> resultList = topicStatisticsService.queryTopicMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryProducerClients")
    public Result<List<InfluxTagDTO>> queryProducerClients(StatisticTopicProducerQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClientName())) {
            return Result.error("-1", "clientName required");
        }
        return Result.success(this.statisticTopicProducerService.queryTopicProducerTagInfo("ip", queryVo.getClientName()));
    }

    @GetMapping(value = "/queryProducerSuccessRate")
    public Result<List<StatisticTopicProducerDTO>> queryProducerSuccessRate(StatisticTopicProducerQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClientName())) {
            return Result.error("-1", "clientName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setType("messageSuccessRate");
        List<StatisticTopicProducerDTO> resultLst = getProducerRateMetrics(queryVo);
        return Result.success(resultLst);
    }

    @GetMapping(value = "/queryProducerFailureRate")
    public Result<List<StatisticTopicProducerDTO>> queryProducerFailureRate(StatisticTopicProducerQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClientName())) {
            return Result.error("-1", "clientName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setType("messageFailureRate");
        List<StatisticTopicProducerDTO> resultLst = getProducerRateMetrics(queryVo);
        return Result.success(resultLst);
    }

    private List<StatisticTopicProducerDTO> getProducerRateMetrics(StatisticTopicProducerQueryVO queryVo) {
        List<StatisticTopicProducerDTO> resultLst = new ArrayList<>();
        queryVo.setName("mean");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));

        queryVo.setName("min1rate");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));

        queryVo.setName("min5rate");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));

        queryVo.setName("min15rate");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));
        return resultLst;
    }

    @GetMapping(value = "/querySendCostRate")
    public Result<List<StatisticTopicProducerDTO>> querySendCostRate(StatisticTopicProducerQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClientName())) {
            return Result.error("-1", "clientName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setType("sendCostRate");
        List<StatisticTopicProducerDTO> resultLst = new ArrayList<>();
        queryVo.setName("percent95");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));

        queryVo.setName("percent999");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));
        return Result.success(resultLst);
    }

    @GetMapping(value = "/queryProducerDistributionMetrics")
    public Result<List<StatisticTopicProducerDTO>> queryProducerDistributionMetrics(StatisticTopicProducerQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClientName())) {
            return Result.error("-1", "clientName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setType("distribution");
        List<StatisticTopicProducerDTO> resultLst = getProducerDistributionRateMetrics(queryVo);
        return Result.success(resultLst);
    }

    @GetMapping(value = "/queryProducerMsgBodyMetrics")
    public Result<List<StatisticTopicProducerDTO>> queryProducerMsgBodyMetrics(StatisticTopicProducerQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClientName())) {
            return Result.error("-1", "clientName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setType("msgBody");
        List<StatisticTopicProducerDTO> resultLst = getProducerDistributionRateMetrics(queryVo);
        return Result.success(resultLst);
    }

    private List<StatisticTopicProducerDTO> getProducerDistributionRateMetrics(StatisticTopicProducerQueryVO queryVo) {
        List<StatisticTopicProducerDTO> resultLst = new ArrayList<>();
        queryVo.setName("less1");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));

        queryVo.setName("less5");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));

        queryVo.setName("less10");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));

        queryVo.setName("less50");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));

        queryVo.setName("less100");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));

        queryVo.setName("less500");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));

        queryVo.setName("less1000");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));

        queryVo.setName("more1000");
        resultLst.addAll(this.statisticTopicProducerService.queryMaxStatisticTopicProducerInfo(queryVo));
        resultLst.addAll(this.statisticTopicProducerService.queryLatestStatisticTopicProducerInfo(queryVo));
        return resultLst;
    }


}

