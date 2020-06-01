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

import com.zto.zms.collector.kafka.mbean.*;

import java.io.Serializable;
import java.util.List;


 /**
  * <p> Description: </p>
  *
  * @author liangyong
  * @date 2018/8/27
  * @since 1.0.0
  */
public class KafkaBrokerInfo implements Serializable {

    private MBeanOSInfo operatingSystem;

    private MBeanNetInfo fetchFollowerTotalTimeMs;

    private MBeanValueInfo activeControllerCount;

    private MBeanRateInfo bytesRejectedPerSec;

    private MBeanRateInfo produceRequestsPerSec;

    private MBeanDoubleValueInfo networkProcessorAvgIdlePercent;

    private MBeanRateInfo isrShrinksPerSec;

    private MBeanThreadInfo threadInfo;

    private MBeanNetInfo produceRequestQueueTimeMs;

    private MBeanRateInfo fetchFollowerRequestsPerSec;

    private MBeanNetInfo fetchConsumerTotalTimeMs;

    private MBeanRateInfo messagesInPerSec;

    private MBeanRateInfo underReplicatedPartitions;

    private MBeanRateInfo bytesOutPerSec;

    private MBeanValueInfo leaderCount;

    private MBeanNetInfo logFlushRateAndTimeMs;

    private MBeanRateInfo bytesInPerSec;

    private MBeanRateInfo requestHandlerAvgIdlePercent;

    private MBeanNetInfo fetchFollowerRequestQueueTimeMs;

    private MBeanNetInfo produceTotalTimeMs;

    private MBeanRateInfo leaderElectionRateAndTimeMs;

    private MBeanRateInfo failedProduceRequestsPerSec;

    private MBeanNetInfo fetchConsumerRequestQueueTimeMs;

    private MBeanRateInfo failedFetchRequestsPerSec;

    private MBeanRateInfo uncleanLeaderElectionsPerSec;

    private MBeanRateInfo fetchConsumerRequestsPerSec;

    private MBeanValueInfo partitionCount;

    private MBeanValueInfo offlinePartitionsCount;

    private List<MBeanBufferPoolInfo> nioInfo;

    private MBeanGCInfo g1OldGeneration;

    private MBeanGCInfo g1YoungGeneration;

    private List<MBeanMemoryInfo> jvmMemoryInfo;



    public MBeanNetInfo getFetchFollowerTotalTimeMs() {
        return fetchFollowerTotalTimeMs;
    }

    public void setFetchFollowerTotalTimeMs(MBeanNetInfo fetchFollowerTotalTimeMs) {
        this.fetchFollowerTotalTimeMs = fetchFollowerTotalTimeMs;
    }

    public MBeanValueInfo getActiveControllerCount() {
        return activeControllerCount;
    }

    public void setActiveControllerCount(MBeanValueInfo activeControllerCount) {
        this.activeControllerCount = activeControllerCount;
    }

    public MBeanRateInfo getBytesRejectedPerSec() {
        return bytesRejectedPerSec;
    }

    public void setBytesRejectedPerSec(MBeanRateInfo bytesRejectedPerSec) {
        this.bytesRejectedPerSec = bytesRejectedPerSec;
    }

    public MBeanRateInfo getProduceRequestsPerSec() {
        return produceRequestsPerSec;
    }

    public void setProduceRequestsPerSec(MBeanRateInfo produceRequestsPerSec) {
        this.produceRequestsPerSec = produceRequestsPerSec;
    }

    public MBeanDoubleValueInfo getNetworkProcessorAvgIdlePercent() {
        return networkProcessorAvgIdlePercent;
    }

    public void setNetworkProcessorAvgIdlePercent(MBeanDoubleValueInfo networkProcessorAvgIdlePercent) {
        this.networkProcessorAvgIdlePercent = networkProcessorAvgIdlePercent;
    }

    public MBeanRateInfo getIsrShrinksPerSec() {
        return isrShrinksPerSec;
    }

    public void setIsrShrinksPerSec(MBeanRateInfo isrShrinksPerSec) {
        this.isrShrinksPerSec = isrShrinksPerSec;
    }



    public MBeanNetInfo getProduceRequestQueueTimeMs() {
        return produceRequestQueueTimeMs;
    }

    public void setProduceRequestQueueTimeMs(MBeanNetInfo produceRequestQueueTimeMs) {
        this.produceRequestQueueTimeMs = produceRequestQueueTimeMs;
    }

    public MBeanRateInfo getFetchFollowerRequestsPerSec() {
        return fetchFollowerRequestsPerSec;
    }

    public void setFetchFollowerRequestsPerSec(MBeanRateInfo fetchFollowerRequestsPerSec) {
        this.fetchFollowerRequestsPerSec = fetchFollowerRequestsPerSec;
    }

    public MBeanNetInfo getFetchConsumerTotalTimeMs() {
        return fetchConsumerTotalTimeMs;
    }

    public void setFetchConsumerTotalTimeMs(MBeanNetInfo fetchConsumerTotalTimeMs) {
        this.fetchConsumerTotalTimeMs = fetchConsumerTotalTimeMs;
    }

    public MBeanRateInfo getMessagesInPerSec() {
        return messagesInPerSec;
    }

    public void setMessagesInPerSec(MBeanRateInfo messagesInPerSec) {
        this.messagesInPerSec = messagesInPerSec;
    }

    public MBeanRateInfo getUnderReplicatedPartitions() {
        return underReplicatedPartitions;
    }

    public void setUnderReplicatedPartitions(MBeanRateInfo underReplicatedPartitions) {
        this.underReplicatedPartitions = underReplicatedPartitions;
    }

    public MBeanRateInfo getBytesOutPerSec() {
        return bytesOutPerSec;
    }

    public void setBytesOutPerSec(MBeanRateInfo bytesOutPerSec) {
        this.bytesOutPerSec = bytesOutPerSec;
    }

    public MBeanValueInfo getLeaderCount() {
        return leaderCount;
    }

    public void setLeaderCount(MBeanValueInfo leaderCount) {
        this.leaderCount = leaderCount;
    }

    public MBeanNetInfo getLogFlushRateAndTimeMs() {
        return logFlushRateAndTimeMs;
    }

    public void setLogFlushRateAndTimeMs(MBeanNetInfo logFlushRateAndTimeMs) {
        this.logFlushRateAndTimeMs = logFlushRateAndTimeMs;
    }

    public MBeanRateInfo getBytesInPerSec() {
        return bytesInPerSec;
    }

    public void setBytesInPerSec(MBeanRateInfo bytesInPerSec) {
        this.bytesInPerSec = bytesInPerSec;
    }

    public MBeanRateInfo getRequestHandlerAvgIdlePercent() {
        return requestHandlerAvgIdlePercent;
    }

    public void setRequestHandlerAvgIdlePercent(MBeanRateInfo requestHandlerAvgIdlePercent) {
        this.requestHandlerAvgIdlePercent = requestHandlerAvgIdlePercent;
    }

    public MBeanNetInfo getFetchFollowerRequestQueueTimeMs() {
        return fetchFollowerRequestQueueTimeMs;
    }

    public void setFetchFollowerRequestQueueTimeMs(MBeanNetInfo fetchFollowerRequestQueueTimeMs) {
        this.fetchFollowerRequestQueueTimeMs = fetchFollowerRequestQueueTimeMs;
    }

    public MBeanNetInfo getProduceTotalTimeMs() {
        return produceTotalTimeMs;
    }

    public void setProduceTotalTimeMs(MBeanNetInfo produceTotalTimeMs) {
        this.produceTotalTimeMs = produceTotalTimeMs;
    }

    public MBeanRateInfo getLeaderElectionRateAndTimeMs() {
        return leaderElectionRateAndTimeMs;
    }

    public void setLeaderElectionRateAndTimeMs(MBeanRateInfo leaderElectionRateAndTimeMs) {
        this.leaderElectionRateAndTimeMs = leaderElectionRateAndTimeMs;
    }

    public MBeanRateInfo getFailedProduceRequestsPerSec() {
        return failedProduceRequestsPerSec;
    }

    public void setFailedProduceRequestsPerSec(MBeanRateInfo failedProduceRequestsPerSec) {
        this.failedProduceRequestsPerSec = failedProduceRequestsPerSec;
    }

    public MBeanNetInfo getFetchConsumerRequestQueueTimeMs() {
        return fetchConsumerRequestQueueTimeMs;
    }

    public void setFetchConsumerRequestQueueTimeMs(MBeanNetInfo fetchConsumerRequestQueueTimeMs) {
        this.fetchConsumerRequestQueueTimeMs = fetchConsumerRequestQueueTimeMs;
    }

    public MBeanRateInfo getFailedFetchRequestsPerSec() {
        return failedFetchRequestsPerSec;
    }

    public void setFailedFetchRequestsPerSec(MBeanRateInfo failedFetchRequestsPerSec) {
        this.failedFetchRequestsPerSec = failedFetchRequestsPerSec;
    }

    public MBeanRateInfo getUncleanLeaderElectionsPerSec() {
        return uncleanLeaderElectionsPerSec;
    }

    public void setUncleanLeaderElectionsPerSec(MBeanRateInfo uncleanLeaderElectionsPerSec) {
        this.uncleanLeaderElectionsPerSec = uncleanLeaderElectionsPerSec;
    }

    public MBeanRateInfo getFetchConsumerRequestsPerSec() {
        return fetchConsumerRequestsPerSec;
    }

    public void setFetchConsumerRequestsPerSec(MBeanRateInfo fetchConsumerRequestsPerSec) {
        this.fetchConsumerRequestsPerSec = fetchConsumerRequestsPerSec;
    }

    public MBeanValueInfo getPartitionCount() {
        return partitionCount;
    }

    public void setPartitionCount(MBeanValueInfo partitionCount) {
        this.partitionCount = partitionCount;
    }

    public MBeanValueInfo getOfflinePartitionsCount() {
        return offlinePartitionsCount;
    }

    public void setOfflinePartitionsCount(MBeanValueInfo offlinePartitionsCount) {
        this.offlinePartitionsCount = offlinePartitionsCount;
    }

    public MBeanGCInfo getG1OldGeneration() {
        return g1OldGeneration;
    }

    public void setG1OldGeneration(MBeanGCInfo g1OldGeneration) {
        this.g1OldGeneration = g1OldGeneration;
    }

    public MBeanGCInfo getG1YoungGeneration() {
        return g1YoungGeneration;
    }

    public void setG1YoungGeneration(MBeanGCInfo g1YoungGeneration) {
        this.g1YoungGeneration = g1YoungGeneration;
    }

    public MBeanOSInfo getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(MBeanOSInfo operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public MBeanThreadInfo getThreadInfo() {
        return threadInfo;
    }

    public void setThreadInfo(MBeanThreadInfo threadInfo) {
        this.threadInfo = threadInfo;
    }

    public List<MBeanBufferPoolInfo> getNioInfo() {
        return nioInfo;
    }

    public void setNioInfo(List<MBeanBufferPoolInfo> nioInfo) {
        this.nioInfo = nioInfo;
    }

    public List<MBeanMemoryInfo> getJvmMemoryInfo() {
        return jvmMemoryInfo;
    }

    public void setJvmMemoryInfo(List<MBeanMemoryInfo> jvmMemoryInfo) {
        this.jvmMemoryInfo = jvmMemoryInfo;
    }
}

