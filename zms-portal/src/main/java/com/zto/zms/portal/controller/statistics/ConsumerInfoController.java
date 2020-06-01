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
import com.zto.zms.portal.service.KafkaConsumerInfoService;
import com.zto.zms.portal.service.MqConsumerInfoService;
import com.zto.zms.service.domain.consumer.ConsumerInfoDTO;
import com.zto.zms.service.domain.consumer.ConsumerInfoQueryVO;
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
public class ConsumerInfoController {

    @Autowired
    MqConsumerInfoService mqConsumerInfoService;
    @Autowired
    KafkaConsumerInfoService kafkaConsumerInfoService;
    @Autowired
    private ConsumerService consumerService;

    /**
     * 新接口 Consumer消费情况
     *
     * @param queryVo
     * @return
     */
    @RequestMapping(value = "/queryConsumerGroupMetricsNew")
    public Result<List<ConsumerInfoDTO>> queryConsumerGroupMetricsNew(ConsumerInfoQueryVO queryVo) {
        //获取集群,topic,消费组
        Consumer consumer = consumerService.selectConsumerGroupByEnv(queryVo.getEnvId(), queryVo.getConsumerId());
        BeanUtils.copyProperties(consumer, queryVo);
        queryVo.setConsumerGroupName(consumer.getName());
        if (StringUtils.isEmpty(queryVo.getClusterName()) || StringUtils.isEmpty(queryVo.getConsumerGroupName())) {
            return Result.error("-1", "clusterName and consumerGroupName required");
        }
        if (null == queryVo.getBeginTime() || null == queryVo.getEndTime()) {
            return Result.error("-1", "beginTime and endTime required");
        }
        List<ConsumerInfoDTO> resultLst = new ArrayList<>();
        boolean isRocketMQ = consumer.getClusterType().equalsIgnoreCase(BrokerType.ROCKETMQ.getName());
        if (isRocketMQ) {
            queryVo.setName("tps");
            resultLst.addAll(mqConsumerInfoService.queryMaxConsumerInfo(queryVo));
            resultLst.addAll(mqConsumerInfoService.queryLatestConsumerInfo(queryVo));
            queryVo.setName("latency");
            resultLst.addAll(mqConsumerInfoService.queryMaxConsumerInfo(queryVo));
            resultLst.addAll(mqConsumerInfoService.queryLatestConsumerInfo(queryVo));
            queryVo.setName("lastConsumingTime");
            resultLst.addAll(mqConsumerInfoService.queryMaxConsumerInfo(queryVo));
            resultLst.addAll(mqConsumerInfoService.queryLatestConsumerInfo(queryVo));
        } else {
            queryVo.setName("tps");
            resultLst.addAll(kafkaConsumerInfoService.queryMaxConsumerInfo(queryVo));
            resultLst.addAll(kafkaConsumerInfoService.queryLatestConsumerInfo(queryVo));
            queryVo.setName("latency");
            resultLst.addAll(kafkaConsumerInfoService.queryMaxConsumerInfo(queryVo));
            resultLst.addAll(kafkaConsumerInfoService.queryLatestConsumerInfo(queryVo));
        }

        return Result.success(resultLst);
    }


}

