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

package com.zto.zms.client;

import com.zto.zms.common.ZmsConst;
import com.zto.zms.common.ZmsType;
import com.zto.zms.logger.ZmsLogger;
import com.zto.zms.metadata.ZmsMetadata;
import com.zto.zms.client.metrics.ZmsMetrics;
import com.zto.zms.zookeeper.ZmsZkClient;
import org.I0Itec.zkclient.IZkDataListener;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by superheizai on 2017/7/26.
 */
public abstract class ZmsProxy<K extends ZmsMetrics> implements IZkDataListener,LifeCycle {

    protected ZmsMetadata metadata;

    public String instanceName;

    protected K zmsMetrics;

    private String proxyName;

    protected boolean isOrderly;
    @Override
    public void handleDataChange(String s, Object o) {

        ZmsMetadata newMetadata;
        if (metadata.getType().equals(ZmsType.TOPIC.getName())) {
            newMetadata = getZkInstance().readTopicMetadata(ZmsProxy.this.metadata.getName());
        } else {
            newMetadata = getZkInstance().readConsumerGroupMetadata(ZmsProxy.this.metadata.getName());
        }
        ZmsLogger.log.info("metadata {} change notified", newMetadata.toString());


        if (!metadata.getClusterMetadata().getBrokerType().equals(newMetadata.getClusterMetadata().getBrokerType())) {
            ZmsLogger.log.error("BrokerType can't be change for topic or consumergroup when running");
            return;
        }
        if (metadata.equals(newMetadata)) {
            ZmsLogger.log.info("ignore the change, for it's the same with before");
            return;
        }
        ZmsMetadata oldMetadata = metadata;
        metadata = newMetadata;

        if (changeConfigAndRestart(oldMetadata, newMetadata)) {
            ZmsLogger.log.info("{} metadata change notify client restart", newMetadata.getName());
            restart();
        }
    }

    @Override
    public void handleDataDeleted(String s) {
    }

    public ZmsProxy(ZmsMetadata metadata, boolean isOrderly, K metrics) {
        this.metadata = metadata;
        this.isOrderly = isOrderly;
        zmsMetrics = metrics;
    }

    public ZmsZkClient getZkInstance() {
        return ZmsZkClient.getInstance();
    }

    public void restart() {
    }


    private void registerWatcher() {
        getZkInstance().subscribeDataChanges(metadata.getZmsPath(), this);
    }

    private void registerName() {
        if (!isStatistic(metadata.getName())) {
            proxyName = String.join("/", metadata.getZmsPath(), ZmsConst.ZMS_IP + "||" + instanceName + "||" + ZmsConst.ZMS_VERSION + "||" + LocalDateTime.now() + "||" + ThreadLocalRandom.current().nextInt(100000));
            getZkInstance().createEphemeral(proxyName);
        }
    }

    private void unregisterWatcher() {
        getZkInstance().unsubscribeDataChanges(metadata.getZmsClusterPath(), this);
        getZkInstance().unsubscribeDataChanges(metadata.getZmsPath(), this);
    }

    protected boolean isStatistic(String name) {
        return ZmsConst.STATISTICS.PING_CONSUMER_NAME.equalsIgnoreCase(name) ||
                ZmsConst.STATISTICS.PING_TOPIC_NAME.equalsIgnoreCase(name) ||
                ZmsConst.STATISTICS.STATISTICS_CONSUMER_CONSUMERINFO.equalsIgnoreCase(name) ||
                ZmsConst.STATISTICS.STATISTICS_CONSUMER_KAFKA_CONSUMERINFO.equalsIgnoreCase(name) ||
                ZmsConst.STATISTICS.STATISTICS_CONSUMER_KAFKA_PRODUCERINFO.equalsIgnoreCase(name) ||
                ZmsConst.STATISTICS.STATISTICS_CONSUMER_KAFKA_PRODUCERINFO.equalsIgnoreCase(name) ||
                ZmsConst.STATISTICS.STATISTICS_CONSUMER_PRODUCERINFO.equalsIgnoreCase(name) ||
                ZmsConst.STATISTICS.STATISTICS_TOPIC_CONSUMERINFO.equalsIgnoreCase(name) ||
                ZmsConst.STATISTICS.STATISTICS_TOPIC_KAFKA_CONSUMERINFO.equalsIgnoreCase(name) ||
                ZmsConst.STATISTICS.STATISTICS_TOPIC_KAFKA_PRODUCERINFO.equalsIgnoreCase(name) ||
                ZmsConst.STATISTICS.STATISTICS_TOPIC_PRODUCERINFO.equalsIgnoreCase(name);
    }

    private void unregisterName() {
        if (!isStatistic(metadata.getName())) {
            getZkInstance().delete(proxyName);
        }
    }


    public abstract boolean changeConfigAndRestart(ZmsMetadata oldMetadata, ZmsMetadata newMetadata);

    @Override
    public void start() {
        registerWatcher();
        registerName();
        running = true;
    }

    @Override
    public void shutdown() {
        running = false;
        unregisterName();
        unregisterWatcher();

    }

    public ZmsMetadata getMetadata() {
        return metadata;
    }


    protected volatile boolean running;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}

