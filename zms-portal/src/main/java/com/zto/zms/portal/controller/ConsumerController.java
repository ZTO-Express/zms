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
import com.zto.zms.utils.Assert;
import com.zto.zms.dal.domain.alert.EnvironmentRefDTO;
import com.zto.zms.dal.model.Consumer;
import com.zto.zms.portal.dto.GatedDTO;
import com.zto.zms.portal.dto.consumer.ConsumerDTO;
import com.zto.zms.portal.dto.consumer.ConsumerQueryDTO;
import com.zto.zms.portal.dto.consumer.ConsumerZmsRegisterDTO;
import com.zto.zms.portal.filter.Operation;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.ConsumerMapperService;
import com.zto.zms.portal.service.ConsumerStatusService;
import com.zto.zms.portal.service.host.HostService;
import com.zto.zms.service.domain.consumer.BackupConsumerMetadataDTO;
import com.zto.zms.service.domain.consumer.ConsumerCopyMsgVO;
import com.zto.zms.service.manager.ZmsContextManager;
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
@RequestMapping("/api/consumer")
public class ConsumerController extends BaseController {
    public static final Logger logger = LoggerFactory.getLogger(ConsumerController.class);

    @Autowired
    private ConsumerMapperService consumerService;

    @Autowired
    private ConsumerStatusService consumerStatusService;

    @Autowired
    private HostService hostService;

    /**
     * 下拉框查询消费组，返回消费组ID和名称
     * 传入集群服务ID，返回该集群下的所有消费组
     *
     * @param clusterId
     * @return
     */
    @GetMapping(value = "/getConsumersByClusterId")
    public Result<List<Consumer>> getConsumersByClusterId(@RequestParam(value = "clusterId", required = false) Integer clusterId) {
        try {
            return Result.success(consumerService.getConsumersByClusterId(clusterId));
        } catch (Exception ex) {
            logger.error("getConsumersByClusterId error, clusterId={} , errMsg={}", clusterId, ex);
            return Result.error("401", ex.getMessage());
        }
    }

    /**
     * 新接口 根据集群获取消费者
     *
     * @param clusterId
     * @param clusterType
     * @return
     */
    @RequestMapping(value = "/queryApprovedConsumers", method = {RequestMethod.GET})
    public Result<List<ConsumerDTO>> queryApprovedConsumersByService(@RequestParam(value = "clusterId", required = false) Integer clusterId,
                                                                     @RequestParam(value = "clusterName", required = false) String clusterName,
                                                                     @RequestParam(value = "clusterType", required = false) String clusterType,
                                                                     @RequestParam(value = "topicId", required = false) Long topicId,
                                                                     @RequestParam(value = "topicName", required = false) String topicName,
                                                                     @RequestParam(value = "envId", required = false) Integer envId
    ) {
        try {
            return Result.success(consumerService.queryApprovedConsumersByService(envId, clusterId, clusterName, clusterType, topicId, topicName));
        } catch (Exception ex) {
            logger.error("queryApprovedConsumers error, clusterId={}, clusterType={}, errMsg={}",
                    clusterId, clusterType, ex);
            return Result.error("401", ex.getMessage());
        }
    }

    /**
     * 消费组列表
     *
     * @param topicQueryDto
     * @return
     */
    @RequestMapping(value = "/query", method = {RequestMethod.GET})
    public Result<PageResult<ConsumerDTO>> queryConsumers(ConsumerQueryDTO topicQueryDto) {
        try {
            return Result.success(consumerService.queryConsumersByService(topicQueryDto));
        } catch (Exception ex) {
            logger.error("queryConsumers error", ex);
            return Result.error("401", ex.getMessage());
        }
    }

    /**
     * 新增消费组
     *
     * @param consumerDto
     * @return
     */
    @RequestMapping(value = "/add", method = {RequestMethod.POST})
    public Result<Integer> addConsumer(@RequestBody ConsumerDTO consumerDto) {
        try {
            if (StringUtils.isEmpty(consumerDto.getName())) {
                return Result.error("401", "consumer name required ");
            }
            if (StringUtils.isEmpty(consumerDto.getDomain())) {
                return Result.error("401", "domain required ");
            }
            if (StringUtils.isEmpty(consumerDto.getMemo())) {
                return Result.error("401", "memo required ");
            }
            if (consumerDto.getDelayThreadhold() == null) {
                return Result.error("401", "delayThreadhold required ");
            }
            return Result.success(consumerService.addConsumer(consumerDto));
        } catch (Exception ex) {
            logger.error("addConsumer error", ex);
            return Result.error("401", ex.getMessage());
        }
    }

    /**
     * 编辑消费组
     *
     * @param consumerDto
     * @return
     */
    @RequestMapping(value = "/update", method = {RequestMethod.PUT})
    public Result<Boolean> updateConsumer(@RequestBody ConsumerDTO consumerDto) {
        try {
            if (null == consumerDto.getId()) {
                return Result.error("401", "consumer id required ");
            }
            if (StringUtils.isEmpty(consumerDto.getName())) {
                return Result.error("401", "consumer name required ");
            }
            if (null == consumerDto.getTopicId()) {
                return Result.error("401", "topic id required ");
            }
            if (StringUtils.isEmpty(consumerDto.getDomain())) {
                return Result.error("401", "domain required ");
            }
            if (StringUtils.isEmpty(consumerDto.getMemo())) {
                return Result.error("401", "memo required ");
            }
            if (consumerDto.getDelayThreadhold() == null) {
                return Result.error("401", "delayThreadhold required ");
            }
            return Result.success(consumerService.updateConsumer(consumerDto, getCurrentUser()));
        } catch (Exception ex) {
            logger.error("updateConsumer error", ex);
            return Result.error("401", ex.getMessage());
        }

    }


    /**
     * 消费组名称校验
     *
     * @param consumerDto
     * @return
     */
    @RequestMapping(value = "/uniqueCheck", method = {RequestMethod.POST})
    public Result<Boolean> uniqueCheck(@RequestBody ConsumerDTO consumerDto) {
        try {
            if (StringUtils.isEmpty(consumerDto.getName())) {
                return Result.error("401", "consumer name required ");
            }
            return Result.success(consumerService.uniqueCheck(consumerDto));
        } catch (Exception ex) {
            logger.error("uniqueCheck error", ex);
            return Result.error("401", ex.getMessage());
        }

    }

    /**
     * 消费组审批
     *
     * @param consumerDto
     * @return
     */
    @Operation(value = "消费组审批", isAdmin = true)
    @RequestMapping(value = "/approve", method = {RequestMethod.POST})
    public Result<Boolean> approveConsumer(@RequestBody ConsumerDTO consumerDto) {
        try {
            if (null == consumerDto.getId()) {
                return Result.error("401", "consumer id required ");
            }
            if (StringUtils.isEmpty(consumerDto.getName())) {
                return Result.error("401", "consumer name required ");
            }
            if (StringUtils.isEmpty(consumerDto.getDomain())) {
                return Result.error("401", "domain required ");
            }
            if (StringUtils.isEmpty(consumerDto.getMemo())) {
                return Result.error("401", "memo required ");
            }
            if (consumerDto.getDelayThreadhold() == null) {
                return Result.error("401", "delayThreadhold required ");
            }
            return Result.success(consumerService.approveConsumer(consumerDto));
        } catch (Exception ex) {
            logger.error("approveConsumer error", ex);
            return Result.error("401", ex.getMessage());
        }

    }

    @Operation(value = "消费组同步到多环境", isAdmin = true)
    @RequestMapping(value = "/copyConsumers", method = {RequestMethod.POST})
    public Result<List<ConsumerCopyMsgVO>> copyConsumers(@RequestBody ConsumerDTO consumerDto) {
        try {
            if (CollectionUtils.isEmpty(consumerDto.getEnvIds())) {
                return Result.error("401", "envIds id required ");
            }

            return Result.success(consumerService.copyConsumers(consumerDto.getEnvIds(), consumerDto.getConsumerIds()));
        } catch (Exception ex) {
            logger.error("approveConsumer error", ex);
            return Result.error("401", ex.getMessage());
        }
    }


    @RequestMapping(value = "/backupMetadata", method = {RequestMethod.POST})
    public Result<Boolean> backupConsumerMetadata(@RequestBody BackupConsumerMetadataDTO consumerMetadataDTO, String sign) {
        try {
            verifySign(consumerMetadataDTO, sign);
            consumerService.backupConsumerMetadata(consumerMetadataDTO);
            return Result.success(null);
        } catch (Exception ex) {
            logger.error("approveConsumer error", ex);
            return Result.error("401", ex.getMessage());
        }
    }


    @Operation(value = "消费组删除")
    @RequestMapping(value = "/delete", method = {RequestMethod.POST})
    public Result<Integer> deleteConsumer(@RequestBody ConsumerDTO consumerDto) {
        try {
            if (consumerDto.getId() == null) {
                return Result.error("401", "consumer id required");
            }
            return Result.success(consumerService.deleteConsumer(consumerDto));
        } catch (Exception ex) {
            logger.error("deleteConsumer error", ex);
            return Result.error("401", ex.getMessage());
        }
    }

    /**
     * 配置灰度
     *
     * @param dto
     * @return
     */
    @Operation(value = "消费组配置灰度测试", isAdmin = true)
    @RequestMapping(value = "/updateGated", method = {RequestMethod.POST})
    public Result updateGated(@RequestBody GatedDTO dto) {
        try {
            if (dto.getId() == null) {
                return Result.error("401", "consumer id required");
            }
            if (StringUtils.isEmpty(dto.getName())) {
                return Result.error("401", "consumer name required");
            }
            return Result.success(consumerService.updateGated(dto, getCurrentUser().getRealName()));
        } catch (Exception ex) {
            logger.error("updateGated error", ex);
            return Result.error("401", ex.getMessage());
        }
    }

    /**
     * 从ZMS_ZK获取消费端注册信息
     *
     * @param consumerDto
     * @return
     */
    @RequestMapping(value = "/consumerZmsRegister", method = {RequestMethod.POST})
    public Result<List<ConsumerZmsRegisterDTO>> consumerZmsRegister(@RequestBody ConsumerDTO consumerDto) {
        ZmsContextManager.setEnv(consumerDto.getEnvId());

        try {
            if (StringUtils.isEmpty(consumerDto.getName())) {
                return Result.error("401", "consumer name required");
            }
            return Result.success(consumerStatusService.consumerZmsRegister(consumerDto.getName()));
        } catch (Exception ex) {
            logger.error("get consumer client ip from zms_zk error", ex);
            return Result.error("401", ex.getMessage());
        }
    }

    /**
     * 根据消费组名称查询关联环境
     */
    @GetMapping(value = "/queryEnvironmentRefByConsumerName")
    public Result<List<EnvironmentRefDTO>> queryEnvironmentRefByConsumerName(@RequestParam(value = "consumerName") String consumerName) {
        return Result.success(consumerService.queryEnvironmentRefByConsumerName(consumerName));
    }


    public void verifySign(BackupConsumerMetadataDTO backupConsumerMetadataDTO, String sign) {
        String token = hostService.getToken(backupConsumerMetadataDTO.getCurrentEnvId());
        Assert.that(HMacVerifyUtil.verifySignature(sign, backupConsumerMetadataDTO, token), "验证签名不通过");
    }

}

