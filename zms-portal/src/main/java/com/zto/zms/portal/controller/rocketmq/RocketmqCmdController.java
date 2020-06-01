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

package com.zto.zms.portal.controller.rocketmq;


import com.google.common.collect.Lists;
import com.zto.zms.portal.dto.BrokerDataDTO;
import com.zto.zms.portal.dto.ResetOffsetDTO;
import com.zto.zms.portal.dto.UpdatePermRequestDTO;
import com.zto.zms.portal.dto.consumer.ConsumerStatusAllDTO;
import com.zto.zms.portal.dto.topic.TopicInMsgDTO;
import com.zto.zms.portal.filter.Operation;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.rocketmq.StatsAllRequestDto;
import com.zto.zms.portal.rocketmq.StatsAllResultDto;
import com.zto.zms.portal.rocketmq.TopicStatusDto;
import com.zto.zms.portal.service.*;
import com.zto.zms.portal.service.topic.TopicInMsgTopService;
import com.zto.zms.portal.service.topic.TopicStatusService;
import com.zto.zms.portal.service.topic.UpdateTopicPermService;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rocketmq/cmd")
public class RocketmqCmdController {

    public static final Logger logger = LoggerFactory.getLogger(RocketmqCmdController.class);

    @Autowired
    StatsAllService statsAllService;

    @Autowired
    BrokerConfigService brokerConfigService;

    @Autowired
    UpdateTopicPermService updateTopicPermService;

    @Autowired
    ConsumerStatusService consumerStatusService;

    @Autowired
    ClusterListService clusterListService;

    @Autowired
    TopicStatusService topicStatusService;

    @Autowired
    TopicInMsgTopService topicInMsgTopService;

    @Autowired
    ConsumerService consumerService;


    @Operation(value = "主题权限修改", isAdmin = true)
    @RequestMapping(value = "/updatePerm", method = RequestMethod.POST)
    public Result<List<UpdatePermRequestDTO>> updatePerm(@RequestBody UpdatePermRequestDTO updatePermRequestDto) {
        try {
            if (StringUtils.isEmpty(updatePermRequestDto.getClusterName())) {
                return Result.error("401", " cluster at least one required ");
            }
            if (StringUtils.isEmpty(updatePermRequestDto.getTopicName())) {
                return Result.error("401", "topic required ");
            }
            if (updatePermRequestDto.getPerm() != 2 && updatePermRequestDto.getPerm() != 4 && updatePermRequestDto.getPerm() != 6) {
                return Result.error("401", "perm should only be 2, 4 or 6 ");
            }

            updateTopicPermService.execute(updatePermRequestDto.getClusterName(), updatePermRequestDto.getTopicName(), updatePermRequestDto.getBroker(), updatePermRequestDto.getPerm());

            Map<String, TopicConfig> topicConfigs = updateTopicPermService.get(updatePermRequestDto.getClusterName(), updatePermRequestDto.getTopicName());

            List<UpdatePermRequestDTO> dtoList = Lists.newArrayList();
            for (Map.Entry<String, TopicConfig> entry : topicConfigs.entrySet()) {
                UpdatePermRequestDTO dto = new UpdatePermRequestDTO();
                dto.setBroker(entry.getKey());
                dto.setClusterName(updatePermRequestDto.getClusterName());
                dto.setPerm(entry.getValue().getPerm());
                dto.setTopicName(entry.getValue().getTopicName());
                dtoList.add(dto);
            }
            return Result.success(dtoList);
        } catch (Exception ex) {
            logger.error("update {} perm on cluster {} for broker {} to {} failed", updatePermRequestDto.getTopicName(), updatePermRequestDto.getClusterName(), updatePermRequestDto.getBroker(), updatePermRequestDto.getPerm(), ex);

            return Result.error("401", ex.getMessage());
        }
    }

    @Operation(value = "getPerm", isAdmin = true)
    @RequestMapping(value = "/getPerm", method = RequestMethod.GET)
    public Result<List<UpdatePermRequestDTO>> getPerm(@RequestParam(value = "topicName", required = false) String topic,
                                                      @RequestParam(value = "clusterName", required = false) String cluster,
                                                      @RequestParam(value = "envId", required = false) Integer envId) {
        try {
            if (StringUtils.isEmpty(cluster)) {
                return Result.error("401", " cluster at least one required ");
            }
            if (StringUtils.isEmpty(topic)) {
                return Result.error("401", "topic required ");
            }
            Map<String, TopicConfig> topicConfigs = updateTopicPermService.get(cluster, topic);

            List<UpdatePermRequestDTO> dtoList = Lists.newArrayList();
            for (Map.Entry<String, TopicConfig> entry : topicConfigs.entrySet()) {
                UpdatePermRequestDTO dto = new UpdatePermRequestDTO();
                dto.setBroker(entry.getKey());
                dto.setClusterName(cluster);
                dto.setPerm(entry.getValue().getPerm());
                dto.setTopicName(entry.getValue().getTopicName());
                dto.setEnvId(envId);
                dtoList.add(dto);
            }
            return Result.success(dtoList);
        } catch (Exception ex) {
            logger.error("read {} perm on cluster {}  failed", topic, cluster, ex);

            return Result.error("401", ex.getMessage());
        }

    }

    @Operation(value = "statsAll", isAdmin = true)
    @GetMapping(value = "/statsAll")
    public Result<List<StatsAllResultDto>> statsAll(StatsAllRequestDto statsAllRequestDto) {

        try {
            if (StringUtils.isEmpty(statsAllRequestDto.getNamesvr()) && StringUtils.isEmpty(statsAllRequestDto.getClusterName())) {
                return Result.error("401", "namesrv and cluster at least one required ");
            }

            return Result.success(statsAllService.execute(statsAllRequestDto.getClusterName(), statsAllRequestDto.getNamesvr(), statsAllRequestDto.getTopicName(), statsAllRequestDto.getConsumerName()));
        } catch (Exception ex) {
            logger.error("execute statsAll command error,   cluster={},namesrv={},topic={},consumer={}, errMsg={}",
                    statsAllRequestDto.getClusterName(), statsAllRequestDto.getNamesvr(), statsAllRequestDto.getTopicName(), statsAllRequestDto.getConsumerName(), ex);
            return Result.error("401", ex.getMessage());
        }

    }

    @Operation(value = "getBrokerConfig", isAdmin = true)
    @RequestMapping(value = "/getBrokerConfig", method = RequestMethod.GET)
    public Result<Map<String, Object>> getBrokerConfig(@RequestParam(value = "namesrvAddr", required = false) String namesrvAddr,
                                                       @RequestParam(value = "clusterName", required = false) String clusterName,
                                                       @RequestParam(value = "brokerAddr") String brokerAddr) {
        try {
            if (StringUtils.isEmpty(namesrvAddr) && StringUtils.isEmpty(clusterName)) {
                return Result.error("401", "namesrvAddr and clusterName at least one required ");
            }
            if (StringUtils.isEmpty(brokerAddr)) {
                return Result.error("401", "brokerAddr required");
            }
            return Result.success(brokerConfigService.getBrokerConfig(namesrvAddr, clusterName, brokerAddr));
        } catch (Exception ex) {
            logger.error("execute getBrokerConfig command error, namesrvAddr={}, clusterName={}, brokerAddr={}, errMsg={}",
                    namesrvAddr, clusterName, brokerAddr, ex);
            return Result.error("401", ex.getMessage());
        }
    }

    @Operation(value = "consumerStatusAll", isAdmin = true)
    @RequestMapping(value = "/consumerStatusAll", method = RequestMethod.GET)
    public Result<ConsumerStatusAllDTO> consumerStatusAll(@RequestParam(value = "clusterName") String clusterName,
                                                          @RequestParam(value = "consumerName") String consumerName) {
        if (StringUtils.isEmpty(clusterName)) {
            return Result.error("401", "clusterName required ");
        }
        if (StringUtils.isEmpty(consumerName)) {
            return Result.error("401", "consumerGroup required");
        }
        return Result.success(consumerStatusService.consumerStatsAll(clusterName, consumerName));

    }


    /**
     * 消费组详情
     *
     * @param envId
     * @param consumerId
     * @return
     */
    @RequestMapping(value = "/consumerStatusAllNew", method = RequestMethod.GET)
    public Result<ConsumerStatusAllDTO> consumerStatusAll2(@RequestParam(value = "envId") int envId,
                                                           @RequestParam(value = "consumerId") Long consumerId) {
        if (0 == envId) {
            return Result.error("401", "envId required ");
        }
        if (null == consumerId) {
            return Result.error("401", "consumerId required");
        }
        return Result.success(consumerStatusService.consumerStatsAllNew(envId, consumerId));
    }

    @Operation(value = "clusterList", isAdmin = true)
    @RequestMapping(value = "/clusterList", method = RequestMethod.GET)
    public Result<List<BrokerDataDTO>> clusterList(@RequestParam(value = "clusterName") String clusterName) throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        if (StringUtils.isEmpty(clusterName)) {
            return Result.error("401", "clusterName required ");
        }
        return Result.success(clusterListService.clusterList(clusterName));
    }

    @Operation(value = "topicStatus", isAdmin = true)
    @RequestMapping(value = "/topicStatus", method = RequestMethod.GET)
    public Result<List<TopicStatusDto>> topicStatus(@RequestParam(value = "clusterName") String clusterName,
                                                    @RequestParam(value = "topicName") String topicName) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        if (StringUtils.isEmpty(clusterName)) {
            return Result.error("401", "clusterName required ");
        }
        if (StringUtils.isEmpty(topicName)) {
            return Result.error("401", "topicName required ");
        }
        return Result.success(topicStatusService.topicStatus(clusterName, topicName));
    }

    @Operation(value = "topicInMsgTop", isAdmin = true)
    @RequestMapping(value = "/topicInMsgTop", method = RequestMethod.GET)
    public Result<List<TopicInMsgDTO>> topicInMsgTop(@RequestParam(value = "clusterName") String clusterName) {
        if (StringUtils.isEmpty(clusterName)) {
            return Result.error("401", "clusterName required ");
        }
        return Result.success(topicInMsgTopService.topicInMsgTopToday(clusterName));
    }

    @Operation(value = "RocketMQ消费重置", isAdmin = true)
    @RequestMapping(value = "/resetOffset", method = RequestMethod.POST)
    public Result<Boolean> resetOffset(@RequestBody ResetOffsetDTO resetOffsetDto) {
        consumerService.resetToTime(resetOffsetDto.getClusterName(), resetOffsetDto.getConsumerName(), resetOffsetDto.getTopicName(), resetOffsetDto.getTime());
        return Result.success(true);
    }
}

