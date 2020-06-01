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

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/5/12
 * @since
 **/
public class BackupConsumerMetadataDTO {

    private Integer originEnvId;
    private Integer currentEnvId;
    private String name;
    private String clusterName;
    private String bindingTopic;

    public BackupConsumerMetadataDTO(Integer originEnvId, Integer currentEnvId, String clusterName, String bindingTopic, String name) {
        this.originEnvId = originEnvId;
        this.currentEnvId = currentEnvId;
        this.name = name;
        this.clusterName = clusterName;
        this.bindingTopic = bindingTopic;
    }

    public Integer getCurrentEnvId() {
        return currentEnvId;
    }

    public void setCurrentEnvId(Integer currentEnvId) {
        this.currentEnvId = currentEnvId;
    }

    public Integer getOriginEnvId() {
        return originEnvId;
    }

    public void setOriginEnvId(Integer originEnvId) {
        this.originEnvId = originEnvId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getBindingTopic() {
        return bindingTopic;
    }

    public void setBindingTopic(String bindingTopic) {
        this.bindingTopic = bindingTopic;
    }

    @Override
    public String toString() {
        return "BackupConsumerMetadataDTO{" +
                "currentEnvId=" + currentEnvId +
                ", originEnvId=" + originEnvId +
                ", name='" + name + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", bindingTopic='" + bindingTopic + '\'' +
                '}';
    }
}

