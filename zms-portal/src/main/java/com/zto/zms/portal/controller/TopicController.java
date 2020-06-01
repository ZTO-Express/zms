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

package com.zto.zms.portal.controller;

import com.zto.zms.service.domain.page.PageResult;
import com.zto.zms.client.producer.SendResult;
import com.zto.zms.utils.Assert;
import com.zto.zms.dal.domain.alert.EnvironmentRefDTO;
import com.zto.zms.dal.model.Topic;
import com.zto.zms.portal.dto.GatedDTO;
import com.zto.zms.portal.dto.topic.SendTopicDTO;
import com.zto.zms.portal.dto.topic.TopicDTO;
import com.zto.zms.portal.dto.topic.TopicQueryDTO;
import com.zto.zms.portal.dto.topic.TopicSummaryDTO;
import com.zto.zms.portal.filter.Operation;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.host.HostService;
import com.zto.zms.portal.service.topic.TopicMapperService;
import com.zto.zms.service.domain.topic.BackupTopicMetadataDTO;
import com.zto.zms.service.domain.topic.TopicEnvironmentInfoVo;
import com.zto.zms.utils.HMacVerifyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by liangyong on 2019/3/4.
 */
@RestController
@RequestMapping("/api/topic")
public class TopicController extends BaseController {
    public static final Logger logger = LoggerFactory.getLogger(TopicController.class);

    @Autowired
    private TopicMapperService topicService;

    @Autowired
    private HostService hostService;


    @GetMapping(value = "/getTopicsByCluster")
    public Result<List<Topic>> getTopicsByCluster(@RequestParam(value = "clusterId", required = false) Integer clusterId,
                                                  @RequestParam(value = "envId", required = false) Integer envId,
                                                  @RequestParam(value = "clusterName", required = false) String clusterName,
                                                  @RequestParam(value = "clusterType", required = false) String clusterType,
                                                  @RequestParam(value = "applicant", required = false) String applicant) {
        return Result.success(topicService.getTopicsByCluster(envId, clusterId, clusterName, clusterType, applicant));
    }

    /**
     * 下拉框查询主题，返回主题ID和名称
     * 传入集群服务ID，返回该集群下的所有主题
     *
     * @param clusterId
     * @return
     */
    @GetMapping(value = "/getTopicsByClusterId")
    public Result<List<Topic>> getTopicsByClusterId(@RequestParam(value = "clusterId", required = false) Integer clusterId) {
        return Result.success(topicService.getTopicsByClusterId(clusterId));
    }

    @GetMapping(value = "/querypage")
    public Result<PageResult<TopicDTO>> queryTopicsPage(TopicQueryDTO topicQueryDto) {
        return Result.success(topicService.queryTopicsPage(topicQueryDto));
    }

    @PostMapping(value = "/add")
    public Result<Integer> addTopic(@RequestBody TopicDTO topicDto) {
        if (StringUtils.isEmpty(topicDto.getName())) {
            return Result.error("401", "topic name required");
        }
        if (StringUtils.isEmpty(topicDto.getApplicant())) {
            return Result.error("401", "applicant required");
        }
        if (StringUtils.isEmpty(topicDto.getDomain())) {
            return Result.error("401", "domain required");
        }
        if (topicDto.getTps() == null) {
            return Result.error("401", "send tps required");
        }
        if (topicDto.getMsgszie() == null) {
            return Result.error("401", "msg size required");
        }

        if (CollectionUtils.isEmpty(topicDto.getEnvironments())) {
            return Result.error("401", "environments required");
        }
        return Result.success(topicService.addTopic(topicDto, getCurrentUser().getRealName()));

    }

    @PutMapping(value = "/{id}/update")
    public Result<Integer> updateTopic(@RequestBody TopicDTO topicDto, @PathVariable Long id) {
        if (id == null) {
            return Result.error("401", "topic id required");
        }
        topicDto.setId(id);
        return Result.success(topicService.updateTopic(topicDto, getCurrentUser()));
    }

    @Operation(value = "删除主题")
    @DeleteMapping(value = "/{id}/delete")
    public Result<Integer> deleteTopic(@PathVariable Long id) {
        if (id == null) {
            return Result.error("401", "topic id required");
        }
        return Result.success(topicService.deleteTopic(id));
    }

    @Operation(value = "审批主题", isAdmin = true)
    @PutMapping(value = "/{id}/approve")
    public Result<List<Integer>> approveTopic(@RequestBody TopicDTO topicDto, @PathVariable Long id) {
        if (id == null) {
            return Result.error("401", "topic id required");
        }
        if (StringUtils.isEmpty(topicDto.getName())) {
            return Result.error("401", "topic name required");
        }
        if (topicDto.getPartitions() == null) {
            return Result.error("401", "partitions required");
        }
        if (topicDto.getPartitions() <= 0 || topicDto.getPartitions() > 64) {
            return Result.error("401", "Partitions数量范围须在1到64！");
        }
        topicDto.setId(id);
        return Result.success(topicService.approveTopic(topicDto, getCurrentUser().getRealName()));
    }

    @Operation(value = "同步主题", isAdmin = true)
    @PutMapping(value = "/{id}/syncTopic")
    public Result<Boolean> syncTopic(@RequestBody List<TopicEnvironmentInfoVo> environments, @PathVariable Long id) {
        if (null == id) {
            return Result.error("401", "topic  required");
        }
        if (CollectionUtils.isEmpty(environments)) {
            return Result.error("401", "environment  required");
        }

        return Result.success(topicService.syncTopic(id, environments, getCurrentUser().getRealName()));
    }

    @PostMapping(value = "/backupMetadata")
    public Result<Boolean> backupTopicMetadata(@RequestBody BackupTopicMetadataDTO backupTopicMetadataDTO, String sign) {
        verifySign(backupTopicMetadataDTO, sign);
        topicService.backupTopicMetadata(backupTopicMetadataDTO);
        return Result.success(null);
    }

    @GetMapping(value = "/uniqueCheck")
    public Result<Boolean> uniqueTopicCheck(@RequestParam(value = "name", required = false) String topicName) {
        if (StringUtils.isEmpty(topicName)) {
            return Result.error("401", "topic name required");
        }
        return Result.success(topicService.uniqueTopicCheck(topicName));
    }

    @Operation(value = "主题灰度配置", isAdmin = true)
    @PutMapping(value = "/{id}/updateGated")
    public Result<Integer> updateGated(@RequestBody GatedDTO dto, @PathVariable Long id) {
        if (id == null) {
            return Result.error("401", "topic id required");
        }
        dto.setId(id);
        return Result.success(topicService.updateGated(dto, getCurrentUser().getRealName()));
    }

    @Operation(value = "消息发送")
    @PostMapping(value = "/sendMsg")
    public Result<SendResult> sendMsg(@RequestBody SendTopicDTO dto) {
        if (StringUtils.isEmpty(dto.getTopic())) {
            return Result.error("401", "topic required");
        }
        if (StringUtils.isEmpty(dto.getMsg())) {
            return Result.error("401", "msg required");
        }
        return Result.success(topicService.sendMsg(dto));
    }


    @GetMapping(value = "/{id}/topicSummary")
    public Result<TopicSummaryDTO> topicSummary(@PathVariable Long id, @RequestParam(value = "envId") Integer envId) {
        try {
            if (null == id) {
                return Result.error("401", " topicId at least one required ");
            }
            return Result.success(topicService.topicSummary(envId, id));
        } catch (Exception ex) {
            logger.error("read  topic {} info  on envId {}  failed", id, envId, ex);
            return Result.error("401", ex.getMessage());
        }
    }

    /**
     * 根据主题名称查询关联环境
     */
    @GetMapping(value = "/queryEnvironmentRefByTopicName")
    public Result<List<EnvironmentRefDTO>> queryEnvironmentRefByTopicName(@RequestParam(value = "topicName") String topicName) {
        return Result.success(topicService.queryEnvironmentRefByTopicName(topicName));
    }

    public void verifySign(BackupTopicMetadataDTO backupTopicMetadataDTO, String sign) {
        String token = hostService.getToken(backupTopicMetadataDTO.getCurrentEnvId());
        Assert.that(HMacVerifyUtil.verifySignature(sign, backupTopicMetadataDTO, token), "验证签名不通过");
    }

}

