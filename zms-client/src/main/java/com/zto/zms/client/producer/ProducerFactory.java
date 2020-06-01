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

import com.zto.zms.common.BrokerType;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.common.ZmsException;
import com.zto.zms.logger.ZmsLogger;
import com.zto.zms.metadata.TopicMetadata;
import com.zto.zms.zookeeper.ZmsZkClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by superheizai on 2017/7/27.
 */
public class ProducerFactory {

    public static final Logger logger = ZmsLogger.log;

    private final static Map<String, ZmsProducerProxy> topicProducers = new ConcurrentHashMap<>();

    private ProducerFactory() {
    }

    public static ZmsProducerProxy getProducer(String topic) {
        return doGetProducer(topic, ZmsConst.DEFAULT_PRODUCER);
    }

    public static ZmsProducerProxy getProducer(String topic, Properties properties) {
        if (properties == null || properties.isEmpty()) {
            return getProducer(topic);
        }
        return doGetProducer(topic, ZmsConst.DEFAULT_PRODUCER, properties);
    }

    public synchronized static void shutdown() {
        for (Map.Entry<String, ZmsProducerProxy> entry : topicProducers.entrySet()) {
            entry.getValue().shutdown();
        }
        topicProducers.clear();
        logger.info("ProducerFactory has been shutdown");
    }

    public synchronized static void shutdown(String topic) {
        String key = topic + "_" + ZmsConst.DEFAULT_PRODUCER;
        if (topicProducers.containsKey(key)) {
            topicProducers.get(key).shutdown();
        }
        logger.info("Producer of " + topic + " has been shutdown");
    }

    static void recycle(String name, String instanceName) {
        String key = name + "_" + instanceName;
        topicProducers.remove(key);
        logger.info("producer {} has been remove", key);
    }

    public static Collection<ZmsProducerProxy> getProducers() {
        return topicProducers.values();
    }


    private static ZmsProducerProxy doGetProducer(String topic, String name, Properties properties) {
        String cacheName = topic + "_" + name;
        if (topicProducers.get(cacheName) == null) {
            synchronized (ProducerFactory.class) {
                if (topicProducers.get(cacheName) == null) {
                    ZmsProducerProxy producer;
                    TopicMetadata metadata;
                    try {
                        metadata = ZmsZkClient.getInstance().readTopicMetadata(topic);
                    } catch (Exception ex) {
                        logger.error("read topic {} metadata error", topic, ex);
                        throw ZmsException.METAINFO_EXCEPTION;
                    }

                    logger.info("Producer create: topic metadata is {}", metadata.toString());
                    if (BrokerType.ROCKETMQ.equals(metadata.getClusterMetadata().getBrokerType())) {
                        producer = new RocketmqProducerProxy(metadata, false, name, properties);
                    } else {
                        producer = new KafkaProducerProxy(metadata, false, name, properties);
                    }
                    topicProducers.putIfAbsent(cacheName, producer);
                    return producer;
                }
            }
        }
        return topicProducers.get(cacheName);
    }

    private static ZmsProducerProxy doGetProducer(String topic, String name) {
        String cacheName = topic + "_" + name;
        if (topicProducers.get(cacheName) == null) {
            synchronized (topicProducers) {
                if (topicProducers.get(cacheName) == null) {
                    ZmsProducerProxy producer = null;
                    TopicMetadata metadata = null;
                    try {
                        metadata = ZmsZkClient.getInstance().readTopicMetadata(topic);
                    } catch (Exception ex) {
                        logger.error("read topic {} metadata error", topic, ex);
                        throw ZmsException.METAINFO_EXCEPTION;
                    }
                    logger.info("Producer create: topic metadata is {}", metadata.toString());
                    if (BrokerType.ROCKETMQ.equals(metadata.getClusterMetadata().getBrokerType())) {
                        producer = new RocketmqProducerProxy(metadata, false, name);
                    } else {
                        producer = new KafkaProducerProxy(metadata, false, name);
                    }

                    topicProducers.putIfAbsent(cacheName, producer);
                    return producer;
                }
            }
        }

        return topicProducers.get(cacheName);
    }
}


