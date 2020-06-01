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
import com.zto.zms.service.domain.rocketmq.MqBrokerInfoDTO;
import com.zto.zms.service.domain.rocketmq.MqBrokerInfoQueryVO;
import com.zto.zms.service.repository.MqBrokerNumberRepository;
import com.zto.zms.service.util.DateUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by liangyong on 2018/9/26.
 */

@Service
public class MqBrokerInfoService {

    @Autowired
    MqBrokerNumberRepository mqBroekrInfoRepository;
    @Autowired
    private ExecutorService executorService;

    public List<MqBrokerInfoDTO> queryMqBrokerMetrics(MqBrokerInfoQueryVO queryVo) {
        List<MqBrokerInfoDTO> resultLst = Lists.newArrayList();
        try {
            List<Future<List<MqBrokerInfoDTO>>> futures = new ArrayList<>();
            futures.add(executorService.submit(() -> queryMaxMqBrokerInfo(queryVo, "putTps")));
            futures.add(executorService.submit(() -> queryLatestMqBrokerInfo(queryVo, "putTps")));
            futures.add(executorService.submit(() -> queryMaxMqBrokerInfo(queryVo, "getTotalTps")));
            futures.add(executorService.submit(() -> queryLatestMqBrokerInfo(queryVo, "getTotalTps")));
            for (Future<List<MqBrokerInfoDTO>> future : futures) {
                resultLst.addAll(future.get());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw ZmsException.FUTURE_GET_EXCEPTION;
        }
        return resultLst;
    }

    public List<MqBrokerInfoDTO> queryMaxMqBrokerInfo(MqBrokerInfoQueryVO queryVo, String name) {
        return this.mqBroekrInfoRepository.queryMaxMqBrokerInfo(queryVo, name, DateUtils.intervalTime(queryVo.getBeginTime(), queryVo.getEndTime())).stream()
                .filter(item -> StringUtils.isNotBlank(item.getBrokerName()) && StringUtils.isNotBlank(item.getClusterName()))
                .map(item -> new MqBrokerInfoDTO(item.getTime().getEpochSecond(), item.getBrokerName()
                        , item.getClusterName(), item.getName(), item.getValue()))
                .collect(Collectors.toList());
    }

    public List<MqBrokerInfoDTO> queryLatestMqBrokerInfo(MqBrokerInfoQueryVO queryVo, String name) {
        return this.mqBroekrInfoRepository.queryLatestMqBrokerInfo(queryVo, name).stream()
                .map(item -> new MqBrokerInfoDTO(item.getTime().getEpochSecond(), item.getBrokerName()
                        , item.getClusterName(), item.getName(), item.getValue()))
                .collect(Collectors.toList());
    }

    public List<InfluxTagDTO> queryMqBrokerTagInfo(String tagName, String clusterName) {
        return this.mqBroekrInfoRepository.queryMqBrokerTagInfo(tagName, clusterName).stream()
                .map(item -> new InfluxTagDTO(item.getValue()))
                .collect(Collectors.toList());
    }

}

