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

package com.zto.zms.portal.service;

import com.zto.zms.common.ZmsException;
import com.zto.zms.service.domain.InfluxTagDTO;
import com.zto.zms.service.domain.kafka.KafkaBrokerInfoDTO;
import com.zto.zms.service.domain.kafka.KafkaBrokerInfoQueryVO;
import com.zto.zms.service.repository.KafkaBrokerInfoRepository;
import com.zto.zms.service.util.DateUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by liangyong on 2018/10/09.
 */

@Service
public class KafkaBrokerInfoService {

    @Autowired
    KafkaBrokerInfoRepository kafkaBrokerInfoRepository;
    @Autowired
    private ExecutorService executorService;

    public List<KafkaBrokerInfoDTO> queryMaxKafkaBrokerInfo(KafkaBrokerInfoQueryVO queryVo) {
        List<KafkaBrokerInfoDTO> resultLst = Lists.newArrayList();
        try {
            List<Future<List<KafkaBrokerInfoDTO>>> futures = new ArrayList<>();
            if (StringUtils.isNotEmpty(queryVo.getBrokerNames())) {
                Arrays.asList(queryVo.getBrokerNames().split(",")).forEach(brokerName -> {
                    Future<List<KafkaBrokerInfoDTO>> future = executorService.submit(() ->
                            this.kafkaBrokerInfoRepository.queryMaxKafkaBrokerInfo(queryVo, brokerName, DateUtils.intervalTime(queryVo.getBeginTime(), queryVo.getEndTime())).stream()
                                    .filter(item -> StringUtils.isNotBlank(item.getBrokerName()) && StringUtils.isNotBlank(item.getClusterName()))
                                    .map(item -> new KafkaBrokerInfoDTO(brokerName, item.getTime().getEpochSecond(), item.getName(), item.getValue()))
                                    .collect(Collectors.toList())
                    );
                    futures.add(future);
                });
            }
            for (Future<List<KafkaBrokerInfoDTO>> future : futures) {
                resultLst.addAll(future.get());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw ZmsException.FUTURE_GET_EXCEPTION;
        }
        return resultLst;
    }

    public List<KafkaBrokerInfoDTO> queryLatestKafkaBrokerInfo(KafkaBrokerInfoQueryVO queryVo) {
        List<KafkaBrokerInfoDTO> resultLst = Lists.newArrayList();
        try {
            List<Future<List<KafkaBrokerInfoDTO>>> futures = new ArrayList<>();
            if (StringUtils.isNotEmpty(queryVo.getBrokerNames())) {
                Arrays.asList(queryVo.getBrokerNames().split(",")).forEach(brokerName -> {
                    Future<List<KafkaBrokerInfoDTO>> future = executorService.submit(() ->
                            this.kafkaBrokerInfoRepository.queryLatestKafkaBrokerInfo(queryVo, brokerName).stream()
                                    .map(item -> new KafkaBrokerInfoDTO(brokerName, item.getTime().getEpochSecond(), item.getName(), item.getValue()))
                                    .collect(Collectors.toList())
                    );
                    futures.add(future);
                });
            }
            for (Future<List<KafkaBrokerInfoDTO>> future : futures) {
                resultLst.addAll(future.get());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw ZmsException.FUTURE_GET_EXCEPTION;
        }
        return resultLst;
    }

    public List<InfluxTagDTO> queryKafkaBrokerTagInfo(String tagName, String clusterName) {
        return this.kafkaBrokerInfoRepository.queryKafkaBrokerTagInfo(tagName, clusterName).stream()
                .map(item -> new InfluxTagDTO(item.getValue()))
                .collect(Collectors.toList());
    }

}

