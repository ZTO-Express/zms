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

package com.zto.zms.client.producer;

import com.alibaba.fastjson.JSON;
import com.zto.zms.client.Zms;
import com.zto.zms.client.ZmsProxy;
import com.zto.zms.client.common.SimpleMessage;
import com.zto.zms.client.common.StatsLoggerType;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.logger.ZmsLogger;
import com.zto.zms.stats.StatsInfo;
import com.zto.zms.metadata.TopicMetadata;
import com.zto.zms.metadata.ZmsMetadata;
import com.zto.zms.client.metrics.ZmsProducerMetrics;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Properties;
import java.util.Random;

/**
 * Created by superheizai on 2017/8/16.
 */
public abstract class ZmsProducerProxy extends ZmsProxy<ZmsProducerMetrics> implements Producer {

    public static final Logger logger = ZmsLogger.log;


    Properties customizedProperties;


    ZmsProducerProxy(ZmsMetadata metadata, boolean isOrderly, String name) {
        super(metadata, isOrderly, new ZmsProducerMetrics(metadata.getName(), name));
    }

    ZmsProducerProxy(ZmsMetadata metadata, boolean isOrderly, String name, Properties properties) {
        super(metadata, isOrderly, new ZmsProducerMetrics(metadata.getName(), name));
        this.customizedProperties = properties;
    }

    @Override
    public void start() {
        if (running) {
            logger.warn("producer {} has been started,can't be start again", instanceName);
            return;
        }
        startProducer();
        super.start();
        logger.info("Producer {} has been started", instanceName);

    }

    public abstract void startProducer();

    public abstract void shutdownProducer();

    @Override
    public boolean changeConfigAndRestart(ZmsMetadata oldMetadata, ZmsMetadata newMetadata) {
        if (oldMetadata.isGatedLaunch() ^ newMetadata.isGatedLaunch()) {

            return true;
        }

        TopicMetadata oldConsumerMeta = (TopicMetadata) oldMetadata;
        TopicMetadata newConsumerMeta = (TopicMetadata) newMetadata;

        return !Objects.equals(oldConsumerMeta.getClusterMetadata(), newConsumerMeta.getClusterMetadata());
    }

    @Override
    public void statistics() {
        if (running && !isStatistic(zmsMetrics.getClientName())) {
            if (StringUtils.isEmpty(metadata.getStatisticsLogger()) || StatsLoggerType.MESSAGE.getName().equalsIgnoreCase(metadata.getStatisticsLogger())) {
                StatsInfo info = zmsMetrics.reportMessageStatistics();
                Zms.sendOneway(ZmsConst.STATISTICS.STATISTICS_TOPIC_PRODUCERINFO, new SimpleMessage(JSON.toJSONBytes(info)));
            } else {
                ZmsLogger.statisticLog.info(zmsMetrics.reportLogStatistics());
            }
        }
    }


    @Override
    public void shutdown() {
        if (!running) {
            logger.warn("Producer {} has been shutdown,can't be shutdown again", instanceName);
            return;
        }
        running = false;
        super.shutdown();
        shutdownProducer();
        ProducerFactory.recycle(metadata.getName(), instanceName);
        logger.info("Producer {} hast been shutdown", instanceName);
    }

    @Override
    public void restart() {
        logger.info("Producer {} begin to restart", instanceName);
        shutdown();
        try {
            Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
            // ignore
        }
    }


    protected volatile boolean running;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}

