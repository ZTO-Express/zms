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

import java.io.Serializable;
import java.util.Date;

public class Topic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建日期
     */
    private Date createDate;

    /**
     * 修改日期
     */
    private Date modifyDate;

    /**
     * 申请人
     */
    private String applicant;

    /**
     * 申请域
     */
    private String domain;

    /**
     * Topic名称
     */
    private String name;

    /**
     * Kafka partition数量
     */
    private Integer partitions;

    /**
     * 与Cluster的对应关系
     */
    private Long clusterId;


    /**
     * cluster名称
     */
    private String clusterName;

    /**
     * cluster类型
     */
    private String clusterType;

    /**
     * cluster启动地址
     */
    private String clusterBootstraps;

    /**
     * memo
     */
    private String memo;

    /**
     * 与Meta的对应关系
     */
    private Integer createFrom;

    /**
     * 当前状态
     */
    private Integer status;

    /**
     * 每秒最大发送量
     */
    private Integer tps;

    /**
     * 消息体大小
     */
    private Integer msgszie;

    /**
     * 与Cluster的对应关系
     */
    private Long gatedCluster;

    /**
     * cluster名称
     */
    private String gatedIps;

    private String alertEmails;
    /**
     * 申请人工号
     */
    private String applicantNo;

    /**
     * kafka主题副本数
     */
    private Integer replication;

    /**
     * 获取主键
     *
     * @return id - 主键
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键
     *
     * @param id 主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取创建日期
     *
     * @return create_date - 创建日期
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * 设置创建日期
     *
     * @param createDate 创建日期
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * 获取申请人
     *
     * @return applicant - 申请人
     */
    public String getApplicant() {
        return applicant;
    }

    /**
     * 设置申请人
     *
     * @param applicant 申请人
     */
    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    /**
     * 获取申请域
     *
     * @return domain - 申请域
     */
    public String getDomain() {
        return domain;
    }

    /**
     * 设置申请域
     *
     * @param domain 申请域
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * 获取Topic名称
     *
     * @return name - Topic名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置Topic名称
     *
     * @param name Topic名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取Kafka partition数量
     *
     * @return partitions - Kafka partition数量
     */
    public Integer getPartitions() {
        return partitions;
    }

    /**
     * 设置Kafka partition数量
     *
     * @param partitions Kafka partition数量
     */
    public void setPartitions(Integer partitions) {
        this.partitions = partitions;
    }

    /**
     * 获取与Cluster的对应关系
     *
     * @return topic_cluster_id - 与Cluster的对应关系
     */
    public Long getClusterId() {
        return clusterId;
    }

    /**
     * 设置与Cluster的对应关系
     *
     * @param clusterId 与Cluster的对应关系
     */
    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    /**
     * 获取与Meta的对应关系
     *
     * @return topic_meta_id - 与Meta的对应关系
     */
    public Integer getCreateFrom() {
        return createFrom;
    }

    /**
     * 设置与Meta的对应关系
     *
     * @param createFrom 与Meta的对应关系
     */
    public void setCreateFrom(Integer createFrom) {
        this.createFrom = createFrom;
    }

    /**
     * 获取当前状态
     *
     * @return status - 当前状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置当前状态
     *
     * @param status 当前状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取每秒最大发送量
     *
     * @return tps - 每秒最大发送量
     */
    public Integer getTps() {
        return tps;
    }

    /**
     * 设置每秒最大发送量
     *
     * @param tps 每秒最大发送量
     */
    public void setTps(Integer tps) {
        this.tps = tps;
    }

    /**
     * 获取消息体大小
     *
     * @return msgSzie - 消息体大小
     */
    public Integer getMsgszie() {
        return msgszie;
    }

    /**
     * 设置消息体大小
     *
     * @param msgszie 消息体大小
     */
    public void setMsgszie(Integer msgszie) {
        this.msgszie = msgszie;
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

    public String getClusterBootstraps() {
        return clusterBootstraps;
    }

    public void setClusterBootstraps(String clusterBootstraps) {
        this.clusterBootstraps = clusterBootstraps;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
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

    public String getApplicantNo() {
        return applicantNo;
    }

    public void setApplicantNo(String applicantNo) {
        this.applicantNo = applicantNo;
    }

    public Integer getReplication() {
        return replication;
    }

    public void setReplication(Integer replication) {
        this.replication = replication;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id=" + id +
                ", createDate=" + createDate +
                ", modifyDate=" + modifyDate +
                ", applicant='" + applicant + '\'' +
                ", domain='" + domain + '\'' +
                ", name='" + name + '\'' +
                ", partitions=" + partitions +
                ", clusterId=" + clusterId +
                ", clusterName='" + clusterName + '\'' +
                ", createFrom=" + createFrom +
                ", memo=" + memo +
                ", gatedIps=" + gatedIps +
                ", gatedCluster=" + gatedCluster +
                ", status=" + status +
                ", tps=" + tps +
                ", msgszie=" + msgszie +
                ",applicantNo=" + applicantNo +
                '}';
    }
}
