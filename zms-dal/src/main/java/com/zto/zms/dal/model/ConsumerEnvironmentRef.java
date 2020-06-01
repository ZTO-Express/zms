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

import java.util.Date;
import java.util.Objects;

/**
 * 消费组与环境集群关联表
 *
 * @author yuhao.zhang
 * @date 2020/1/13
 */
public class ConsumerEnvironmentRef {

    private Integer id;
    private Long consumerId;
    private Integer environmentId;
    private Integer serviceId;
    private String creator;
    private String modifier;
    private Date gmtCreate;
    private Date gmtModified;
    private String environmentName;
    /**
     * cluster名称
     */
    private String clusterName;
    /**
     * cluster类型
     */
    private String clusterType;

    public ConsumerEnvironmentRef() {
    }

    public ConsumerEnvironmentRef(Long consumerId, Integer environmentId) {
        this.consumerId = consumerId;
        this.environmentId = environmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsumerEnvironmentRef that = (ConsumerEnvironmentRef) o;
        return Objects.equals(consumerId, that.consumerId) &&
                Objects.equals(environmentId, that.environmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consumerId, environmentId);
    }

    private Integer gatedServiceId;

    private String gatedIps;

    private ZmsEnvironment zmsEnvironment = new ZmsEnvironment();

    public Integer getGatedServiceId() {
        return gatedServiceId;
    }

    public void setGatedServiceId(Integer gatedServiceId) {
        this.gatedServiceId = gatedServiceId;
    }

    public String getGatedIps() {
        return gatedIps;
    }

    public void setGatedIps(String gatedIps) {
        this.gatedIps = gatedIps;
    }

    public ZmsEnvironment getZmsEnvironment() {
        return zmsEnvironment;
    }

    public void setZmsEnvironment(ZmsEnvironment zmsEnvironment) {
        this.zmsEnvironment = zmsEnvironment;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(Long consumerId) {
        this.consumerId = consumerId;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
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

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }
}

