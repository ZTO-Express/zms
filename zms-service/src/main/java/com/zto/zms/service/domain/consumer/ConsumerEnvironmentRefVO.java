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

package com.zto.zms.service.domain.consumer;

public class ConsumerEnvironmentRefVO {

    private Integer id;
    private Long consumerId;
    private Integer environmentId;
    private String environmentName;
    private Integer serviceId;
    private String serverName;

    /**
     * cluster名称
     */
    private String clusterName;


    /**
     * cluster类型
     */
    private String clusterType;

    /**
     * 灰度集群
     */
    private String gatedIps;

    private Integer gatedServiceId;

    private String gatedServiceName;


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

    public String getGatedIps() {
        return gatedIps;
    }

    public void setGatedIps(String gatedIps) {
        this.gatedIps = gatedIps;
    }

    public Integer getGatedServiceId() {
        return gatedServiceId;
    }

    public void setGatedServiceId(Integer gatedServiceId) {
        this.gatedServiceId = gatedServiceId;
    }

    public String getGatedServiceName() {
        return gatedServiceName;
    }

    public void setGatedServiceName(String gatedServiceName) {
        this.gatedServiceName = gatedServiceName;
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

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}

