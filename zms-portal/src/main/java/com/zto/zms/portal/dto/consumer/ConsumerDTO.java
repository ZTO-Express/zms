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

package com.zto.zms.portal.dto.consumer;

import com.zto.zms.service.domain.consumer.ConsumerEnvironmentRefVO;

import java.util.Date;
import java.util.List;

/**
 * Created by liangyong on 2019/4/1.
 */
public class ConsumerDTO {

    private Long id;
    private Date modifyDate;
    private Long clusterId;
    private String clusterName;
    private String clusterType;
    private String memo;
    private String applicant;
    private Boolean broadcast;
    private String domain;
    private String name;
    private Long topicId;
    private String topicName;
    private Boolean consumerFrom;
    private Integer status;
    private Long gatedCluster;
    private String gatedClusterName;
    private String gatedIps;
    private Long delayThreadhold;
    private int needUpdateServer;
    private int needUpdateZk;
    private String applicantNo;


    private List<ConsumerEnvironmentRefVO> consumerEnvironmentRefVos;
    /**
     * 环境名称
     */
    private String envName;

    private int envId;

    private List<Integer> envIds;
    private List<Long> consumerIds;

    public List<Integer> getEnvIds() {
        return envIds;
    }

    public void setEnvIds(List<Integer> envIds) {
        this.envIds = envIds;
    }

    public List<Long> getConsumerIds() {
        return consumerIds;
    }

    public void setConsumerIds(List<Long> consumerIds) {
        this.consumerIds = consumerIds;
    }

    public List<ConsumerEnvironmentRefVO> getConsumerEnvironmentRefVos() {
        return consumerEnvironmentRefVos;
    }

    public void setConsumerEnvironmentRefVos(List<ConsumerEnvironmentRefVO> consumerEnvironmentRefVos) {
        this.consumerEnvironmentRefVos = consumerEnvironmentRefVos;
    }

    public int getEnvId() {
        return envId;
    }

    public void setEnvId(int envId) {
        this.envId = envId;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
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

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    public Boolean getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(Boolean broadcast) {
        this.broadcast = broadcast;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Boolean getConsumerFrom() {
        return consumerFrom;
    }

    public void setConsumerFrom(Boolean consumerFrom) {
        this.consumerFrom = consumerFrom;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getGatedCluster() {
        return gatedCluster;
    }

    public void setGatedCluster(Long gatedCluster) {
        this.gatedCluster = gatedCluster;
    }

    public String getGatedIps() {
        return gatedIps;
    }

    public void setGatedIps(String gatedIps) {
        this.gatedIps = gatedIps;
    }

    public Long getDelayThreadhold() {
        return delayThreadhold;
    }

    public void setDelayThreadhold(Long delayThreadhold) {
        this.delayThreadhold = delayThreadhold;
    }

    public int getNeedUpdateServer() {
        return needUpdateServer;
    }

    public void setNeedUpdateServer(int needUpdateServer) {
        this.needUpdateServer = needUpdateServer;
    }

    public int getNeedUpdateZk() {
        return needUpdateZk;
    }

    public void setNeedUpdateZk(int needUpdateZk) {
        this.needUpdateZk = needUpdateZk;
    }

    public String getGatedClusterName() {
        return gatedClusterName;
    }

    public void setGatedClusterName(String gatedClusterName) {
        this.gatedClusterName = gatedClusterName;
    }

    public String getApplicantNo() {
        return applicantNo;
    }

    public void setApplicantNo(String applicantNo) {
        this.applicantNo = applicantNo;
    }

    @Override
    public String toString() {
        return "ConsumerDto{" +
                "id=" + id +
                ", modifyDate=" + modifyDate +
                ", clusterId=" + clusterId +
                ", clusterName='" + clusterName + '\'' +
                ", clusterType='" + clusterType + '\'' +
                ", memo='" + memo + '\'' +
                ", applicant='" + applicant + '\'' +
                ", broadcast=" + broadcast +
                ", domain='" + domain + '\'' +
                ", name='" + name + '\'' +
                ", topicId=" + topicId +
                ", topicName='" + topicName + '\'' +
                ", consumerFrom=" + consumerFrom +
                ", status=" + status +
                ", gatedCluster=" + gatedCluster +
                ", gatedClusterName='" + gatedClusterName + '\'' +
                ", gatedIps='" + gatedIps + '\'' +
                ", delayThreadhold=" + delayThreadhold +
                ", needUpdateServer=" + needUpdateServer +
                ", needUpdateZk=" + needUpdateZk +
                '}';
    }
}

