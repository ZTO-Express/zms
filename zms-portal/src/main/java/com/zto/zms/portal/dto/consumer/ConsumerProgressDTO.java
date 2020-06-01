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

package com.zto.zms.portal.dto.consumer;

/**
 * Created by liangyong on 2019/3/1.
 */
public class ConsumerProgressDTO implements Comparable<ConsumerProgressDTO> {

    private String topic;
    private String BrokerName;
    private int qid;
    private Long brokerOffset;
    private Long consumerOffset;
    private String clientIP;
    private Long diff;
    private String lastTime;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBrokerName() {
        return BrokerName;
    }

    public void setBrokerName(String brokerName) {
        BrokerName = brokerName;
    }

    public int getQid() {
        return qid;
    }

    public void setQid(int qid) {
        this.qid = qid;
    }

    public Long getBrokerOffset() {
        return brokerOffset;
    }

    public void setBrokerOffset(Long brokerOffset) {
        this.brokerOffset = brokerOffset;
    }

    public Long getConsumerOffset() {
        return consumerOffset;
    }

    public void setConsumerOffset(Long consumerOffset) {
        this.consumerOffset = consumerOffset;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public Long getDiff() {
        return diff;
    }

    public void setDiff(Long diff) {
        this.diff = diff;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    @Override
    public int compareTo(ConsumerProgressDTO o) {
        return (int) (o.diff - diff);
    }
}

