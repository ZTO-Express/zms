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

package com.zto.zms.portal.service.topic;

import com.zto.zms.common.BrokerType;
import com.zto.zms.dal.mapper.ZmsServiceMapper;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.service.KafkaTopicInfoService;
import com.zto.zms.portal.service.MqTopicInfoService;
import com.zto.zms.service.domain.kafka.KafkaTopicInfoQueryVO;
import com.zto.zms.service.domain.rocketmq.MqTopicInfoQueryVO;
import com.zto.zms.service.domain.topic.TopicInfoDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Class: TopicStatisticsService</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/2/3
 **/
@Service
public class TopicStatisticsService {

    @Autowired
    private MqTopicInfoService mqTopicInfoService;

    @Autowired
    private KafkaTopicInfoService kafkaTopicInfoService;

    @Autowired
    private ZmsServiceMapper zmsServiceMapper;

    /**
     * 主题客户端指标
     *
     * @param queryVo
     * @return
     */
    public List<TopicInfoDTO> queryTopicMetrics(MqTopicInfoQueryVO queryVo) {
        ZmsServiceEntity zmsServiceEntity = zmsServiceMapper.getClusterByEnvIdAndName(queryVo.getEnvId(), queryVo.getClusterName());
        List<TopicInfoDTO> resultLst = new ArrayList<>();
        if (BrokerType.ROCKETMQ.name().equals(zmsServiceEntity.getServerType())) {
            queryVo.setName("tps");
            resultLst.addAll(this.mqTopicInfoService.queryMaxTopicInfo(queryVo));
            resultLst.addAll(this.mqTopicInfoService.queryLatestTopicInfo(queryVo));
            return resultLst;
        }
        KafkaTopicInfoQueryVO kafkaQueryVo = new KafkaTopicInfoQueryVO();
        BeanUtils.copyProperties(queryVo, kafkaQueryVo);
        kafkaQueryVo.setAttributeName("MessagesInPerSec");
        kafkaQueryVo.setName("oneMinuteRate");
        kafkaQueryVo.setBrokerName("");
        resultLst.addAll(this.kafkaTopicInfoService.queryMaxKafkaTopicInfo(kafkaQueryVo));
        resultLst.addAll(this.kafkaTopicInfoService.queryLatestKafkaTopicInfo(kafkaQueryVo));
        return resultLst;
    }

}

