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

import com.zto.zms.common.BrokerType;
import com.zto.zms.dal.model.Consumer;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.ConsumerService;
import com.zto.zms.portal.service.KafkaTopicInfoService;
import com.zto.zms.portal.service.MqTopicInfoService;
import com.zto.zms.service.domain.kafka.KafkaTopicInfoQueryVO;
import com.zto.zms.service.domain.rocketmq.MqTopicInfoQueryVO;
import com.zto.zms.service.domain.topic.TopicInfoDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyong on 2018/9/26.
 */

@RestController
@RequestMapping("/api/statistics")
public class TopicInfoController {

    @Autowired
    MqTopicInfoService mqTopicInfoService;
    @Autowired
    KafkaTopicInfoService kafkaTopicInfoService;
    @Autowired
    private ConsumerService consumerService;

    /**
     * 新接口 客户端指标
     *
     * @param queryVo
     * @return
     */
    @RequestMapping(value = "/queryTopicMetricsNew")
    public Result<List<TopicInfoDTO>> queryTopicMetricsNew(MqTopicInfoQueryVO queryVo) {
        //获取集群,topic,消费组
        Consumer consumer = consumerService.selectConsumerGroupByEnv(queryVo.getEnvId(), queryVo.getConsumerId());
        if (null == consumer) {
            return null;
        }
        BeanUtils.copyProperties(consumer, queryVo);
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getTopicName())) {
            return Result.error("-1", "clusterName and topicName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        boolean isRocketMq = consumer.getClusterType().equalsIgnoreCase(BrokerType.ROCKETMQ.getName());
        List<TopicInfoDTO> resultLst = new ArrayList<>();
        if (isRocketMq) {
            queryVo.setName("tps");
            resultLst.addAll(this.mqTopicInfoService.queryMaxTopicInfo(queryVo));
            resultLst.addAll(this.mqTopicInfoService.queryLatestTopicInfo(queryVo));
            return Result.success(resultLst);
        }
        KafkaTopicInfoQueryVO kafkaQueryVo = new KafkaTopicInfoQueryVO();
        BeanUtils.copyProperties(queryVo, kafkaQueryVo);
        kafkaQueryVo.setAttributeName("MessagesInPerSec");
        kafkaQueryVo.setName("oneMinuteRate");
        kafkaQueryVo.setBrokerName("");
        resultLst.addAll(this.kafkaTopicInfoService.queryMaxKafkaTopicInfo(kafkaQueryVo));
        resultLst.addAll(this.kafkaTopicInfoService.queryLatestKafkaTopicInfo(kafkaQueryVo));
        return Result.success(resultLst);
    }


}

