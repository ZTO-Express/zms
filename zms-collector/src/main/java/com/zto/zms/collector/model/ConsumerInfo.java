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

import java.util.List;

public class ConsumerInfo {

    private long latency;

    private double tps;

    private long lastConsumingTime;

    private List<String> consumers = Lists.newArrayList();

    public ConsumerInfo(long latency, double tps) {
        this.latency = latency;
        this.tps = tps;
    }

    public void add(long latency, double tps) {
        this.tps = this.tps + tps;
        this.latency = this.latency + latency;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public double getTps() {
        return tps;
    }

    public void setTps(double tps) {
        this.tps = tps;
    }

    public List<String> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<String> consumers) {
        this.consumers = consumers;
    }

    public long getLastConsumingTime() {
        return lastConsumingTime;
    }

    public void setLastConsumingTime(long lastConsumingTime) {
        this.lastConsumingTime = lastConsumingTime;
    }

    public void updateLastConsumingTime(long lastConsumingTime) {
        if (this.lastConsumingTime < lastConsumingTime) {
            this.lastConsumingTime = lastConsumingTime;
        }
    }
}

