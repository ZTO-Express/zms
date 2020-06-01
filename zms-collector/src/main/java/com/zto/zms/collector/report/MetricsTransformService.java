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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zto.zms.stats.*;
import com.zto.zms.collector.kafka.mbean.MBeanRateInfo;
import com.zto.zms.collector.model.KafkaMetrics;
import com.zto.zms.collector.model.*;
import com.zto.zms.service.domain.MetricsDo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by liangyong on 2018/9/17.
 */

@Service
public class MetricsTransformService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsTransformService.class);

    private Map<String, Long> cacheMap = Maps.newHashMap();

    public List<MetricsDo> transformMqRtInfo(ClusterRtTime info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        if (null == info.getTimes()) {
            return metricsDoList;
        }
        info.getTimes().forEach((timer) -> {
            MetricsDo<Double> metricsDo = new MetricsDo<Double>();
            Map<String, String> tagOptions = metricsDo.getTagOptions();
            Map<String, Double> segmentMap = metricsDo.getSegmentMap();
            tagOptions.put("clusterName", info.getCluster());
            tagOptions.put("brokerName", timer.getBrokerName());
            segmentMap.put("result", (double) timer.getResult());
            segmentMap.put("rt", (double) timer.getRt());
            metricsDo.setTagOptions(tagOptions);
            metricsDo.setSegmentMap(segmentMap);
            metricsDoList.add(metricsDo);
        });
        return metricsDoList;
    }

    public List<MetricsDo> transformStrDataKafkaCluster(KafkaMetrics info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        if (null == info) {
            return metricsDoList;
        }
        MetricsDo<String> metricsDo = new MetricsDo<String>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, String> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        segmentMap.put("controllerHost", info.getControllerHost());
        segmentMap.put("controllerId", String.valueOf(info.getControllerId()));
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        metricsDoList.add(metricsDo);
        return metricsDoList;
    }

    public List<MetricsDo> transformDoubleDataKafkaCluster(KafkaMetrics info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        if (null == info || null == info.getBrokersMetrics()) {
            return metricsDoList;
        }
        double totalTps = 0d;
        Map<String, KafkaBrokerInfo> brokerInfoMap = info.getBrokersMetrics();
        for (Map.Entry<String, KafkaBrokerInfo> entry : brokerInfoMap.entrySet()) {
            KafkaBrokerInfo kafkaBrokerInfo = entry.getValue();
            if (null == kafkaBrokerInfo.getMessagesInPerSec()) {
                continue;
            }
            totalTps = addCalc(totalTps, kafkaBrokerInfo.getMessagesInPerSec().getOneMinuteRate());
        }
        return clusterTpsMetrics(metricsDoList, info.getClusterName(), totalTps);
    }

    private double addCalc(double value1, double value2) {
        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return b1.add(b2).doubleValue();
    }

    public List<MetricsDo> transformMqCluster(RocketmqStatus info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        if (null == info) {
            return metricsDoList;
        }
        return clusterTpsMetrics(metricsDoList, info.getClusterName(), info.getTotalTps());
    }

    private List<MetricsDo> clusterTpsMetrics(List<MetricsDo> metricsDoList, String clusterName, double totalTps) {
        MetricsDo<Double> metricsDo = new MetricsDo<>();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        tagOptions.put("clusterName", clusterName);
        segmentMap.put("totalTps", totalTps);
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        metricsDoList.add(metricsDo);
        return metricsDoList;
    }

    public List<MetricsDo> transformMqBroker(RocketmqStatus info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        if (null == info.getBrokers()) {
            return metricsDoList;
        }
        info.getBrokers().forEach((broker) -> {
            MetricsDo<Double> metricsDo = new MetricsDo<Double>();
            Map<String, String> tagOptions = metricsDo.getTagOptions();
            Map<String, Double> segmentMap = metricsDo.getSegmentMap();
            tagOptions.put("clusterName", info.getClusterName());
            tagOptions.put("brokerName", broker.getBrokerName());
            segmentMap.put("bootTime", broker.getBootTime());
            segmentMap.put("brokerId", (double) broker.getBrokerId());
            segmentMap.put("getTotalTps", broker.getGetTotalTps());
            segmentMap.put("msgGetTotalTodayNow", (double) broker.getMsgGetTotalTodayNow());
            segmentMap.put("msgPutTotalTodayNow", (double) broker.getMsgPutTotalTodayNow());
            segmentMap.put("pageCacheLockTimeMillis", (double) broker.getPageCacheLockTimeMillis());
            segmentMap.put("pullThreadPoolQueueCapacity", (double) broker.getPullThreadPoolQueueCapacity());
            segmentMap.put("pullThreadPoolQueueHeadWaitTimeMills", (double) broker.getPullThreadPoolQueueHeadWaitTimeMills());
            segmentMap.put("pullThreadPoolQueueSize", (double) broker.getPullThreadPoolQueueSize());
            segmentMap.put("putTps", broker.getPutTps());
            segmentMap.put("sendThreadPoolQueueCapacity", (double) broker.getSendThreadPoolQueueCapacity());
            segmentMap.put("sendThreadPoolQueueSize", (double) broker.getSendThreadPoolQueueSize());
            metricsDo.setTagOptions(tagOptions);
            metricsDo.setSegmentMap(segmentMap);
            metricsDoList.add(metricsDo);
        });
        return metricsDoList;
    }

    public List<MetricsDo> transformStringDataMqBroker(RocketmqStatus info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        if (null == info.getBrokers()) {
            return metricsDoList;
        }
        info.getBrokers().forEach((broker) -> {
            MetricsDo<String> metricsStrDo = new MetricsDo<>();
            Map<String, String> tagOptions = metricsStrDo.getTagOptions();
            Map<String, String> segmentStrMap = metricsStrDo.getSegmentMap();
            tagOptions.put("clusterName", info.getClusterName());
            tagOptions.put("brokerName", broker.getBrokerName());
            segmentStrMap.put("ip", broker.getIp());
            segmentStrMap.put("putMessageDistributeTime", broker.getPutMessageDistributeTime());
            metricsStrDo.setTagOptions(tagOptions);
            metricsStrDo.setSegmentMap(segmentStrMap);
            metricsDoList.add(metricsStrDo);
        });
        return metricsDoList;
    }


    public List<MetricsDo> transformMqTopic(RocketmqStatus info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        if (null == info.getTopicInfos()) {
            return metricsDoList;
        }
        for (Map.Entry<String, TopicInfo> entry : info.getTopicInfos().entrySet()) {
            MetricsDo<Double> metricsDo = new MetricsDo<Double>();
            Map<String, String> tagOptions = metricsDo.getTagOptions();
            Map<String, Double> segmentMap = metricsDo.getSegmentMap();
            tagOptions.put("clusterName", info.getClusterName());
            tagOptions.put("topicName", entry.getKey());
            segmentMap.put("cntInToday", entry.getValue().getCntInToday());
            segmentMap.put("tps", entry.getValue().getTps());
            metricsDo.setTagOptions(tagOptions);
            metricsDo.setSegmentMap(segmentMap);
            metricsDoList.add(metricsDo);
        }
        return metricsDoList;
    }

    public List<MetricsDo> transformMqConsumer(RocketmqStatus info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        if (null == info.getConsumerInfos()) {
            return metricsDoList;
        }
        for (Map.Entry<String, com.zto.zms.collector.model.ConsumerInfo> entry : info.getConsumerInfos().entrySet()) {
            MetricsDo<Double> metricsDo = new MetricsDo<Double>();
            Map<String, String> tagOptions = metricsDo.getTagOptions();
            Map<String, Double> segmentMap = metricsDo.getSegmentMap();
            tagOptions.put("clusterName", info.getClusterName());
            tagOptions.put("consumerGroup", entry.getKey());
            segmentMap.put("lastConsumingTime", (double) entry.getValue().getLastConsumingTime());
            segmentMap.put("latency", (double) entry.getValue().getLatency());
            segmentMap.put("tps", entry.getValue().getTps());
            metricsDo.setTagOptions(tagOptions);
            metricsDo.setSegmentMap(segmentMap);
            metricsDoList.add(metricsDo);
        }
        return metricsDoList;
    }

    public List<MetricsDo> transformStrDataMqConsumer(RocketmqStatus info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        if (null == info.getConsumerInfos()) {
            return metricsDoList;
        }
        for (Map.Entry<String, com.zto.zms.collector.model.ConsumerInfo> entry : info.getConsumerInfos().entrySet()) {
            MetricsDo<String> metricsStrDo = new MetricsDo<>();
            Map<String, String> tagOptions = metricsStrDo.getTagOptions();
            Map<String, String> segmentStrMap = metricsStrDo.getSegmentMap();
            tagOptions.put("clusterName", info.getClusterName());
            tagOptions.put("consumerGroup", entry.getKey());
            List<String> consumers = entry.getValue().getConsumers();
            StringBuilder builder = new StringBuilder();
            if (!CollectionUtils.isEmpty(consumers)) {
                for (int i = 0; i < consumers.size(); i++) {
                    builder.append(consumers.get(i));
                    if (i != (consumers.size() - 1)) {
                        builder.append(",");
                    }
                }
            }
            segmentStrMap.put("consumers", consumers.toString());
            metricsStrDo.setTagOptions(tagOptions);
            metricsStrDo.setSegmentMap(segmentStrMap);
            metricsDoList.add(metricsStrDo);
        }
        return metricsDoList;
    }


    public List<MetricsDo> transformKafkaTopic(KafkaMetrics info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        for (TopicMetrics topicMetrics : info.getTopicMetricsInfo()) {
            metricsDoList.add(getMessagesInPerSecMetricsDo(info, topicMetrics));
            metricsDoList.add(getBytesOutPerSecMetricsDo(info, topicMetrics));
            metricsDoList.add(getBytesInPerSecMetricsDo(info, topicMetrics));
            metricsDoList.add(getFailedProduceRequestsPerSecMetricsDo(info, topicMetrics));
            metricsDoList.add(getFailedFetchRequestsPerSecMetricsDo(info, topicMetrics));
            metricsDoList.add(getBytesRejectedPerSecMetricsDo(info, topicMetrics));
            TopicBrokerMetricsInfo topicBrokerMetricsInfo = topicMetrics.getTopicBrokerMetricsInfo();
            Map<String, TopicMetrics> topicBrokerInfoMap = topicBrokerMetricsInfo.getTopicBrokerMetrics();
            for (Map.Entry<String, TopicMetrics> entry : topicBrokerInfoMap.entrySet()) {
                metricsDoList.add(getTopicBrokerMessagesInPerSecMetricsDo(info.getClusterName(), topicMetrics.getTopic(), entry));
                metricsDoList.add(getTopicBrokerBytesOutPerSecMetricsDo(info.getClusterName(), topicMetrics.getTopic(), entry));
                metricsDoList.add(getTopicBrokerBytesInPerSecMetricsDo(info.getClusterName(), topicMetrics.getTopic(), entry));
                metricsDoList.add(getTopicBrokerFailedProduceRequestsPerSecMetricsDo(info.getClusterName(), topicMetrics.getTopic(), entry));
                metricsDoList.add(getTopicBrokerFailedFetchRequestsPerSecMetricsDo(info.getClusterName(), topicMetrics.getTopic(), entry));
                metricsDoList.add(getTopicBrokerBytesRejectedPerSecMetricsDo(info.getClusterName(), topicMetrics.getTopic(), entry));
            }
        }
        return metricsDoList;
    }

    private MetricsDo getTopicBrokerBytesRejectedPerSecMetricsDo(String clusterName, String topicName, Map.Entry<String, TopicMetrics> entry) {
        String brokerName = entry.getKey();
        TopicMetrics kafkaTopicBrokerInfo = entry.getValue();
        if (null == kafkaTopicBrokerInfo.getBytesRejectedPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", clusterName);
        tagOptions.put("topicName", topicName);
        tagOptions.put("brokerName", brokerName);
        addBytesRejectedPerSecMetrics(tagOptions, segmentMap, kafkaTopicBrokerInfo.getBytesRejectedPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private void addBytesRejectedPerSecMetrics(Map<String, String> tagOptions, Map<String, Double> segmentMap, MBeanRateInfo bytesRejectedPerSec) {
        tagOptions.put("attributeName", "BytesRejectedPerSec");
        addPerSecMetrics(segmentMap, bytesRejectedPerSec);
    }

    private MetricsDo getTopicBrokerFailedFetchRequestsPerSecMetricsDo(String clusterName, String topicName, Map.Entry<String, TopicMetrics> entry) {
        String brokerName = entry.getKey();
        TopicMetrics kafkaTopicBrokerInfo = entry.getValue();
        if (null == kafkaTopicBrokerInfo.getFailedFetchRequestsPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", clusterName);
        tagOptions.put("topicName", topicName);
        tagOptions.put("brokerName", brokerName);
        addFailedFetchRequestsPerSecMetrics(tagOptions, segmentMap, kafkaTopicBrokerInfo.getFailedFetchRequestsPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private void addFailedFetchRequestsPerSecMetrics(Map<String, String> tagOptions, Map<String, Double> segmentMap, MBeanRateInfo failedFetchRequestsPerSec) {
        tagOptions.put("attributeName", "FailedFetchRequestsPerSec");
        addPerSecMetrics(segmentMap, failedFetchRequestsPerSec);
    }

    private void addPerSecMetrics( Map<String, Double> segmentMap, MBeanRateInfo failedFetchRequestsPerSec) {
        addMinuteRateMetrics(segmentMap, failedFetchRequestsPerSec);
    }

    private MetricsDo getTopicBrokerFailedProduceRequestsPerSecMetricsDo(String clusterName, String topicName, Map.Entry<String, TopicMetrics> entry) {
        String brokerName = entry.getKey();
        TopicMetrics kafkaTopicBrokerInfo = entry.getValue();
        if (null == kafkaTopicBrokerInfo.getFailedProduceRequestsPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", clusterName);
        tagOptions.put("topicName", topicName);
        tagOptions.put("brokerName", brokerName);
        addFailedProduceRequestsPerSec(kafkaTopicBrokerInfo.getFailedProduceRequestsPerSec(), tagOptions, segmentMap);
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo<Double> getTopicBrokerBytesInPerSecMetricsDo(String clusterName, String topicName, Map.Entry<String, TopicMetrics> entry) {
        String brokerName = entry.getKey();
        TopicMetrics kafkaTopicBrokerInfo = entry.getValue();
        if (null == kafkaTopicBrokerInfo.getBytesInPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", clusterName);
        tagOptions.put("topicName", topicName);
        addBytesInPerSecMetrics(brokerName, tagOptions, segmentMap, kafkaTopicBrokerInfo.getBytesInPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getTopicBrokerBytesOutPerSecMetricsDo(String clusterName, String topicName, Map.Entry<String, TopicMetrics> entry) {
        String brokerName = entry.getKey();
        TopicMetrics kafkaTopicBrokerInfo = entry.getValue();
        if (null == kafkaTopicBrokerInfo.getBytesOutPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", clusterName);
        tagOptions.put("topicName", topicName);
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "BytesOutPerSec");
        addMinuteRateMetrics(segmentMap, kafkaTopicBrokerInfo.getBytesOutPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getTopicBrokerMessagesInPerSecMetricsDo(String clusterName, String topicName, Map.Entry<String, TopicMetrics> entry) {
        String brokerName = entry.getKey();
        TopicMetrics kafkaTopicBrokerInfo = entry.getValue();
        if (null == kafkaTopicBrokerInfo.getMessagesInPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", clusterName);
        tagOptions.put("topicName", topicName);
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "MessagesInPerSec");
        addMinuteRateMetrics(segmentMap, kafkaTopicBrokerInfo.getMessagesInPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getBytesRejectedPerSecMetricsDo(KafkaMetrics info, TopicMetrics topicMetrics) {
        if (null == topicMetrics.getBytesRejectedPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("topicName", topicMetrics.getTopic());
        tagOptions.put("attributeName", "BytesRejectedPerSec");
        addMinuteRateMetrics(segmentMap, topicMetrics.getBytesRejectedPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getFailedFetchRequestsPerSecMetricsDo(KafkaMetrics info, TopicMetrics topicMetrics) {
        if (null == topicMetrics.getFailedProduceRequestsPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("topicName", topicMetrics.getTopic());
        tagOptions.put("attributeName", "FailedFetchRequestsPerSec");
        addMinuteRateMetrics(segmentMap, topicMetrics.getFailedProduceRequestsPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getFailedProduceRequestsPerSecMetricsDo(KafkaMetrics info, TopicMetrics topicMetrics) {
        if (null == topicMetrics.getFailedProduceRequestsPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("topicName", topicMetrics.getTopic());
        addFailedProduceRequestsPerSec(topicMetrics.getFailedProduceRequestsPerSec(), tagOptions, segmentMap);
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private void addFailedProduceRequestsPerSec(MBeanRateInfo failedProduceRequestsPerSec, Map<String, String> tagOptions, Map<String, Double> segmentMap) {
        tagOptions.put("attributeName", "FailedProduceRequestsPerSec");
        addPerSecMetrics(segmentMap, failedProduceRequestsPerSec);
    }

    private MetricsDo getBytesInPerSecMetricsDo(KafkaMetrics info, TopicMetrics topicMetrics) {
        if (null == topicMetrics.getBytesInPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("topicName", topicMetrics.getTopic());
        tagOptions.put("attributeName", "BytesInPerSec");
        addMinuteRateMetrics(segmentMap, topicMetrics.getBytesInPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getBytesOutPerSecMetricsDo(KafkaMetrics info, TopicMetrics topicMetrics) {
        if (null == topicMetrics.getBytesOutPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("topicName", topicMetrics.getTopic());
        tagOptions.put("attributeName", "BytesOutPerSec");
        addMinuteRateMetrics(segmentMap, topicMetrics.getBytesOutPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getMessagesInPerSecMetricsDo(KafkaMetrics info, TopicMetrics topicMetrics) {
        if (null == topicMetrics.getMessagesInPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("topicName", topicMetrics.getTopic());
        tagOptions.put("attributeName", "MessagesInPerSec");
        addMinuteRateMetrics(segmentMap, topicMetrics.getMessagesInPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    public List<MetricsDo> transformKafkaEnv(KafkaMetrics info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        Map<String, KafkaBrokerInfo> brokerInfoMap = info.getBrokersMetrics();
        for (Map.Entry<String, KafkaBrokerInfo> entry : brokerInfoMap.entrySet()) {
            metricsDoList.addAll(getjvmMemoryInfoMetricsDo(info, entry));
            metricsDoList.add(getOperatingSystemMetricsDo(info, entry));
            metricsDoList.add(getNetworkProcessorAvgIdlePercentMetricsDo(info, entry));
            metricsDoList.add(getThreadInfoMetricsDo(info, entry));
            metricsDoList.add(getG1YoungGenerationMetricsDo(info, entry));
            metricsDoList.add(getG1OldGenerationMetricsDo(info, entry));
            metricsDoList.addAll(getNioInfoMetricsDo(info, entry));
        }
        return metricsDoList;
    }

    private MetricsDo getG1OldGenerationMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getG1OldGeneration()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "G1 Old Generation");
        segmentMap.put("collectionCount", (double) brokerInfo.getG1OldGeneration().getCollectionCount());
        segmentMap.put("collectionTime", (double) brokerInfo.getG1OldGeneration().getCollectionTime());
        segmentMap.put("lastGcDuration", (double) brokerInfo.getG1OldGeneration().getLastGcDuration());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getG1YoungGenerationMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getG1YoungGeneration()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "G1 Young Generation");
        segmentMap.put("collectionCount", (double) brokerInfo.getG1YoungGeneration().getCollectionCount());
        segmentMap.put("collectionTime", (double) brokerInfo.getG1YoungGeneration().getCollectionTime());
        segmentMap.put("lastGcDuration", (double) brokerInfo.getG1YoungGeneration().getLastGcDuration());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private List<MetricsDo<Double>> getNioInfoMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        List<MetricsDo<Double>> metricsDoList = Lists.newArrayList();
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getNioInfo()) {
            return metricsDoList;
        }
        brokerInfo.getNioInfo().forEach((nioInfo) -> {
            MetricsDo<Double> metricsDo = new MetricsDo<Double>();
            Map<String, String> tagOptions = metricsDo.getTagOptions();
            Map<String, Double> segmentMap = metricsDo.getSegmentMap();
            tagOptions.put("clusterName", info.getClusterName());
            tagOptions.put("brokerName", brokerName);
            tagOptions.put("attributeName", nioInfo.getName());
            segmentMap.put("capacity", (double) nioInfo.getCapacity());
            segmentMap.put("count", (double) nioInfo.getCount());
            segmentMap.put("used", (double) nioInfo.getUsed());
            metricsDo.setTagOptions(tagOptions);
            metricsDo.setSegmentMap(segmentMap);
            metricsDoList.add(metricsDo);
        });
        return metricsDoList;
    }

    private MetricsDo getThreadInfoMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getThreadInfo()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "ThreadInfo");
        segmentMap.put("peakThreadCount", (double) brokerInfo.getThreadInfo().getPeakThreadCount());
        segmentMap.put("threadCount", (double) brokerInfo.getThreadInfo().getThreadCount());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getNetworkProcessorAvgIdlePercentMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getNetworkProcessorAvgIdlePercent()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "NetworkProcessorAvgIdlePercent");
        segmentMap.put("value", brokerInfo.getNetworkProcessorAvgIdlePercent().getValue());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getOperatingSystemMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getOperatingSystem()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "OperatingSystem");
        segmentMap.put("freePhysicalMemorySize", (double) brokerInfo.getOperatingSystem().getFreePhysicalMemorySize());
        segmentMap.put("maxFileDescriptorCount", (double) brokerInfo.getOperatingSystem().getMaxFileDescriptorCount());
        segmentMap.put("openFileDescriptorCount", (double) brokerInfo.getOperatingSystem().getOpenFileDescriptorCount());
        segmentMap.put("processCpuLoad", brokerInfo.getOperatingSystem().getProcessCpuLoad());
        segmentMap.put("systemCpuLoad", brokerInfo.getOperatingSystem().getSystemCpuLoad());
        segmentMap.put("systemLoadAverage", brokerInfo.getOperatingSystem().getSystemLoadAverage());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private List<MetricsDo<Double>> getjvmMemoryInfoMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        List<MetricsDo<Double>> metricsDoList = Lists.newArrayList();
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getJvmMemoryInfo()) {
            return metricsDoList;
        }
        brokerInfo.getJvmMemoryInfo().forEach((jvmMemoryInfo) -> {
            MetricsDo<Double> metricsDo = new MetricsDo<Double>();
            Map<String, Double> segmentMap = metricsDo.getSegmentMap();
            Map<String, String> tagOptions = metricsDo.getTagOptions();
            tagOptions.put("clusterName", info.getClusterName());
            tagOptions.put("brokerName", brokerName);
            tagOptions.put("attributeName", jvmMemoryInfo.getName());
            segmentMap.put("commited", (double) jvmMemoryInfo.getCommited());
            segmentMap.put("inited", (double) jvmMemoryInfo.getInited());
            segmentMap.put("max", (double) jvmMemoryInfo.getMax());
            segmentMap.put("used", (double) jvmMemoryInfo.getUsed());
            metricsDo.setTagOptions(tagOptions);
            metricsDo.setSegmentMap(segmentMap);
            metricsDoList.add(metricsDo);
        });
        return metricsDoList;
    }


    public List<MetricsDo> transformKafkaBroker(KafkaMetrics info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        Map<String, KafkaBrokerInfo> brokerInfoMap = info.getBrokersMetrics();
        for (Map.Entry<String, KafkaBrokerInfo> entry : brokerInfoMap.entrySet()) {
            metricsDoList.add(getFetchFollowerTotalTimeMsMetricsDo(info, entry));
            metricsDoList.add(getActiveControllerCountMetricsDo(info, entry));
            metricsDoList.add(getBytesRejectedPerSecMetricsDo(info, entry));
            metricsDoList.add(getProduceRequestsPerSecMetricsDo(info, entry));
            metricsDoList.add(getIsrShrinksPerSecMetricsDo(info, entry));
            metricsDoList.add(getProduceRequestQueueTimeMsMetricsDo(info, entry));
            metricsDoList.add(getFetchFollowerRequestsPerSecMetricsDo(info, entry));
            metricsDoList.add(getFetchConsumerTotalTimeMsMetricsDo(info, entry));
            metricsDoList.add(getMessagesInPerSecMetricsDo(info, entry));
            metricsDoList.add(getUnderReplicatedPartitionsMetricsDo(info, entry));
            metricsDoList.add(getBytesOutPerSecMetricsDo(info, entry));
            metricsDoList.add(getLeaderCountMetricsDo(info, entry));
            metricsDoList.add(getLogFlushRateAndTimeMsMetricsDo(info, entry));
            metricsDoList.add(getBytesInPerSecMetricsDo(info, entry));
            metricsDoList.add(getRequestHandlerAvgIdlePercentMetricsDo(info, entry));
            metricsDoList.add(getFetchFollowerRequestQueueTimeMsMetricsDo(info, entry));
            metricsDoList.add(getProduceTotalTimeMsMetricsDo(info, entry));
            metricsDoList.add(getLeaderElectionRateAndTimeMsMetricsDo(info, entry));
            metricsDoList.add(getFailedProduceRequestsPerSecMetricsDo(info, entry));
            metricsDoList.add(getFetchConsumerRequestQueueTimeMsMetricsDo(info, entry));
            metricsDoList.add(getFailedFetchRequestsPerSecMetricsDo(info, entry));
            metricsDoList.add(getUncleanLeaderElectionsPerSecMetricsDo(info, entry));
            metricsDoList.add(getFetchConsumerRequestsPerSecMetricsDo(info, entry));
            metricsDoList.add(getPartitionCountMetricsDo(info, entry));
            metricsDoList.add(getOfflinePartitionsCountMetricsDo(info, entry));
        }
        return metricsDoList;
    }

    private MetricsDo getOfflinePartitionsCountMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getOfflinePartitionsCount()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "OfflinePartitionsCount");
        segmentMap.put("value", (double) brokerInfo.getOfflinePartitionsCount().getValue());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getPartitionCountMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getPartitionCount()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "PartitionCount");
        segmentMap.put("value", (double) brokerInfo.getPartitionCount().getValue());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getFetchConsumerRequestsPerSecMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getFetchConsumerRequestsPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "FetchConsumerRequestsPerSec");
        addMinuteRateMetrics(segmentMap, brokerInfo.getFetchConsumerRequestsPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getUncleanLeaderElectionsPerSecMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getUncleanLeaderElectionsPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "UncleanLeaderElectionsPerSec");
        addMinuteRateMetrics(segmentMap, brokerInfo.getUncleanLeaderElectionsPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getFailedFetchRequestsPerSecMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getFailedFetchRequestsPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        addFailedFetchRequestsPerSecMetrics(tagOptions, segmentMap, brokerInfo.getFailedFetchRequestsPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getFetchConsumerRequestQueueTimeMsMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getFetchConsumerRequestQueueTimeMs()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "FetchConsumerRequestQueueTimeMs");
        segmentMap.put("50thPercentile", brokerInfo.getFetchConsumerRequestQueueTimeMs().get_50thPercentile());
        segmentMap.put("75thPercentile", brokerInfo.getFetchConsumerRequestQueueTimeMs().get_75thPercentile());
        segmentMap.put("95thPercentile", brokerInfo.getFetchConsumerRequestQueueTimeMs().get_95thPercentile());
        segmentMap.put("98thPercentile", brokerInfo.getFetchConsumerRequestQueueTimeMs().get_98thPercentile());
        segmentMap.put("999thPercentile", brokerInfo.getFetchConsumerRequestQueueTimeMs().get_999thPercentile());
        segmentMap.put("99thPercentile", brokerInfo.getFetchConsumerRequestQueueTimeMs().get_99thPercentile());
        segmentMap.put("count", (double) brokerInfo.getFetchConsumerRequestQueueTimeMs().getCount());
        segmentMap.put("max", brokerInfo.getFetchConsumerRequestQueueTimeMs().getMax());
        segmentMap.put("mean", brokerInfo.getFetchConsumerRequestQueueTimeMs().getMean());
        segmentMap.put("min", brokerInfo.getFetchConsumerRequestQueueTimeMs().getMin());
        segmentMap.put("stdDev", brokerInfo.getFetchConsumerRequestQueueTimeMs().getStdDev());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getFailedProduceRequestsPerSecMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getFailedProduceRequestsPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        addFailedProduceRequestsPerSec(brokerInfo.getFailedProduceRequestsPerSec(), tagOptions, segmentMap);
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getLeaderElectionRateAndTimeMsMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getLeaderElectionRateAndTimeMs()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "LeaderElectionRateAndTimeMs");
        addMinuteRateMetrics(segmentMap, brokerInfo.getLeaderElectionRateAndTimeMs());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getProduceTotalTimeMsMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getProduceTotalTimeMs()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "ProduceTotalTimeMs");
        segmentMap.put("50thPercentile", brokerInfo.getProduceTotalTimeMs().get_50thPercentile());
        segmentMap.put("75thPercentile", brokerInfo.getProduceTotalTimeMs().get_75thPercentile());
        segmentMap.put("95thPercentile", brokerInfo.getProduceTotalTimeMs().get_95thPercentile());
        segmentMap.put("98thPercentile", brokerInfo.getProduceTotalTimeMs().get_98thPercentile());
        segmentMap.put("999thPercentile", brokerInfo.getProduceTotalTimeMs().get_999thPercentile());
        segmentMap.put("99thPercentile", brokerInfo.getProduceTotalTimeMs().get_99thPercentile());
        segmentMap.put("count", (double) brokerInfo.getProduceTotalTimeMs().getCount());
        segmentMap.put("max", brokerInfo.getProduceTotalTimeMs().getMax());
        segmentMap.put("mean", brokerInfo.getProduceTotalTimeMs().getMean());
        segmentMap.put("min", brokerInfo.getProduceTotalTimeMs().getMin());
        segmentMap.put("stdDev", brokerInfo.getProduceTotalTimeMs().getStdDev());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getFetchFollowerRequestQueueTimeMsMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getFetchFollowerRequestQueueTimeMs()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "FetchFollowerRequestQueueTimeMs");
        segmentMap.put("50thPercentile", brokerInfo.getFetchFollowerRequestQueueTimeMs().get_50thPercentile());
        segmentMap.put("75thPercentile", brokerInfo.getFetchFollowerRequestQueueTimeMs().get_75thPercentile());
        segmentMap.put("95thPercentile", brokerInfo.getFetchFollowerRequestQueueTimeMs().get_95thPercentile());
        segmentMap.put("98thPercentile", brokerInfo.getFetchFollowerRequestQueueTimeMs().get_98thPercentile());
        segmentMap.put("999thPercentile", brokerInfo.getFetchFollowerRequestQueueTimeMs().get_999thPercentile());
        segmentMap.put("99thPercentile", brokerInfo.getFetchFollowerRequestQueueTimeMs().get_99thPercentile());
        segmentMap.put("count", (double) brokerInfo.getFetchFollowerRequestQueueTimeMs().getCount());
        segmentMap.put("max", brokerInfo.getFetchFollowerRequestQueueTimeMs().getMax());
        segmentMap.put("mean", brokerInfo.getFetchFollowerRequestQueueTimeMs().getMean());
        segmentMap.put("min", brokerInfo.getFetchFollowerRequestQueueTimeMs().getMin());
        segmentMap.put("stdDev", brokerInfo.getFetchFollowerRequestQueueTimeMs().getStdDev());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getRequestHandlerAvgIdlePercentMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getRequestHandlerAvgIdlePercent()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "RequestHandlerAvgIdlePercent");
        addMinuteRateMetrics(segmentMap, brokerInfo.getRequestHandlerAvgIdlePercent());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getBytesInPerSecMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getBytesInPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        addBytesInPerSecMetrics(brokerName,  tagOptions, segmentMap, brokerInfo.getBytesInPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private void addBytesInPerSecMetrics(String brokerName, Map<String, String> tagOptions, Map<String, Double> segmentMap, MBeanRateInfo bytesInPerSec) {
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "BytesInPerSec");
        addMinuteRateMetrics(segmentMap, bytesInPerSec);
    }



    private MetricsDo getLogFlushRateAndTimeMsMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getLogFlushRateAndTimeMs()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "LogFlushRateAndTimeMs");
        segmentMap.put("50thPercentile", brokerInfo.getLogFlushRateAndTimeMs().get_50thPercentile());
        segmentMap.put("75thPercentile", brokerInfo.getLogFlushRateAndTimeMs().get_75thPercentile());
        segmentMap.put("95thPercentile", brokerInfo.getLogFlushRateAndTimeMs().get_95thPercentile());
        segmentMap.put("98thPercentile", brokerInfo.getLogFlushRateAndTimeMs().get_98thPercentile());
        segmentMap.put("999thPercentile", brokerInfo.getLogFlushRateAndTimeMs().get_999thPercentile());
        segmentMap.put("99thPercentile", brokerInfo.getLogFlushRateAndTimeMs().get_99thPercentile());
        segmentMap.put("count", (double) brokerInfo.getLogFlushRateAndTimeMs().getCount());
        segmentMap.put("max", brokerInfo.getLogFlushRateAndTimeMs().getMax());
        segmentMap.put("mean", brokerInfo.getLogFlushRateAndTimeMs().getMean());
        segmentMap.put("min", brokerInfo.getLogFlushRateAndTimeMs().getMin());
        segmentMap.put("stdDev", brokerInfo.getLogFlushRateAndTimeMs().getStdDev());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getLeaderCountMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getLeaderCount()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "LeaderCount");
        segmentMap.put("value", (double) brokerInfo.getLeaderCount().getValue());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getBytesOutPerSecMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getBytesOutPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "BytesOutPerSec");
        addMinuteRateMetrics(segmentMap, brokerInfo.getBytesOutPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getUnderReplicatedPartitionsMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getUnderReplicatedPartitions()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "UnderReplicatedPartitions");
        addMinuteRateMetrics(segmentMap, brokerInfo.getUnderReplicatedPartitions());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private void addMinuteRateMetrics(Map<String, Double> segmentMap, MBeanRateInfo underReplicatedPartitions) {
        segmentMap.put("count", (double) underReplicatedPartitions.getCount());
        segmentMap.put("fifteenMinuteRate", underReplicatedPartitions.getFifteenMinuteRate());
        segmentMap.put("fiveMinuteRate", underReplicatedPartitions.getFiveMinuteRate());
        segmentMap.put("meanRate", underReplicatedPartitions.getMeanRate());
        segmentMap.put("oneMinuteRate", underReplicatedPartitions.getOneMinuteRate());
    }

    private MetricsDo getMessagesInPerSecMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getMessagesInPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "MessagesInPerSec");
        addMinuteRateMetrics(segmentMap, brokerInfo.getMessagesInPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getFetchConsumerTotalTimeMsMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getFetchConsumerTotalTimeMs()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "FetchConsumerTotalTimeMs");
        segmentMap.put("50thPercentile", brokerInfo.getFetchConsumerTotalTimeMs().get_50thPercentile());
        segmentMap.put("75thPercentile", brokerInfo.getFetchConsumerTotalTimeMs().get_75thPercentile());
        segmentMap.put("95thPercentile", brokerInfo.getFetchConsumerTotalTimeMs().get_95thPercentile());
        segmentMap.put("98thPercentile", brokerInfo.getFetchConsumerTotalTimeMs().get_98thPercentile());
        segmentMap.put("999thPercentile", brokerInfo.getFetchConsumerTotalTimeMs().get_999thPercentile());
        segmentMap.put("99thPercentile", brokerInfo.getFetchConsumerTotalTimeMs().get_99thPercentile());
        segmentMap.put("count", (double) brokerInfo.getFetchConsumerTotalTimeMs().getCount());
        segmentMap.put("max", brokerInfo.getFetchConsumerTotalTimeMs().getMax());
        segmentMap.put("mean", brokerInfo.getFetchConsumerTotalTimeMs().getMean());
        segmentMap.put("min", brokerInfo.getFetchConsumerTotalTimeMs().getMin());
        segmentMap.put("stdDev", brokerInfo.getFetchConsumerTotalTimeMs().getStdDev());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getFetchFollowerRequestsPerSecMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getFetchFollowerRequestsPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "FetchFollowerRequestsPerSec");
        addMinuteRateMetrics(segmentMap, brokerInfo.getFetchFollowerRequestsPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getProduceRequestQueueTimeMsMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getProduceRequestQueueTimeMs()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "ProduceRequestQueueTimeMs");
        segmentMap.put("50thPercentile", brokerInfo.getProduceRequestQueueTimeMs().get_50thPercentile());
        segmentMap.put("75thPercentile", brokerInfo.getProduceRequestQueueTimeMs().get_75thPercentile());
        segmentMap.put("95thPercentile", brokerInfo.getProduceRequestQueueTimeMs().get_95thPercentile());
        segmentMap.put("98thPercentile", brokerInfo.getProduceRequestQueueTimeMs().get_98thPercentile());
        segmentMap.put("999thPercentile", brokerInfo.getProduceRequestQueueTimeMs().get_999thPercentile());
        segmentMap.put("99thPercentile", brokerInfo.getProduceRequestQueueTimeMs().get_99thPercentile());
        segmentMap.put("count", (double) brokerInfo.getProduceRequestQueueTimeMs().getCount());
        segmentMap.put("max", brokerInfo.getProduceRequestQueueTimeMs().getMax());
        segmentMap.put("mean", brokerInfo.getProduceRequestQueueTimeMs().getMean());
        segmentMap.put("min", brokerInfo.getProduceRequestQueueTimeMs().getMin());
        segmentMap.put("stdDev", brokerInfo.getProduceRequestQueueTimeMs().getStdDev());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getIsrShrinksPerSecMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getIsrShrinksPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "IsrShrinksPerSec");
        addMinuteRateMetrics(segmentMap, brokerInfo.getIsrShrinksPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getProduceRequestsPerSecMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getProduceRequestsPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "ProduceRequestsPerSec");
        addMinuteRateMetrics(segmentMap, brokerInfo.getProduceRequestsPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getBytesRejectedPerSecMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getBytesRejectedPerSec()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        addBytesRejectedPerSecMetrics(tagOptions, segmentMap, brokerInfo.getBytesRejectedPerSec());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getActiveControllerCountMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getActiveControllerCount()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "ActiveControllerCount");
        segmentMap.put("value", (double) brokerInfo.getActiveControllerCount().getValue());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }

    private MetricsDo getFetchFollowerTotalTimeMsMetricsDo(KafkaMetrics info, Map.Entry<String, KafkaBrokerInfo> entry) {
        String brokerName = entry.getKey();
        KafkaBrokerInfo brokerInfo = entry.getValue();
        if (null == brokerInfo.getFetchFollowerTotalTimeMs()) {
            return null;
        }
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getClusterName());
        tagOptions.put("brokerName", brokerName);
        tagOptions.put("attributeName", "FetchFollowerTotalTimeMs");
        segmentMap.put("50thPercentile", brokerInfo.getFetchFollowerTotalTimeMs().get_50thPercentile());
        segmentMap.put("75thPercentile", brokerInfo.getFetchFollowerTotalTimeMs().get_75thPercentile());
        segmentMap.put("95thPercentile", brokerInfo.getFetchFollowerTotalTimeMs().get_95thPercentile());
        segmentMap.put("98thPercentile", brokerInfo.getFetchFollowerTotalTimeMs().get_98thPercentile());
        segmentMap.put("999thPercentile", brokerInfo.getFetchFollowerTotalTimeMs().get_999thPercentile());
        segmentMap.put("99thPercentile", brokerInfo.getFetchFollowerTotalTimeMs().get_99thPercentile());
        segmentMap.put("count", (double) brokerInfo.getFetchFollowerTotalTimeMs().getCount());
        segmentMap.put("max", brokerInfo.getFetchFollowerTotalTimeMs().getMax());
        segmentMap.put("mean", brokerInfo.getFetchFollowerTotalTimeMs().getMean());
        segmentMap.put("min", brokerInfo.getFetchFollowerTotalTimeMs().getMin());
        segmentMap.put("stdDev", brokerInfo.getFetchFollowerTotalTimeMs().getStdDev());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        return metricsDo;
    }


    public List<MetricsDo> transformClusterMetrics(ClusterRtTime info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", info.getCluster());
        segmentMap.put("clusterNums", (double) info.getClusterNums());
        metricsDo.setTagOptions(tagOptions);
        metricsDo.setSegmentMap(segmentMap);
        metricsDoList.add(metricsDo);
        return metricsDoList;
    }


    public List<MetricsDo> transformKafkaConsumerMetricsInfo(KafkaConsumerMetricsInfo info) {
        List<MetricsDo> metricsDoList = Lists.newArrayList();
        if (null == info.getKafkaLagInfos()) {
            return metricsDoList;
        }
        for (ConsumerStatus item : info.getKafkaLagInfos()) {
            if (StringUtils.isEmpty(item.getTopic()) || StringUtils.isEmpty(item.getGroup())) {
                continue;
            }
            String offsetKey = info.getClusterName().concat(".").concat(item.getTopic()).concat(".").concat(item.getGroup()).concat(".consumerOffset").toLowerCase();
            String timeKey = info.getClusterName().concat(".").concat(item.getTopic()).concat(".").concat(item.getGroup()).concat(".timestamp").toLowerCase();
            Long offsets = cacheMap.get(offsetKey);
            Long time = cacheMap.get(timeKey);
            long tps = 0L;
            try {
                if (null != offsets && null != time) {
                    long t = Math.abs((info.getTimestamp() - time)) / 1000;
                    long o = Math.abs(item.getConsumerOffset() - offsets);
                    if (t != 0) {
                        tps = o / t;
                    }
                }
            } catch (Throwable e) {
                logger.error("KafkaConsumerMetrics tps calc error." + e);
            }
            cacheMap.put(offsetKey, item.getConsumerOffset());
            cacheMap.put(timeKey, info.getTimestamp());
            MetricsDo<Double> metricsDo = new MetricsDo<Double>();
            Map<String, String> tagOptions = metricsDo.getTagOptions();
            Map<String, Double> segmentMap = metricsDo.getSegmentMap();
            tagOptions.put("clusterName", info.getClusterName());
            tagOptions.put("topicName", item.getTopic());
            tagOptions.put("consumerGroup", item.getGroup());
            segmentMap.put("brokerOffset", (double) item.getBrokerOffset());
            segmentMap.put("consumerOffset", (double) item.getConsumerOffset());
            segmentMap.put("latency", (double) item.getLag());
            segmentMap.put("tps", (double) tps);
            metricsDo.setTagOptions(tagOptions);
            metricsDo.setSegmentMap(segmentMap);
            metricsDoList.add(metricsDo);
        }
        return metricsDoList;
    }

    public List<MetricsDo> transform(ProducerStats info) {
        List<MetricsDo> metricsLst = Lists.newArrayList();
        if (null == info.getDistributions()) {
            return metricsLst;
        }
        info.getDistributions().forEach((distribution) -> {
            MetricsDo metricsDo = new MetricsDo();
            Map<String, String> tagOptions = metricsDo.getTagOptions();
            Map<String, Long> segmentMap = metricsDo.getSegmentMap();
            String ip = info.getClientInfo() == null ? "" : info.getClientInfo().getIp();
            String clientName = info.getClientInfo() == null ? "" : info.getClientInfo().getClientName();
            tagOptions.put("ip", ip);
            tagOptions.put("clientName", clientName);
            tagOptions.put("type", distribution.getType());
            segmentMap.put("less1", distribution.getLessThan1Ms());
            segmentMap.put("less5", distribution.getLessThan5Ms());
            segmentMap.put("less10", distribution.getLessThan10Ms());
            segmentMap.put("less50", distribution.getLessThan50Ms());
            segmentMap.put("less100", distribution.getLessThan100Ms());
            segmentMap.put("less500", distribution.getLessThan500Ms());
            segmentMap.put("less1000", distribution.getLessThan1000Ms());
            segmentMap.put("more1000", distribution.getMoreThan1000Ms());
            metricsDo.setTagOptions(tagOptions);
            metricsDo.setSegmentMap(segmentMap);
            metricsLst.add(metricsDo);
        });

        info.getMeters().forEach((meter) -> {
            MetricsDo<Double> metricsDoubleDo = getMetersMetrics(meter, info.getClientInfo());
            metricsLst.add(metricsDoubleDo);
        });

        info.getTimers().forEach((timer) -> {
            MetricsDo<Double> metricsDo =  getTimerMetrics(info.getClientInfo(),timer);
            metricsLst.add(metricsDo);
        });
        return metricsLst;
    }


    public List<MetricsDo> transform(ConsumerStats info){
        List<MetricsDo> metricsDoubleLst = Lists.newArrayList();
        if(null == info.getMeters()){
            return metricsDoubleLst;
        }
        info.getMeters().forEach((meter)->{
            MetricsDo<Double> metricsDo = getMetersMetrics(meter, info.getClientInfo());
            metricsDoubleLst.add(metricsDo);
        });

        info.getTimers().forEach((timer)->{
            MetricsDo<Double> metricsDo = getTimerMetrics(info.getClientInfo(), timer);
            metricsDoubleLst.add(metricsDo);
        });
        return metricsDoubleLst;
    }


    private MetricsDo<Double> getTimerMetrics(ClientInfo clientInfo, TimerInfo timer) {
        String ip =clientInfo==null ? "" : clientInfo.getIp();
        String clientName = clientInfo==null ? "" : clientInfo.getClientName();
        MetricsDo<Double> metricsDo = new MetricsDo<Double>();
        Map<String,String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("ip",ip);
        tagOptions.put("clientName",clientName);
        tagOptions.put("type",timer.getType());
        segmentMap.put("median", timer.getMedian()<1000000 ? timer.getMedian():(timer.getMedian()/1000000));
        segmentMap.put("mean", timer.getMean()<1000000 ? timer.getMean():(timer.getMean()/1000000));
        segmentMap.put("max", timer.getMax()<1000000 ? timer.getMax():(timer.getMax()/1000000));
        segmentMap.put("min", timer.getMin()<1000000 ? timer.getMin():(timer.getMin()/1000000));
        segmentMap.put("percent75", timer.getPercent75()<1000000 ? timer.getPercent75():(timer.getPercent75()/1000000));
        segmentMap.put("percent90", timer.getPercent90()<1000000 ? timer.getPercent90():(timer.getPercent90()/1000000));
        segmentMap.put("percent95", timer.getPercent95()<1000000 ? timer.getPercent95():(timer.getPercent95()/1000000));
        segmentMap.put("percent98", timer.getPercent98()<1000000 ? timer.getPercent98():(timer.getPercent98()/1000000));
        segmentMap.put("percent99", timer.getPercent99()<1000000 ? timer.getPercent99():(timer.getPercent99()/1000000));
        segmentMap.put("percent999", timer.getPercent999()<1000000 ? timer.getPercent999():(timer.getPercent999()/1000000));
        metricsDo.setSegmentMap(segmentMap);
        metricsDo.setTagOptions(tagOptions);
        return metricsDo;
    }


    private MetricsDo<Double> getMetersMetrics(MeterInfo meter, ClientInfo clientInfo) {
        String ip = clientInfo == null ? "" : clientInfo.getIp();
        String clientName = clientInfo == null ? "" : clientInfo.getClientName();
        MetricsDo<Double> metricsDoubleDo = new MetricsDo<Double>();
        Map<String, String> tagOptions = metricsDoubleDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDoubleDo.getSegmentMap();
        tagOptions.put("ip", ip);
        tagOptions.put("clientName", clientName);
        tagOptions.put("type", meter.getType());
        segmentMap.put("count", (double) meter.getCount());
        segmentMap.put("mean", meter.getMean());
        segmentMap.put("min1rate", meter.getMin1Rate());
        segmentMap.put("min5rate", meter.getMin5Rate());
        segmentMap.put("min15rate", meter.getMin15Rate());
        metricsDoubleDo.setTagOptions(tagOptions);
        metricsDoubleDo.setSegmentMap(segmentMap);
        return metricsDoubleDo;
    }

}

