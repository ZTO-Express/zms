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

package com.zto.zms.portal.manage.impl;

import com.google.common.collect.Lists;
import com.zto.zms.common.ZmsServiceTypeEnum;
import com.zto.zms.dal.domain.service.ZmsClusterServiceDTO;
import com.zto.zms.dal.mapper.ProcessPropertyValueRefMapper;
import com.zto.zms.dal.mapper.ZmsHostMapper;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.dal.model.ServiceInstance;
import com.zto.zms.dal.model.ServiceProperty;
import com.zto.zms.portal.common.ServicePropertiesNameEnum;
import com.zto.zms.portal.manage.ClusterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Kafka集群操作类
 *
 * @author sunkai
 * @date 2020/5/18
 **/
@Component("cluster-kafka")
public class KafkaClusterManager implements ClusterManager {

    @Autowired
    private ProcessPropertyValueRefMapper processPropertyValueRefMapper;
    @Autowired
    private ZmsHostMapper zmsHostMapper;

    /**
     * 查询KAFKA集群的启动地址、服务地址、版本号
     */
    @Override
    public void getAddressAndVersion(ZmsClusterServiceDTO item, List<ServiceInstance> instances, List<Integer> processIds, List<ServiceProperty> servicePropertyList) {
        Integer kafkaBrokerPortPropertyId = getServicePropertyId(servicePropertyList, ZmsServiceTypeEnum.KAFKA.name(), ServicePropertiesNameEnum.KAFKA_PORT);
        Integer kafkaZkServicePropertyId = getServicePropertyId(servicePropertyList, ZmsServiceTypeEnum.KAFKA.name(), ServicePropertiesNameEnum.ZK_SERVICE);
        Integer kafkaVersionPropertyId = getServicePropertyId(servicePropertyList, ZmsServiceTypeEnum.KAFKA.name(), ServicePropertiesNameEnum.CLUSTER_VERSION);
        // 根据进程ID和配置属性ID查询实例配置参数
        List<ProcessPropertyValueRef> refList = processPropertyValueRefMapper.listByProcessIdAndPropertyId(processIds,
                Lists.newArrayList(kafkaBrokerPortPropertyId, kafkaZkServicePropertyId, kafkaVersionPropertyId));
        // 查询集群的所有kafka端口号
        Map<Integer, String> kafkaPortPropertyValueRef = refList.stream()
                .filter(ref -> ref.getPropertyId().equals(kafkaBrokerPortPropertyId))
                .collect(Collectors.toMap(ProcessPropertyValueRef::getInstanceId, ProcessPropertyValueRef::getCurrentValue));
        // 集群的zookeeper服务地址
        String zookeeperAddress = refList.stream()
                .filter(ref -> ref.getPropertyId().equals(kafkaZkServicePropertyId))
                .findFirst().orElse(new ProcessPropertyValueRef()).getRealValue();
        // 查询版本号，版本号是服务的属性，不是服务实例的属性
        Map<Integer, String> versionPropertyValueRef = refList.stream()
                .filter(ref -> ref.getPropertyId().equals(kafkaVersionPropertyId))
                .collect(Collectors.toMap(ProcessPropertyValueRef::getInstanceId, ProcessPropertyValueRef::getCurrentValue));
        // 拼接集群的启动地址
        item.setStartAddress(joinIpAndPort(instances, kafkaPortPropertyValueRef, ","));
        item.setServiceAddress(zookeeperAddress);
        item.setVersion(versionPropertyValueRef.values().stream().findFirst().orElse(""));
    }

    /**
     * 拼接集群的启动地址和服务地址
     */
    private String joinIpAndPort(List<ServiceInstance> instances, Map<Integer, String> propertyValueRef, String split) {
        StringBuilder address = new StringBuilder();
        instances.stream()
                .filter(instance -> propertyValueRef.containsKey(instance.getId()))
                .forEach(instance -> {
                    // 查询服务实例对应的主机
                    String hostIp = zmsHostMapper.getEnableIPById(instance.getHostId());
                    address.append(split).append(hostIp).append(":").append(propertyValueRef.get(instance.getId()));
                });
        return address.length() > 0 ? address.deleteCharAt(0).toString() : "";
    }

    /**
     * 筛选配置属性ID
     */
    private Integer getServicePropertyId(List<ServiceProperty> servicePropertyList, String serviceType, ServicePropertiesNameEnum propertiesNameEnum) {
        return servicePropertyList.stream()
                .filter(item -> serviceType.equals(item.getServiceType())
                        && propertiesNameEnum.getInstanceType().equals(item.getInstanceType())
                        && propertiesNameEnum.getGroup().equals(item.getPropertyGroup())
                        && propertiesNameEnum.getName().equals(item.getPropertyName()))
                .findFirst().orElse(new ServiceProperty()).getId();
    }

}

