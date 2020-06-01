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

package com.zto.zms.dal.domain.alert;

import java.util.Objects;

/**
 * 告警列表查询返回参数
 * 告警新增修改传参
 *
 * @author sun kai
 * @date 2020/1/14
 */
public class AlertRuleEnvironmentRefDTO {

    private Integer id;
    private Long alertRuleId;
    private Integer environmentId;
    private String environmentName;
    private String serverName;
    private String serverType;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getAlertRuleId() {
        return alertRuleId;
    }

    public void setAlertRuleId(Long alertRuleId) {
        this.alertRuleId = alertRuleId;
    }

    public Integer getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(Integer environmentId) {
        this.environmentId = environmentId;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        Integer envId = (Integer) o;
        return environmentId.equals(envId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(environmentId);
    }

}

