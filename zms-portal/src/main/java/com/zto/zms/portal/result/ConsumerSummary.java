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

package com.zto.zms.portal.result;

import com.zto.zms.service.domain.influxdb.ConsumerInfo;

import java.math.BigDecimal;

public class ConsumerSummary {

    private double latency;
    private double tps;
    private String cluster;
    private String consumer;
    private String owner;

    public ConsumerSummary(ConsumerInfo consumerLatency, ConsumerInfo consumerTps) {
        if (consumerLatency != null) {
            this.latency = consumerLatency.getValue();
            this.cluster = consumerLatency.getClusterName();
            this.consumer = consumerLatency.getConsumerGroup();
        }
        if (consumerTps != null && consumerTps.getValue() > 0) {
            try {
                this.tps = BigDecimal.valueOf(consumerTps.getValue()).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
            } catch (Exception ex) {
                this.tps = consumerTps.getValue();
            }
        }
    }


    public double getLatency() {
        return latency;
    }

    public void setLatency(double latency) {
        this.latency = latency;
    }

    public double getTps() {
        return tps;
    }

    public void setTps(double tps) {
        this.tps = tps;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}

