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

import com.zto.zms.client.common.*;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.common.ZmsException;
import com.zto.zms.metadata.ConsumerGroupMetadata;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.utils.collection.CollectionUtil;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by superheizai on 2017/8/15.
 */
public class RocketmqConsumerProxy extends ZmsConsumerProxy {

    public static final Logger logger = LoggerFactory.getLogger(RocketmqConsumerProxy.class);

    private DefaultMQPushConsumer consumer;
    private Set<String> tags;


    RocketmqConsumerProxy(ConsumerGroupMetadata metadata, boolean isOrderly, String instanceName, Set<String> tags, Properties properties, MessageListener listener) {
        super(metadata, isOrderly, instanceName, properties, listener);
        this.instanceName = instanceName;
        this.tags = tags;
        start();
    }

    @Override
    public void register(MessageListener listener) {
        if (isOrderly) {
            consumer.registerMessageListener((MessageListenerOrderly) (msgs, context) -> {
                if (msgs.size() < 1) {
                    return ConsumeOrderlyStatus.SUCCESS;
                }

                long begin = System.currentTimeMillis();
                MsgConsumedStatus msgConsumedStatus = MsgConsumedStatus.SUCCEED;
                try {

                    if (listener.isEasy()) {
                        for (MessageExt msg : msgs) {
                            ConsumeMessage consumeMessage = ConsumeMessage.parse(msg);
                            msgConsumedStatus = listener.onMessage(consumeMessage);
                        }
                    } else {
                        msgConsumedStatus = listener.onMessage(msgs);
                    }
                    zmsMetrics.consumeSuccessRate().mark();
                } catch (Throwable throwable) {
                    logger.error("consumer msg failed for {} batch", msgs.get(0).getMsgId(), throwable);
                    msgConsumedStatus = MsgConsumedStatus.RETRY;
                    zmsMetrics.consumeFailureRate().mark();
                }
                if (!msgConsumedStatus.equals(MsgConsumedStatus.SUCCEED)) {
                    zmsMetrics.userCostTimeMs().update(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }

                zmsMetrics.userCostTimeMs().update(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
                return ConsumeOrderlyStatus.SUCCESS;
            });
        } else {
            consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
                if (msgs.size() < 1) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                long begin = System.currentTimeMillis();

                MsgConsumedStatus msgConsumedStatus = MsgConsumedStatus.SUCCEED;
                try {
                    if (listener.isEasy()) {
                        for (MessageExt msg : msgs) {
                            ConsumeMessage consumeMessage = ConsumeMessage.parse(msg);
                            msgConsumedStatus = listener.onMessage(consumeMessage);
                        }
                    } else {
                        msgConsumedStatus = listener.onMessage(msgs);
                    }
                    zmsMetrics.consumeSuccessRate().mark();
                } catch (Throwable throwable) {
                    logger.error("consumer msg failed for {} batch", msgs.get(0).getMsgId(), throwable);
                    msgConsumedStatus = MsgConsumedStatus.RETRY;
                    zmsMetrics.consumeFailureRate().mark();
                }
                if (!msgConsumedStatus.equals(MsgConsumedStatus.SUCCEED)) {
                    zmsMetrics.userCostTimeMs().update(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
                    context.setDelayLevelWhenNextConsume(msgConsumedStatus.level);
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

                zmsMetrics.userCostTimeMs().update(System.currentTimeMillis() - begin, TimeUnit.MILLISECONDS);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            });
        }

        try {
            consumer.start();
        } catch (MQClientException e) {
            logger.error("RocketMQConsumer start error", e);
            throw ZmsException.CONSUMER_START_EXCEPTION;
        }
    }

    @Override
    protected void consumerStart() {
        consumer = new DefaultMQPushConsumer(metadata.getName());
        if (metadata.isGatedLaunch()) {
            consumer.setNamesrvAddr(metadata.getGatedCluster().getBootAddr());
            consumer.setClientIP("consumer-client-id-" + metadata.getGatedCluster().getClusterName() + "-" + ZmsConst.ZMS_IP);
        } else {
            consumer.setNamesrvAddr(metadata.getClusterMetadata().getBootAddr());
            consumer.setClientIP("consumer-client-id-" + metadata.getClusterMetadata().getClusterName() + "-" + ZmsConst.ZMS_IP);
        }
        consumer.setVipChannelEnabled(false);

        String bindingTopic = ((ConsumerGroupMetadata) metadata).getBindingTopic();
        String consumeFrom = ((ConsumerGroupMetadata) metadata).getConsumeFrom();
        String broadCast = ((ConsumerGroupMetadata) metadata).getBroadcast();

        if (((ConsumerGroupMetadata) metadata).needSuspend()) {
            logger.error("consumer {} suspend is on, please set it to off first", metadata.getName());
            throw new RuntimeException("consumer {} suspend is on, please set it to off first");
        }

        if (StringUtils.isEmpty(consumeFrom)) {
            consumer.setConsumeFromWhere(org.apache.rocketmq.common.consumer.ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        } else {
            if (ConsumeFromWhere.EARLIEST.getName().equalsIgnoreCase(consumeFrom)) {
                consumer.setConsumeFromWhere(org.apache.rocketmq.common.consumer.ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
            } else {
                consumer.setConsumeFromWhere(org.apache.rocketmq.common.consumer.ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            }
        }
        if (!StringUtils.isEmpty(broadCast) && Boolean.valueOf(broadCast)) {
            consumer.setMessageModel(MessageModel.BROADCASTING);
        }

        if (customizedProperties != null) {
            addUserDefinedProperties(customizedProperties);
        }

        logger.info("consumer {} start with param {}", instanceName, buildConsumerInfo(consumer));
        try {
            if (CollectionUtil.isNotEmpty(tags)) {
                String combinedTags = StringUtils.join(tags, "||");
                logger.info("consumer {} start with tags {}", instanceName, combinedTags);
                consumer.subscribe(bindingTopic, combinedTags);
            } else {
                consumer.subscribe(bindingTopic, "*");
            }

        } catch (MQClientException e) {
            logger.error("RocketMQConsumer register {} error", bindingTopic, e);
            throw ZmsException.CONSUMER_START_EXCEPTION;
        }
    }

    @Override
    protected void consumerShutdown() {
        consumer.shutdown();
    }

    @Override
    public void addUserDefinedProperties(Properties properties) {
        if (properties.containsKey(ZmsConst.CLIENT_CONFIG.CONSUME_MESSAGES_SIZE)) {
            consumer.setPullBatchSize(
                    Integer.parseInt((String) properties.get(ZmsConst.CLIENT_CONFIG.CONSUME_MESSAGES_SIZE)));
        }

        if (properties.containsKey(ZmsConst.CLIENT_CONFIG.ROCKETMQ_CONSUME_BATCH)) {
            consumer.setConsumeMessageBatchMaxSize(
                    Integer.parseInt((String) properties.get(ZmsConst.CLIENT_CONFIG.ROCKETMQ_CONSUME_BATCH)));
        }
        if (properties.containsKey(ZmsConst.CLIENT_CONFIG.CONSUME_THREAD_MIN)) {
            int consumeThreadMin = Integer.parseInt((String) properties.get(ZmsConst.CLIENT_CONFIG.CONSUME_THREAD_MIN));
            consumer.setConsumeThreadMin(consumeThreadMin);
        }

        if (properties.containsKey(ZmsConst.CLIENT_CONFIG.CONSUME_THREAD_MAX)) {
            int consumeThreadMax = Integer.parseInt((String) properties.get(ZmsConst.CLIENT_CONFIG.CONSUME_THREAD_MAX));
            consumer.setConsumeThreadMax(consumeThreadMax);
        }
    }


    private String buildConsumerInfo(DefaultMQPushConsumer consumer) {
        StringBuilder consumerInfo = new StringBuilder();
        consumerInfo.append(" clientIP: ").append(consumer.getClientIP());
        consumerInfo.append(System.lineSeparator());
        consumerInfo.append(" nameSrv: ").append(consumer.getNamesrvAddr());
        consumerInfo.append(System.lineSeparator());
        consumerInfo.append(" batchSize: ").append(consumer.getPullBatchSize());
        consumerInfo.append(System.lineSeparator());
        consumerInfo.append(" consumeThreadMin: ").append(consumer.getConsumeThreadMin());
        consumerInfo.append(System.lineSeparator());
        consumerInfo.append(" consumeThreadMax: ").append(consumer.getConsumeThreadMax());
        return consumerInfo.toString();
    }

}








