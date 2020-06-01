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

package com.zto.zms.client.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.zto.zms.stats.ConsumerStats;
import com.zto.zms.stats.StatsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ZmsConsumerMetrics extends ZmsMetrics {
    public static final Logger logger = LoggerFactory.getLogger(ZmsConsumerMetrics.class);

    private final Meter consumeSuccessRate;

    private final Meter consumeFailureRate;

    private final Timer userCostTimeMs;

    private static final String CONSUMER_METRIC_GROUP = "ZmsConsumerMetrics";

    public ZmsConsumerMetrics(String clientName, String zmsName) {
        super(clientName, zmsName);
        this.consumeSuccessRate = ZmsMetricsRegistry.REGISTRY.meter(ZmsMetricsRegistry.buildName(CONSUMER_METRIC_GROUP, "messageSuccessRate", clientName, zmsName));
        this.consumeFailureRate = ZmsMetricsRegistry.REGISTRY.meter(ZmsMetricsRegistry.buildName(CONSUMER_METRIC_GROUP, "consumeFailureRate", clientName, zmsName));
        this.userCostTimeMs = ZmsMetricsRegistry.REGISTRY.timer(ZmsMetricsRegistry.buildName(CONSUMER_METRIC_GROUP, "userCostTimeMs", clientName, zmsName));
    }

    public Meter consumeSuccessRate() {
        return consumeSuccessRate;
    }

    public Meter consumeFailureRate() {
        return consumeFailureRate;
    }

    public Timer userCostTimeMs() {
        return userCostTimeMs;
    }

    @Override
    public StatsInfo reportMessageStatistics() {
        ConsumerStats info = new ConsumerStats();
        info.setClientInfo(getClientInfo());
        info.getMeters().add(transfer(consumeSuccessRate, "consumeSuccessRate"));
        info.getMeters().add(transfer(consumeFailureRate, "consumeFailureRate"));
        info.getTimers().add(transfer(userCostTimeMs, "userCostTimeMs"));
        return info;
    }

    @Override
    public String reportLogStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getClientName()).append("--").append(this.getZmsName()).append(":\n");
        stringBuilder.append("SuccessMessagePerSec     ");
        try {
            processMeter(this.consumeSuccessRate, stringBuilder);
            stringBuilder.append("FailureMessagePerSec");
            processMeter(this.consumeFailureRate, stringBuilder);
            stringBuilder.append("userCostTimeMs");
            processTimer(this.userCostTimeMs, stringBuilder);
        } catch (Exception e) {
            logger.error("output statistics error", e);
        }
        return stringBuilder.toString();
    }
}

