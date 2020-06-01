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
import java.util.List;

public class Consumer implements Serializable {
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
     * memo
     */
    private String memo;

    /**
     * 申请人
     */
    private String applicant;

    /**
     * RocketMQ 是否广播
     */
    private Boolean broadcast;

    /**
     * 申请域
     */
    private String domain;

    /**
     * Consumer名称
     */
    private String name;


    /**
     * 与Topic的对应关系
     */
    private Long topicId;
    /**
     * 与Topic的对应关系
     */

    private String topicName;

    /**
     * ConsumerFrom 是否为earliest
     */
    private Boolean consumerFrom;

    /**
     * 当前状态
     */
    private Integer status;


    /**
     * 与Cluster的对应关系
     */
    private Long gatedCluster;


    /**
     * cluster名称
     */
    private String gatedIps;


    private Integer gatedServiceId;

    /**
     * 堆积告警阈值
     */
    private Long delayThreadhold;


    /**
     * 是否更新server
     */
    private int needUpdateServer;

    /**
     * 是否 更新zk
     */
    private int needUpdateZk;
    /**
     * 申请人工号
     */
    private String applicantNo;


    private static final long serialVersionUID = 1L;


    /**
     * 环境列表
     */
    private List<ConsumerEnvironmentRef> envs;

    private int environmentId;

    public Integer getGatedServiceId() {
        return gatedServiceId;
    }

    public void setGatedServiceId(Integer gatedServiceId) {
        this.gatedServiceId = gatedServiceId;
    }

    public int getEnvironmentId() {
        return environmentId;
    }

    public void setEnvironmentId(int environmentId) {
        this.environmentId = environmentId;
    }

    public List<ConsumerEnvironmentRef> getEnvs() {
        return envs;
    }

    public void setEnvs(List<ConsumerEnvironmentRef> envs) {
        this.envs = envs;
    }

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
     * 获取RocketMQ 是否广播
     *
     * @return broadcast - RocketMQ 是否广播
     */
    public Boolean getBroadcast() {
        return broadcast;
    }

    /**
     * 设置RocketMQ 是否广播
     *
     * @param broadcast RocketMQ 是否广播
     */
    public void setBroadcast(Boolean broadcast) {
        this.broadcast = broadcast;
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
     * 获取Consumer名称
     *
     * @return name - Consumer名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置Consumer名称
     *
     * @param name Consumer名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取与Topic的对应关系
     *
     * @return topic_id - 与Topic的对应关系
     */
    public Long getTopicId() {
        return topicId;
    }

    /**
     * 设置与Topic的对应关系
     *
     * @param topicId 与Topic的对应关系
     */
    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    /**
     * 获取ConsumerFrom 是否为earliest
     *
     * @return consumer_from - ConsumerFrom 是否为earliest
     */
    public Boolean getConsumerFrom() {
        return consumerFrom;
    }

    /**
     * 设置ConsumerFrom 是否为earliest
     *
     * @param consumerFrom ConsumerFrom 是否为earliest
     */
    public void setConsumerFrom(Boolean consumerFrom) {
        this.consumerFrom = consumerFrom;
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
     * 获取堆积告警阈值
     *
     * @return delay_threadhold - 堆积告警阈值
     */
    public Long getDelayThreadhold() {
        return delayThreadhold;
    }

    /**
     * 设置堆积告警阈值
     *
     * @param delayThreadhold 堆积告警阈值
     */
    public void setDelayThreadhold(Long delayThreadhold) {
        this.delayThreadhold = delayThreadhold;
    }


    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
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

    public String getApplicantNo() {
        return applicantNo;
    }

    public void setApplicantNo(String applicantNo) {
        this.applicantNo = applicantNo;
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

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "Consumer{" +
                "id=" + id +
                ", createDate=" + createDate +
                ", modifyDate=" + modifyDate +
                ", clusterId=" + clusterId +
                ", clusterName='" + clusterName + '\'' +
                ", applicant='" + applicant + '\'' +
                ", broadcast=" + broadcast +
                ", domain='" + domain + '\'' +
                ", name='" + name + '\'' +
                ", topicId=" + topicId +
                ", topicName='" + topicName + '\'' +
                ", memo='" + memo + '\'' +
                ", gatedIps='" + gatedIps + '\'' +
                ", gatedCluster='" + gatedCluster + '\'' +
                ", consumerFrom=" + consumerFrom +
                ", status=" + status +
                ", needUpdateServer=" + needUpdateServer +
                ", needUpdateZk=" + needUpdateZk +
                ", delayThreadhold=" + delayThreadhold +
                ", applicantNo=" + applicantNo +
                '}';
    }
}
