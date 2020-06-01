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

import com.google.common.collect.Lists;
import com.zto.zms.client.common.ConsumeFromWhere;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.client.config.ConsumerConfig;
import com.zto.zms.logger.ZmsLogger;
import com.zto.zms.metadata.ConsumerGroupMetadata;
import com.zto.zms.metadata.ZmsMetadata;
import com.zto.zms.utils.ExecutorServiceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.springside.modules.utils.net.NetUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by superheizai on 2017/8/15.
 */
public class KafkaConsumerProxy extends ZmsConsumerProxy {


    public static final Logger logger = ZmsLogger.log;

    KafkaConsumer consumer;
    List<ThreadPoolExecutor> executors = Lists.newArrayList();
    Properties kafkaProperties = new Properties();


    private final Map<String, Map<Integer, Long>> offsets = new HashMap<>();


    private final BlockingQueue<ConsumerRecord> acks = new LinkedBlockingQueue<>(100000);

    private void addOffset(ConsumerRecord record) {
        this.offsets.computeIfAbsent(record.topic(), v -> new ConcurrentHashMap<>())
                .compute(record.partition(), (k, v) -> v == null ? record.offset() : Math.max(v, record.offset()));
    }


    public KafkaConsumerProxy(ZmsMetadata metadata, boolean isOrderly, String instanceName, Properties properties, MessageListener listener) {
        super(metadata, isOrderly, instanceName, properties, listener);
        this.instanceName = instanceName;
        start();
    }


    @Override
    protected void consumerStart() {
        kafkaProperties.putAll(ConsumerConfig.KAFKA.KAFKA_CONFIG);
        if (metadata.isGatedLaunch()) {
            kafkaProperties.put("bootstrap.servers", metadata.getGatedCluster().getBootAddr());
        } else {
            kafkaProperties.put("bootstrap.servers", metadata.getClusterMetadata().getBootAddr());

        }
        kafkaProperties.put("group.id", metadata.getName());
        kafkaProperties.put("enable.auto.commit", false);
        kafkaProperties.put("client.id", metadata.getName() + "--" + NetUtil.getLocalHost() + "--" + ThreadLocalRandom.current().nextInt(100000));

        String consumeFrom = ((ConsumerGroupMetadata) metadata).getConsumeFrom();

        if (StringUtils.isEmpty(consumeFrom)) {
            kafkaProperties.put("auto.offset.reset", ConsumeFromWhere.EARLIEST.getName());

        } else {
            if (ConsumeFromWhere.EARLIEST.getName().equalsIgnoreCase(consumeFrom)) {
                kafkaProperties.put("auto.offset.reset", ConsumeFromWhere.EARLIEST.getName());
            } else {
                kafkaProperties.put("auto.offset.reset", ConsumeFromWhere.LATEST.getName());
            }
        }
        if (customizedProperties != null) {
            addUserDefinedProperties(customizedProperties);
        }

        logger.info("consumer {} start with param {}", instanceName, buildConsumerInfo(kafkaProperties));
        consumer = new KafkaConsumer(kafkaProperties);
        consumer.subscribe(Lists.newArrayList(((ConsumerGroupMetadata) metadata).getBindingTopic()), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> collection) {
                logger.info("partition revoked for {} at {}", metadata.getName(), LocalDateTime.now());
                commitOffsets();
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> collection) {
                logger.info("partition assigned for {} at {}", metadata.getName(), LocalDateTime.now());
                logger.info("partition assigned " + StringUtils.joinWith(",", collection));
                for (TopicPartition partition : collection) {
                    OffsetAndMetadata offset = consumer.committed(partition);
                    if (ConsumeFromWhere.LATEST.getName().equalsIgnoreCase((String) kafkaProperties.get("auto.offset.reset"))) {
                        if (offset == null) {
                            consumer.seek(partition, 0);
                        }
                    }

                }
            }
        });
    }


    private String buildConsumerInfo(Properties properties) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            stringBuilder.append(entry.getKey() + ": " + entry.getValue());
            stringBuilder.append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    @Override
    public void register(MessageListener listener) {

        String threadName = "ZmsKafkaPollThread-" + metadata.getName() + "-" + instanceName + LocalDateTime.now();
        Thread zmsPullThread = new Thread(() -> {

            try {
                while (running) {
                    ConsumerRecords records = null;
                    records = consumer.poll(Duration.ofMillis(3000));
                    logger.debug("messaged pulled at {} for topic {} ", System.currentTimeMillis(), ((ConsumerGroupMetadata) metadata).getBindingTopic());
                    if (listener instanceof KafkaBatchMsgListener) {
                        KafkaBatchMsgListener batchMsgListener = (KafkaBatchMsgListener) listener;
                        submitBatchRecords(records, batchMsgListener);
                    } else {
                        submitRecords(records, listener);
                    }
                    commitOffsets();
                }
            } catch (WakeupException ex) {
                logger.info("consumer poll wakeup", ex.getMessage());
            } catch (Exception ex) {
                logger.error("consume poll error", ex);
            } finally {
                consumerShutdown();
            }
        }, threadName);
        zmsPullThread.setUncaughtExceptionHandler((t, e) -> logger.error("{} thread get a exception ", threadName, e));
        zmsPullThread.start();


    }

    private synchronized void commitOffsets() {
        handleAcks();
        Map<TopicPartition, OffsetAndMetadata> commits = buildCommits();
        if (!commits.isEmpty()) {
            consumer.commitAsync(commits, new LoggingCommitCallback());
        }
    }


    private void handleAcks() {
        ConsumerRecord record = this.acks.poll();
        while (record != null) {
            addOffset(record);
            record = this.acks.poll();
        }
    }


    private synchronized Map<TopicPartition, OffsetAndMetadata> buildCommits() {

        Map<TopicPartition, OffsetAndMetadata> commits = new HashMap<>();
        for (Map.Entry<String, Map<Integer, Long>> entry : this.offsets.entrySet()) {
            for (Map.Entry<Integer, Long> offset : entry.getValue().entrySet()) {
                commits.put(new TopicPartition(entry.getKey(), offset.getKey()),
                        new OffsetAndMetadata(offset.getValue() + 1));
            }
        }
        this.offsets.clear();
        return commits;
    }

    private void submitBatchRecords(ConsumerRecords records, final KafkaBatchMsgListener listener) {
        if (!records.isEmpty()) {
            Iterable<ConsumerRecord> recordsIter = records.records(((ConsumerGroupMetadata) metadata).getBindingTopic());
            ArrayList<ConsumerRecord> consumerRecords = Lists.newArrayList(recordsIter);
            try {
                long begin = System.currentTimeMillis();
                MsgConsumedStatus msgConsumedStatus = listener.onMessage(consumerRecords);
                long duration = System.currentTimeMillis() - begin;
                zmsMetrics.userCostTimeMs().update(duration, TimeUnit.MILLISECONDS);
                logger.debug("kafka batch consume records count {} consumed {}", consumerRecords.size(), msgConsumedStatus.name());
            } catch (Throwable ex) {
                logger.error("consume message error", ex);
            } finally {
                for (int i = 0; i < consumerRecords.size(); i++) {
                    ConsumerRecord consumerRecord = consumerRecords.get(i);
                    boolean offer = KafkaConsumerProxy.this.acks.offer(consumerRecord);
                    while (!offer) {
                        logger.info("add consumer record to acks full and trigger commit offsets at {}", LocalDateTime.now());
                        commitOffsets();
                        offer = KafkaConsumerProxy.this.acks.offer(consumerRecord);
                    }
                }
            }
        }
    }

    private void submitRecords(ConsumerRecords records, final MessageListener listener) {
        if (!records.isEmpty()) {

            Iterable<ConsumerRecord> recordsIter = records.records(((ConsumerGroupMetadata) metadata).getBindingTopic());


            final CountDownLatch countDownLatch = new CountDownLatch(records.count());

            ArrayList<ConsumerRecord> consumerRecords = Lists.newArrayList(recordsIter);

            for (int i = 0; i < consumerRecords.size(); i++) {

                ConsumerRecord consumerRecord = consumerRecords.get(i);

                logger.debug("consumerRecord submitted topic {} partition {} offset {}", consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());

                Runnable task = () -> {
                    try {

                        long begin = System.currentTimeMillis();
                        MsgConsumedStatus msgConsumedStatus;
                        if (listener.isEasy()) {
                            ConsumeMessage consumeMessage = ConsumeMessage.parse(consumerRecord);
                            msgConsumedStatus = listener.onMessage(consumeMessage);
                        } else {
                            msgConsumedStatus = listener.onMessage(consumerRecord);
                        }
                        long duration = System.currentTimeMillis() - begin;
                        zmsMetrics.userCostTimeMs().update(duration, TimeUnit.MILLISECONDS);
                        logger.debug("consumerRecord  topic {} partition {} offset {} consumed {}", consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), msgConsumedStatus.name());
                    } catch (Throwable ex) {
                        logger.error("consume message error", ex);
                    } finally {
                        boolean offer = KafkaConsumerProxy.this.acks.offer(consumerRecord);
                        while (!offer) {
                            logger.info("add consumer record to acks full and trigger commit offsets at {}", LocalDateTime.now());
                            commitOffsets();
                            offer = KafkaConsumerProxy.this.acks.offer(consumerRecord);
                        }
                        countDownLatch.countDown();
                    }

                };

                boolean done = false;
                while (!done) {
                    try {
                        ThreadPoolExecutor executor = executors.get(consumerRecord.partition() % executors.size());
                        executor.execute(task);
                        done = true;
                    } catch (RejectedExecutionException ex) {
                        logger.error("consume slow, wait for moment");
                        try {
                            Thread.sleep(50L);
                        } catch (InterruptedException e) {
                            logger.error("interupted when consume slow");
                        }
                    }
                }
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                logger.error("wait for kafka consumer latch interupted", e);
            }
        }
    }


    @Override
    protected void consumerShutdown() {
        try {
            consumer.close();
        } catch (ConcurrentModificationException ex) {
            logger.info("consumer shutdown changes to wakeup for: {}", ex.getMessage());
            consumer.wakeup();
        }
        for (ThreadPoolExecutor executor : executors) {
            ExecutorServiceUtils.gracefullyShutdown(executor);
        }
        executors.clear();
    }


    @Override
    public void statistics() {
        super.statistics();
    }

    @Override
    public void addUserDefinedProperties(Properties properties) {


        if (properties.containsKey(ZmsConst.CLIENT_CONFIG.CONSUME_MESSAGES_SIZE)) {
            kafkaProperties.put("max.poll.records", properties.get(ZmsConst.CLIENT_CONFIG.CONSUME_MESSAGES_SIZE));
        }


        int threadsNumMin;
        int threadsNumMax;

        if (properties.containsKey(ZmsConst.CLIENT_CONFIG.CONSUME_THREAD_MIN)) {
            threadsNumMin = Integer.valueOf((String) properties.get(ZmsConst.CLIENT_CONFIG.CONSUME_THREAD_MIN));
        } else {
            threadsNumMin = Runtime.getRuntime().availableProcessors();
        }
        if (properties.containsKey(ZmsConst.CLIENT_CONFIG.CONSUME_THREAD_MAX)) {
            threadsNumMax = Integer.valueOf((String) properties.get(ZmsConst.CLIENT_CONFIG.CONSUME_THREAD_MAX));
        } else {
            if (Runtime.getRuntime().availableProcessors() * 2 <= threadsNumMin) {
                threadsNumMax = threadsNumMin;
            } else {
                threadsNumMax = Runtime.getRuntime().availableProcessors() * 2;
            }
        }
        logger.info("kafka consumer thread set to min: " + threadsNumMin + " max: " + threadsNumMax);


        if (!isOrderly) {

            ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue(10000);
            ThreadPoolExecutor.AbortPolicy policy = new ThreadPoolExecutor.AbortPolicy();

            ThreadFactory threadFactory = new ThreadFactory() {
                AtomicInteger index = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "ZmsKafkaConsumerThread_" + index.incrementAndGet());
                }
            };
            ThreadPoolExecutor executor = new ThreadPoolExecutor(threadsNumMin, threadsNumMax, 1000, TimeUnit.MILLISECONDS, blockingQueue, threadFactory, policy);
            executors.add(executor);
        } else {

            for (int i = 0; i < threadsNumMax; i++) {
                ArrayBlockingQueue blockingQueue = new ArrayBlockingQueue(1000);
                ThreadPoolExecutor.AbortPolicy policy = new ThreadPoolExecutor.AbortPolicy();
                final int factoryIndex = i;
                ThreadFactory threadFactory = new ThreadFactory() {
                    AtomicInteger index = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "ZmsKafkaConsumerThread_" + factoryIndex + "_" + index.incrementAndGet());
                    }
                };
                ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 1000, TimeUnit.MILLISECONDS, blockingQueue, threadFactory, policy);
                executors.add(executor);
            }
        }

    }


    private static final class LoggingCommitCallback implements OffsetCommitCallback {

        public static final Logger logger = ZmsLogger.log;

        LoggingCommitCallback() {
            super();
        }

        @Override
        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
            if (exception != null) {
                logger.error("Commit failed for " + offsets, exception);
            } else {
                logger.debug("Commits for " + offsets + " completed");
            }
        }

    }

}







