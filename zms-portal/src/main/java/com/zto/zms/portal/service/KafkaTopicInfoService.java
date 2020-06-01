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

import com.zto.zms.service.domain.kafka.KafkaTopicInfoQueryVO;
import com.zto.zms.service.domain.topic.TopicInfoDTO;
import com.zto.zms.service.repository.KafkaTopicInfoRepository;
import com.zto.zms.service.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liangyong on 2018/9/26.
 */

@Service
public class KafkaTopicInfoService {

    @Autowired
    KafkaTopicInfoRepository kafkaTopicInfoRepository;

    public List<TopicInfoDTO> queryMaxKafkaTopicInfo(KafkaTopicInfoQueryVO queryVo) {
        return this.kafkaTopicInfoRepository.queryMaxKafkaTopicInfo(queryVo, DateUtils.intervalTime(queryVo.getBeginTime(), queryVo.getEndTime())).stream()
                .filter(item -> StringUtils.isNotBlank(item.getTopicName()) && StringUtils.isNotBlank(item.getClusterName()))
                .map(item -> new TopicInfoDTO(item.getTime().getEpochSecond(), item.getClusterName(),
                        item.getTopicName(), item.getName(), item.getValue()))
                .collect(Collectors.toList());
    }

    public List<TopicInfoDTO> queryLatestKafkaTopicInfo(KafkaTopicInfoQueryVO queryVo) {
        return this.kafkaTopicInfoRepository.queryLatestKafkaInfo(queryVo).stream()
                .map(item -> new TopicInfoDTO(item.getTime().getEpochSecond(), item.getClusterName(),
                        item.getTopicName(), item.getName(), item.getValue()))
                .collect(Collectors.toList());
    }

}

