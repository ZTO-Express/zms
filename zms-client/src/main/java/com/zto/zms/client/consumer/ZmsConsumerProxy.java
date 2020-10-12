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

package com.zto.zms.client.consumer;

import com.alibaba.fastjson.JSON;
import com.zto.zms.client.Zms;
import com.zto.zms.client.ZmsProxy;
import com.zto.zms.client.common.SimpleMessage;
import com.zto.zms.client.common.StatsLoggerType;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.logger.ZmsLogger;
import com.zto.zms.stats.StatsInfo;
import com.zto.zms.metadata.ConsumerGroupMetadata;
import com.zto.zms.metadata.ZmsMetadata;
import com.zto.zms.client.metrics.ZmsConsumerMetrics;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by superheizai on 2017/8/16.
 */
public abstract class ZmsConsumerProxy extends ZmsProxy<ZmsConsumerMetrics> implements Consumer {

    public static final Logger logger = ZmsLogger.log;

    protected MessageListener listener;
    protected Properties customizedProperties;


    public ZmsConsumerProxy(ZmsMetadata metadata, boolean isOrderly, String name, Properties properties, MessageListener listener) {
        super(metadata, isOrderly, new ZmsConsumerMetrics(metadata.getName(), name));
        this.listener = listener;
        this.customizedProperties = properties;
    }


    @Override
    public void start() {
        if (running) {
            logger.warn("Conumser {} has been started,cant'be started again", instanceName);
            return;
        }
        consumerStart();
        super.start();
        register(listener);
        logger.info("ConsumerProxy started at {}, consumer group name:{}", System.currentTimeMillis(), metadata.getName());

    }

    protected abstract void consumerStart();

    @Override
    public void restart() {
        logger.info("consumer {} begin to restart", instanceName);
        shutdown();
        try {
            Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
            //ignore
        }
        start();
    }


    @Override
    public boolean changeConfigAndRestart(ZmsMetadata oldMetadata, ZmsMetadata newMetadata) {
        if (oldMetadata.isGatedLaunch() ^ newMetadata.isGatedLaunch()) {
            return true;
        }

        ConsumerGroupMetadata oldConsumerMeta = (ConsumerGroupMetadata) oldMetadata;
        ConsumerGroupMetadata newConsumerMeta = (ConsumerGroupMetadata) newMetadata;

        return !Objects.equals(oldConsumerMeta.getClusterMetadata(), newConsumerMeta.getClusterMetadata()) ||
                !Objects.equals(oldConsumerMeta.getBindingTopic(), newConsumerMeta.getBindingTopic()) ||
                !Objects.equals(oldConsumerMeta.getBroadcast(), newConsumerMeta.getBroadcast()) ||
                !Objects.equals(oldConsumerMeta.getConsumeFrom(), newConsumerMeta.getConsumeFrom());

    }

    @Override
    public void shutdown() {
        if (!running) {
            logger.warn("Consumer {} has been shutdown,cant'be shutdown again", instanceName);
            return;
        }
        running = false;
        super.shutdown();
        consumerShutdown();
        ConsumerFactory.recycle(metadata.getName(), instanceName);
        logger.info("Consumer {} shutdown", instanceName);
    }


    @Override
    public void statistics() {
        if (running && !isStatistic(zmsMetrics.getClientName())) {
            if (StringUtils.isEmpty(metadata.getStatisticsLogger()) || StatsLoggerType.MESSAGE.getName().equalsIgnoreCase(metadata.getStatisticsLogger())) {
                StatsInfo info = zmsMetrics.reportMessageStatistics();
                Zms.sendOneway(ZmsConst.STATISTICS.STATISTICS_TOPIC_CONSUMERINFO, new SimpleMessage(JSON.toJSONBytes(info)));
            } else {
                ZmsLogger.statisticLog.info(zmsMetrics.reportLogStatistics());
            }
        }

    }

    protected abstract void consumerShutdown();

    @Override
    abstract public void addUserDefinedProperties(Properties properties);
}

