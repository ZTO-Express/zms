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

import com.zto.zms.service.domain.consumer.ConsumerInfoDTO;
import com.zto.zms.service.domain.consumer.ConsumerInfoQueryVO;
import com.zto.zms.service.repository.KakfaConsumerNumberRepository;
import com.zto.zms.service.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liangyong on 2018/11/01.
 */

@Service
public class KafkaConsumerInfoService {

    @Autowired
    KakfaConsumerNumberRepository kafkaConsumerNumberRepository;

    public List<ConsumerInfoDTO> queryMaxConsumerInfo(ConsumerInfoQueryVO queryVo) {
        return this.kafkaConsumerNumberRepository.queryMaxConsumerInfo(queryVo, DateUtils.intervalTime(queryVo.getBeginTime(), queryVo.getEndTime())).stream()
                .filter(item -> StringUtils.isNotBlank(item.getConsumerGroup()) && StringUtils.isNotBlank(item.getClusterName()))
                .map(item -> new ConsumerInfoDTO(item.getTime().getEpochSecond(), item.getClusterName(),
                        item.getConsumerGroup(), item.getTopicName(), item.getName(), item.getValue()))
                .collect(Collectors.toList());
    }

    public List<ConsumerInfoDTO> queryLatestConsumerInfo(ConsumerInfoQueryVO queryVo) {
        return this.kafkaConsumerNumberRepository.queryLatestConsumerInfo((queryVo)).stream()
                .map(item -> new ConsumerInfoDTO(item.getTime().getEpochSecond(), item.getClusterName(),
                        item.getConsumerGroup(), item.getTopicName(), item.getName(), item.getValue()))
                .collect(Collectors.toList());
    }

}

