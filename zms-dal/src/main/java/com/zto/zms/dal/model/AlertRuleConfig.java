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

package com.zto.zms.dal.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.Date;

public class AlertRuleConfig implements Serializable {

    public static final Logger logger = LoggerFactory.getLogger(AlertRuleConfig.class);

    private Long id;
    private String name;
    private String type;
    private String field;
    private String operator;
    private Integer target;
    private String scope;
    private String cluster;
    private Integer triggerTimes;
    private String triggerOperator;
    private String alertUser;
    private String alertMobile;
    private String alertEmail;
    private String alertDingding;
    private Boolean effect;
    private Date createDate;
    private Date modifyDate;
    private String influxSql;
    private String description;
    private String effectFrom;
    private String effectTo;
    private String alertUserNo;

    public String getRuleKey() {
        return this.getType() + "_" + this.getName() + "_" + this.getField();
    }

    public boolean isInEffectTimePeriod() {
        try {
            LocalTime now = LocalTime.now();
            LocalTime fromTime = LocalTime.parse(effectFrom);
            LocalTime toTime = LocalTime.parse(effectTo);
            return check(now, fromTime, toTime);
        } catch (Throwable ex) {
            logger.error("check time error from {} to {}", effectFrom, effectTo, ex);
            return true;
        }

    }

    public static boolean check(LocalTime now, LocalTime fromTime, LocalTime toTime) {

        if (fromTime.getHour() < toTime.getHour()) {
            if ((now.compareTo(fromTime) >= 0) && (now.compareTo(toTime) <= 0)) {
                return true;
            }
        } else {
            if ((now.compareTo(fromTime) >= 0) || (now.compareTo(toTime) <= 0)) {

                return true;
            }
        }

        return false;

    }

    public Long getId() {
        return id;
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

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
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

    public void setId(Long id) {
        this.id = id;
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getInfluxSql() {
        return influxSql;
    }

    public void setInfluxSql(String influxSql) {
        this.influxSql = influxSql;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getTriggerOperator() {
        return triggerOperator;
    }

    public void setTriggerOperator(String triggerOperator) {
        this.triggerOperator = triggerOperator;
    }

    public String getAlertUser() {
        return alertUser;
    }

    public void setAlertUser(String alertUser) {
        this.alertUser = alertUser;
    }

    public String getAlertMobile() {
        return alertMobile;
    }

    public void setAlertMobile(String alertMobile) {
        this.alertMobile = alertMobile;
    }

    public String getAlertEmail() {
        return alertEmail;
    }

    public void setAlertEmail(String alertEmail) {
        this.alertEmail = alertEmail;
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

    public String getAlertUserNo() {
        return alertUserNo;
    }

    public void setAlertUserNo(String alertUserNo) {
        this.alertUserNo = alertUserNo;
    }

    @Override
    public String toString() {
        return "AlertRule{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", field='" + field + '\'' +
                ", operator='" + operator + '\'' +
                ", target=" + target +
                ", scope='" + scope + '\'' +
                ", cluster='" + cluster + '\'' +
                ", triggerTimes=" + triggerTimes +
                ", triggerOperator='" + triggerOperator + '\'' +
                ", alertUser='" + alertUser + '\'' +
                ", alertMobile='" + alertMobile + '\'' +
                ", alertEmail='" + alertEmail + '\'' +
                ", alertDingding='" + alertDingding + '\'' +
                ", effect=" + effect +
                ", id=" + id +
                ", createDate=" + createDate +
                ", modifyDate=" + modifyDate +
                ", influxSql='" + influxSql + '\'' +
                ", description='" + description + '\'' +
                ", effectFrom='" + effectFrom + '\'' +
                ", effectTo='" + effectTo + '\'' +
                ", alertUserNo='" + alertUserNo + '\'' +
                '}';
    }
}


