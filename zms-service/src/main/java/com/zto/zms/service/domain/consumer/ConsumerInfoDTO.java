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
 * Created by liangyong on 2018/9/26.
 */
public class ConsumerInfoDTO {

    private long timestamp;
    private String clusterName;
    private String consumerGroup;
    private String topicName;
    private String name;
    private double value;

    public ConsumerInfoDTO(long timestamp, String clusterName, String consumerGroup, String name, double value) {
        this.timestamp = timestamp;
        this.clusterName = clusterName;
        this.consumerGroup = consumerGroup;
        this.name = name;
        this.value = value;
    }

    public ConsumerInfoDTO(long timestamp, String clusterName, String consumerGroup, String topicName, String name, double value) {
        this.timestamp = timestamp;
        this.clusterName = clusterName;
        this.consumerGroup = consumerGroup;
        this.topicName = topicName;
        this.name = name;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
}

