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

package com.zto.zms.portal.service.monitor;

import com.google.common.collect.Lists;
import com.zto.zms.common.BrokerType;
import com.zto.zms.service.domain.page.PageResult;
import com.zto.zms.dal.mapper.ConsumerMapper;
import com.zto.zms.dal.mapper.ZmsServiceMapper;
import com.zto.zms.dal.model.Consumer;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.result.ConsumerSummary;
import com.zto.zms.portal.result.ConsumerSummaryExt;
import com.zto.zms.service.domain.influxdb.ConsumerInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/4/14
 **/
@Service
public class MonitorServiceAdapt implements MonitorServiceManager {

    @Autowired
    private Map<String, IMonitorService> monitorServiceMap;

    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private ZmsServiceMapper zmsServiceMapper;

    public IMonitorService currentMonitorService(String clusterType) {
        return monitorServiceMap.get("MonitorService-" + clusterType.toLowerCase());
    }

    @Override
    public PageResult<ConsumerSummary> getConsumerSummariesByPage(String clusterType, String clusterName, String consumerName, int envId, int pageNo, int pageSize, Boolean allDelay) {
        if (StringUtils.isNotBlank(consumerName)) {
            List<Consumer> consumerList = consumerMapper.selectConsumerByNameAndEnvId(consumerName.toLowerCase(), envId, clusterType);
            if (CollectionUtils.isEmpty(consumerList)) {
                return new PageResult<>(pageNo, pageSize);
            }
            Consumer consumer = consumerList.get(0);
            if (null != consumer) {
                clusterName = consumer.getClusterName();
                clusterType = consumer.getClusterType();
            }
            if (StringUtils.isBlank(clusterName) || StringUtils.isBlank(clusterType)) {
                return new PageResult<>(pageNo, pageSize);
            }
            ConsumerInfo consumerLatency = currentMonitorService(clusterType).readLatency(clusterName, consumerName);
            ConsumerInfo consumerTps = currentMonitorService(clusterType).readTps(clusterName, consumerName);
            ConsumerSummaryExt summary = new ConsumerSummaryExt(consumerLatency, consumerTps);
            if (StringUtils.isEmpty(summary.getConsumer())) {
                return new PageResult<>(pageNo, pageSize);
            }
            summary.setDelayThreadhold(consumer.getDelayThreadhold());
            if (consumer.getStatus() > 0) {
                summary.setConsumerId(consumer.getId());
            }
            summary.setOwner(consumer.getApplicant());
            return new PageResult<>(pageNo, pageSize, 1, Lists.newArrayList(summary));
        }
        List<ConsumerInfo> latencies = new ArrayList<>();
        List<ConsumerInfo> tpses = new ArrayList<>();
        if (StringUtils.isBlank(clusterName)) {
            if (StringUtils.isEmpty(clusterType)) {
                //集群类型为空,获取所有类型延迟消息
                for (String cluster : BrokerType.getBrokerTypes()) {
                    //消费延迟
                    List<ConsumerInfo> latency = currentMonitorService(cluster).readLatencies();
                    List<ConsumerInfo> tps = currentMonitorService(cluster).readTpses();
                    latencies.addAll(latency);
                    tpses.addAll(tps);
                }
            } else {
                latencies = currentMonitorService(clusterType).readLatencies();
                //消费延迟
                tpses = currentMonitorService(clusterType).readTpses();
            }
        } else {
            if (StringUtils.isEmpty(clusterType)) {
                ZmsServiceEntity zmsServiceEntity = zmsServiceMapper.getByEnvIdAndName(envId, clusterName);
                clusterType = zmsServiceEntity.getServerType();
            }
            latencies = currentMonitorService(clusterType).readLatencies(Lists.newArrayList(clusterName));
            //消费组tps
            tpses = currentMonitorService(clusterType).readTpses(Lists.newArrayList(clusterName));
        }
        long total = latencies.size();
        if (0 == total) {
            return new PageResult<>(pageNo, pageSize);
        }
        //只展示大于积压阈值的延迟信息
        if (null != allDelay && allDelay) {
            List<Consumer> consumers = consumerMapper.selectConsumerNameAnddDelayhold();
            Map<String, Long> consumerMap = consumers.stream().collect(Collectors.toMap(Consumer::getName, Consumer::getDelayThreadhold));
            latencies = latencies.stream()
                    .filter(item -> (null != consumerMap.get(item.getConsumerGroup()) && item.getValue() > consumerMap.get(item.getConsumerGroup())))
                    .collect(Collectors.toList());
            total = latencies.size();
            if (0 == total) {
                return new PageResult<>(pageNo, pageSize);
            }
        }
        //内存分页
        latencies = latencies.stream()
                .sorted(Comparator.comparing(ConsumerInfo::getValue).reversed())
                .skip(pageSize * (pageNo - 1))
                .limit(pageSize)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(latencies)) {
            return new PageResult<>(pageNo, pageSize, total, Lists.newArrayList());
        }
        List<String> consumerGroups = latencies.stream().map(ConsumerInfo::getConsumerGroup).collect(Collectors.toList());

        Map<String, Consumer> consumerNameMap = consumerMapper.listConsumerByNames(consumerGroups).stream().collect(Collectors.toMap(Consumer::getName, item -> item, (k1, k2) -> k1));
        Map<String, ConsumerInfo> consumerTpsMap = tpses.stream().collect(Collectors.toMap(ConsumerInfo::getConsumerGroup, item -> item, (k1, k2) -> k2));

        List<ConsumerSummary> summaries = latencies.stream()
                .filter(item -> StringUtils.isNotBlank(item.getConsumerGroup()))
                .map(item -> {
                    ConsumerSummaryExt consumerSummary = new ConsumerSummaryExt(item, consumerTpsMap.get(item.getConsumerGroup()));
                    if (consumerNameMap.containsKey(item.getConsumerGroup())) {
                        Consumer consumer = consumerNameMap.get(item.getConsumerGroup());
                        consumerSummary.setOwner(consumer.getApplicant());
                        consumerSummary.setDelayThreadhold(consumer.getDelayThreadhold());
                        if (consumer.getStatus() > 0) {
                            consumerSummary.setConsumerId(consumer.getId());
                        }
                    }
                    return consumerSummary;
                }).collect(Collectors.toList());
        return new PageResult<>(pageNo, pageSize, total, summaries);
    }


}

