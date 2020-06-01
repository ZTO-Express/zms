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


import com.google.common.base.Joiner;
import com.zto.zms.portal.dto.MigrationDTO;
import com.zto.zms.portal.filter.Operation;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.MigrationService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 集群迁移 主题迁移 消费迁移
 */
@RestController
@RequestMapping("/api/migrate")
public class MigrationController {

    public static final Logger logger = LoggerFactory.getLogger(MigrationController.class);

    @Autowired
    MigrationService migrationService;

    /**
     * 主题迁移或者消费组迁移
     */
    @Operation(value = "主题迁移/消费组迁移", isAdmin = true)
    @GetMapping(value = "/migrateZk")
    public Result<String> migrateZk(MigrationDTO dto) {
        try {
            if (dto.getTargetClusterId() == null) {
                return Result.error("401", "targetClusterId required");
            }
            if (StringUtils.isEmpty(dto.getTopics()) && StringUtils.isEmpty(dto.getConsumers())) {
                return Result.error("401", "topics or consumers required");
            }
            List<String> resultList = migrationService.migrateZk(dto);
            if (!CollectionUtils.isEmpty(resultList)) {
                return Result.error("401", "migrate failure, resultList=" + Joiner.on(",").join(resultList));
            }
            return Result.success("success");
        } catch (Exception ex) {
            logger.error("migrateZk error", ex);
            return Result.error("401", ex.getMessage());
        }
    }

    /**
     * 集群迁移Topic类型
     */
    @Operation(value = "集群迁移主题类型", isAdmin = true)
    @GetMapping(value = "/migrateClusterTopicZk")
    public Result<String> migrateClusterTopicZk(MigrationDTO dto) {
        try {
            if (dto.getTargetClusterId() == null) {
                return Result.error("401", "targetClusterId required");
            }
            if (dto.getSrcClusterId() == null) {
                return Result.error("401", "srcClusterId required");
            }
            List<String> resultList = migrationService.migrateClusterTopicZk(dto);
            if (!CollectionUtils.isEmpty(resultList)) {
                return Result.error("401", "cluster migrate failure, resultList=" + Joiner.on(",").join(resultList));
            }
            return Result.success("success");
        } catch (Exception ex) {
            logger.error("migrateClusterTopicZk error", ex);
            return Result.error("401", ex.getMessage());
        }
    }

    /**
     * 集群迁移consumerGroup类型
     */
    @Operation(value = "集群迁移消费组类型", isAdmin = true)
    @GetMapping(value = "/migrateClusterConsumerZk")
    public Result<String> migrateClusterConsumerZk(MigrationDTO dto) {
        try {
            if (dto.getTargetClusterId() == null) {
                return Result.error("401", "targetClusterId required");
            }
            if (dto.getSrcClusterId() == null) {
                return Result.error("401", "srcClusterId required");
            }
            List<String> resultList = migrationService.migrateClusterConsumerZk(dto);
            if (!CollectionUtils.isEmpty(resultList)) {
                return Result.error("401", "cluster migrate failure, resultList=" + Joiner.on(",").join(resultList));
            }
            return Result.success("success");
        } catch (Exception ex) {
            logger.error("migrateClusterConsumerZk error", ex);
            return Result.error("401", ex.getMessage());
        }
    }

}

