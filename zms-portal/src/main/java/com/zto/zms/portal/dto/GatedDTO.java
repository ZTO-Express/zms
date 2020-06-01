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

import com.zto.zms.service.domain.consumer.ConsumerEnvironmentRefVO;
import com.zto.zms.service.domain.topic.TopicEnvironmentInfoVo;

import java.util.List;

public class GatedDTO {

    private String gatedIps;
    private Long gatedClusterId;
    private String gatedCluster;
    private Long id;
    private String name;
    private List<TopicEnvironmentInfoVo> environments;

    private List<ConsumerEnvironmentRefVO> consumerEnvironmentRefVos;

    public List<ConsumerEnvironmentRefVO> getConsumerEnvironmentRefVos() {
        return consumerEnvironmentRefVos;
    }

    public void setConsumerEnvironmentRefVos(List<ConsumerEnvironmentRefVO> consumerEnvironmentRefVos) {
        this.consumerEnvironmentRefVos = consumerEnvironmentRefVos;
    }

    public String getGatedIps() {
        return gatedIps;
    }

    public void setGatedIps(String gatedIps) {
        this.gatedIps = gatedIps;
    }

    public Long getGatedClusterId() {
        return gatedClusterId;
    }

    public void setGatedClusterId(Long gatedClusterId) {
        this.gatedClusterId = gatedClusterId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGatedCluster() {
        return gatedCluster;
    }

    public void setGatedCluster(String gatedCluster) {
        this.gatedCluster = gatedCluster;
    }

    public List<TopicEnvironmentInfoVo> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<TopicEnvironmentInfoVo> environments) {
        this.environments = environments;
    }

}

