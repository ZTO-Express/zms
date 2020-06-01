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

import com.google.common.collect.Sets;
import com.zto.zms.client.common.SimpleMessage;
import com.zto.zms.client.common.ZmsMessage;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.client.consumer.ConsumerFactory;
import com.zto.zms.client.consumer.ConsumerGroup;
import com.zto.zms.client.consumer.MessageListener;
import com.zto.zms.logger.ZmsLogger;
import com.zto.zms.client.metrics.ZmsStatsReporter;
import com.zto.zms.client.producer.Producer;
import com.zto.zms.client.producer.ProducerFactory;
import com.zto.zms.client.producer.SendResult;
import com.zto.zms.client.producer.SendCallback;
import com.zto.zms.zookeeper.ZmsZkClient;
import org.slf4j.Logger;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by superheizai on 2017/7/26.
 */
public class Zms implements LifeCycle {

    public static final Logger logger = ZmsLogger.log;

    private final ZmsStatsReporter reporter;

    private static final Zms instance = new Zms();


    private Zms() {
        logger.info("zms version {} initialized for {}", ZmsConst.ZMS_VERSION, ZmsConst.ZMS_IP);
        running = true;
        reporter = new ZmsStatsReporter();
        reporter.start(10, TimeUnit.SECONDS);
        logger.info("zms initilized");
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
        reporter.shutdown();
        ProducerFactory.shutdown();
        ConsumerFactory.shutdown();
        ZmsZkClient.getInstance().close();
        running = false;
        logger.info("zms has been shutdown");
    }

    /**
     * 关闭指定Topic的Producer
     *
     * @param topic
     */
    public static void stopProducer(String topic) {
        ProducerFactory.shutdown(topic);
    }

    /**
     * 关闭指定消费组的Consumer
     *
     * @param consumerGroup
     */
    public static void stopConsumer(String consumerGroup) {
        ConsumerFactory.shutdown(consumerGroup);
    }

    public static void stop() {
        instance.shutdown();
    }

    public static void subscribe(String consumerGroup, MessageListener listener) {
        instance.doSubscribe(consumerGroup, listener);
    }

    public static void subscribe(String consumerGroup, Set<String> tags, MessageListener listener, Properties properties) {

        instance.doSubscribe(consumerGroup, tags, listener, properties);
    }


    public static void subscribe(String consumerGroup, MessageListener listener, Properties properties) {

        instance.doSubscribe(consumerGroup, Sets.newHashSet(), listener, properties);
    }

    public static void subscribe(String consumerGroup, Set<String> tags, MessageListener listener) {

        instance.doSubscribe(consumerGroup, tags, listener);
    }

    public static void subscribe(String consumerGroup, String tag, MessageListener listener) {

        instance.doSubscribe(consumerGroup, Sets.newHashSet(tag), listener);
    }

    public static SendResult send(String topic, SimpleMessage simpleMessage) {
        return instance.doSendSync(topic, simpleMessage, null);
    }


    public static void sendAsync(String topic, SimpleMessage simpleMessage, SendCallback callBack) {
        instance.doSendAsync(topic, simpleMessage, null, callBack);
    }


    public static void sendOneway(String topic, SimpleMessage simpleMessage) {
        instance.doSendOneway(topic, simpleMessage);
    }


    public static SendResult send(String topic, SimpleMessage simpleMessage, Properties properties) {
        return instance.doSendSync(topic, simpleMessage, properties);
    }


    public static void sendAsync(String topic, SimpleMessage simpleMessage, Properties properties, SendCallback callBack) {
        instance.doSendAsync(topic, simpleMessage, properties, callBack);
    }

    private void doSubscribe(String consumerGroup, MessageListener listener) {

        if (!running) {
            logger.error("ZMS is not running,will not consume message");
            return;
        }
        ConsumerFactory.getConsumer(new ConsumerGroup(consumerGroup), new Properties(), listener);

    }

    private void doSubscribe(String consumerGroup, Set<String> tags, MessageListener listener) {
        if (!running) {
            logger.error("ZMS is not running,will not consume message");
            return;
        }
        ConsumerFactory.getConsumer(new ConsumerGroup(consumerGroup, ZmsConst.DEFAULT_CONSUMER, tags), new Properties(), listener);
    }

    private void doSubscribe(String consumerGroup, Set<String> tags, MessageListener listener, Properties properties) {
        if (!running) {
            logger.error("ZMS is not running,will not consume message");
            return;
        }
        ConsumerFactory.getConsumer(new ConsumerGroup(consumerGroup, ZmsConst.DEFAULT_CONSUMER, tags), properties, listener);
    }

    private void doSendOneway(String topic, SimpleMessage simpleMessage) {
        if (!running) {
            logger.warn("ZMS is not running,will not send message");
            return;
        }
        Producer producer = ProducerFactory.getProducer(topic);
        producer.oneway(new ZmsMessage(simpleMessage));
    }

    private SendResult doSendSync(String topic, SimpleMessage simpleMessage, Properties properties) {
        if (!running) {
            logger.warn("ZMS is not running,will not send message");
            return SendResult.buildErrorResult("ZMS is not running");
        }
        Producer producer = ProducerFactory.getProducer(topic, properties);

        return producer.syncSend(new ZmsMessage(simpleMessage));
    }

    private void doSendAsync(String topic, SimpleMessage simpleMessage, Properties properties, SendCallback callBack) {
        if (!running) {
            logger.warn("ZMS is not running,will not send message");
            return;
        }

        Producer producer = ProducerFactory.getProducer(topic, properties);
        producer.asyncSend(new ZmsMessage(simpleMessage), callBack);
    }


    protected volatile boolean running;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

}

