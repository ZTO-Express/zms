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

package com.zto.zms.portal.dto.serve;

import com.zto.zms.dal.model.ServicePropertyValue;

import java.util.List;

/**
 * 服务配置查询、实例配置查询返回
 *
 * @author sun kai
 * @date 2020/2/13
 */
public class ServicePropertyQueryDTO {

    private Integer id;

    private String serviceType;

    private String instanceType;

    private String propertyName;

    private String propertyGroup;

    private Short isDependencies;

    private String chooseType;

    private String description;

    private String displayGroup;

    private String displayName;

    private String defaultValue;

    private Short isRequired;

    private Short isReadOnly;

    private String valueType;

    private Integer minLen;

    private Integer maxLen;

    private Integer minValue;

    private Integer maxValue;

    private String scope;

    private Integer instanceId;

    private String propertyValueRefId;
    /**
     * 前配置值
     */
    private String currentValue;

    // 配置选项
    private List<ServicePropertyValue> propertyValueList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyGroup() {
        return propertyGroup;
    }

    public void setPropertyGroup(String propertyGroup) {
        this.propertyGroup = propertyGroup;
    }

    public Short getIsDependencies() {
        return isDependencies;
    }

    public void setIsDependencies(Short isDependencies) {
        this.isDependencies = isDependencies;
    }

    public String getChooseType() {
        return chooseType;
    }

    public void setChooseType(String chooseType) {
        this.chooseType = chooseType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayGroup() {
        return displayGroup;
    }

    public void setDisplayGroup(String displayGroup) {
        this.displayGroup = displayGroup;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Short getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Short isRequired) {
        this.isRequired = isRequired;
    }

    public Short getIsReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(Short isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public Integer getMinLen() {
        return minLen;
    }

    public void setMinLen(Integer minLen) {
        this.minLen = minLen;
    }

    public Integer getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(Integer maxLen) {
        this.maxLen = maxLen;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Integer getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    public String getPropertyValueRefId() {
        return propertyValueRefId;
    }

    public void setPropertyValueRefId(String propertyValueRefId) {
        this.propertyValueRefId = propertyValueRefId;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public List<ServicePropertyValue> getPropertyValueList() {
        return propertyValueList;
    }

    public void setPropertyValueList(List<ServicePropertyValue> propertyValueList) {
        this.propertyValueList = propertyValueList;
    }
}
