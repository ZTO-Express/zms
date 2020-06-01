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

import com.zto.zms.common.BrokerType;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.common.ZmsException;
import com.zto.zms.logger.ZmsLogger;
import com.zto.zms.metadata.ConsumerGroupMetadata;
import com.zto.zms.zookeeper.ZmsZkClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by superheizai on 2017/7/27.
 */
public class ConsumerFactory {

    private static Map<String, ZmsConsumerProxy> consumers = new ConcurrentHashMap<>();

    public static final Logger logger = ZmsLogger.log;

    private ConsumerFactory() {
    }


    public static ZmsConsumerProxy getConsumer(ConsumerGroup consumerGroup, Properties properties, MessageListener listener) {
        return doGetConsumer(consumerGroup.getConsumerGroup(), consumerGroup.getConsumerName(), consumerGroup.getTags(), properties, listener);
    }

    private static ZmsConsumerProxy doGetConsumer(String consumerGroup, String name, Set<String> tags, Properties properties, MessageListener listener) {
        String cacheName = consumerGroup + "_" + name;
        if (consumers.get(cacheName) == null) {
            synchronized (ConsumerFactory.class) {
                if (consumers.get(cacheName) == null) {
                    ZmsConsumerProxy consumer;
                    ConsumerGroupMetadata metadata;
                    try {
                        metadata = ZmsZkClient.getInstance().readConsumerGroupMetadata(consumerGroup);
                    } catch (Throwable ex) {
                        logger.error("get consumer metadata error", ex);
                        throw ZmsException.METAINFO_EXCEPTION;
                    }
                    logger.info("Consumer created {}", metadata.toString());
                    boolean isOrderly = false;

                    if (properties.containsKey(ZmsConst.CLIENT_CONFIG.CONSUME_ORDERLY)) {
                        isOrderly=Boolean.parseBoolean(properties.getProperty(ZmsConst.CLIENT_CONFIG.CONSUME_ORDERLY));
                    }
                    if (BrokerType.ROCKETMQ.equals(metadata.getClusterMetadata().getBrokerType())) {
                        consumer = new RocketmqConsumerProxy(metadata, isOrderly, name, tags, properties, listener);
                    } else {
                        consumer = new KafkaConsumerProxy(metadata, isOrderly, name, properties, listener);
                    }
                    consumers.putIfAbsent(cacheName, consumer);
                    return consumer;
                }
            }
        }
        return consumers.get(cacheName);
    }

    public synchronized static void shutdown() {
        for (Map.Entry<String, ZmsConsumerProxy> entry : consumers.entrySet()) {
            entry.getValue().shutdown();
        }
        consumers.clear();
        logger.info("ConsumerFactory shutdown");
    }

    public synchronized static void shutdown(String consumerGroup) {
        String key = consumerGroup + "_" + ZmsConst.DEFAULT_CONSUMER;
        if (consumers.containsKey(key)) {
            consumers.get(key).shutdown();
        }
        logger.info("ConsumerFactory shutdown");
    }

    static void recycle(String name, String instanceName) {
        String key = name + "_" + instanceName;
        consumers.remove(key);
        logger.info("Consumer {} removed", key);
    }

    public static Collection<ZmsConsumerProxy> getConsumers() {
        return consumers.values();
    }
}

