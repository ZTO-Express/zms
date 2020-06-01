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
import com.zto.zms.dal.domain.alert.AlertRuleDTO;
import com.zto.zms.dal.mapper.AlertRuleDetailMapper;
import com.zto.zms.dal.model.AlertRuleConfig;
import com.zto.zms.portal.filter.Operation;
import com.zto.zms.portal.result.ID;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.AlertService;
import com.zto.zms.service.domain.alert.AlertRuleCheckDTO;
import com.zto.zms.service.domain.alert.QueryAlertVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by liangyong on 2018/9/27.
 */

@RestController
@RequestMapping("/api/alert")
public class AlertController extends BaseController {

    @Autowired
    AlertRuleDetailMapper mapper;

    public static final Logger logger = LoggerFactory.getLogger(AlertController.class);

    @Autowired
    AlertService alertService;

    /**
     * 告警列表查询
     */
    @GetMapping(value = "/queryAlerts")
    public Result<PageResult<AlertRuleDTO>> queryAlerts(QueryAlertVO queryVo) {
        return Result.success(alertService.queryAlerts(queryVo));
    }

    /**
     * 告警名称查询
     */
    @GetMapping(value = "/queryAlertNames")
    public Result<List<String>> queryAlertNames() {
        return Result.success(alertService.queryAlertNames());
    }

    /**
     * 告警新增
     */
    @PostMapping(value = "/addAlert")
    public Result<ID> addAlert(@RequestBody AlertRuleDTO alertRuleDto) {
        if (CollectionUtils.isEmpty(alertRuleDto.getRefList())) {
            return Result.error("500", "环境未选择");
        }
        return alertService.insert(alertRuleDto, getCurrentUser().getRealName());
    }

    /**
     * 告警修改
     */
    @PutMapping(value = "/{id}/updateAlert")
    public Result<ID> updateAlert(@RequestBody AlertRuleDTO alertRuleDto, @PathVariable Long id) {
        if (id == null) {
            return Result.error("500", "alert id required");
        }
        if (CollectionUtils.isEmpty(alertRuleDto.getRefList())) {
            return Result.error("500", "环境未选择");
        }
        alertRuleDto.setId(id);
        return alertService.update(alertRuleDto, getCurrentUser());
    }

    /**
     * 告警删除
     */
    @Operation(value = "删除告警配置", isAdmin = true)
    @DeleteMapping(value = "/deleteAlert/{id}")
    public Result<ID> deleteAlert(@PathVariable Long id) {
        AlertRuleConfig alertRule = new AlertRuleConfig();
        alertRule.setId(id);
        return alertService.delete(alertRule);
    }

    /**
     * 告警名称新增修改时唯一性校验
     */
    @GetMapping(value = "/uniqueCheck")
    public Result<Boolean> uniqueCheck(AlertRuleCheckDTO alertRule) {
        if (StringUtils.isBlank(alertRule.getName())) {
            return Result.error("401", "alert rule name required.");
        }
        if (StringUtils.isBlank(alertRule.getField())) {
            return Result.error("401", "alert rule field required.");
        }
        if (StringUtils.isBlank(alertRule.getType())) {
            return Result.error("401", "alert rule type required.");
        }
        return Result.success(alertService.uniqueCheck(alertRule));
    }


    /**
     * zms-alert服务启动时查询当前环境下所有的告警规则
     */
    @GetMapping(value = "/getEffectAlertRulesByEnv")
    public Result<List<AlertRuleConfig>> getEffectAlertRulesByEnv(@RequestParam(value = "env") String env) {
        return Result.success(alertService.getEffectAlertRulesByEnv(env));
    }

}

