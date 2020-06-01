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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class RocketmqStatus {

    private String clusterName;
    private long timestamp;
    private double totalTps;
    private int brokersCnt;
    private List<BrokerInfo> brokers = Lists.newArrayList();
    private Map<String, ConsumerInfo> consumerInfos = Maps.newHashMap();
    private Map<String, TopicInfo> topicInfos = Maps.newHashMap();

    public List<BrokerInfo> getBrokers() {
        return brokers;
    }

    public void setBrokers(List<BrokerInfo> brokers) {
        this.brokers = brokers;
    }


    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {

        this.clusterName = clusterName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, ConsumerInfo> getConsumerInfos() {
        return consumerInfos;
    }

    public void setConsumerInfos(Map<String, ConsumerInfo> consumerInfos) {
        this.consumerInfos = consumerInfos;
    }


    public Map<String, TopicInfo> getTopicInfos() {
        return topicInfos;
    }

    public void setTopicInfos(Map<String, TopicInfo> topicInfos) {
        this.topicInfos = topicInfos;
    }


    public double getTotalTps() {
        return totalTps;
    }

    public void setTotalTps(double totalTps) {
        this.totalTps = totalTps;
    }

    public int getBrokersCnt() {
        return brokersCnt;
    }

    public void setBrokersCnt(int brokersCnt) {
        this.brokersCnt = brokersCnt;
    }
}

