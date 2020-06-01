/**
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

import request from '@/utils/request'
import * as env from '@/config/env.js'
const HostName = env.HostName

// RtTime监控节点查询
export function queryRtTimeBrokerNames(params) {
  return request({
    url: `${HostName}/api/statistics/queryRtTimeBrokerNames`,
    method: 'get',
    params: params
  })
}

// RtTime监控指标查询
export function queryRtTimeMetrics(params) {
  return request({
    url: `${HostName}/api/statistics/queryRtTimeMetrics`,
    method: 'get',
    params: params
  })
}

// RocketMQ节点查询
export function queryMqBrokerList(params) {
  return request({
    url: `${HostName}/api/statistics/queryMqBrokerList`,
    method: 'get',
    params: params
  })
}

// RocketMQ节点详情
export function queryMqBrokerMetrics(params) {
  return request({
    url: `${HostName}/api/statistics/queryMqBrokerMetrics`,
    method: 'get',
    params: params
  })
}

// Kafka节点详情
export function queryKafkaBrokerList(params) {
  return request({
    url: `${HostName}/api/statistics/queryKafkaBrokerList`,
    method: 'get',
    params: params
  })
}

export function queryActiveControllerCount(params) {
  return request({
    url: `${HostName}/api/statistics/queryActiveControllerCount`,
    method: 'get',
    params: params
  })
}

export function queryBytesInPerSec(params) {
  return request({
    url: `${HostName}/api/statistics/queryBytesInPerSec`,
    method: 'get',
    params: params
  })
}

export function queryBytesOutPerSec(params) {
  return request({
    url: `${HostName}/api/statistics/queryBytesOutPerSec`,
    method: 'get',
    params: params
  })
}

export function queryBytesRejectedPerSec(params) {
  return request({
    url: `${HostName}/api/statistics/queryBytesRejectedPerSec`,
    method: 'get',
    params: params
  })
}

export function queryFailedFetchRequestsPerSec(params) {
  return request({
    url: `${HostName}/api/statistics/queryFailedFetchRequestsPerSec`,
    method: 'get',
    params: params
  })
}

export function queryFailedProduceRequestsPerSec(params) {
  return request({
    url: `${HostName}/api/statistics/queryFailedProduceRequestsPerSec`,
    method: 'get',
    params: params
  })
}

export function queryFetchConsumerRequestQueueTimeMs(params) {
  return request({
    url: `${HostName}/api/statistics/queryFetchConsumerRequestQueueTimeMs`,
    method: 'get',
    params: params
  })
}

export function queryFetchConsumerRequestsPerSec(params) {
  return request({
    url: `${HostName}/api/statistics/queryFetchConsumerRequestsPerSec`,
    method: 'get',
    params: params
  })
}

export function queryFetchConsumerTotalTimeMs(params) {
  return request({
    url: `${HostName}/api/statistics/queryFetchConsumerTotalTimeMs`,
    method: 'get',
    params: params
  })
}

export function queryFetchFollowerRequestQueueTimeMs(params) {
  return request({
    url: `${HostName}/api/statistics/queryFetchFollowerRequestQueueTimeMs`,
    method: 'get',
    params: params
  })
}

export function queryFetchFollowerRequestsPerSec(params) {
  return request({
    url: `${HostName}/api/statistics/queryFetchFollowerRequestsPerSec`,
    method: 'get',
    params: params
  })
}

export function queryFetchFollowerTotalTimeMs(params) {
  return request({
    url: `${HostName}/api/statistics/queryFetchFollowerTotalTimeMs`,
    method: 'get',
    params: params
  })
}

export function queryIsrShrinksPerSec(params) {
  return request({
    url: `${HostName}/api/statistics/queryIsrShrinksPerSec`,
    method: 'get',
    params: params
  })
}

export function queryLeaderCount(params) {
  return request({
    url: `${HostName}/api/statistics/queryLeaderCount`,
    method: 'get',
    params: params
  })
}

export function queryLeaderElectionRateAndTimeMs(params) {
  return request({
    url: `${HostName}/api/statistics/queryLeaderElectionRateAndTimeMs`,
    method: 'get',
    params: params
  })
}

export function queryLogFlushRateAndTimeMs(params) {
  return request({
    url: `${HostName}/api/statistics/queryLogFlushRateAndTimeMs`,
    method: 'get',
    params: params
  })
}

export function queryMessagesInPerSec(params) {
  return request({
    url: `${HostName}/api/statistics/queryMessagesInPerSec`,
    method: 'get',
    params: params
  })
}

export function queryOfflinePartitionsCount(params) {
  return request({
    url: `${HostName}/api/statistics/queryOfflinePartitionsCount`,
    method: 'get',
    params: params
  })
}

export function queryPartitionCount(params) {
  return request({
    url: `${HostName}/api/statistics/queryPartitionCount`,
    method: 'get',
    params: params
  })
}

export function queryProduceRequestQueueTimeMs(params) {
  return request({
    url: `${HostName}/api/statistics/queryProduceRequestQueueTimeMs`,
    method: 'get',
    params: params
  })
}

export function queryProduceRequestsPerSec(params) {
  return request({
    url: `${HostName}/api/statistics/queryProduceRequestsPerSec`,
    method: 'get',
    params: params
  })
}

export function queryProduceTotalTimeMs(params) {
  return request({
    url: `${HostName}/api/statistics/queryProduceTotalTimeMs`,
    method: 'get',
    params: params
  })
}

export function queryRequestHandlerAvgIdlePercent(params) {
  return request({
    url: `${HostName}/api/statistics/queryRequestHandlerAvgIdlePercent`,
    method: 'get',
    params: params
  })
}

export function queryUncleanLeaderElectionsPerSec(params) {
  return request({
    url: `${HostName}/api/statistics/queryUncleanLeaderElectionsPerSec`,
    method: 'get',
    params: params
  })
}

export function queryUnderReplicatedPartitions(params) {
  return request({
    url: `${HostName}/api/statistics/queryUnderReplicatedPartitions`,
    method: 'get',
    params: params
  })
}

// kafka-jvm详情
export function queryG1OldGeneration(params) {
  return request({
    url: `${HostName}/api/statistics/queryG1OldGeneration`,
    method: 'get',
    params: params
  })
}

export function queryG1YoungGeneration(params) {
  return request({
    url: `${HostName}/api/statistics/queryG1YoungGeneration`,
    method: 'get',
    params: params
  })
}

export function queryNetworkProcessorAvgIdlePercent(params) {
  return request({
    url: `${HostName}/api/statistics/queryNetworkProcessorAvgIdlePercent`,
    method: 'get',
    params: params
  })
}

export function queryOperatingSystem(params) {
  return request({
    url: `${HostName}/api/statistics/queryOperatingSystem`,
    method: 'get',
    params: params
  })
}

export function queryHeapMemoryUsage(params) {
  return request({
    url: `${HostName}/api/statistics/queryHeapMemoryUsage`,
    method: 'get',
    params: params
  })
}

export function queryNonHeapMemoryUsage(params) {
  return request({
    url: `${HostName}/api/statistics/queryNonHeapMemoryUsage`,
    method: 'get',
    params: params
  })
}

export function queryMetaspace(params) {
  return request({
    url: `${HostName}/api/statistics/queryMetaspace`,
    method: 'get',
    params: params
  })
}

export function queryMetaspacePeak(params) {
  return request({
    url: `${HostName}/api/statistics/queryMetaspacePeak`,
    method: 'get',
    params: params
  })
}

export function queryGV1SurvivorSpace(params) {
  return request({
    url: `${HostName}/api/statistics/queryGV1SurvivorSpace`,
    method: 'get',
    params: params
  })
}

export function queryG1SurvivorSpacePeak(params) {
  return request({
    url: `${HostName}/api/statistics/queryG1SurvivorSpacePeak`,
    method: 'get',
    params: params
  })
}

export function queryCompressedClassSpace(params) {
  return request({
    url: `${HostName}/api/statistics/queryCompressedClassSpace`,
    method: 'get',
    params: params
  })
}

export function queryCompressedClassSpacePeak(params) {
  return request({
    url: `${HostName}/api/statistics/queryCompressedClassSpacePeak`,
    method: 'get',
    params: params
  })
}

export function queryG1EdenSpace(params) {
  return request({
    url: `${HostName}/api/statistics/queryG1EdenSpace`,
    method: 'get',
    params: params
  })
}

export function queryG1EdenSpacePeak(params) {
  return request({
    url: `${HostName}/api/statistics/queryG1EdenSpacePeak`,
    method: 'get',
    params: params
  })
}

export function queryG1OldGen(params) {
  return request({
    url: `${HostName}/api/statistics/queryG1OldGen`,
    method: 'get',
    params: params
  })
}

export function queryG1OldGenPeak(params) {
  return request({
    url: `${HostName}/api/statistics/queryG1OldGenPeak`,
    method: 'get',
    params: params
  })
}

export function queryCodecache(params) {
  return request({
    url: `${HostName}/api/statistics/queryCodecache`,
    method: 'get',
    params: params
  })
}

export function queryCodecachePeek(params) {
  return request({
    url: `${HostName}/api/statistics/queryCodecachePeek`,
    method: 'get',
    params: params
  })
}

export function queryThreadInfo(params) {
  return request({
    url: `${HostName}/api/statistics/queryThreadInfo`,
    method: 'get',
    params: params
  })
}

export function queryMappedInfo(params) {
  return request({
    url: `${HostName}/api/statistics/queryMappedInfo`,
    method: 'get',
    params: params
  })
}

export function queryDirectInfo(params) {
  return request({
    url: `${HostName}/api/statistics/queryDirectInfo`,
    method: 'get',
    params: params
  })
}
