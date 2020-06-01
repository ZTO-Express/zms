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

package com.zto.zms.portal.rocketmq;

/**
 * Created by liangyong on 2019/2/15.
 */
public class StatsAllResultDto {

    private String topic;
    private String consumerGroup;
    private Long accumulation;
    private Double inTPS;
    private Double outTPS;
    private Long inMsg24Hour;
    private Long outMsg24Hour;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Long getAccumulation() {
        return accumulation;
    }

    public void setAccumulation(Long accumulation) {
        this.accumulation = accumulation;
    }

    public Double getInTPS() {
        return inTPS;
    }

    public void setInTPS(Double inTPS) {
        this.inTPS = inTPS;
    }

    public Double getOutTPS() {
        return outTPS;
    }

    public void setOutTPS(Double outTPS) {
        this.outTPS = outTPS;
    }

    public Long getInMsg24Hour() {
        return inMsg24Hour;
    }

    public void setInMsg24Hour(Long inMsg24Hour) {
        this.inMsg24Hour = inMsg24Hour;
    }

    public Long getOutMsg24Hour() {
        return outMsg24Hour;
    }

    public void setOutMsg24Hour(Long outMsg24Hour) {
        this.outMsg24Hour = outMsg24Hour;
    }
}

