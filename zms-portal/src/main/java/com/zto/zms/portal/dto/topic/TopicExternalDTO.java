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

package com.zto.zms.portal.dto.topic;

import java.util.Date;

/**
 * 外部系统查询主题返回参数
 *
 * @author sun kai
 * @date 2020/4/11
 */
public class TopicExternalDTO {

    private Long id;
    private Date createDate;
    private Date modifyDate;
    private String applicant;
    private String domain;
    private String name;
    private Integer partitions;
    private Long clusterId;
    private String clusterName;
    private String clusterType;
    private String clusterBootAddress;
    private String memo;
    private Integer status;
    private Integer tps;
    private Integer msgszie;
    private Long gatedCluster;
    private String gatedClusterName;
    private String gatedIps;
    private String alertEmails;

    private String applicantNo;

    public Long getId() {
        return id;
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

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
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

    public Integer getPartitions() {
        return partitions;
    }

    public void setPartitions(Integer partitions) {
        this.partitions = partitions;
    }

    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterBootAddress() {
        return clusterBootAddress;
    }

    public void setClusterBootAddress(String clusterBootAddress) {
        this.clusterBootAddress = clusterBootAddress;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getTps() {
        return tps;
    }

    public void setTps(Integer tps) {
        this.tps = tps;
    }

    public Integer getMsgszie() {
        return msgszie;
    }

    public void setMsgszie(Integer msgszie) {
        this.msgszie = msgszie;
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

    public String getAlertEmails() {
        return alertEmails;
    }

    public void setAlertEmails(String alertEmails) {
        this.alertEmails = alertEmails;
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
}

