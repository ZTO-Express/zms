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


import java.util.Date;
import java.util.List;

/**
 * 告警列表查询返回参数
 * 告警新增修改传参
 *
 * @author sun kai
 * @date 2020/1/14
 */
public class AlertRuleDTO {

    private Long id;
    private String field;
    private String name;
    private String type;
    private String operator;
    private Integer target;
    private String scope;
    private Integer triggerTimes;
    private String description;
    private String influxSql;
    private String triggerOperator;
    private String cluster;
    private String alertUser;
    private String alertEmail;
    private String alertMobile;
    private String alertDingding;
    private Boolean effect;
    private String effectFrom;
    private String effectTo;
    private Date createDate;
    private Date modifyDate;
    private List<AlertRuleEnvironmentRefDTO> refList;
    private List<EnvironmentRefDTO> environmentRefDtos;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Integer getTarget() {
        return target;
    }

    public void setTarget(Integer target) {
        this.target = target;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Integer getTriggerTimes() {
        return triggerTimes;
    }

    public void setTriggerTimes(Integer triggerTimes) {
        this.triggerTimes = triggerTimes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInfluxSql() {
        return influxSql;
    }

    public void setInfluxSql(String influxSql) {
        this.influxSql = influxSql;
    }

    public String getTriggerOperator() {
        return triggerOperator;
    }

    public void setTriggerOperator(String triggerOperator) {
        this.triggerOperator = triggerOperator;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getAlertUser() {
        return alertUser;
    }

    public void setAlertUser(String alertUser) {
        this.alertUser = alertUser;
    }

    public String getAlertEmail() {
        return alertEmail;
    }

    public void setAlertEmail(String alertEmail) {
        this.alertEmail = alertEmail;
    }

    public String getAlertMobile() {
        return alertMobile;
    }

    public void setAlertMobile(String alertMobile) {
        this.alertMobile = alertMobile;
    }

    public String getAlertDingding() {
        return alertDingding;
    }

    public void setAlertDingding(String alertDingding) {
        this.alertDingding = alertDingding;
    }

    public Boolean getEffect() {
        return effect;
    }

    public void setEffect(Boolean effect) {
        this.effect = effect;
    }

    public String getEffectFrom() {
        return effectFrom;
    }

    public void setEffectFrom(String effectFrom) {
        this.effectFrom = effectFrom;
    }

    public String getEffectTo() {
        return effectTo;
    }

    public void setEffectTo(String effectTo) {
        this.effectTo = effectTo;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public List<AlertRuleEnvironmentRefDTO> getRefList() {
        return refList;
    }

    public void setRefList(List<AlertRuleEnvironmentRefDTO> refList) {
        this.refList = refList;
    }

    public List<EnvironmentRefDTO> getEnvironmentRefDtos() {
        return environmentRefDtos;
    }

    public void setEnvironmentRefDtos(List<EnvironmentRefDTO> environmentRefDtos) {
        this.environmentRefDtos = environmentRefDtos;
    }
}

