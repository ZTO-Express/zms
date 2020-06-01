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

package com.zto.zms.service.domain;

/**
 * Created by liangyong on 2018/11/6.
 */
public class OffsetStatisticDTO {

    private String clusterName;
    private String topicName;
    private long timestamp;
    private String name;
    private long value;

    public OffsetStatisticDTO(String clusterName, String topicName, long timestamp, String name, long value) {
        this.clusterName = clusterName;
        this.topicName = topicName;
        this.timestamp = timestamp;
        this.name = name;
        this.value = value;
    }

    public OffsetStatisticDTO(String clusterName, long timestamp, String name, long value) {
        this.clusterName = clusterName;
        this.timestamp = timestamp;
        this.name = name;
        this.value = value;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}

