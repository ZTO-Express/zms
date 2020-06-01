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
import com.google.common.collect.Sets;
import com.zto.zms.common.BrokerType;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.collector.kafka.*;
import com.zto.zms.collector.model.*;
import com.zto.zms.service.domain.MetricsDo;
import com.zto.zms.service.influx.InfluxdbClient;
import com.zto.zms.service.domain.influxdb.KafkaConsumerNumberInfo;
import com.zto.zms.service.domain.influxdb.KafkaTopicInfo;
import com.zto.zms.service.influx.TopicDailyOffsetsInfo;
import com.zto.zms.service.kafka.KafkaAdminClient;
import com.zto.zms.service.repository.TopicDailyOffsetsRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by liangyong on 2018/11/5.
 */
@Component("kafka")
public class KafkaReporter  extends AbstractReporter {

    private static Logger logger = LoggerFactory.getLogger(KafkaReporter.class);


    @Autowired
    private MetricsTransformService metricsTransformService;

    @Autowired
    private InfluxdbClient influxdbClient;

    @Autowired
    private KafkaMetaManager kafkaMetaManager;

    @Autowired
    private KafkaJMXCollector kafkaJMXCollector;

    @Autowired
    private KafkaProducerManager kafkaProducerManager;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private TopicDailyOffsetsRepository topicDailyOffsetsRepository;

    @Autowired
    private ExtensionRepository extensionRepository;


    private Map<String, Long> topicMaxOffsets = Maps.newConcurrentMap();

    @Override
    public void refreshMetaOfCluster(ClusterMetadata cluster) {
        kafkaMetaManager.putClusterController(cluster);
        kafkaMetaManager.putClusterNodes(cluster);
        kafkaMetaManager.putClusterTopics(cluster);
        kafkaMetaManager.putKafkaConsumers(cluster);
    }


    @Override
    public void collectMqMetrics(ClusterMetadata cluster) {
        this.collectKafkaMetrics(cluster);
        this.collectKafkaConsumerInfo(cluster);
    }

    @Override
    public void recordMaxOffset(ClusterMetadata cluster) {
        try {
            long maxClusterOffset = 0L;
            List<String> topics = kafkaMetaManager.getClusterTopics(cluster);
            Map<String, List<Integer>> topicPartitions = kafkaService.findTopicPartitions(cluster, topics);
            for (String topic : kafkaMetaManager.getClusterTopics(cluster)) {
                long maxTopicOffset = 0L;
                if (topicPartitions.containsKey(topic)) {
                    for (Integer partition : topicPartitions.get(topic)) {
                        maxTopicOffset += kafkaService.getLogSize(cluster, topic, partition);
                    }
                } else {
                    logger.error("Find topic partition error:{}", topic);
                }
                this.saveStatisticTopicOffset(cluster.getClusterName(), topic, maxTopicOffset);

                String cacheKey = cluster.getClusterName() + "." + topic;
                topicMaxOffsets.put(cacheKey, maxTopicOffset);
                maxClusterOffset += maxTopicOffset;
            }
            this.saveStatisticClusterOffset(cluster.getClusterName(), maxClusterOffset);
        } catch (Throwable e) {
            logger.error("recordMaxOffset error", e);
        }
    }

    @Override
    public void recordMaxOffsetInflux(ClusterMetadata cluster) {
        String clusterName = cluster.getClusterName();
        List<MetricsDo> metricsLst = Lists.newArrayList();
        try {
            List<String> topics = kafkaMetaManager.getClusterTopics(cluster);
            Map<String, List<Integer>> topicPartitions = kafkaService.findTopicPartitions(cluster, topics);

            for (String topic : topics) {
                MetricsDo<Double> metricsDo = new MetricsDo<Double>();
                Map<String, String> tagOptions = metricsDo.getTagOptions();
                Map<String, Double> segmentMap = metricsDo.getSegmentMap();
                tagOptions.put("clusterName", clusterName);
                tagOptions.put("topicName", topic);
                Long maxOffset = 0L;
                if (topicPartitions.containsKey(topic)) {
                    for (Integer partition : topicPartitions.get(topic)) {
                        maxOffset += kafkaService.getLogSize(cluster, topic, partition);
                    }
                }
                segmentMap.put("maxOffset", Double.valueOf(maxOffset));
                Long standardVal = 0L;
                String cacheKey = clusterName + "." + topic;
                if (null == topicMaxOffsets.get(cacheKey)) {
                    TopicDailyOffsetsInfo latestStatisticTopicOffset = topicDailyOffsetsRepository.getLastTopicMaxOffset(clusterName,topic);
                    if(null!= latestStatisticTopicOffset){
                        standardVal =  Double.valueOf(latestStatisticTopicOffset.getValue()).longValue();
                        topicMaxOffsets.put(cacheKey, standardVal);
                    }
                } else {
                    standardVal = topicMaxOffsets.get(cacheKey);
                }
                Long increaseVal = maxOffset - standardVal;
                segmentMap.put("increaseValue", Double.valueOf(increaseVal));
                segmentMap.put("standardValue", Double.valueOf(standardVal));
                metricsLst.add(metricsDo);
            }
            if (!CollectionUtils.isEmpty(metricsLst)) {
                influxdbClient.writeData(metricsLst, ZmsConst.Measurement.STATISTIC_TOPIC_OFFSETS_INFO, System.currentTimeMillis());
            }
        } catch (Throwable e) {
            logger.error(clusterName + " message increase metrics collect error:" + e.getMessage());
        }

    }

    private void collectKafkaMetrics(ClusterMetadata cluster) {
        String clusterName = cluster.getClusterName();
        List<String> uriLst = new ArrayList<String>();
        try {
            Collection<Node> nodes = kafkaMetaManager.getClusterNodes(cluster);
            nodes.forEach(node -> uriLst.add(node.host() + ":9999"));
            List<String> topicLst = kafkaMetaManager.getClusterTopics(cluster);
            KafkaMetrics kafkaMetrics = kafkaJMXCollector.buildKafkaMetricsInfo(clusterName, uriLst, topicLst);
            Node controller = kafkaMetaManager.getClusterController(cluster);
            int controllerId = controller.id();
            String controllerHost = controller.host();
            kafkaMetrics.setControllerHost(controllerHost);
            kafkaMetrics.setControllerId(controllerId);


            List<MetricsDo> kafkaBrokerLst = metricsTransformService.transformKafkaBroker(kafkaMetrics);
            metricBuffer.put(ZmsConst.Measurement.KAFKA_BROKER_INFO, kafkaBrokerLst);
            List<MetricsDo> kafkaTopicLst = metricsTransformService.transformKafkaTopic(kafkaMetrics);
            metricBuffer.put(ZmsConst.Measurement.KAFKA_TOPIC_INFO, kafkaTopicLst);
            List<MetricsDo> kafkaEnvLst = metricsTransformService.transformKafkaEnv(kafkaMetrics);
            metricBuffer.put(ZmsConst.Measurement.KAFKA_ENV_INFO, kafkaEnvLst);
            List<MetricsDo> clusterStrDataLst = metricsTransformService.transformStrDataKafkaCluster(kafkaMetrics);
            metricBuffer.put(ZmsConst.Measurement.CLUSTER_STRING_INFO, clusterStrDataLst);
            List<MetricsDo> clusterDoubleDataLst = metricsTransformService.transformDoubleDataKafkaCluster(kafkaMetrics);
            metricBuffer.put(ZmsConst.Measurement.CLUSTER_NUMBER_INFO, clusterDoubleDataLst);
        } catch (Exception e) {
            logger.error(clusterName + " kafka metrics collect metrics error", e);
        }
    }

    @Override
    public void collectRtTime(ClusterMetadata cluster) {
        try {
            ClusterRtTime clusterRtTime = new ClusterRtTime();
            clusterRtTime.setCluster(cluster.getClusterName());
            clusterRtTime.setTimestamp(System.currentTimeMillis());
            Collection<Node> nodes = kafkaMetaManager.getClusterNodes(cluster);
            if(CollectionUtils.isEmpty(nodes)){
                logger.error("collectRtTime error:cluster nodes is empty,{}",cluster);
                return;
            }
            clusterRtTime.setClusterNums(nodes.size());
            List<RtTime> rtTimes = Lists.newArrayList();
            nodes.forEach(node -> {
                String topic = KafkaAdminClient.buildTopicName(node, cluster.getClusterName());
                ProducerRecord record = new ProducerRecord(topic, "hello".getBytes());
                RtTime rtTime = new RtTime();
                rtTime.setBrokerName(topic);
                long begin = System.currentTimeMillis();
                try {
                    RecordMetadata o = (RecordMetadata) kafkaProducerManager.getKafkaProducer(cluster).send(record).get();
                    rtTime.setRt(System.currentTimeMillis() - begin);
                    rtTime.setResult(0);
                    rtTime.setStatus("SEND_OK");
                } catch (Exception e) {
                    rtTime.setRt(-1);
                    rtTime.setResult(5);
                    rtTime.setStatus("FAILED");
                    logger.error("send test message failed", e);
                }
                rtTimes.add(rtTime);
            });
            clusterRtTime.setTimes(rtTimes);
            this.saveRtMetrics(clusterRtTime);
        } catch (Throwable ex) {
            logger.error("collectRtTime error", ex);
        }
    }


    public void collectKafkaConsumerInfo(ClusterMetadata cluster) {
        try {
            KafkaConsumerMetricsInfo metricsInfo = new KafkaConsumerMetricsInfo();
            List<ConsumerStatus> statusList = metricsInfo.getKafkaLagInfos();
            List<ConsumerGroupInfo> consumerGroups = kafkaMetaManager.getKafkaConsumers(cluster);
            Map<String, List<String>> consumerGroupMap = new HashMap<String, List<String>>();
            Map<String, List<String>> consumers = null;
            for (ConsumerGroupInfo consumerGroup : consumerGroups) {
                String group = consumerGroup.getGroup();
                List<String> topics = new ArrayList<>();
                for (String topic : kafkaService.getKafkaConsumerTopics(cluster, group)) {
                    topics.add(topic);
                }
                consumerGroupMap.put(group, topics);
            }
            consumers = consumerGroupMap;


            Set<String> topics = Sets.newHashSet();
            consumers.forEach((key, value) -> topics.addAll(value));
            Map<String, List<Integer>> topicPartitions = kafkaService.findTopicPartitions(cluster, topics);

            for (Map.Entry<String, List<String>> entry : consumers.entrySet()) {
                String group = entry.getKey();
                Map<TopicPartition, Long> consumerOffsetMap = kafkaService.getKafkaOffset(cluster, group);
                for (String topic : entry.getValue()) {
                    ConsumerStatus consumerStatus = new ConsumerStatus();
                    if (topicPartitions.containsKey(topic)) {
                        for (Integer partition : topicPartitions.get(topic)) {
                            long logSize = kafkaService.getLogSize(cluster, topic, partition);
                            Long offset = consumerOffsetMap.get(new TopicPartition(topic, partition));
                            if (null == offset) {
                                offset = 0L;
                            }
                            consumerStatus.setGroup(group);
                            consumerStatus.setTopic(topic);
                            if (logSize != 0L) {
                                long lag = consumerStatus.getLag() + (offset == -1 ? 0 : logSize - offset);
                                consumerStatus.setLag(lag);
                            }
                            consumerStatus.setBrokerOffset(logSize + consumerStatus.getBrokerOffset());
                            consumerStatus.setConsumerOffset(offset + consumerStatus.getConsumerOffset());
                        }
                        statusList.add(consumerStatus);
                    }
                }
            }

            metricsInfo.setClusterName(cluster.getClusterName());
            metricsInfo.setTimestamp(System.currentTimeMillis());
            List<MetricsDo> metricsDoList = metricsTransformService.transformKafkaConsumerMetricsInfo(metricsInfo);
            metricBuffer.put(ZmsConst.Measurement.KAFKA_CONSUMER_NUMBER_INFO, metricsDoList);

        } catch (Throwable ex) {
            logger.error("get consumers lag error", ex);
        }
    }

    @Override
    public void topicTpsTop10() {
        try {
            List<KafkaTopicInfo> topicInfoList = extensionRepository.queryKafkaTopicMaxTps();
            if (CollectionUtils.isEmpty(topicInfoList)) {
                return;
            }
            List<MetricsDo> tpsPOList = topicInfoList.stream()
                    .sorted(Comparator.comparing(KafkaTopicInfo::getValue).reversed().thenComparing(KafkaTopicInfo::getTopicName))
                    .limit(10)
                    .map(item -> {
                        MetricsDo<Double> metricsDo = new MetricsDo();
                        Map<String, String> tagOptions = metricsDo.getTagOptions();
                        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
                        tagOptions.put("clusterName", item.getClusterName());
                        tagOptions.put("topicName", item.getTopicName());
                        tagOptions.put("clusterType", BrokerType.KAFKA.getName());
                        tagOptions.put("attributeName", "top10");
                        segmentMap.put("tps", item.getValue());
                        metricsDo.setTagOptions(tagOptions);
                        metricsDo.setSegmentMap(segmentMap);
                        return metricsDo;
                    }).collect(Collectors.toList());
            metricBuffer.put(ZmsConst.Measurement.TOPIC_TOP_INFO, tpsPOList);
        } catch (Throwable e) {
            logger.error("topicTpsTop10 error", e);
        }
    }

    @Override
    public void consumerLatencyTop10() {
        try {
            List<KafkaConsumerNumberInfo> consumerInfos = extensionRepository.queryKafkaConsumerLatency();
            if (CollectionUtils.isEmpty(consumerInfos)) {
                return;
            }
            List<KafkaConsumerNumberInfo> consumerTpsInfos = extensionRepository.queryKafkaConsumerGroupTps();
            if (CollectionUtils.isEmpty(consumerTpsInfos)) {
                return;
            }
            Map<String, Double> consumerTpsMap = extensionRepository.filterKafkaTps(consumerTpsInfos);
            if (CollectionUtils.isEmpty(consumerTpsMap)) {
                return;
            }
            List<MetricsDo> tpsPOList = consumerInfos.stream()
                    .filter(item -> item.getValue() > 0 && consumerTpsMap.containsKey(item.getConsumerGroup()))
                    .sorted(Comparator.comparing(KafkaConsumerNumberInfo::getValue).reversed().thenComparing(KafkaConsumerNumberInfo::getConsumerGroup))
                    .limit(10)
                    .map(item -> {
                        MetricsDo<Double> metricsDo = new MetricsDo();
                        Map<String, String> tagOptions = metricsDo.getTagOptions();
                        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
                        tagOptions.put("clusterName", item.getClusterName());
                        tagOptions.put("consumerGroup", item.getConsumerGroup());
                        tagOptions.put("clusterType", BrokerType.KAFKA.getName());
                        tagOptions.put("attributeName", "top10");
                        segmentMap.put("latency", item.getValue());
                        metricsDo.setTagOptions(tagOptions);
                        metricsDo.setSegmentMap(segmentMap);
                        return metricsDo;
                    }).collect(Collectors.toList());
            metricBuffer.put(ZmsConst.Measurement.CONSUMER_TOP_INFO, tpsPOList);
        } catch (Throwable e) {
            logger.error("consumerLatencyTop10 error", e);
        }
    }
}

