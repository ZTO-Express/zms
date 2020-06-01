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

package com.zto.zms.portal.dto;

/**
 * 集群迁移、主题迁移、消费迁移、集群重建传参
 *
 * @author sun kai
 * @date 2020/1/17
 */
public class MigrationDTO {

    /**
     * 主题ID，逗号分隔
     */
    private String topics;
    /**
     * 消费组ID，逗号分隔
     */
    private String consumers;
    private Integer srcClusterId;
    private Integer targetClusterId;
    private String consumePosition;
    private String srcCluster;
    private String targetCluster;
    private Integer envId;

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public String getConsumers() {
        return consumers;
    }

    public void setConsumers(String consumers) {
        this.consumers = consumers;
    }

    public Integer getSrcClusterId() {
        return srcClusterId;
    }

    public void setSrcClusterId(Integer srcClusterId) {
        this.srcClusterId = srcClusterId;
    }

    public Integer getTargetClusterId() {
        return targetClusterId;
    }

    public void setTargetClusterId(Integer targetClusterId) {
        this.targetClusterId = targetClusterId;
    }

    public String getConsumePosition() {
        return consumePosition;
    }

    public void setConsumePosition(String consumePosition) {
        this.consumePosition = consumePosition;
    }

    public String getSrcCluster() {
        return srcCluster;
    }

    public void setSrcCluster(String srcCluster) {
        this.srcCluster = srcCluster;
    }

    public String getTargetCluster() {
        return targetCluster;
    }

    public void setTargetCluster(String targetCluster) {
        this.targetCluster = targetCluster;
    }

    public Integer getEnvId() {
        return envId;
    }

    public void setEnvId(Integer envId) {
        this.envId = envId;
    }
}

