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

package com.zto.zms.portal.service.serve;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.zto.zms.common.ZmsServiceTypeEnum;
import com.zto.zms.utils.Assert;
import com.zto.zms.common.ServiceProcess;
import com.zto.zms.dal.domain.service.ZmsClusterServiceDTO;
import com.zto.zms.dal.mapper.*;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.dal.model.ServiceInstance;
import com.zto.zms.dal.model.ServiceProperty;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.common.InstanceStatusEnum;
import com.zto.zms.portal.common.ServicePropertiesNameEnum;
import com.zto.zms.portal.listener.impl.AdaptServiceEventListenerImpl;
import com.zto.zms.portal.manage.impl.ClusterManagerImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务、集群
 * Created by sun kai on 2020/1/8
 */
@Service
public class ServeService {

    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ZmsHostMapper zmsHostMapper;
    @Autowired
    private ServicePropertyMapper servicePropertyMapper;
    @Autowired
    private ServiceProcessMapper serviceProcessMapper;
    @Autowired
    private ProcessPropertyValueRefMapper processPropertyValueRefMapper;
    @Autowired
    private ProcessPropertyValueRefMapper processPropValueRefMapper;
    @Autowired
    private AdaptServiceEventListenerImpl adaptServiceEventListener;
    @Autowired
    private ClusterManagerImpl clusterManagerImpl;

    /**
     * 集群下拉框查询
     */
    public List<ZmsClusterServiceDTO> listClusters(Integer envId, String envName, String serviceType, String serviceName) {
        return zmsServiceMapper.queryClusters(envId, envName, null, serviceType, serviceName);
    }

    /**
     * 集群迁移-列表查询
     */
    public List<ZmsClusterServiceDTO> queryClusters(Integer envId, String keyWord) {
        keyWord = StringUtils.isEmpty(keyWord) ? null : keyWord.trim();
        List<ZmsClusterServiceDTO> clusterServiceDtoList = zmsServiceMapper.queryClustersByKeyWord(envId, keyWord);
        if (CollectionUtils.isEmpty(clusterServiceDtoList)) {
            return clusterServiceDtoList;
        }
        // 集群ID
        List<Integer> clusterIds = clusterServiceDtoList.stream().map(ZmsClusterServiceDTO::getId).collect(Collectors.toList());
        // 查询所有集群服务实例
        List<ServiceInstance> serviceInstances = serviceInstanceMapper.listInstanceByServiceIds(clusterIds);
        // 查询所有集群启动进程
        List<ServiceProcess> serviceProcessList = serviceProcessMapper.listLastSuccessProcessByServiceIds(clusterIds);
        // 查询集群的所有配置参数(ROCKETMQ、KAFKA)
        List<ServiceProperty> servicePropertyList = servicePropertyMapper.getClusterProperty();
        // 查询集群启动地址、服务地址、版本号
        getAddressAndVersion(clusterServiceDtoList, serviceInstances, serviceProcessList, servicePropertyList);
        return clusterServiceDtoList;
    }

    /**
     * 查询集群的启动地址、服务地址、版本号
     */
    private void getAddressAndVersion(List<ZmsClusterServiceDTO> clusterServiceDtoList, List<ServiceInstance> serviceInstances,
									  List<ServiceProcess> serviceProcessList, List<ServiceProperty> servicePropertyList) {
        clusterServiceDtoList.forEach(item -> {
            // 根据服务ID筛选服务实例
            List<ServiceInstance> instances = serviceInstances.stream()
                    .filter(instance -> instance.getServiceId().equals(item.getId())).collect(Collectors.toList());
            List<Integer> instanceIds = instances.stream().map(ServiceInstance::getId).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(instances)) {
                // 根据服务ID筛选实例进程
                List<Integer> processIds = serviceProcessList.stream()
                        .filter(processId -> instanceIds.contains(processId.getInstanceId()))
                        .map(ServiceProcess::getServiceProcessId).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(processIds)) {
                    clusterManagerImpl.getAddressAndVersion(item, instances, processIds, servicePropertyList);
                }
            }
        });
    }

    /**
     * 环境下单一服务列表查询
     */
    public List<ZmsServiceEntity> listByEnvIdAndServiceType(Integer envId, String serviceType) {
        return zmsServiceMapper.listByEnvIdAndType(envId, serviceType);
    }

    /**
     * 根据服务ID查询服务和环境信息
     */
    public ZmsClusterServiceDTO getServiceAndEnvById(Integer serviceId) {
        return zmsServiceMapper.getServiceAndEnvById(serviceId);
    }

    /**
     * 服务重命名
     */
    public int renameService(Integer id, String serverName) {
        ZmsServiceEntity serviceEntity = zmsServiceMapper.getById(id);
        Assert.that(null != serviceEntity, "服务不存在");
        ZmsServiceEntity zmsServiceEntity = zmsServiceMapper.getByEnvIdAndName(serviceEntity.getEnvironmentId(), serverName);
        Assert.that(null == zmsServiceEntity || id.equals(zmsServiceEntity.getId()), "服务名称已经存在");
        return zmsServiceMapper.rename(id, serverName);
    }

    /**
     * 服务名称校验
     */
    public boolean validServerName(Integer envId, String serverName) {
        return null == zmsServiceMapper.getByEnvIdAndName(envId, serverName);
    }

    /**
     * zookeeper集群地址
     */
    public List<String> listZookeeperAddr(Integer zkServiceId) {
        List<ServiceInstance> serviceInstance = serviceInstanceMapper.listStartInstanceByServiceId(zkServiceId);
        List<String> zookeepers = Lists.newArrayList();
        //服务配置属性
        List<ProcessPropertyValueRef> propertyValueRefs = getServicePropertyValueRefs(zkServiceId, ServicePropertiesNameEnum.ZK_CLIENT_PORT);
        Map<Integer, ProcessPropertyValueRef> propertyValueRefMap = propertyValueRefs.stream().collect(
                Collectors.toMap(ProcessPropertyValueRef::getInstanceId, s -> s, (k1, k2) -> k1));

        for (ServiceInstance instance : serviceInstance) {
            //获取host
            String hostIp = zmsHostMapper.getEnableIPById(instance.getHostId());
            //获取clientPort
            String clientPort = propertyValueRefMap.get(instance.getId()).getCurrentValue();
            zookeepers.add(hostIp + ":" + clientPort);
        }
        return zookeepers;
    }

    public List<ProcessPropertyValueRef> getServicePropertyValueRefs(Integer serviceId, ServicePropertiesNameEnum propertiesNameEnum) {
        ZmsServiceEntity serviceEntity = zmsServiceMapper.getById(serviceId);
        Integer servicePropertyId = servicePropertyMapper
                .getIdByServiceTypeAndName(serviceEntity.getServerType(), propertiesNameEnum.getInstanceType(), propertiesNameEnum.getGroup(), propertiesNameEnum.getName());
        //最近一发布成功进程
        List<Integer> processIds = serviceProcessMapper.listLastSuccessProcess(serviceId);
        Assert.that(!CollectionUtils.isEmpty(processIds), "The service did not publish successful process information");

        return processPropValueRefMapper.listByProcessId(processIds, servicePropertyId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer deleteService(Integer id) {
        ZmsServiceEntity service = zmsServiceMapper.getById(id);
        List<ServiceInstance> instances = serviceInstanceMapper.listAllByServiceId(id);
        if (!CollectionUtils.isEmpty(instances)) {
            boolean match = instances.stream().anyMatch(item -> !InstanceStatusEnum.STOP.name().equals(item.getInstanceStatus()));
            if (match) {
                throw new IllegalArgumentException("请先停止服务节点");
            }
        }
        if (ZmsServiceTypeEnum.ZOOKEEPER.name().equals(service.getServerType()) || ZmsServiceTypeEnum.INFLUXDB.name().equals(service.getServerType())) {
            // 判断服务是否被依赖
            List<Integer> ids = servicePropertyMapper.getIdByServiceType(service.getServerType());
            List<String> serviceNames = processPropertyValueRefMapper.listRunningService(ids, service.getId());
            if (!CollectionUtils.isEmpty(serviceNames)) {
                throw new IllegalArgumentException("存在依赖服务：" + Joiner.on(",").join(serviceNames) + "无法删除");
            }
        }
        if (!CollectionUtils.isEmpty(instances)) {
            // 逻辑删除服务实例
            serviceInstanceMapper.deleteByIds(instances.stream().map(ServiceInstance::getId).collect(Collectors.toList()));
        }
        // 逻辑删除服务
        zmsServiceMapper.deleteById(id);
        //服务删除监听
        adaptServiceEventListener.remove(service);
        return id;
    }

}

