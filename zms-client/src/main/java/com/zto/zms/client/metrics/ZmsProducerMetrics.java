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
import com.zto.zms.stats.ProducerStats;
import com.zto.zms.stats.StatsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ZmsProducerMetrics extends ZmsMetrics {

    public static final Logger logger = LoggerFactory.getLogger(ZmsProducerMetrics.class);
    private final Meter messageSuccessRate;
    private final Meter messageFailureRate;
    private final Timer sendCostRate;
    private Distribution msgBody;

    private static final String PRODUCER_METRIC_GROUP = "ZmsProducerMetrics";

    public Distribution getDistribution() {
        return distribution;
    }

    private Distribution distribution;

    public ZmsProducerMetrics(String clientName, String zmsName) {
        super(clientName, zmsName);
        this.messageSuccessRate = ZmsMetricsRegistry.REGISTRY.meter(ZmsMetricsRegistry.buildName(PRODUCER_METRIC_GROUP, "messageSuccessRate", clientName, zmsName));
        this.messageFailureRate = ZmsMetricsRegistry.REGISTRY.meter(ZmsMetricsRegistry.buildName(PRODUCER_METRIC_GROUP, "messageFailureRate", clientName, zmsName));
        this.sendCostRate = ZmsMetricsRegistry.REGISTRY.timer(ZmsMetricsRegistry.buildName(PRODUCER_METRIC_GROUP, "sendCostRate", clientName, zmsName));
        this.msgBody = Distribution.newDistribution(ZmsMetricsRegistry.buildName(PRODUCER_METRIC_GROUP, "msgBody", clientName, zmsName));
        this.distribution = Distribution.newDistribution(ZmsMetricsRegistry.buildName(PRODUCER_METRIC_GROUP, "distribution", clientName, zmsName));
    }

    public Meter messageSuccessRate() {
        return messageSuccessRate;
    }

    public Meter messageFailureRate() {
        return messageFailureRate;
    }

    public Timer sendCostRate() {
        return sendCostRate;
    }

    public Distribution msgBody() {
        return msgBody;
    }

    @Override
    public StatsInfo reportMessageStatistics() {
        ProducerStats info = new ProducerStats();
        Distribution old = distribution;
        Distribution oldMsgBody = msgBody;
        renewDistribution();
        info.setClientInfo(getClientInfo());
        info.getDistributions().add(transfer(old, "distribution"));
        info.getMeters().add(transfer(messageSuccessRate, "messageSuccessRate"));
        info.getMeters().add(transfer(messageFailureRate, "messageFailureRate"));
        info.getTimers().add(transfer(sendCostRate, "sendCostRate"));
        info.getDistributions().add(transfer(oldMsgBody, "msgBody"));
        return info;
    }

    private void renewDistribution() {
        distribution = Distribution.newDistribution(distribution.getName());
        msgBody = Distribution.newDistribution(msgBody.getName());
    }

    @Override
    public String reportLogStatistics() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.getClientName()).append("--").append(this.getZmsName()).append(":\n");
        try {
            stringBuilder.append("SuccessMessagePerSec     ");
            processMeter(this.messageSuccessRate, stringBuilder);

            stringBuilder.append("ProducerSendRateAndTimeMs");
            processTimer(this.sendCostRate, stringBuilder);

            stringBuilder.append("FailureMessagePerSec     ");
            processMeter(this.messageFailureRate, stringBuilder);

            Distribution old = distribution;
            Distribution oldMsgBody = msgBody;
            renewDistribution();
            stringBuilder.append(old.output());
            stringBuilder.append(oldMsgBody.output());
        } catch (Exception e) {
            logger.error("output statistics error", e);
        }
        return stringBuilder.toString();
    }


}

