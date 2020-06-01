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
import com.zto.zms.common.BrokerType;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.collector.model.*;
import com.zto.zms.collector.rocketmq.MqMetaManager;
import com.zto.zms.collector.rocketmq.MqProducerManager;
import com.zto.zms.service.domain.MetricsDo;
import com.zto.zms.service.domain.topic.TopicStats;
import com.zto.zms.service.influx.InfluxdbClient;
import com.zto.zms.service.domain.influxdb.MqConsumerNumberInfo;
import com.zto.zms.service.influx.MqTopicInfo;
import com.zto.zms.service.influx.TopicDailyOffsetsInfo;
import com.zto.zms.service.mq.MqAdminManager;
import com.zto.zms.service.repository.TopicDailyOffsetsRepository;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.*;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.store.stats.BrokerStatsManager;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by liangyong on 2018/11/5.
 */
@Component("rocketmq")
public class RocketMQReporter extends AbstractReporter{

    private static Logger logger = LoggerFactory.getLogger(RocketMQReporter.class);


    @Autowired
    private MetricsTransformService metricsTransformService;

    @Autowired
    private TopicDailyOffsetsRepository topicDailyOffsetsRepository;

    @Autowired
    private InfluxdbClient influxdbClient;

    @Autowired
    private MqAdminManager mqAdminManager;

    @Autowired
    private MqProducerManager mqProducerManager;

    @Autowired
    private MqMetaManager mqMetaManager;

    @Autowired
    private ExtensionRepository extensionRepository;


    private Map<String,Long> topicMaxOffsets = Maps.newConcurrentMap();


    @Override
    public void refreshMetaOfCluster(ClusterMetadata cluster) {
        mqMetaManager.putClusterInfo(cluster);
        mqMetaManager.putClusterTopics(cluster);
    }

    @Override
    public void collectMqMetrics(ClusterMetadata cluster) {
        String clusterName = cluster.getClusterName();
        try {
            DefaultMQAdminExt mqAdmin = mqAdminManager.getMqAdmin(clusterName);
            ClusterInfo clusterInfo = mqMetaManager.getClusterInfo(cluster);
            Set<String> topicList = mqMetaManager.getClusterTopics(cluster);
            RocketmqStatus rocketmqStatus = new RocketmqStatus();
            rocketmqStatus.setTimestamp(System.currentTimeMillis());
            rocketmqStatus.setClusterName(clusterName);
            Map<String, ConsumerInfo> consumerInfos = Maps.newHashMap();
            Map<String, TopicInfo> topicInfos = Maps.newHashMap();
            List<BrokerInfo> brokerInfos = Lists.newArrayList();
            double totalTps = 0d;

            for (Map.Entry<String, BrokerData> stringBrokerDataEntry : clusterInfo.getBrokerAddrTable().entrySet()) {
                BrokerData brokerData = stringBrokerDataEntry.getValue();
                String brokerName = brokerData.getBrokerName();

                String brokerAddr = brokerData.getBrokerAddrs().get(MixAll.MASTER_ID);
                KVTable runtimeStatsTable = mqAdmin.fetchBrokerRuntimeStats(brokerAddr);
                HashMap<String, String> runtimeStatus = runtimeStatsTable.getTable();
                BrokerInfo brokerInfo = new BrokerInfo();
                brokerInfo.setIp(brokerAddr.substring(0, brokerAddr.indexOf(":")));
                brokerInfo.setBrokerName(brokerName);
                brokerInfo.setBrokerId(MixAll.MASTER_ID);
                brokerInfo.setPageCacheLockTimeMillis(parseNullable(runtimeStatus, "pageCacheLockTimeMills"));
                brokerInfo.setSendThreadPoolQueueSize(parseNullable(runtimeStatus, "sendThreadPoolQueueSize"));
                brokerInfo.setPullThreadPoolQueueSize(parseNullable(runtimeStatus, "pullThreadPoolQueueSize"));
                brokerInfo.setPullThreadPoolQueueCapacity(parseNullable(runtimeStatus, "pullThreadPoolQueueCapacity"));
                brokerInfo.setSendThreadPoolQueueCapacity(parseNullable(runtimeStatus, "sendThreadPoolQueueCapacity"));
                brokerInfo.setPullThreadPoolQueueHeadWaitTimeMills(parseNullable(runtimeStatus, "pullThreadPoolQueueHeadWaitTimeMills"));
                brokerInfo.setMsgGetTotalTodayNow(parseNullableLong(runtimeStatus, "msgGetTotalTodayNow"));
                brokerInfo.setGetTotalTps(Double.parseDouble(runtimeStatus.get("getTotalTps").split(" ")[0]));
                double putTps = Math.ceil(Double.parseDouble(runtimeStatus.get("putTps").split(" ")[0]));
                brokerInfo.setPutTps(putTps);
                totalTps = totalTps + putTps;
                brokerInfo.setRuntime(runtimeStatus.get("runtime"));
                brokerInfo.setBootTime(Double.valueOf(runtimeStatus.get("bootTimestamp")));
                brokerInfo.setEarliestMsgTime(runtimeStatus.get("earliestMessageTimeStamp"));
                brokerInfo.setPutMessageDistributeTime(runtimeStatus.get("putMessageDistributeTime"));

                brokerInfos.add(brokerInfo);

                for (String topic : topicList) {
                    try {
                        BrokerStatsData topicPutNums = mqAdmin.viewBrokerStatsData(brokerAddr, BrokerStatsManager.TOPIC_PUT_NUMS, topic);

                        // add topic related data
                        addTopicInfo(topicInfos, topic, topicPutNums);

                        // add retry topic related data, retry is based on  group
                        GroupList groupList = mqAdmin.queryTopicConsumeByWho(topic);
                        for (String group : groupList.getGroupList()) {
                            String statsKey = buildStatsKey(topic, group);
                            BrokerStatsData bsd = mqAdmin.viewBrokerStatsData(brokerAddr, BrokerStatsManager.SNDBCK_PUT_NUMS, statsKey);
                            addTopicInfo(topicInfos, statsKey, bsd);
                        }


                    } catch (MQClientException ex) {
                        if (ex.getResponseCode() == 1) {
                            logger.info(ex.getMessage());
                        } else {
                            logger.error("get topic_put_nums or send_back_nums error");
                        }

                    } catch (Exception ex) {
                        logger.error("get topic_put_nums or send_back_nums error");
                    }

                }
//                            AdminBrokerProcessor里面,line 1171&1172，说明一个list每个条目对应一个consumergroup的map，每个map只有一个元素，是consumergroup对应着
//                             所有topic的ConsumeStats。每个topic对应一个ConsumeStats
                try {
                    ConsumeStatsList consumeStatsList = mqAdmin.fetchConsumeStatsInBroker(brokerAddr, false, 5000);
                    for (Map<String, List<ConsumeStats>> consumerStats : consumeStatsList.getConsumeStatsList()) {
                        if (consumerStats.entrySet().size() > 1) {
                            logger.error("get rocketmq status: consumeStatsList,size is more than 1");
                        }
                        for (Map.Entry<String, List<ConsumeStats>> stringListEntry : consumerStats.entrySet()) {
                            String consumer = stringListEntry.getKey();
                            List<ConsumeStats> consumeStats = stringListEntry.getValue();

                            long lag = calcLag(consumeStats);
                            double tps = calcTps(consumeStats);
                            long lastConsumingTime = calcDelayInSecond(consumeStats);

                            if (consumerInfos.containsKey(consumer)) {
                                ConsumerInfo consumerInfo = consumerInfos.get(consumer);
                                consumerInfo.add(lag, tps);
                                consumerInfo.updateLastConsumingTime(lastConsumingTime);
                                consumerInfos.put(consumer, consumerInfo);
                            } else {
                                ConsumerInfo value = new ConsumerInfo(lag, tps);
                                value.setLastConsumingTime(lastConsumingTime);
                                DefaultMQAdminExt mqAdminExt = mqAdminManager.getMqAdmin(clusterName);

                                try {
                                    ConsumerConnection consumerConnection = mqAdminExt.examineConsumerConnectionInfo(consumer);
                                    for (Connection connection : consumerConnection.getConnectionSet()) {
                                        String clientId = connection.getClientId();
                                        value.getConsumers().add(clientId);
                                    }
                                } catch (MQClientException err) {
//                                    error code 17代表retry topic不存在: CODE: 17  DESC: No topic route info in name server for the topic: %RETRY%SubscribeOther
                                    if (err.getResponseCode() == 17) {
                                        logger.info(" get consumer {} related info error ", consumer, err.getMessage());
                                    } else {
                                        logger.error("read consumer info error", err);
                                    }
                                } catch (MQBrokerException err) {
//                                    error code 206代表消费者不在线： CODE: 206  DESC: the consumer group[medivhOrderStatus] not online
                                    if (err.getResponseCode() == 206) {
                                        logger.info(" get consumer {} related info error ", consumer, err.getMessage());
                                    } else {
                                        logger.error("read consumer info error", err);
                                    }
                                } catch (Exception err) {
                                    logger.error("op=getClientId error", err);
                                }

                                consumerInfos.put(consumer, value);
                            }
                        }
                    }

                } catch (Exception ex) {
                    logger.error("get rocketmq status: consumeStatsList failed", ex);
                }

            }

            rocketmqStatus.setConsumerInfos(consumerInfos);
            rocketmqStatus.setTopicInfos(topicInfos);
            rocketmqStatus.setBrokers(brokerInfos);
            rocketmqStatus.setBrokersCnt(brokerInfos.size());
            rocketmqStatus.setTotalTps(Math.ceil(totalTps));
            //集群totalTps信息
            List<MetricsDo> mqClusterLst = metricsTransformService.transformMqCluster(rocketmqStatus);
            metricBuffer.put(ZmsConst.Measurement.CLUSTER_NUMBER_INFO,mqClusterLst);
            //broker节点指标信息
            List<MetricsDo> mqBrokerLst = metricsTransformService.transformMqBroker(rocketmqStatus);
            metricBuffer.put(ZmsConst.Measurement.MQ_BROKER_NUMBER_INFO,mqBrokerLst);
            List<MetricsDo> mqStrDataBrokerLst = metricsTransformService.transformStringDataMqBroker(rocketmqStatus);
            metricBuffer.put(ZmsConst.Measurement.MQ_BROKER_STRING_INFO,mqStrDataBrokerLst);
            //topic tps信息
            List<MetricsDo> mqTopicLst = metricsTransformService.transformMqTopic(rocketmqStatus);
            metricBuffer.put(ZmsConst.Measurement.MQ_TOPIC_INFO,mqTopicLst);
            //consumer tps、latency、lastConsumingTime信息
            List<MetricsDo> mqConsumerLst = metricsTransformService.transformMqConsumer(rocketmqStatus);
            metricBuffer.put(ZmsConst.Measurement.MQ_CONSUMER_NUMBER_INFO,mqConsumerLst);
            List<MetricsDo> mqStrDataConsumerLst = metricsTransformService.transformStrDataMqConsumer(rocketmqStatus);
            metricBuffer.put(ZmsConst.Measurement.MQ_CONSUMER_STRING_INFO,mqStrDataConsumerLst);

        } catch (Exception e) {
            logger.error("collect rocketmq status error", e);
        }
    }

    @Override
    public void collectRtTime(ClusterMetadata cluster) {

        ClusterRtTime clusterRtTime = new ClusterRtTime();
        ClusterInfo clusterInfo = null;
        try {
            clusterInfo = mqMetaManager.getClusterInfo(cluster);
        } catch (Exception e) {
            logger.error("try to read clusterInfo error", e);
            return;
        }
        clusterRtTime.setCluster(cluster.getClusterName());

        clusterRtTime.setClusterNums(calcClusterNums(clusterInfo));

        List<RtTime> times = Lists.newArrayList();
        for (Map.Entry<String, BrokerData> stringBrokerDataEntry : clusterInfo.getBrokerAddrTable().entrySet()) {
            BrokerData brokerData = stringBrokerDataEntry.getValue();
            String brokerName = brokerData.getBrokerName();
            long begin = System.currentTimeMillis();
            SendResult sendResult = null;
            RtTime time = new RtTime();
            time.setBrokerName(brokerName);
            try {
                sendResult = mqProducerManager.getMqProducer(cluster).send(new Message(brokerName, "hello".getBytes()));
                long end = System.currentTimeMillis() - begin;
                SendStatus sendStatus = sendResult.getSendStatus();
                time.setRt(end);
                time.setStatus(sendStatus.name());
                time.setResult(sendStatus.ordinal());
            } catch (Exception e) {
                time.setRt(-1);
                time.setStatus("FAILED");
                time.setResult(5);
                logger.error("send rt test msg failed", e);
            }

            times.add(time);

        }
        clusterRtTime.setTimes(times);
        this.saveRtMetrics(clusterRtTime);
    }

    private int calcClusterNums(ClusterInfo clusterInfo) {
        int brokers = 0;
        Set<Map.Entry<String, BrokerData>> entries = clusterInfo.getBrokerAddrTable().entrySet();
        for (Map.Entry<String, BrokerData> entry : entries) {
            brokers += entry.getValue().getBrokerAddrs().entrySet().size();
        }

        return brokers;
    }

    private String buildStatsKey(String topic, String group) {
        StringBuffer strBuilder = new StringBuffer();
        strBuilder.append(topic);
        strBuilder.append("@");
        strBuilder.append(group);
        return strBuilder.toString();
    }


    public static Double calcTps(List<ConsumeStats> consumeStats) {
        double tps = 0D;
        for (ConsumeStats consumeStat : consumeStats) {
            tps += consumeStat.getConsumeTps();
        }
        return tps;
    }


    /**
     * 统计最大的秒级最后更新时间，对部分消费情况进行告警收集
     * @param consumeStats
     * @return
     */
    public static Long calcDelayInSecond(List<ConsumeStats> consumeStats) {
        long delay = 0L;
        long currentTimeMillis = System.currentTimeMillis();
        for (ConsumeStats consumeStat : consumeStats) {
            for (MessageQueue messageQueue : consumeStat.getOffsetTable().keySet()) {
                if (messageQueue.getTopic().startsWith("%RETRY%")) {
                    continue;
                }
                OffsetWrapper offsetWrapper = consumeStat.getOffsetTable().get(messageQueue);
                long tmp = 0L;
                if (offsetWrapper.getLastTimestamp() > 0) {
                    tmp = currentTimeMillis - offsetWrapper.getLastTimestamp();
                    if (tmp > delay) {
                        delay = tmp;
                    }
                }
            }

        }
        return delay / 1000;
    }

    private void addTopicInfo(Map<String, TopicInfo> topicInfos, String statsKey, BrokerStatsData bsd) {
        if (topicInfos.containsKey(statsKey)) {
            TopicInfo topicInfo = topicInfos.get(statsKey);
            topicInfo.add(bsd.getStatsMinute().getTps(), compute24HourSum(bsd));
            topicInfos.put(statsKey, topicInfo);
        } else {
            topicInfos.put(statsKey, new TopicInfo(bsd.getStatsMinute().getTps(), compute24HourSum(bsd)));
        }
    }


    public static long compute24HourSum(BrokerStatsData bsd) {
        if (bsd.getStatsDay().getSum() != 0) {
            return bsd.getStatsDay().getSum();
        }

        if (bsd.getStatsHour().getSum() != 0) {
            return bsd.getStatsHour().getSum();
        }

        if (bsd.getStatsMinute().getSum() != 0) {
            return bsd.getStatsMinute().getSum();
        }

        return 0;
    }


    private Integer parseNullable(Map<String, String> map, String name) {
        if (map.containsKey(name)) {
            return Integer.valueOf(map.get(name));
        }
        return -1;
    }


    private Long parseNullableLong(Map<String, String> map, String name) {
        if (map.containsKey(name)) {
            return Long.valueOf(map.get(name));
        }
        return -1L;
    }


    public Long calcLag(List<ConsumeStats> consumeStats) {
        Long lag = 0L;
        for (ConsumeStats consumeStat : consumeStats) {
            lag += consumeStat.computeTotalDiff();
        }
        return lag;
    }

    @Override
    public void recordMaxOffset(ClusterMetadata cluster) {
        String clusterName = cluster.getClusterName();
        try{
            long maxClusterOffset = 0L;
            for (String topic : mqMetaManager.getClusterTopics(cluster)) {
                long maxTopicOffset = 0L;
                boolean ignore =
                        !StringUtils.isEmpty(topic) &&
                        (topic.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX) || topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX));
                if (ignore) {
                    continue;
                }
                List<TopicStats> topicStatsLst;
                try {
                    topicStatsLst = TopicStats.fromTopicStats(stats(cluster, topic));
                } catch (Throwable e) {
                    logger.error("Fetch topic metadata error, cluster=" + clusterName + ", topic=" + topic, e);
                    continue;
                }
                for (TopicStats topicStats : topicStatsLst) {
                    maxTopicOffset += topicStats.getMaxOffset();
                }
                this.saveStatisticTopicOffset(clusterName, topic, maxTopicOffset);
                String cacheKey = clusterName + "." + topic;
                topicMaxOffsets.put(cacheKey, maxTopicOffset);
                maxClusterOffset += maxTopicOffset;
            }
            this.saveStatisticClusterOffset(clusterName, maxClusterOffset);
            logger.info(clusterName + " Cluster offset statistic has been insert into mysql.");
        }catch (Throwable e){
            logger.error(clusterName + " message increase metrics collect error:" + e.getMessage());
        }
    }

    @Override
    public void recordMaxOffsetInflux(ClusterMetadata cluster) {
        String clusterName = cluster.getClusterName();
        try{
            List<MetricsDo> metricsList = Lists.newArrayList();
            List<MetricsDo> metricsDLQLst = Lists.newArrayList();
            for(String topic : mqMetaManager.getClusterTopics(cluster)){
                if (topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX)) {
                    continue;
                }
                MetricsDo<Double> metricsDo = new MetricsDo<>();
                Map<String,String> tagOptions = metricsDo.getTagOptions();
                Map<String, Double> segmentMap = metricsDo.getSegmentMap();
                tagOptions.put("clusterName",clusterName);
                tagOptions.put("topicName",topic);
                List<TopicStats> topicStatsLst;
                try{
                    topicStatsLst = TopicStats.fromTopicStats(stats(cluster,topic));
                }catch (Throwable e){
                    logger.error("Fetch topic metadata error, cluster=" + clusterName+  ", topic=" + topic + " \r\n" + e.getMessage());
                    continue;
                }
                long maxOffset = 0L;
                for(TopicStats topicStats : topicStatsLst){
                    maxOffset += topicStats.getMaxOffset();
                }
                segmentMap.put("maxOffset", (double) maxOffset);
                if(!topic.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX)){
                    Long standardVal = 0L;
                    String cacheKey = clusterName+"."+topic;
                    if(null == topicMaxOffsets.get(cacheKey)){
                        TopicDailyOffsetsInfo latestStatisticTopicOffset = topicDailyOffsetsRepository.getLastTopicMaxOffset(cluster.getClusterName(),topic);
                        if(null!= latestStatisticTopicOffset){
                            standardVal =  Double.valueOf(latestStatisticTopicOffset.getValue()).longValue();
                            topicMaxOffsets.put(cacheKey,standardVal);
                        }
                    }else{
                        standardVal = topicMaxOffsets.get(cacheKey);
                    }
                    Long increaseVal = maxOffset - standardVal;
                    segmentMap.put("increaseValue", Double.valueOf(increaseVal));
                    segmentMap.put("standardValue",Double.valueOf(standardVal));
                    metricsList.add(metricsDo);
                }else{
                    metricsDLQLst.add(metricsDo);
                }
            }
            if(!CollectionUtils.isEmpty(metricsList)){
                influxdbClient.writeData(metricsList,ZmsConst.Measurement.STATISTIC_TOPIC_OFFSETS_INFO,System.currentTimeMillis());
            }
            if(!CollectionUtils.isEmpty(metricsDLQLst)){
                influxdbClient.writeData(metricsDLQLst,ZmsConst.Measurement.STATISTIC_DLQ_TOPIC_OFFSETS_INFO,System.currentTimeMillis());
            }
        }catch (Throwable e){
            logger.error(clusterName + " message increase metrics collect error:" + e.getMessage());
        }
    }

    private TopicStatsTable stats(ClusterMetadata cluster, String topic) {
        try {
            DefaultMQAdminExt mqAdminExt = mqAdminManager.getMqAdmin(cluster.getClusterName());
            return mqAdminExt.examineTopicStats(topic);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void topicTpsTop10() {
        try {
            List<MqTopicInfo> topicInfoList = extensionRepository.queryMqTopicMaxTps();
            List<MetricsDo> metricsDos = topicInfoList.stream()
                    .sorted(Comparator.comparing(MqTopicInfo::getValue).reversed()
                            .thenComparing(MqTopicInfo::getTopicName))
                    .limit(10)
                    .map(item -> {
                        MetricsDo<Double> metricsDo = new MetricsDo();
                        Map<String, String> tagOptions = metricsDo.getTagOptions();
                        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
                        tagOptions.put("clusterName", item.getClusterName());
                        tagOptions.put("topicName", item.getTopicName());
                        tagOptions.put("clusterType", BrokerType.ROCKETMQ.getName());
                        tagOptions.put("attributeName", "top10");
                        segmentMap.put("tps", item.getValue());
                        metricsDo.setTagOptions(tagOptions);
                        metricsDo.setSegmentMap(segmentMap);
                        return metricsDo;
                    }).collect(Collectors.toList());
            metricBuffer.put(ZmsConst.Measurement.TOPIC_TOP_INFO, metricsDos);
        } catch (Throwable e) {
            logger.error("topicTpsTop10 error", e);
        }
    }

    @Override
    public void consumerLatencyTop10() {
        try {
            List<MqConsumerNumberInfo> consumerLatencyInfos = extensionRepository.queryMqConsumerLatency();
            if (CollectionUtils.isEmpty(consumerLatencyInfos)) {
                return;
            }
            List<MqConsumerNumberInfo> consumerTpsInfos = extensionRepository.queryMqConsumerGroupTps();
            if (CollectionUtils.isEmpty(consumerTpsInfos)) {
                return;
            }
            Map<String, Double> consumerTpsMap = extensionRepository.filterMqTps(consumerTpsInfos);
            if (CollectionUtils.isEmpty(consumerTpsMap)) {
                return;
            }
            List<MetricsDo> tpsPOList = consumerLatencyInfos.stream()
                    .filter(item -> item.getValue() > 0 && consumerTpsMap.containsKey(item.getConsumerGroup()))
                    .sorted(Comparator.comparing(MqConsumerNumberInfo::getValue).reversed()
                            .thenComparing(MqConsumerNumberInfo::getConsumerGroup))
                    .limit(10)
                    .map(item -> {
                        MetricsDo<Double> metricsDo = new MetricsDo();
                        Map<String, String> tagOptions = metricsDo.getTagOptions();
                        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
                        tagOptions.put("clusterName", item.getClusterName());
                        tagOptions.put("consumerGroup", item.getConsumerGroup());
                        tagOptions.put("clusterType", BrokerType.ROCKETMQ.getName());
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

