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

package com.zto.zms.collector.model;
import java.util.List;
import java.util.Map;

public class KafkaMetrics {


    private String clusterName;
    private Long timestamp;
    private List<String> brokers;
    private int controllerId;
    private String controllerHost;
    private Map<String,KafkaBrokerInfo> brokersMetrics;
    private List<TopicMetrics> topicMetricsInfo;

    private List<Map<String, Object>> topicMetrics;


    public Map<String, KafkaBrokerInfo> getBrokersMetrics() {
        return brokersMetrics;
    }

    public void setBrokersMetrics(Map<String, KafkaBrokerInfo> brokersMetrics) {
        this.brokersMetrics = brokersMetrics;
    }

    public List<TopicMetrics> getTopicMetricsInfo() {
        return topicMetricsInfo;
    }

    public void setTopicMetricsInfo(List<TopicMetrics> topicMetricsInfo) {
        this.topicMetricsInfo = topicMetricsInfo;
    }

    public List<Map<String, Object>> getTopicMetrics() {
        return topicMetrics;
    }

    public void setTopicMetrics(List<Map<String, Object>> topicMetrics) {
        this.topicMetrics = topicMetrics;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<String> brokers) {
        this.brokers = brokers;
    }

    public int getControllerId() {
        return controllerId;
    }

    public void setControllerId(int controllerId) {
        this.controllerId = controllerId;
    }

    public String getControllerHost() {
        return controllerHost;
    }

    public void setControllerHost(String controllerHost) {
        this.controllerHost = controllerHost;
    }
}

