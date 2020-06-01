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

import com.zto.zms.collector.kafka.mbean.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * Created by liangyong on 2018/8/14.
 */
@Service
public class KafkaMbeanServiceImpl implements KafkaMbeanService {


    private static Logger logger = LoggerFactory.getLogger(KafkaMbeanServiceImpl.class);

    @Override
    public MBeanRateInfo bytesInPerSec(String uri) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo bytesInPerSec(String uri, String topic) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesInPerSec,topic=" + topic;
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo bytesOutPerSec(String uri) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo bytesOutPerSec(String uri, String topic) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesOutPerSec,topic=" + topic;
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo bytesRejectedPerSec(String uri) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesRejectedPerSec";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo bytesRejectedPerSec(String uri, String topic) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=BytesRejectedPerSec,topic=" + topic;
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo messagesInPerSec(String uri) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo messagesInPerSec(String uri, String topic) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec,topic=" + topic;
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo failedFetchRequestsPerSec(String uri) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=FailedFetchRequestsPerSec";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo failedFetchRequestsPerSec(String uri, String topic) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=FailedFetchRequestsPerSec,topic=" + topic;
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo failedProduceRequestsPerSec(String uri) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=FailedProduceRequestsPerSec";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo failedProduceRequestsPerSec(String uri, String topic) {
        String mbean = "kafka.server:type=BrokerTopicMetrics,name=FailedProduceRequestsPerSec,topic=" + topic;
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanValueInfo activeControllerCount(String uri) {
        String mbean = "kafka.controller:type=KafkaController,name=ActiveControllerCount";
        return getMbeanInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo leaderElectionRateAndTimeMs(String uri) {
        String mbean = "kafka.controller:type=ControllerStats,name=LeaderElectionRateAndTimeMs";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo uncleanLeaderElectionsPerSec(String uri) {
        String mbean = "kafka.controller:type=ControllerStats,name=UncleanLeaderElectionsPerSec";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanValueInfo leaderCount(String uri) {
        String mbean = "kafka.server:type=ReplicaManager,name=LeaderCount";
        return getMbeanInfo(uri, mbean);
    }
    @Override
    public MBeanValueInfo partitionCount(String uri) {
        String mbean = "kafka.server:type=ReplicaManager,name=PartitionCount";
        return getMbeanInfo(uri, mbean);
    }
    @Override
    public MBeanValueInfo offlinePartitionsCount(String uri) {
        String mbean = "kafka.controller:type=KafkaController,name=OfflinePartitionsCount";
        return getMbeanInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo isrShrinksPerSec(String uri) {
        String mbean = "kafka.server:type=ReplicaManager,name=IsrShrinksPerSec";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanDoubleValueInfo networkProcessorAvgIdlePercent(String uri) {
        String mbean = "kafka.network:type=SocketServer,name=NetworkProcessorAvgIdlePercent";
        return getMbeanDoubleInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo requestHandlerAvgIdlePercent(String uri) {
        String mbean = "kafka.server:type=KafkaRequestHandlerPool,name=RequestHandlerAvgIdlePercent";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo produceRequestsPerSec(String uri) {
        String mbean = "kafka.network:type=RequestMetrics,name=RequestsPerSec,request=Produce";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo fetchConsumerRequestsPerSec(String uri) {
        String mbean = "kafka.network:type=RequestMetrics,name=RequestsPerSec,request=FetchConsumer";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanRateInfo fetchFollowerRequestsPerSec(String uri) {
        String mbean = "kafka.network:type=RequestMetrics,name=RequestsPerSec,request=FetchFollower";
        return getMbeanRateInfo(uri, mbean);
    }
    @Override
    public MBeanNetInfo produceTotalTimeMs(String uri) {
        String mbean = "kafka.network:type=RequestMetrics,name=TotalTimeMs,request=Produce";
        return getMbeanNetInfo(uri, mbean);
    }
    @Override
    public MBeanNetInfo fetchConsumerTotalTimeMs(String uri) {
        String mbean = "kafka.network:type=RequestMetrics,name=TotalTimeMs,request=FetchConsumer";
        return getMbeanNetInfo(uri, mbean);
    }
    @Override
    public MBeanNetInfo fetchFollowerTotalTimeMs(String uri) {
        String mbean = "kafka.network:type=RequestMetrics,name=TotalTimeMs,request=FetchFollower";
        return getMbeanNetInfo(uri, mbean);
    }
    @Override
    public MBeanNetInfo fetchFollowerRequestQueueTimeMs(String uri) {
        String mbean = "kafka.network:type=RequestMetrics,name=RequestQueueTimeMs,request=FetchFollower";
        return getMbeanNetInfo(uri, mbean);
    }
    @Override
    public MBeanNetInfo fetchConsumerRequestQueueTimeMs(String uri) {
        String mbean = "kafka.network:type=RequestMetrics,name=RequestQueueTimeMs,request=FetchConsumer";
        return getMbeanNetInfo(uri, mbean);
    }
    @Override
    public MBeanNetInfo produceRequestQueueTimeMs(String uri) {
        String mbean = "kafka.network:type=RequestMetrics,name=RequestQueueTimeMs,request=Produce";
        return getMbeanNetInfo(uri, mbean);
    }
    @Override
    public MBeanNetInfo logFlushRateAndTimeMs(String uri) {
        String mbean = "kafka.log:type=LogFlushStats,name=LogFlushRateAndTimeMs";
        return getMbeanNetInfo(uri, mbean);
    }

    private MBeanNetInfo getMbeanNetInfo(String uri, String mbean) {
        MBeanNetInfo mbeanNetInfo = new MBeanNetInfo();
        try {
            MBeanServerConnection mbeanSvrConn = MBeanConnector.getJmxConnection(uri);
            ObjectName objectName = new ObjectName(mbean);
            long count = (Long) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.COUNT);
            double min = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.MIN);
            double max = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.MAX);
            double mean = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.MEAN);
            double stdDev = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.STD_DEV);
            double percentile50th = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.PERCENTILE_50TH);
            double percentile75th = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.PERCENTILE_75TH);
            double percentile95th = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.PERCENTILE_95TH);
            double percentile98th = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.PERCENTILE_98TH);
            double percentile99th = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.PERCENTILE_99TH);
            double percentile999th = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.PERCENTILE_999TH);
            mbeanNetInfo.setCount(count);
            mbeanNetInfo.setMin(min);
            mbeanNetInfo.setMax(max);
            mbeanNetInfo.setMean(mean);
            mbeanNetInfo.setStdDev(stdDev);
            mbeanNetInfo.set_50thPercentile(percentile50th);
            mbeanNetInfo.set_75thPercentile(percentile75th);
            mbeanNetInfo.set_95thPercentile(percentile95th);
            mbeanNetInfo.set_98thPercentile(percentile98th);
            mbeanNetInfo.set_99thPercentile(percentile99th);
            mbeanNetInfo.set_999thPercentile(percentile999th);
        } catch (Exception e) {
            logger.error("Get kafka mbean network metrics faild ", e);
        }
        return mbeanNetInfo;
    }

    private MBeanValueInfo getMbeanInfo(String uri, String mbean) {
        MBeanValueInfo mBeanValueInfo = new MBeanValueInfo();
        try {
            MBeanServerConnection mbeanSvrConn = MBeanConnector.getJmxConnection(uri);
            long value = Long.parseLong(mbeanSvrConn.getAttribute(new ObjectName(mbean), MetricsConstants.MBean.VALUE).toString());
            mBeanValueInfo.setValue(value);
        } catch (InstanceNotFoundException e) {
            logger.info("mbean {} not exist on {}", mbean, uri);

        } catch (Exception e) {
            logger.error("Get kafka mbean value metrics faild ", e);
        }
        return mBeanValueInfo;
    }

    private MBeanDoubleValueInfo getMbeanDoubleInfo(String uri, String mbean) {
        MBeanDoubleValueInfo mBeanValueInfo = new MBeanDoubleValueInfo();
        try {
            MBeanServerConnection mbeanSvrConn = MBeanConnector.getJmxConnection(uri);
            double value = Double.parseDouble(mbeanSvrConn.getAttribute(new ObjectName(mbean), MetricsConstants.MBean.VALUE).toString());
            mBeanValueInfo.setValue(value);
        } catch (InstanceNotFoundException e) {
            logger.info("mbean {} not exist on {}", mbean, uri);

        } catch (Exception e) {
            logger.error("Get kafka mbean value metrics faild ", e);
        }
        return mBeanValueInfo;
    }

    private MBeanRateInfo getMbeanRateInfo(String uri, String mbean) {
        MBeanRateInfo mBeanRateInfo = new MBeanRateInfo();
        try {

            MBeanServerConnection mbeanSvrConn = MBeanConnector.getJmxConnection(uri);
            ObjectName objectName = new ObjectName(mbean);
//            mbeanSvrConn.getAttributes(objectName,new String[]{MetricsConstants.MBean.COUNT,})
            long count = (Long) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.COUNT);
            double meanRate = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.MEAN_RATE);
            double fiveMinuteRate = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.FIVE_MINUTE_RATE);
            double fifteenMinuteRate = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.FIFTEEN_MINUTE_RATE);
            double oneMinuteRate = (Double) mbeanSvrConn.getAttribute(objectName, MetricsConstants.MBean.ONE_MINUTE_RATE);
            mBeanRateInfo.setCount(count);
            mBeanRateInfo.setMeanRate(meanRate);
            mBeanRateInfo.setFiveMinuteRate(fiveMinuteRate);
            mBeanRateInfo.setFifteenMinuteRate(fifteenMinuteRate);
            mBeanRateInfo.setOneMinuteRate(oneMinuteRate);
        } catch (InstanceNotFoundException e) {
            logger.info("mbean {} not exist on {}", mbean, uri);

        } catch (Exception e) {

            logger.error("getMbeanRateInfo error", e);
        }
        return mBeanRateInfo;
    }

}

