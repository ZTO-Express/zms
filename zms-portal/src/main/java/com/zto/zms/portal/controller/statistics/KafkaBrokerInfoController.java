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
import com.zto.zms.portal.service.KafkaBrokerInfoService;
import com.zto.zms.service.domain.InfluxTagDTO;
import com.zto.zms.service.domain.kafka.KafkaBrokerInfoDTO;
import com.zto.zms.service.domain.kafka.KafkaBrokerInfoQueryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Kafka节点详情
 * Created by liangyong on 2018/10/09.
 */
@RestController
@RequestMapping("/api/statistics")
public class KafkaBrokerInfoController {

    @Autowired
    KafkaBrokerInfoService kafkaBrokerInfoService;

    @GetMapping(value = "/queryKafkaBrokerList")
    public Result<List<InfluxTagDTO>> queryKafkaBrokerList(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName())) {
            return Result.error("-1", "clusterName required");
        }
        return Result.success(this.kafkaBrokerInfoService.queryKafkaBrokerTagInfo("brokerName", queryVo.getClusterName()));
    }

    @GetMapping(value = "/queryActiveControllerCount")
    public Result<List<KafkaBrokerInfoDTO>> queryActiveControllerCount(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("ActiveControllerCount");
        List<KafkaBrokerInfoDTO> resultList = new ArrayList<>();
        resultList.addAll(this.kafkaBrokerInfoService.queryMaxKafkaBrokerInfo(queryVo));
        resultList.addAll(this.kafkaBrokerInfoService.queryLatestKafkaBrokerInfo(queryVo));
        return Result.success(resultList);

    }

    @GetMapping(value = "/queryBytesInPerSec")
    public Result<List<KafkaBrokerInfoDTO>> queryBytesInPerSec(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("BytesInPerSec");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryBytesOutPerSec")
    public Result<List<KafkaBrokerInfoDTO>> queryBytesOutPerSec(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("BytesOutPerSec");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryBytesRejectedPerSec")
    public Result<List<KafkaBrokerInfoDTO>> queryBytesRejectedPerSec(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("BytesRejectedPerSec");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryFailedFetchRequestsPerSec")
    public Result<List<KafkaBrokerInfoDTO>> queryFailedFetchRequestsPerSec(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("FailedFetchRequestsPerSec");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryFailedProduceRequestsPerSec")
    public Result<List<KafkaBrokerInfoDTO>> queryFailedProduceRequestsPerSec(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("FailedProduceRequestsPerSec");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryFetchConsumerRequestQueueTimeMs")
    public Result<List<KafkaBrokerInfoDTO>> queryFetchConsumerRequestQueueTimeMs(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("FetchConsumerRequestQueueTimeMs");
        return Result.success(getPercentileMetrics(queryVo));

    }

    @GetMapping(value = "/queryFetchConsumerRequestsPerSec")
    public Result<List<KafkaBrokerInfoDTO>> queryFetchConsumerRequestsPerSec(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("FetchConsumerRequestsPerSec");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryFetchConsumerTotalTimeMs")
    public Result<List<KafkaBrokerInfoDTO>> queryFetchConsumerTotalTimeMs(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("FetchConsumerTotalTimeMs");
        return Result.success(getPercentileMetrics(queryVo));
    }

    @GetMapping(value = "/queryFetchFollowerRequestQueueTimeMs")
    public Result<List<KafkaBrokerInfoDTO>> queryFetchFollowerRequestQueueTimeMs(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("FetchFollowerRequestQueueTimeMs");
        return Result.success(getPercentileMetrics(queryVo));
    }

    @GetMapping(value = "/queryFetchFollowerRequestsPerSec")
    public Result<List<KafkaBrokerInfoDTO>> queryFetchFollowerRequestsPerSec(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("FetchFollowerRequestsPerSec");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryFetchFollowerTotalTimeMs")
    public Result<List<KafkaBrokerInfoDTO>> queryFetchFollowerTotalTimeMs(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("FetchFollowerTotalTimeMs");
        return Result.success(getPercentileMetrics(queryVo));
    }

    @GetMapping(value = "/queryIsrShrinksPerSec")
    public Result<List<KafkaBrokerInfoDTO>> queryIsrShrinksPerSec(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("IsrShrinksPerSec");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryLeaderCount")
    public Result<List<KafkaBrokerInfoDTO>> queryLeaderCount(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("LeaderCount");
        List<KafkaBrokerInfoDTO> resultList = new ArrayList<>();
        queryVo.setName("value");
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryMaxKafkaBrokerInfo(queryVo));
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryLatestKafkaBrokerInfo(queryVo));
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryLeaderElectionRateAndTimeMs")
    public Result<List<KafkaBrokerInfoDTO>> queryLeaderElectionRateAndTimeMs(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("LeaderElectionRateAndTimeMs");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryLogFlushRateAndTimeMs")
    public Result<List<KafkaBrokerInfoDTO>> queryLogFlushRateAndTimeMs(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("LogFlushRateAndTimeMs");
        return Result.success(getPercentileMetrics(queryVo));
    }

    @GetMapping(value = "/queryMessagesInPerSec")
    public Result<List<KafkaBrokerInfoDTO>> queryMessagesInPerSec(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("MessagesInPerSec");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryOfflinePartitionsCount")
    public Result<List<KafkaBrokerInfoDTO>> queryOfflinePartitionsCount(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("OfflinePartitionsCount");
        List<KafkaBrokerInfoDTO> resultList = new ArrayList<>();
        queryVo.setName("value");
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryMaxKafkaBrokerInfo(queryVo));
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryLatestKafkaBrokerInfo(queryVo));
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryPartitionCount")
    public Result<List<KafkaBrokerInfoDTO>> queryPartitionCount(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("PartitionCount");
        List<KafkaBrokerInfoDTO> resultList = new ArrayList<>();
        queryVo.setName("value");
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryMaxKafkaBrokerInfo(queryVo));
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryLatestKafkaBrokerInfo(queryVo));
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryProduceRequestQueueTimeMs")
    public Result<List<KafkaBrokerInfoDTO>> queryProduceRequestQueueTimeMs(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("ProduceRequestQueueTimeMs");
        return Result.success(getPercentileMetrics(queryVo));
    }

    @GetMapping(value = "/queryProduceRequestsPerSec")
    public Result<List<KafkaBrokerInfoDTO>> queryProduceRequestsPerSec(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("ProduceRequestsPerSec");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryProduceTotalTimeMs")
    public Result<List<KafkaBrokerInfoDTO>> queryProduceTotalTimeMs(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("ProduceTotalTimeMs");
        return Result.success(getPercentileMetrics(queryVo));
    }


    @GetMapping(value = "/queryRequestHandlerAvgIdlePercent")
    public Result<List<KafkaBrokerInfoDTO>> queryRequestHandlerAvgIdlePercent(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("RequestHandlerAvgIdlePercent");
        return Result.success(getRateMetrics(queryVo));
    }

    @GetMapping(value = "/queryUncleanLeaderElectionsPerSec")
    public Result<List<KafkaBrokerInfoDTO>> queryUncleanLeaderElectionsPerSec(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("UncleanLeaderElectionsPerSec");
        return Result.success(getRateMetrics(queryVo));
    }


    @GetMapping(value = "/queryUnderReplicatedPartitions")
    public Result<List<KafkaBrokerInfoDTO>> queryUnderReplicatedPartitions(KafkaBrokerInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("UnderReplicatedPartitions");
        return Result.success(getRateMetrics(queryVo));
    }

    public List<KafkaBrokerInfoDTO> getRateMetrics(KafkaBrokerInfoQueryVO queryVo) {
        List<KafkaBrokerInfoDTO> resultList = new ArrayList<>();
        queryVo.setName("oneMinuteRate");
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryMaxKafkaBrokerInfo(queryVo));
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryLatestKafkaBrokerInfo(queryVo));

        queryVo.setName("meanRate");
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryMaxKafkaBrokerInfo(queryVo));
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryLatestKafkaBrokerInfo(queryVo));

        queryVo.setName("fiveMinuteRate");
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryMaxKafkaBrokerInfo(queryVo));
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryLatestKafkaBrokerInfo(queryVo));

        queryVo.setName("fifteenMinuteRate");
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryMaxKafkaBrokerInfo(queryVo));
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryLatestKafkaBrokerInfo(queryVo));
        return resultList;
    }


    public List<KafkaBrokerInfoDTO> getPercentileMetrics(KafkaBrokerInfoQueryVO queryVo) {
        List<KafkaBrokerInfoDTO> resultList = new ArrayList<>();
        queryVo.setName("999thPercentile");
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryMaxKafkaBrokerInfo(queryVo));
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryLatestKafkaBrokerInfo(queryVo));
        queryVo.setName("99thPercentile");
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryMaxKafkaBrokerInfo(queryVo));
        resultList.addAll(KafkaBrokerInfoController.this.kafkaBrokerInfoService.queryLatestKafkaBrokerInfo(queryVo));
        return resultList;
    }


}

