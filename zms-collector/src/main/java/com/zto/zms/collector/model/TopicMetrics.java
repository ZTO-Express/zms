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

package com.zto.zms.collector.model;

import com.zto.zms.collector.kafka.mbean.MBeanRateInfo;

 /**
  * <p> Description: </p>
  *
  * @author liangyong
  * @date 2018/9/12
  * @since 1.0.0
  */
public class TopicMetrics {


    private String topic;
    private MBeanRateInfo messagesInPerSec;
    private MBeanRateInfo bytesOutPerSec;
    private MBeanRateInfo bytesInPerSec;
    private MBeanRateInfo bytesRejectedPerSec;
    private MBeanRateInfo failedProduceRequestsPerSec;
    private MBeanRateInfo failedFetchRequestsPerSec;

    TopicBrokerMetricsInfo topicBrokerMetricsInfo;


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public MBeanRateInfo getMessagesInPerSec() {
        return messagesInPerSec;
    }

    public void setMessagesInPerSec(MBeanRateInfo messagesInPerSec) {
        this.messagesInPerSec = messagesInPerSec;
    }

    public MBeanRateInfo getBytesOutPerSec() {
        return bytesOutPerSec;
    }

    public void setBytesOutPerSec(MBeanRateInfo bytesOutPerSec) {
        this.bytesOutPerSec = bytesOutPerSec;
    }

    public MBeanRateInfo getBytesInPerSec() {
        return bytesInPerSec;
    }

    public void setBytesInPerSec(MBeanRateInfo bytesInPerSec) {
        this.bytesInPerSec = bytesInPerSec;
    }

    public MBeanRateInfo getBytesRejectedPerSec() {
        return bytesRejectedPerSec;
    }

    public void setBytesRejectedPerSec(MBeanRateInfo bytesRejectedPerSec) {
        this.bytesRejectedPerSec = bytesRejectedPerSec;
    }

    public MBeanRateInfo getFailedProduceRequestsPerSec() {
        return failedProduceRequestsPerSec;
    }

    public void setFailedProduceRequestsPerSec(MBeanRateInfo failedProduceRequestsPerSec) {
        this.failedProduceRequestsPerSec = failedProduceRequestsPerSec;
    }

    public MBeanRateInfo getFailedFetchRequestsPerSec() {
        return failedFetchRequestsPerSec;
    }

    public void setFailedFetchRequestsPerSec(MBeanRateInfo failedFetchRequestsPerSec) {
        this.failedFetchRequestsPerSec = failedFetchRequestsPerSec;
    }

    public TopicBrokerMetricsInfo getTopicBrokerMetricsInfo() {
        return topicBrokerMetricsInfo;
    }

    public void setTopicBrokerMetricsInfo(TopicBrokerMetricsInfo topicBrokerMetricsInfo) {
        this.topicBrokerMetricsInfo = topicBrokerMetricsInfo;
    }
}

