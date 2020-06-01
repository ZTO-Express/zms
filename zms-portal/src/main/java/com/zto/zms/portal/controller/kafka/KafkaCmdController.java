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

package com.zto.zms.portal.controller.kafka;


import com.zto.zms.portal.dto.ResetOffsetDTO;
import com.zto.zms.portal.filter.Operation;
import com.zto.zms.portal.kafka.KafkaConsumerSummary;
import com.zto.zms.portal.kafka.KafkaTopicSummary;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.ConsumerService;
import com.zto.zms.portal.service.KafkaTopicService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kafka/cmd")
public class KafkaCmdController {

    public static Logger logger = LoggerFactory.getLogger(KafkaCmdController.class);

    @Autowired
    KafkaTopicService kafkaCmdService;

    @Autowired
    ConsumerService consumerService;

    @Operation(value = "Kafka消费重置", isAdmin = true)
    @RequestMapping(value = "/resetOffset", method = RequestMethod.POST)
    public Result<KafkaConsumerSummary> resetOffset(@RequestBody ResetOffsetDTO resetOffsetDto) {
        KafkaConsumerSummary summary;
        try {
            if ("earliest".equalsIgnoreCase(resetOffsetDto.getType())) {
                consumerService.resetToEarliest(resetOffsetDto.getClusterName(), resetOffsetDto.getConsumerName(), resetOffsetDto.getTopicName());
            } else if ("latest".equalsIgnoreCase(resetOffsetDto.getType())) {
                consumerService.resetToLatest(resetOffsetDto.getClusterName(), resetOffsetDto.getConsumerName(), resetOffsetDto.getTopicName());
            } else if ("offset".equalsIgnoreCase(resetOffsetDto.getType())) {
                consumerService.resetToOffset(resetOffsetDto.getClusterName(), resetOffsetDto.getConsumerName(), resetOffsetDto.getTopicName(), resetOffsetDto.getOffset());
            } else {
                consumerService.resetToTime(resetOffsetDto.getClusterName(), resetOffsetDto.getConsumerName(), resetOffsetDto.getTopicName(), resetOffsetDto.getTime());
            }
            summary = kafkaCmdService.consumerGroupSummary(resetOffsetDto.getClusterName(), resetOffsetDto.getConsumerName());

        } catch (Exception ex) {
            logger.error("reset offset error ", ex);
            return Result.error("500", ex.getMessage());
        }

        return Result.success(summary);
    }

    /**
     * 消费组详情
     *
     * @param envId
     * @param consumerId
     * @return
     */
    @RequestMapping(value = "/consumerSummary", method = RequestMethod.GET)
    public Result<KafkaConsumerSummary> consumerStatusAll(@RequestParam(value = "envId") int envId,
                                                          @RequestParam(value = "consumerId") Long consumerId) {
        try {
            if (0 == envId) {
                return Result.error("401", "envId required ");
            }
            if (null == consumerId) {
                return Result.error("401", "consumerId required");
            }
            return Result.success(kafkaCmdService.consumerGroupSummary(envId, consumerId));
        } catch (Exception ex) {
            logger.error("execute consumerStatusAll command error, clusterName={}, consumerGroup={} , errMsg={}",
                    envId, consumerId, ex);
            return Result.error("401", ex.getMessage());
        }
    }

    @RequestMapping(value = "/topicSummary", method = RequestMethod.GET)
    public Result<KafkaTopicSummary> topicSummary(@RequestParam(value = "clusterName", required = false) String cluster,
                                                  @RequestParam(value = "topicName", required = false) String topic,
                                                  @RequestParam(value = "envId", required = false) Integer evnId) {
        try {
            if (StringUtils.isEmpty(cluster)) {
                return Result.error("401", " cluster at least one required ");
            }
            if (StringUtils.isEmpty(topic)) {
                return Result.error("401", "consumer required ");
            }
            KafkaTopicSummary summary = kafkaCmdService.topicSummary(cluster, topic);
            summary.setEnvId(evnId);
            return Result.success(summary);
        } catch (Exception ex) {
            logger.error("read  topic {} info  on cluster {}  failed", topic, cluster, ex);
            return Result.error("401", ex.getMessage());
        }
    }


}

