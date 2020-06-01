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

import com.zto.zms.client.common.ZmsMessage;
import com.zto.zms.client.config.ProducerConfig;
import com.zto.zms.logger.ZmsLogger;
import com.zto.zms.metadata.ZmsMetadata;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.springside.modules.utils.net.NetUtil;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Created by superheizai on 2017/7/27.
 */
public class KafkaProducerProxy extends ZmsProducerProxy {
    public static final Logger logger = ZmsLogger.log;

    private KafkaProducer producer;

    private int sendTimeOut = 3000;

    public KafkaProducerProxy(ZmsMetadata metadata, boolean isOrderly, String instanceName) {
        super(metadata, isOrderly, instanceName);
        this.instanceName = instanceName;
        start();
    }

    public KafkaProducerProxy(ZmsMetadata metadata, String instanceName) {
        this(metadata,false, instanceName);
    }

    public KafkaProducerProxy(ZmsMetadata metadata, boolean isOrderly, String instanceName, Properties properties) {
        super(metadata, isOrderly, instanceName, properties);
        this.instanceName = instanceName;
        start();
    }

    @Override
    public void startProducer() {
        Properties kafkaProperties = new Properties();
        kafkaProperties.putAll(ProducerConfig.KAFKA.KAFKA_CONFIG);
        if (metadata.isGatedLaunch()) {
            kafkaProperties.put("bootstrap.servers", metadata.getGatedCluster().getBootAddr());
        } else {
            kafkaProperties.put("bootstrap.servers", metadata.getClusterMetadata().getBootAddr());
        }
        kafkaProperties.put("client.id", metadata.getName() + "--" + NetUtil.getLocalHost() + "--" + ThreadLocalRandom.current().nextInt(100000));
        if (customizedProperties != null) {
            kafkaProperties.putAll(customizedProperties);
        }
        reviseKafkaConfig(kafkaProperties);
        producer = new KafkaProducer(kafkaProperties);
    }

    private void reviseKafkaConfig(Properties properties) {
        if (properties.containsKey(ProducerConfig.TIMEOUT)) {
            int timeout = (Integer) properties.get(ProducerConfig.TIMEOUT);
            sendTimeOut = timeout;

            String acks = (String) properties.get("acks");
            if (StringUtils.isNotEmpty(acks) && (("0".equals(acks) || "1".equals(acks)))) {
                properties.put("request.timeout.ms", timeout);
            } else {
                logger.info("timeout only take effect for acks=0 or 1");
            }
            properties.remove(ProducerConfig.TIMEOUT);
        }

    }

    @Override
    public void shutdownProducer() {
        producer.close(Duration.ofSeconds(3));
    }


    @Override
    public SendResult syncSend(ZmsMessage zmsMessage) {
        if (!running) {
            return SendResult.FAILURE_NOTRUNNING;
        }
        ProducerRecord<String, byte[]> record = new ProducerRecord(metadata.getName(), zmsMessage.getKey(), zmsMessage.getPayload());
        long startTime = System.currentTimeMillis();
        try {
            Future<RecordMetadata> sendResult = producer.send(record);
            zmsMetrics.msgBody().markSize(record.value().length);
            RecordMetadata metadata = sendResult.get(sendTimeOut, TimeUnit.MILLISECONDS);
            long duration = System.currentTimeMillis() - startTime;
            zmsMetrics.sendCostRate().update(duration, TimeUnit.MILLISECONDS);
            zmsMetrics.messageSuccessRate().mark();
            zmsMetrics.getDistribution().markTime(duration);
            return SendResult.buildSuccessResult(metadata.offset(), "", metadata.topic(), metadata.partition());
        } catch (InterruptedException e) {
            zmsMetrics.messageFailureRate().mark();
            ZmsLogger.log.error("produce syncSend and wait interuptted", e);
            return SendResult.FAILURE_INTERUPRION;
        } catch (ExecutionException e) {
            zmsMetrics.messageFailureRate().mark();
            ZmsLogger.log.error("produce syncSend and wait got exception", e);
            if (e.getCause() instanceof org.apache.kafka.common.errors.TimeoutException) {
                return SendResult.FAILURE_TIMEOUT;
            } else {
                String errMsg = "execution got exception when syncSend and wait message: ";
                if (e.getCause() != null && StringUtils.isNoneBlank(e.getCause().getMessage())) {
                    errMsg += e.getCause().getMessage();
                }
                return SendResult.buildErrorResult(errMsg);
            }
        } catch (TimeoutException e) {
            ZmsLogger.log.error("produce syncSend and wait timeout", e);
            zmsMetrics.messageFailureRate().mark();
            return SendResult.FAILURE_TIMEOUT;
        }
    }


    @Override
    public void asyncSend(ZmsMessage zmsMessage, SendCallback callBack) {
        ProducerRecord<String, byte[]> record = new ProducerRecord(metadata.getName(), zmsMessage.getKey(), zmsMessage.getPayload());
        long startTime = System.currentTimeMillis();
        zmsMetrics.msgBody().markSize(record.value().length);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                zmsMetrics.messageFailureRate().mark();
                callBack.onException(exception);
                return;
            }
            long duration = System.currentTimeMillis() - startTime;
            SendResult sendResponse = SendResult.buildSuccessResult(metadata.offset(), "", metadata.topic(), metadata.partition());
            zmsMetrics.sendCostRate().update(duration, TimeUnit.MILLISECONDS);
            zmsMetrics.messageSuccessRate().mark();
            zmsMetrics.getDistribution().markTime(duration);
            callBack.onResult(sendResponse);
        });
    }

    @Override
    public void oneway(ZmsMessage zmsMessage) {
        zmsMetrics.msgBody().markSize(zmsMessage.getPayload().length);
        producer.send(new ProducerRecord(metadata.getName(), zmsMessage.getPayload()));
    }


}


