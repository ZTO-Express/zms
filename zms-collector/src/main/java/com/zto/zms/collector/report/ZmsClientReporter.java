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

package com.zto.zms.collector.report;


import com.alibaba.fastjson.JSON;
import com.zto.zms.client.Zms;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.client.consumer.ConsumeMessage;
import com.zto.zms.client.consumer.MessageListener;
import com.zto.zms.client.consumer.MsgConsumedStatus;
import com.zto.zms.stats.ConsumerStats;
import com.zto.zms.stats.ProducerStats;
import com.zto.zms.service.config.ZmsConsumerConf;
import com.zto.zms.service.domain.MetricsDo;
import com.zto.zms.service.influx.InfluxdbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;

/**
 * <p>Class: ZmsClientReporter</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2019/11/13
 **/
@Component
public class ZmsClientReporter {

    private static Logger logger = LoggerFactory.getLogger(ZmsClientReporter.class);
    @Autowired
    private InfluxdbClient influxdbClient;
    @Autowired
    MetricsTransformService metricsTransformService;
    @Autowired
    private ZmsConsumerConf zmsConsumerConf;

    public void refreshZmsClientConsumerInfo() {
        try {
            Zms.subscribe(zmsConsumerConf.getConsumerInfoConsumer(), new MessageListener() {
                @Override
                public MsgConsumedStatus onMessage(ConsumeMessage consumeMessage) {
                    String consumerInfoMsg = new String(consumeMessage.getPayload());
                    try {
                        ConsumerStats info = JSON.parseObject(consumerInfoMsg, ConsumerStats.class);
                        List<MetricsDo> metricsLst = metricsTransformService.transform(info);
                        Long timestamp = System.currentTimeMillis();
                        if (null != info.getTimestamp()) {
                            timestamp = info.getTimestamp();
                        }
                        influxdbClient.writeData(metricsLst, ZmsConst.Measurement.STATISTIC_TOPIC_CONSUMER_INFO, timestamp);
                    } catch (Exception e) {
                        logger.error(MessageFormat.format("ConsumerStatisticsInfo process error,errMsg={0}, consumerInfoMsg={1}", e, consumerInfoMsg), e);
                    }
                    return MsgConsumedStatus.SUCCEED;
                }
            });
            logger.info("subscribe consumerInfo group");
        } catch (Exception e) {
            logger.error("subscribe consumerInfo group", e);
        }
    }

    public void refreshZmsClientProducerInfo() {
        try {
            Zms.subscribe(zmsConsumerConf.getProducerInfoConsumer(), new MessageListener() {
                @Override
                public MsgConsumedStatus onMessage(ConsumeMessage consumeMessage) {
                    String consumerInfoMsg = new String(consumeMessage.getPayload());
                    try {
                        ProducerStats info = JSON.parseObject(consumerInfoMsg, ProducerStats.class);
                        List<MetricsDo> metricsLst = metricsTransformService.transform(info);
                        Long timestamp = System.currentTimeMillis();
                        if (null != info.getTimestamp()) {
                            timestamp = info.getTimestamp();
                        }
                        influxdbClient.writeData(metricsLst, ZmsConst.Measurement.STATISTIC_TOPIC_PRODUCER_INFO, timestamp);
                    } catch (Exception e) {
                        logger.error(MessageFormat.format("ProducerStatisticInfo process error, errMsg={0}, consumerInfoMsg={1}", e, consumerInfoMsg),e);
                    }
                    return MsgConsumedStatus.SUCCEED;
                }
            });
            logger.info("subscribe producerInfo group");
        } catch (Exception e) {
            logger.error("subscribe producerInfo group", e);
        }
    }

}

