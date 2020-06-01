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

package com.zto.zms.collector.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.zto.zms.collector.model.KafkaBrokerInfo;
import com.zto.zms.collector.model.KafkaMetrics;
import com.zto.zms.collector.kafka.mbean.MBeanGCInfo;
import com.zto.zms.collector.kafka.mbean.MBeanRateInfo;
import com.zto.zms.collector.model.TopicBrokerMetricsInfo;
import com.zto.zms.collector.model.TopicMetrics;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by liangyong on 2018/8/15.
 */
@Service
public class KafkaJMXCollector {

    private static Logger logger = LoggerFactory.getLogger(KafkaJMXCollector.class);


    @Autowired
    private GCMBeanService gcmBeanService;
    @Autowired
    private KafkaMbeanService kafkaMbeanService;
    @Autowired
    private OSMBeanService osmBeanService;
    @Autowired
    private ThreadMBeanService threadMBeanService;
    @Autowired
    private MemoryMBeanService memoryMBeanService;


    public Map<String, KafkaBrokerInfo> buildBrokersMetricsInfo(List<String> uriList) {
        Map<String, KafkaBrokerInfo> brokerGather = new HashMap<>();
        for (String uri : uriList) {
            String broker = uri.split(":")[0];
            KafkaBrokerInfo brokerMetrics = new KafkaBrokerInfo();
            //kafka metrics
            brokerMetrics.setLogFlushRateAndTimeMs(kafkaMbeanService.logFlushRateAndTimeMs(uri));
            brokerMetrics.setUnderReplicatedPartitions(kafkaMbeanService.uncleanLeaderElectionsPerSec(uri));
            brokerMetrics.setMessagesInPerSec(kafkaMbeanService.messagesInPerSec(uri));
            brokerMetrics.setBytesInPerSec(kafkaMbeanService.bytesInPerSec(uri));
            brokerMetrics.setBytesOutPerSec(kafkaMbeanService.bytesOutPerSec(uri));
            brokerMetrics.setBytesRejectedPerSec(kafkaMbeanService.bytesRejectedPerSec(uri));
            brokerMetrics.setFailedFetchRequestsPerSec(kafkaMbeanService.failedFetchRequestsPerSec(uri));
            brokerMetrics.setFailedProduceRequestsPerSec(kafkaMbeanService.failedProduceRequestsPerSec(uri));
            brokerMetrics.setLeaderCount(kafkaMbeanService.leaderCount(uri));
            brokerMetrics.setPartitionCount(kafkaMbeanService.partitionCount(uri));
            brokerMetrics.setOfflinePartitionsCount(kafkaMbeanService.offlinePartitionsCount(uri));
            brokerMetrics.setRequestHandlerAvgIdlePercent(kafkaMbeanService.requestHandlerAvgIdlePercent(uri));
            brokerMetrics.setLeaderElectionRateAndTimeMs(kafkaMbeanService.leaderElectionRateAndTimeMs(uri));
            brokerMetrics.setUncleanLeaderElectionsPerSec(kafkaMbeanService.uncleanLeaderElectionsPerSec(uri));
            brokerMetrics.setActiveControllerCount(kafkaMbeanService.activeControllerCount(uri));
            brokerMetrics.setProduceRequestsPerSec(kafkaMbeanService.produceRequestsPerSec(uri));
            brokerMetrics.setFetchConsumerRequestsPerSec(kafkaMbeanService.fetchConsumerRequestsPerSec(uri));
            brokerMetrics.setFetchFollowerRequestsPerSec(kafkaMbeanService.fetchFollowerRequestsPerSec(uri));
            brokerMetrics.setProduceTotalTimeMs(kafkaMbeanService.produceTotalTimeMs(uri));
            brokerMetrics.setFetchConsumerTotalTimeMs(kafkaMbeanService.fetchConsumerTotalTimeMs(uri));
            brokerMetrics.setFetchFollowerTotalTimeMs(kafkaMbeanService.fetchFollowerTotalTimeMs(uri));
            brokerMetrics.setProduceRequestQueueTimeMs(kafkaMbeanService.produceRequestQueueTimeMs(uri));
            brokerMetrics.setFetchFollowerRequestQueueTimeMs(kafkaMbeanService.fetchFollowerRequestQueueTimeMs(uri));
            brokerMetrics.setFetchConsumerRequestQueueTimeMs(kafkaMbeanService.fetchConsumerRequestQueueTimeMs(uri));
            brokerMetrics.setNetworkProcessorAvgIdlePercent(kafkaMbeanService.networkProcessorAvgIdlePercent(uri));
            brokerMetrics.setIsrShrinksPerSec(kafkaMbeanService.isrShrinksPerSec(uri));
            MBeanGCInfo oldGc = gcmBeanService.oldGenerationOfG1(uri);
            brokerMetrics.setG1OldGeneration(oldGc);
            MBeanGCInfo youngGc = gcmBeanService.youngGenerationOfG1(uri);
            brokerMetrics.setG1YoungGeneration(youngGc);
            brokerMetrics.setJvmMemoryInfo(memoryMBeanService.getJvmMemoryInfo(uri));
            brokerMetrics.setNioInfo(memoryMBeanService.getMemoryBufferPoolInfo(uri));
            //OS Metrics
            brokerMetrics.setOperatingSystem(osmBeanService.operatingSystem(uri));
            //Thread Metrics
            brokerMetrics.setThreadInfo(threadMBeanService.collectThread(uri));
            brokerGather.put(broker, brokerMetrics);
        }
        return brokerGather;
    }


    public Map<String, Object> buildTopicMetricsInfo(List<String> uriList, String topic) {
        Map<String, Object> topicGather = new HashMap<String, Object>();
        topicGather.put("Topic", topic);
        for (String uri : uriList) {
            String broker = uri.split(":")[0];
            Map<String, MBeanRateInfo> topicBorkerMetrics = new HashMap<String, MBeanRateInfo>();
            MBeanRateInfo bytesInPerSec = kafkaMbeanService.bytesInPerSec(uri, topic);
            MBeanRateInfo bytesOutPerSec = kafkaMbeanService.bytesOutPerSec(uri, topic);
            MBeanRateInfo bytesRejectedPerSec = kafkaMbeanService.bytesRejectedPerSec(uri, topic);
            MBeanRateInfo failedFetchRequestsPerSec = kafkaMbeanService.failedFetchRequestsPerSec(uri, topic);
            MBeanRateInfo failedProduceRequestsPerSec = kafkaMbeanService.failedProduceRequestsPerSec(uri, topic);
            MBeanRateInfo messagesInPerSec = kafkaMbeanService.messagesInPerSec(uri, topic);
            topicBorkerMetrics.put("BytesInPerSec", bytesInPerSec);
            topicBorkerMetrics.put("BytesOutPerSec", bytesOutPerSec);
            topicBorkerMetrics.put("BytesRejectedPerSec", bytesRejectedPerSec);
            topicBorkerMetrics.put("FailedFetchRequestsPerSec", failedFetchRequestsPerSec);
            topicBorkerMetrics.put("FailedProduceRequestsPerSec", failedProduceRequestsPerSec);
            topicBorkerMetrics.put("MessagesInPerSec", messagesInPerSec);
            topicGather.put(broker, topicBorkerMetrics);
            topicGather.put("BytesInPerSec", calcBytesInPerSec(topicGather, bytesInPerSec));
            topicGather.put("BytesOutPerSec", calcBytesOutPerSec(topicGather, bytesOutPerSec));
            topicGather.put("BytesRejectedPerSec", calcBytesRejectedPerSec(topicGather, bytesRejectedPerSec));
            topicGather.put("FailedFetchRequestsPerSec", calcFailedFetchRequestsPerSec(topicGather, failedFetchRequestsPerSec));
            topicGather.put("FailedProduceRequestsPerSec", calcFailedProduceRequestsPerSec(topicGather, failedProduceRequestsPerSec));
            topicGather.put("MessagesInPerSec", calcMessagesInPerSec(topicGather, messagesInPerSec));
        }
        return topicGather;
    }

    public KafkaMetrics buildKafkaMetricsInfo(String clusterName, List<String> uriList, List<String> topicList) {
        KafkaMetrics metrics = new KafkaMetrics();
        List<String> brokerList = brokerList(uriList);

        metrics.setBrokers(brokerList);
        metrics.setTimestamp(System.currentTimeMillis());
        metrics.setClusterName(clusterName);
        metrics.setBrokersMetrics(this.buildBrokersMetricsInfo(uriList));

        List<Map<String, Object>> topicsMetrics = new ArrayList<>();
        for (String topic : topicList) {
            topicsMetrics.add(buildTopicMetricsInfo(uriList, topic));
        }
        metrics.setTopicMetrics(topicsMetrics);
        String tbStr = JSONObject.toJSONString(topicsMetrics);
        List<JSONObject> list = JSONObject.parseArray(tbStr,JSONObject.class);
        List<TopicMetrics> topicMetricsInfo = new ArrayList<>();
        list.forEach((tItem) -> {
            TopicMetrics tMetric = JSONObject.toJavaObject(tItem,TopicMetrics.class);
            BeanUtils.copyProperties(tItem,tMetric,TopicMetrics.class);
            tItem.remove("MessagesInPerSec");
            tItem.remove("BytesOutPerSec");
            tItem.remove("BytesInPerSec");
            tItem.remove("BytesRejectedPerSec");
            tItem.remove("FailedProduceRequestsPerSec");
            tItem.remove("Topic");
            tItem.remove("FailedFetchRequestsPerSec");
            String tbJson = JSON.toJSONString(tItem);
            tbJson = "{\"topicBrokerMetrics\":".concat(tbJson).concat("}");
            TopicBrokerMetricsInfo topicBrokerMetricsInfo = JSON.parseObject(tbJson, TopicBrokerMetricsInfo.class);
            tMetric.setTopicBrokerMetricsInfo(topicBrokerMetricsInfo);
            topicMetricsInfo.add(tMetric);
            });
        metrics.setTopicMetricsInfo(topicMetricsInfo);
        return metrics;
    }

    private List<String> brokerList(List<String> uriList) {
        List<String> brokerList = new ArrayList<>();
        for (String uri : uriList) {
            brokerList.add(uri.split(":")[0]);
        }
        return brokerList;
    }

    private MBeanRateInfo calcFailedProduceRequestsPerSec(Map<String, Object> topicGather, MBeanRateInfo mBeanRateInfo) {
        MBeanRateInfo mbeanRateInfoGather = (MBeanRateInfo) topicGather.get("FailedProduceRequestsPerSec");
        if (mbeanRateInfoGather == null) {
            mbeanRateInfoGather = new MBeanRateInfo();
            BeanUtils.copyProperties(mBeanRateInfo, mbeanRateInfoGather);
            return mbeanRateInfoGather;
        }
        return gatherMBeanRateInfo(mBeanRateInfo, mbeanRateInfoGather);
    }

    private MBeanRateInfo calcMessagesInPerSec(Map<String, Object> topicGather, MBeanRateInfo mBeanRateInfo) {
        MBeanRateInfo mbeanRateInfoGather = (MBeanRateInfo) topicGather.get("MessagesInPerSec");
        if (mbeanRateInfoGather == null) {
            mbeanRateInfoGather = new MBeanRateInfo();
            BeanUtils.copyProperties(mBeanRateInfo, mbeanRateInfoGather);
            return mbeanRateInfoGather;
        }
        return gatherMBeanRateInfo(mBeanRateInfo, mbeanRateInfoGather);
    }

    private MBeanRateInfo calcFailedFetchRequestsPerSec(Map<String, Object> topicGather, MBeanRateInfo mBeanRateInfo) {
        MBeanRateInfo mbeanRateInfoGather = (MBeanRateInfo) topicGather.get("FailedFetchRequestsPerSec");
        if (mbeanRateInfoGather == null) {
            mbeanRateInfoGather = new MBeanRateInfo();
            BeanUtils.copyProperties(mBeanRateInfo, mbeanRateInfoGather);
            return mbeanRateInfoGather;
        }
        return gatherMBeanRateInfo(mBeanRateInfo, mbeanRateInfoGather);
    }

    private MBeanRateInfo calcBytesRejectedPerSec(Map<String, Object> topicGather, MBeanRateInfo mBeanRateInfo) {
        MBeanRateInfo mbeanRateInfoGather = (MBeanRateInfo) topicGather.get("BytesRejectedPerSec");
        if (mbeanRateInfoGather == null) {
            mbeanRateInfoGather = new MBeanRateInfo();
            BeanUtils.copyProperties(mBeanRateInfo, mbeanRateInfoGather);
            return mbeanRateInfoGather;
        }
        return gatherMBeanRateInfo(mBeanRateInfo, mbeanRateInfoGather);
    }


    private MBeanRateInfo calcBytesOutPerSec(Map<String, Object> topicGather, MBeanRateInfo mBeanRateInfo) {
        MBeanRateInfo mbeanRateInfoGather = (MBeanRateInfo) topicGather.get("BytesOutPerSec");
        if (mbeanRateInfoGather == null) {
            mbeanRateInfoGather = new MBeanRateInfo();
            BeanUtils.copyProperties(mBeanRateInfo, mbeanRateInfoGather);
            return mbeanRateInfoGather;
        }
        return gatherMBeanRateInfo(mBeanRateInfo, mbeanRateInfoGather);
    }


    private MBeanRateInfo calcBytesInPerSec(Map<String, Object> topicGather, MBeanRateInfo mBeanRateInfo) {
        MBeanRateInfo mbeanRateInfoGather = (MBeanRateInfo) topicGather.get("BytesInPerSec");
        if (mbeanRateInfoGather == null) {
            mbeanRateInfoGather = new MBeanRateInfo();
            BeanUtils.copyProperties(mBeanRateInfo, mbeanRateInfoGather);
            return mbeanRateInfoGather;
        }
        return gatherMBeanRateInfo(mBeanRateInfo, mbeanRateInfoGather);
    }

    private double addCalc(double value1, double value2) {

        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return b1.add(b2).doubleValue();
    }

    private long addCalc(long value1, long value2) {

        BigDecimal b1 = BigDecimal.valueOf(value1);
        BigDecimal b2 = BigDecimal.valueOf(value2);
        return b1.add(b2).longValue();
    }


    private MBeanRateInfo gatherMBeanRateInfo(MBeanRateInfo mBeanRateInfo, MBeanRateInfo mbeanRateInfoGather) {
        mbeanRateInfoGather.setCount(addCalc(mbeanRateInfoGather.getCount(), mBeanRateInfo.getCount()));
        mbeanRateInfoGather.setOneMinuteRate(addCalc(mbeanRateInfoGather.getOneMinuteRate(), mBeanRateInfo.getOneMinuteRate()));
        mbeanRateInfoGather.setFiveMinuteRate(addCalc(mbeanRateInfoGather.getFiveMinuteRate(), mBeanRateInfo.getFiveMinuteRate()));
        mbeanRateInfoGather.setFifteenMinuteRate(addCalc(mbeanRateInfoGather.getFifteenMinuteRate(), mBeanRateInfo.getFifteenMinuteRate()));
        mbeanRateInfoGather.setMeanRate(addCalc(mbeanRateInfoGather.getMeanRate(), mBeanRateInfo.getMeanRate()));
        return mbeanRateInfoGather;
    }


}

