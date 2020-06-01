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


import com.zto.zms.collector.kafka.mbean.MBeanDoubleValueInfo;
import com.zto.zms.collector.kafka.mbean.MBeanNetInfo;
import com.zto.zms.collector.kafka.mbean.MBeanRateInfo;
import com.zto.zms.collector.kafka.mbean.MBeanValueInfo;

/**
 * Created by liangyong on 2018/8/14.
 */
public interface KafkaMbeanService {

    /**
     * 消息入站速率（Byte）
     * @param uri
     * @return
     */
    MBeanRateInfo bytesInPerSec(String uri);

    /**
     * 某个Topic消息入站速率（Byte）
     * @param uri
     * @param topic
     * @return
     */
    MBeanRateInfo bytesInPerSec(String uri, String topic);

    /**
     * 消息出站速率（Byte）
     * @param uri
     * @return
     */
    MBeanRateInfo bytesOutPerSec(String uri);

    /**
     * 某个Topic消息出站速率（Byte）
     * @param uri
     * @param topic
     * @return
     */
    MBeanRateInfo bytesOutPerSec(String uri, String topic);

    /**
     * 请求被拒速率
     * @param uri
     * @return
     */
    MBeanRateInfo bytesRejectedPerSec(String uri);

    /**
     * 某个Topic请求被拒速率
     * @param uri
     * @param topic
     * @return
     */
    MBeanRateInfo bytesRejectedPerSec(String uri, String topic);


    /**
     * 消息入站速率（message）
     * @param uri
     * @return
     */
    MBeanRateInfo messagesInPerSec(String uri);

    /**
     * 某个topic消息入站速率（message）
     * @param uri
     * @return
     */
    MBeanRateInfo messagesInPerSec(String uri, String topic);

    /**
     * 失败拉去请求速率
     * @param uri
     * @return
     */
    MBeanRateInfo failedFetchRequestsPerSec(String uri);

    /**
     * 某个Topic失败拉去请求速率
     * @param uri
     * @return
     */
    MBeanRateInfo failedFetchRequestsPerSec(String uri, String topic);

    /**
     * 发送请求失败速率
     * @param uri
     * @return
     */
    MBeanRateInfo failedProduceRequestsPerSec(String uri);

    /**
     * 某个Topic发送请求失败速率
     * @param uri
     * @return
     */
    MBeanRateInfo failedProduceRequestsPerSec(String uri, String topic);

    /**
     * Controller存活JMX指标
     * @param uri
     * @return
     */
    MBeanValueInfo activeControllerCount(String uri);

    /**
     * Leader选举比率
     * @param uri
     * @return
     */
    MBeanRateInfo leaderElectionRateAndTimeMs(String uri);

    /**
     * Unclean Leader选举比率
     * @param uri
     * @return
     */
    MBeanRateInfo uncleanLeaderElectionsPerSec(String uri);

    /**
     * Leader分区数
     * @param uri
     * @return
     */
    MBeanValueInfo leaderCount(String uri);

    /**
     * Partition数量
     * @param uri
     * @return
     */
    MBeanValueInfo partitionCount(String uri);

    /**
     * 下线Partition数量
     * @param uri
     * @return
     */
    MBeanValueInfo offlinePartitionsCount(String uri);

    /**
     * ISR变化速率
     * @param uri
     * @return
     */
    MBeanRateInfo isrShrinksPerSec(String uri);

    /**
     * Broker I/O工作处理线程空闲率
     * @param uri
     * @return
     */
    MBeanDoubleValueInfo networkProcessorAvgIdlePercent(String uri);

    /**
     * Broker 网络处理线程空闲率
     * @param uri
     * @return
     */
    MBeanRateInfo requestHandlerAvgIdlePercent(String uri);

    /**
     * 请求速率
     * @param uri
     * @return
     */
    MBeanRateInfo produceRequestsPerSec(String uri);

    /**
     * Consumer拉取速率
     * @param uri
     * @return
     */
    MBeanRateInfo fetchConsumerRequestsPerSec(String uri);

    /**
     * Follower 拉去速率
     * @param uri
     * @return
     */
    MBeanRateInfo fetchFollowerRequestsPerSec(String uri);

    /**
     * Request total time
     * @param uri
     * @return
     */
    MBeanNetInfo produceTotalTimeMs(String uri);

    /**
     * Consumer fetch total time
     * @param uri
     * @return
     */
    MBeanNetInfo fetchConsumerTotalTimeMs(String uri);

    /**
     * Follower fetch total time
     * @param uri
     * @return
     */
    MBeanNetInfo fetchFollowerTotalTimeMs(String uri);

    /**
     * Time the follower fetch request waits in the request queue
     * @param uri
     * @return
     */
    MBeanNetInfo fetchFollowerRequestQueueTimeMs(String uri);

    /**
     * Time the Consumer fetch request waits in the request queue
     * @param uri
     * @return
     */
    MBeanNetInfo fetchConsumerRequestQueueTimeMs(String uri);

    /**
     * Time the Produce fetch request waits in the request queue
     * @param uri
     * @return
     */
    MBeanNetInfo produceRequestQueueTimeMs(String uri);

    /**
     * Log flush rate and time
     * @param uri
     * @return
     */
    MBeanNetInfo logFlushRateAndTimeMs(String uri);
}

