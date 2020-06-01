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

package com.zto.zms.dal.domain.service;

/**
 * 服务属性配置表 增加是否依赖服务和依赖服务配置值
 *
 * @author sun kai
 * @date 2020/03/03
 */
public class ServicePropertyValueRefDTO {

    private Integer id;

    private Integer serviceId;

    private Integer instanceId;

    private Integer propertyId;

    private Integer propertyValueId;

    private String currentValue;

    private String startedValue;

    private String serviceType;

    private String instanceType;

    private String propertyName;

    private String propertyGroup;

    private String confApiKey;

    private Short isDependencies;

    private String realValue;

    private String valueType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Integer propertyId) {
        this.propertyId = propertyId;
    }

    public Integer getPropertyValueId() {
        return propertyValueId;
    }

    public void setPropertyValueId(Integer propertyValueId) {
        this.propertyValueId = propertyValueId;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getStartedValue() {
        return startedValue;
    }

    public void setStartedValue(String startedValue) {
        this.startedValue = startedValue;
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

    public String getConfApiKey() {
        return confApiKey;
    }

    public void setConfApiKey(String confApiKey) {
        this.confApiKey = confApiKey;
    }

    public Short getIsDependencies() {
        return isDependencies;
    }

    public void setIsDependencies(Short isDependencies) {
        this.isDependencies = isDependencies;
    }

    public String getRealValue() {
        return realValue;
    }

    public void setRealValue(String realValue) {
        this.realValue = realValue;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }
}

