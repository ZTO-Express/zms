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

import com.zto.zms.service.domain.InfluxTagDTO;
import com.zto.zms.service.domain.topic.StatisticTopicConsumerDTO;
import com.zto.zms.service.domain.topic.StatisticTopicConsumerQueryVO;
import com.zto.zms.service.repository.StatisticTopicConsumerRepository;
import com.zto.zms.service.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liangyong on 2018/10/12.
 */

@Service
public class StatisticTopicConsumerService {

    @Autowired
    StatisticTopicConsumerRepository statisticTopicConsumerRepository;


    public List<StatisticTopicConsumerDTO> queryMaxStatisticTopicConsumerInfo(StatisticTopicConsumerQueryVO queryVo) {
        return this.statisticTopicConsumerRepository.queryMaxStatisticTopicConsumerInfo(queryVo, DateUtils.intervalTime(queryVo.getBeginTime(), queryVo.getEndTime())).stream()
                .filter(item -> StringUtils.isNotBlank(item.getClientName()) && StringUtils.isNotBlank(item.getType()))
                .map(item -> new StatisticTopicConsumerDTO(item.getTime().getEpochSecond(), item.getClientName(), item.getIp(), item.getName(), item.getValue()))
                .collect(Collectors.toList());
    }

    public List<StatisticTopicConsumerDTO> queryLatestStatisticTopicConsumerInfo(StatisticTopicConsumerQueryVO queryVo) {
        return this.statisticTopicConsumerRepository.queryLatestStatisticTopicConsumerInfo(queryVo).stream()
                .map(item -> new StatisticTopicConsumerDTO(item.getTime().getEpochSecond(), item.getClientName(), item.getIp(), item.getName(), item.getValue()))
                .collect(Collectors.toList());
    }

    public List<InfluxTagDTO> queryTopicConsumerTagInfo(String tagName, String clientName) {
        return this.statisticTopicConsumerRepository.queryTopicConsumerTagInfo(tagName, clientName).stream()
                .map(item -> new InfluxTagDTO(item.getValue()))
                .collect(Collectors.toList());
    }

}

