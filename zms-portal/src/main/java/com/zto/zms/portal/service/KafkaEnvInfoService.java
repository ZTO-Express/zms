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
import com.zto.zms.service.domain.kafka.KafkaEnvInfoDTO;
import com.zto.zms.service.domain.kafka.KafkaEnvInfoQueryVO;
import com.zto.zms.service.repository.KafkaEnvInfoRepository;
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
 * Created by liangyong on 2018/10/10.
 */

@Service
public class KafkaEnvInfoService {

    @Autowired
    private KafkaEnvInfoRepository kafkaEnvInfoRepository;
    @Autowired
    private ExecutorService executorService;


    public List<KafkaEnvInfoDTO> queryMaxKafkaEnvInfo(KafkaEnvInfoQueryVO queryVo) {
        List<KafkaEnvInfoDTO> resultLst = Lists.newArrayList();
        try {
            List<Future<List<KafkaEnvInfoDTO>>> futures = new ArrayList<>();
            if (StringUtils.isNotEmpty(queryVo.getBrokerNames())) {
                Arrays.asList(queryVo.getBrokerNames().split(",")).forEach(brokerName -> {
                    Future<List<KafkaEnvInfoDTO>> future = executorService.submit(() ->
                            kafkaEnvInfoRepository.queryMaxKafkaEnvInfo(queryVo, brokerName, DateUtils.intervalTime(queryVo.getBeginTime(), queryVo.getEndTime())).stream()
                                    .filter(item -> StringUtils.isNotBlank(item.getBrokerName()) && StringUtils.isNotBlank(item.getClusterName()))
                                    .map(item -> new KafkaEnvInfoDTO(brokerName, item.getTime().getEpochSecond(), item.getName(), item.getValue()))
                                    .collect(Collectors.toList())
                    );
                    futures.add(future);
                });
            }
            for (Future<List<KafkaEnvInfoDTO>> future : futures) {
                resultLst.addAll(future.get());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw ZmsException.FUTURE_GET_EXCEPTION;
        }
        return resultLst;
    }

    public List<KafkaEnvInfoDTO> queryLatestKafkaEnvInfo(KafkaEnvInfoQueryVO queryVo) {
        List<KafkaEnvInfoDTO> resultLst = Lists.newArrayList();
        try {
            List<Future<List<KafkaEnvInfoDTO>>> futures = new ArrayList<>();
            if (StringUtils.isNotEmpty(queryVo.getBrokerNames())) {
                Arrays.asList(queryVo.getBrokerNames().split(",")).forEach(brokerName -> {
                    Future<List<KafkaEnvInfoDTO>> future = executorService.submit(() ->
                            this.kafkaEnvInfoRepository.queryLatestKafkaEnvInfo(queryVo, brokerName).stream()
                                    .map(item -> new KafkaEnvInfoDTO(brokerName, item.getTime().getEpochSecond(), item.getName(), item.getValue()))
                                    .collect(Collectors.toList())
                    );
                    futures.add(future);
                });
            }
            for (Future<List<KafkaEnvInfoDTO>> future : futures) {
                resultLst.addAll(future.get());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw ZmsException.FUTURE_GET_EXCEPTION;
        }
        return resultLst;
    }

}

