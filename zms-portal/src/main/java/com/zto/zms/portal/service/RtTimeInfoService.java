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
import com.zto.zms.service.domain.RtTimeInfoDTO;
import com.zto.zms.service.domain.RtTimeInfoQueryVO;
import com.zto.zms.service.repository.RtTimeInfoRepository;
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
 * Created by liangyong on 2018/10/12.
 */

@Service
public class RtTimeInfoService {

    @Autowired
    RtTimeInfoRepository rtTimeInfoRepository;
    @Autowired
    private ExecutorService executorService;

    public List<RtTimeInfoDTO> queryRtTimeMetrics(RtTimeInfoQueryVO queryVo) {
        List<RtTimeInfoDTO> resultLst = Lists.newArrayList();
        try {
            List<Future<List<RtTimeInfoDTO>>> futures = new ArrayList<>();
            futures.add(executorService.submit(() -> queryMaxRtTimeInfo(queryVo, "result")));
            futures.add(executorService.submit(() -> queryLatestRtTimeInfo(queryVo, "result")));
            futures.add(executorService.submit(() -> queryMaxRtTimeInfo(queryVo, "rt")));
            futures.add(executorService.submit(() -> queryLatestRtTimeInfo(queryVo, "rt")));
            for (Future<List<RtTimeInfoDTO>> future : futures) {
                resultLst.addAll(future.get());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw ZmsException.FUTURE_GET_EXCEPTION;
        }
        return resultLst;
    }

    public List<RtTimeInfoDTO> queryMaxRtTimeInfo(RtTimeInfoQueryVO queryVo, String name) {
        return this.rtTimeInfoRepository.queryMaxRtTimeInfo(queryVo, name, DateUtils.intervalTime(queryVo.getBeginTime(), queryVo.getEndTime())).stream()
                .filter(item -> StringUtils.isNotBlank(item.getBrokerName()) && StringUtils.isNotBlank(item.getClusterName()))
                .map(item -> new RtTimeInfoDTO(item.getClusterName(), item.getBrokerName(), item.getTime().getEpochSecond(), item.getName(), item.getValue()))
                .collect(Collectors.toList());
    }

    public List<RtTimeInfoDTO> queryLatestRtTimeInfo(RtTimeInfoQueryVO queryVo, String name) {
        return this.rtTimeInfoRepository.queryLatestRtTimeInfo(queryVo, name).stream()
                .map(item -> new RtTimeInfoDTO(item.getClusterName(), item.getBrokerName(), item.getTime().getEpochSecond(), item.getName(), item.getValue()))
                .collect(Collectors.toList());
    }

    public List<InfluxTagDTO> queryRtTimeTagInfo(String tagName, String clusterName) {
        return this.rtTimeInfoRepository.queryRtTimeTagInfo(tagName, clusterName).stream()
                .map(item -> new InfluxTagDTO(item.getValue()))
                .collect(Collectors.toList());
    }

}

