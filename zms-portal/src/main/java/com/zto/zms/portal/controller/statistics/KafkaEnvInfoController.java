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
import com.zto.zms.portal.service.KafkaEnvInfoService;
import com.zto.zms.service.domain.kafka.KafkaEnvInfoDTO;
import com.zto.zms.service.domain.kafka.KafkaEnvInfoQueryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * kafka JVM详情
 * Created by liangyong on 2018/10/10.
 */

@RestController
@RequestMapping("/api/statistics")
public class KafkaEnvInfoController {

    @Autowired
    KafkaEnvInfoService kafkaEnvInfoService;


    @GetMapping(value = "/queryG1OldGeneration")
    public Result<List<KafkaEnvInfoDTO>> queryG1OldGeneration(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("G1 Old Generation");
        List<KafkaEnvInfoDTO> resultList = getGenerationMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryG1YoungGeneration")
    public Result<List<KafkaEnvInfoDTO>> queryG1YoungGeneration(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("G1 Young Generation");
        List<KafkaEnvInfoDTO> resultList = getGenerationMetrics(queryVo);
        return Result.success(resultList);
    }

    public List<KafkaEnvInfoDTO> getGenerationMetrics(KafkaEnvInfoQueryVO queryVo) {
        List<KafkaEnvInfoDTO> resultList = new ArrayList<>();
        queryVo.setName("collectionTime");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));

        queryVo.setName("collectionCount");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));

        queryVo.setName("lastGcDuration");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));
        return resultList;
    }

    @GetMapping(value = "/queryNetworkProcessorAvgIdlePercent")
    public Result<List<KafkaEnvInfoDTO>> queryNetworkProcessorAvgIdlePercent(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("NetworkProcessorAvgIdlePercent");
        List<KafkaEnvInfoDTO> resultList = new ArrayList<>();
        queryVo.setName("value");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryOperatingSystem")
    public Result<List<KafkaEnvInfoDTO>> queryOperatingSystem(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("OperatingSystem");
        List<KafkaEnvInfoDTO> resultList = new ArrayList<>();
        queryVo.setName("freePhysicalMemorySize");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));

        queryVo.setName("openFileDescriptorCount");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));
        queryVo.setName("processCpuLoad");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));
        queryVo.setName("systemCpuLoad");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));
        queryVo.setName("systemLoadAverage");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));

        queryVo.setName("maxFileDescriptorCount");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));

        return Result.success(resultList);
    }

    @GetMapping(value = "/queryHeapMemoryUsage")
    public Result<List<KafkaEnvInfoDTO>> queryHeapMemoryUsage(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("heapMemoryUsage");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryNonHeapMemoryUsage")
    public Result<List<KafkaEnvInfoDTO>> queryNonHeapMemoryUsage(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("nonHeapMemoryUsage");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryMetaspace")
    public Result<List<KafkaEnvInfoDTO>> queryMetaspace(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("metaspace");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryMetaspacePeak")
    public Result<List<KafkaEnvInfoDTO>> queryMetaspacePeak(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("metaspace_peak");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryGV1SurvivorSpace")
    public Result<List<KafkaEnvInfoDTO>> queryGV1SurvivorSpace(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("g1 survivor space");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryG1SurvivorSpacePeak")
    public Result<List<KafkaEnvInfoDTO>> queryG1SurvivorSpacePeak(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("g1 survivor space_peak");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryCompressedClassSpace")
    public Result<List<KafkaEnvInfoDTO>> queryCompressedClassSpace(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("compressed class space");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryCompressedClassSpacePeak")
    public Result<List<KafkaEnvInfoDTO>> queryCompressedClassSpacePeak(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("compressed class space_peak");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryG1EdenSpace")
    public Result<List<KafkaEnvInfoDTO>> queryG1EdenSpace(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("g1 eden space");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryG1EdenSpacePeak")
    public Result<List<KafkaEnvInfoDTO>> queryG1EdenSpacePeak(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("g1 eden space_peak");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryG1OldGen")
    public Result<List<KafkaEnvInfoDTO>> queryG1OldGen(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("g1 old gen");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryG1OldGenPeak")
    public Result<List<KafkaEnvInfoDTO>> queryG1OldGenPeak(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("g1 old gen_peak");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryCodecache")
    public Result<List<KafkaEnvInfoDTO>> queryCodeCache(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("code cache");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryCodecachePeek")
    public Result<List<KafkaEnvInfoDTO>> queryCodeCachePeek(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("code cache_peak");
        List<KafkaEnvInfoDTO> resultList = getJvmMemoryMetrics(queryVo);
        return Result.success(resultList);
    }

    public List<KafkaEnvInfoDTO> getJvmMemoryMetrics(KafkaEnvInfoQueryVO queryVo) {
        List<KafkaEnvInfoDTO> resultList = new ArrayList<>();
        queryVo.setName("used");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));

        queryVo.setName("commited");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));
        return resultList;
    }

    @GetMapping(value = "/queryThreadInfo")
    public Result<List<KafkaEnvInfoDTO>> queryThreadInfo(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("ThreadInfo");
        List<KafkaEnvInfoDTO> resultList = new ArrayList<>();
        queryVo.setName("threadCount");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));
        queryVo.setName("peakThreadCount");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryMappedInfo")
    public Result<List<KafkaEnvInfoDTO>> queryMappedInfo(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("mapped");
        List<KafkaEnvInfoDTO> resultList = getNioInfoMetrics(queryVo);
        return Result.success(resultList);
    }

    @GetMapping(value = "/queryDirectInfo")
    public Result<List<KafkaEnvInfoDTO>> queryDirectInfo(KafkaEnvInfoQueryVO queryVo) {
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getBrokerNames())) {
            return Result.error("-1", "clusterName and brokerName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        queryVo.setAttributeName("direct");
        List<KafkaEnvInfoDTO> resultList = getNioInfoMetrics(queryVo);
        return Result.success(resultList);
    }

    public List<KafkaEnvInfoDTO> getNioInfoMetrics(KafkaEnvInfoQueryVO queryVo) {
        List<KafkaEnvInfoDTO> resultList = new ArrayList<>();
        queryVo.setName("used");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));

        queryVo.setName("capacity");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));

        queryVo.setName("count");
        resultList.addAll(this.kafkaEnvInfoService.queryMaxKafkaEnvInfo(queryVo));
        resultList.addAll(this.kafkaEnvInfoService.queryLatestKafkaEnvInfo(queryVo));

        return resultList;
    }
}

