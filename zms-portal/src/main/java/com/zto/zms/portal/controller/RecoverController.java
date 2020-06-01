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


import com.zto.zms.portal.dto.MigrationDTO;
import com.zto.zms.portal.filter.Operation;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.RecoverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 集群重建
 */
@RestController
@RequestMapping("/api/recover")
public class RecoverController {

    public static final Logger logger = LoggerFactory.getLogger(RecoverController.class);

    @Autowired
    RecoverService recoverService;

    @Operation(value = "集群重建", isAdmin = true)
    @GetMapping(value = "/clusterRecover")
    public Result<String> clusterRecover(MigrationDTO dto) {
        try {
            if (dto.getTargetClusterId() == null) {
                return Result.error("401", "targetClusterId required");
            }
            if (dto.getSrcClusterId() == null) {
                return Result.error("401", "srcClusterId required");
            }
            recoverService.clusterRecover(dto);
            return Result.success("success");
        } catch (Exception ex) {
            logger.error("clusterRecover error", ex);
            return Result.error("401", ex.getMessage());
        }
    }
}

