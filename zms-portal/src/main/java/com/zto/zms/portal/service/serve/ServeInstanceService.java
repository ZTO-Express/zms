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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zto.zms.common.ZmsServiceTypeEnum;
import com.zto.zms.utils.Assert;
import com.zto.zms.dal.domain.service.HostServiceInstanceDTO;
import com.zto.zms.common.ServiceProcess;
import com.zto.zms.dal.domain.service.ServicePropertyValueRefDTO;
import com.zto.zms.dal.domain.service.ZmsClusterServiceDTO;
import com.zto.zms.dal.mapper.*;
import com.zto.zms.dal.model.*;
import com.zto.zms.portal.assemble.impl.AdaptAddInstanceConsumeConfigAssembleImpl;
import com.zto.zms.portal.assemble.impl.AdaptConfigFileAssembleImpl;
import com.zto.zms.portal.common.*;
import com.zto.zms.portal.dto.AddInstanceAssembleDTO;
import com.zto.zms.portal.dto.serve.*;
import com.zto.zms.portal.listener.impl.AdaptServiceInstanceListener;
import com.zto.zms.portal.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 服务实例
 * Created by sun kai on 2020/1/8
 */
@Service
public class ServeInstanceService {

    private final String process_null_template = "'%s' did not publish successful process information";

    public static final Logger logger = LoggerFactory.getLogger(ServeInstanceService.class);

    @Autowired
    private ProcessService processService;
    @Autowired
    private ZmsHostMapper zmsHostMapper;
    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ServicePropertyMapper servicePropertyMapper;
    @Autowired
    private ServicePropertyValueMapper servicePropertyValueMapper;
    @Autowired
    private ServicePropertyValueRefMapper servicePropertyValueRefMapper;
    @Autowired
    private ServiceProcessMapper serviceProcessMapper;
    @Autowired
    private ProcessPropertyValueRefMapper processPropertyValueRefMapper;
    @Autowired
    private ConfigApiMapper configApiMapper;
    @Autowired
    private AdaptAddInstanceConsumeConfigAssembleImpl addInstanceConsumeConfigAssemble;
    @Autowired
    private AdaptConfigFileAssembleImpl adaptConfigFileAssemble;
    @Autowired
    private AdaptServiceInstanceListener adaptServiceInstanceListener;
    @Autowired
    private ZmsEnvironmentMapper zmsEnvironmentMapper;

    public List<HostServiceInstanceDTO> queryHostServiceInstance(Integer serviceId) {
        List<HostServiceInstanceDTO> HostServiceInstanceDTOList = serviceInstanceMapper.queryHostServiceInstance(serviceId);
        if (CollectionUtils.isEmpty(HostServiceInstanceDTOList)) {
            return Lists.newArrayList();
        }
        List<ServiceInstance> startInstances = HostServiceInstanceDTOList.stream()
                .filter(item -> InstanceStatusEnum.START.name().equals(item.getInstanceStatus()))
                .map(item -> {
                    ServiceInstance instance = new ServiceInstance();
                    instance.setId(item.getId());
                    instance.setInstanceType(item.getInstanceType());
                    return instance;
                }).collect(Collectors.toList());
        Map<Integer, List<ProcessPropertyValueRef>> currentPropertyValueRefMap = null;
        if (!CollectionUtils.isEmpty(startInstances)) {
            // 预生成当前配置对应的发布配置
            currentPropertyValueRefMap = getProcessPropertyValueRefList(serviceId, startInstances, false)
                    .stream().collect(Collectors.groupingBy(ProcessPropertyValueRef::getInstanceId));
        }
        return compareServiceInstanceConfig(HostServiceInstanceDTOList, currentPropertyValueRefMap);
    }

    /**
     * 比较服务实例当前配置和发布的配置，判断是否需要重启
     */
    private List<HostServiceInstanceDTO> compareServiceInstanceConfig(List<HostServiceInstanceDTO> HostServiceInstanceDTOList,
                                                                      Map<Integer, List<ProcessPropertyValueRef>> currentAllPropertyValueRefMap) {
        return HostServiceInstanceDTOList.stream().map(item -> {
            HostServiceInstanceDTO HostServiceInstanceDTO = new HostServiceInstanceDTO();
            BeanUtils.copyProperties(item, HostServiceInstanceDTO);
            if (InstanceStatusEnum.START.name().equals(item.getInstanceStatus())) {
                // 当前服务实例对应的启动进程
                com.zto.zms.dal.model.ServiceProcess lastRunningProcess = serviceProcessMapper.listRunningProcessByInstance(item.getId());
                Assert.that(null != lastRunningProcess, String.format(process_null_template, item.getInstanceName()));
                Integer processId = lastRunningProcess.getId();

                // 当前配置，包含服务配置和服务实例配置
                Map<String, String> currentPropertyValueRefMap = sortPropertyValueRefs(currentAllPropertyValueRefMap.get(item.getId()));
                // 服务检测时间
                ServiceProcess serviceProcess = serviceProcessMapper.getByProcessId(processId);
                HostServiceInstanceDTO.setLastMonitorTime(serviceProcess == null ? null : serviceProcess.getLastMonitorTime());
                // 发布配置，包含服务配置和服务实例配置
                List<ProcessPropertyValueRef> processPropertyValueRefs = processPropertyValueRefMapper.listByServiceProcessId(processId);
                Map<String, String> processPropertyValueRefMap = sortPropertyValueRefs(processPropertyValueRefs);
                boolean match = processPropertyValueRefMap.entrySet().stream().anyMatch(entry -> filterProcessPropertyValueRefMap(entry, currentPropertyValueRefMap));
                if (match || !currentPropertyValueRefMap.isEmpty()) {
                    HostServiceInstanceDTO.setPublishDesc("配置已更改，请重启");
                }
            }
            return HostServiceInstanceDTO;
        }).collect(Collectors.toList());
    }

    /**
     * 校验是否需要重启
     */
    private boolean filterProcessPropertyValueRefMap(Map.Entry<String, String> entry, Map<String, String> currentPropertyValueRefMap) {
        if (currentPropertyValueRefMap.containsKey(entry.getKey())) {
            String currentValue = currentPropertyValueRefMap.get(entry.getKey());
            currentPropertyValueRefMap.remove(entry.getKey());
            return !currentValue.equals(entry.getValue());
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer deleteServiceInstance(List<Integer> ids) {
        List<ServiceInstance> instances = serviceInstanceMapper.listByInstanceIds(ids);
        if (!CollectionUtils.isEmpty(instances)) {
            boolean match = instances.stream().anyMatch(item -> !InstanceStatusEnum.STOP.name().equals(item.getInstanceStatus()));
            if (match) {
                throw new IllegalArgumentException("请先停止服务节点");
            }
            // 逻辑删除服务实例
            ids = instances.stream().map(ServiceInstance::getId).collect(Collectors.toList());
            serviceInstanceMapper.deleteByIds(ids);
            // 集群类型ZK节点变更，先变更，否则查不到已删除的实例
            instances.forEach(item -> adaptServiceInstanceListener.remove(item));
        }
        return ids.size();
    }

    /**
     * 根据实例ID查询实例服务环境信息
     */
    public EnvServiceInstanceDTO getServiceAndEnvByInstanceId(Integer instanceId) {
        HostServiceInstanceDTO instance = serviceInstanceMapper.getHostByInstanceId(instanceId);
        EnvServiceInstanceDTO instanceDTO = new EnvServiceInstanceDTO();
        BeanUtils.copyProperties(instance, instanceDTO);
        ZmsClusterServiceDTO serviceDTO = zmsServiceMapper.getServiceAndEnvById(instanceDTO.getServiceId());
        instanceDTO.setEnvironmentId(serviceDTO.getEnvironmentId());
        instanceDTO.setEnvironmentName(serviceDTO.getEnvironmentName());
        instanceDTO.setServerName(serviceDTO.getServerName());
        instanceDTO.setServerType(serviceDTO.getServerType());
        instanceDTO.setServerStatus(serviceDTO.getServerStatus());
        return instanceDTO;
    }

    /**
     * 查询服务、服务实例的配置项
     */
    public List<ServicePropertyInstanceQueryDTO> getServiceInstanceConfig(Integer serviceId, Integer instanceId) {
        if (null != serviceId) {
            // 服务配置
            ZmsClusterServiceDTO serviceDTO = zmsServiceMapper.getServiceAndEnvById(serviceId);
            List<ServiceInstance> instanceList = serviceInstanceMapper.listAllByServiceId(serviceId);
            instanceList.sort(Comparator.comparing(ServiceInstance::getInstanceName));
            List<String> scopes = Lists.newArrayList(ServicePropertyScopeEnum.SERVICE.name(), ServicePropertyScopeEnum.SERVICE_CONFIG_GROUP.name());
            return getServicePropertyConfigForService(serviceDTO, instanceList, scopes);
        } else if (null != instanceId) {
            // 服务实例配置
            EnvServiceInstanceDTO serviceInstanceDTO = getServiceAndEnvByInstanceId(instanceId);
            return getServicePropertyConfigForInstance(serviceInstanceDTO);
        }
        return Lists.newArrayList();
    }

    /**
     * 查询服务的配置，并查询当前配置的值
     */
    private List<ServicePropertyInstanceQueryDTO> getServicePropertyConfigForService(ZmsClusterServiceDTO serviceDTO, List<ServiceInstance> instanceList, List<String> scopes) {
        // 查询配置属性
        List<ServicePropertyQueryDTO> propertyQueryDTOList = getServiceInstanceConfig(serviceDTO.getEnvironmentId(), serviceDTO.getServerType(), null, scopes);
        // 查询配置值
        List<ServicePropertyValueRefDTO> propertyValueRefDTOList = servicePropertyValueRefMapper.listAllByServiceId(serviceDTO.getId());

        List<ServicePropertyQueryDTO> propertyList = Lists.newArrayList();
        propertyQueryDTOList.forEach(item -> {
            if (InstanceTypeEnum.SERVICE.name().equals(item.getInstanceType())) {
                List<ServicePropertyValueRefDTO> propertyValueList = propertyValueRefDTOList.stream()
                        .filter(prop -> prop.getPropertyId().equals(item.getId()))
                        .collect(Collectors.toList());
                propertyList.add(getServicePropertyQueryDTO(item, propertyValueList));
            } else {
                instanceList.stream().filter(instance -> instance.getInstanceType().equals(item.getInstanceType()))
                        .forEach(instance -> {
                            List<ServicePropertyValueRefDTO> propertyValueList = propertyValueRefDTOList.stream()
                                    .filter(prop -> prop.getPropertyId().equals(item.getId()) && prop.getInstanceId().equals(instance.getId()))
                                    .collect(Collectors.toList());
                            ServicePropertyQueryDTO propertyQueryDTO = getServicePropertyQueryDTO(item, propertyValueList);
                            propertyQueryDTO.setInstanceId(instance.getId());
                            propertyList.add(propertyQueryDTO);
                        });
            }
        });

        List<ServicePropertyInstanceQueryDTO> propertyDTOList = Lists.newArrayList();
        // 服务配置值
        ServicePropertyInstanceQueryDTO servicePropertyQueryDTO = new ServicePropertyInstanceQueryDTO();
        servicePropertyQueryDTO.setPropertyList(propertyList.stream()
                .filter(item -> InstanceTypeEnum.SERVICE.name().equals(item.getInstanceType())).collect(Collectors.toList()));
        propertyDTOList.add(servicePropertyQueryDTO);

        // 实例的服务属性配置值
        Map<Integer, List<ServicePropertyQueryDTO>> instancePropertyMap = propertyList.stream()
                .filter(item -> !InstanceTypeEnum.SERVICE.name().equals(item.getInstanceType()))
                .collect(Collectors.groupingBy(ServicePropertyQueryDTO::getInstanceId));
        instanceList.stream().filter(item -> instancePropertyMap.containsKey(item.getId())).forEach(item -> {
            ServicePropertyInstanceQueryDTO instancePropertyQueryDTO = new ServicePropertyInstanceQueryDTO();
            instancePropertyQueryDTO.setInstanceId(item.getId());
            instancePropertyQueryDTO.setInstanceName(item.getInstanceName());
            instancePropertyQueryDTO.setPropertyList(instancePropertyMap.get(item.getId()));
            propertyDTOList.add(instancePropertyQueryDTO);
        });

        return propertyDTOList;
    }

    /**
     * 查询服务实例的配置，并查询当前配置的值
     */
    private List<ServicePropertyInstanceQueryDTO> getServicePropertyConfigForInstance(EnvServiceInstanceDTO serviceInstanceDTO) {
        // 查询实例配置属性
        List<ServicePropertyQueryDTO> propertyQueryDTOList = getServiceInstanceConfig(serviceInstanceDTO.getEnvironmentId(),
                serviceInstanceDTO.getServerType(), serviceInstanceDTO.getInstanceType(), null);
        // 查询配置值
        List<ServicePropertyValueRefDTO> propertyValueRefDTOList = servicePropertyValueRefMapper.queryByInstanceId(serviceInstanceDTO.getId());

        List<ServicePropertyQueryDTO> propertyList = Lists.newArrayList();
        propertyQueryDTOList.forEach(item -> {
            List<ServicePropertyValueRefDTO> propertyValueList = propertyValueRefDTOList.stream()
                    .filter(prop -> prop.getPropertyId().equals(item.getId()))
                    .collect(Collectors.toList());
            ServicePropertyQueryDTO propertyQueryDTO = getServicePropertyQueryDTO(item, propertyValueList);
            propertyList.add(propertyQueryDTO);
        });

        ServicePropertyInstanceQueryDTO propertyDTO = new ServicePropertyInstanceQueryDTO();
        propertyDTO.setInstanceId(serviceInstanceDTO.getId());
        propertyDTO.setInstanceName(serviceInstanceDTO.getInstanceName());
        propertyDTO.setPropertyList(propertyList);
        return Lists.newArrayList(propertyDTO);
    }

    private ServicePropertyQueryDTO getServicePropertyQueryDTO(ServicePropertyQueryDTO item, List<ServicePropertyValueRefDTO> propertyValueList) {
        ServicePropertyQueryDTO propertyQueryDTO = new ServicePropertyQueryDTO();
        BeanUtils.copyProperties(item, propertyQueryDTO);
        if (!CollectionUtils.isEmpty(propertyValueList)) {
            propertyQueryDTO.setPropertyValueRefId(propertyValueList.stream().map(i -> i.getId().toString()).collect(Collectors.joining(",")));
            propertyQueryDTO.setCurrentValue(propertyValueList.stream().map(ServicePropertyValueRefDTO::getCurrentValue).collect(Collectors.joining(",")));
        }
        return propertyQueryDTO;
    }

    /**
     * 添加服务、添加服务实例时查询配置项
     */
    public List<ServicePropertyQueryDTO> getAddServiceInstanceConfig(Integer envId, String serviceType, List<String> instanceTypes) {
        List<String> scopes;
        if (CollectionUtils.isEmpty(instanceTypes)) {
            // 添加服务查询
            scopes = Lists.newArrayList(ServicePropertyScopeEnum.SERVICE_CONFIG_GROUP.name());
        } else {
            // 添加服务实例查询
            scopes = Lists.newArrayList(ServicePropertyScopeEnum.SERVICE_CONFIG_GROUP.name(),
                    ServicePropertyScopeEnum.INSTANCE_CONFIG_GROUP.name());
        }
        return getServiceProperty(envId, serviceType, instanceTypes, scopes);
    }

    /**
     * 查询服务、服务实例的配置项
     */
    public List<ServicePropertyQueryDTO> getServiceInstanceConfig(Integer envId, String serviceType,
                                                                  String instanceType, List<String> scopes) {
        return getServiceProperty(envId, serviceType,
                StringUtils.isEmpty(instanceType) ? null : Lists.newArrayList(instanceType), scopes);
    }

    /**
     * 查询服务、服务实例的配置项
     */
    public List<ServicePropertyQueryDTO> getServiceProperty(Integer envId, String serviceType,
                                                            List<String> instanceTypes, List<String> scopes) {
        return servicePropertyMapper.getServiceProperty(ZmsServiceTypeEnum.getServiceType(serviceType), instanceTypes, scopes).stream()
                .map(item -> getServicePropertyQueryDTO(envId, item)).collect(Collectors.toList());
    }

    /**
     * 查询服务或服务实例的配置项
     */
    private ServicePropertyQueryDTO getServicePropertyQueryDTO(Integer envId, ServiceProperty item) {
        ServicePropertyQueryDTO servicePropertyQueryDTO = new ServicePropertyQueryDTO();
        BeanUtils.copyProperties(item, servicePropertyQueryDTO);
        if (item.getIsDependencies() == 1) {
            if (ServicePropertiesNameEnum.ENVID_SERVICE.getName().equals(item.getPropertyName())) {
                servicePropertyQueryDTO.setPropertyValueList(zmsEnvironmentMapper.listEnableEnv().stream()
                        .filter(environment -> !environment.getId().equals(envId))
                        .map(i -> {
                            ServicePropertyValue propertyValue = new ServicePropertyValue();
                            propertyValue.setId(i.getId());
                            propertyValue.setPropertyValue(i.getId().toString());
                            propertyValue.setDisplayValue(i.getEnvironmentName());
                            return propertyValue;
                        }).collect(Collectors.toList()));
                return servicePropertyQueryDTO;
            }
            // 查询依赖服务选项，转为配置选项前端展示item
            servicePropertyQueryDTO.setPropertyValueList(zmsServiceMapper.findByEnvIdAndType(envId, item.getPropertyName()).stream()
                    .map(i -> {
                        ServicePropertyValue propertyValue = new ServicePropertyValue();
                        propertyValue.setId(i.getId());
                        propertyValue.setPropertyValue(i.getId().toString());
                        propertyValue.setDisplayValue(i.getServerName());
                        return propertyValue;
                    }).collect(Collectors.toList()));
        } else if (!ServicePropertyChooseTypeEnum.TEXT.name().equals(item.getChooseType())) {
            if ("BOOLEAN".equals(item.getValueType())) {
                servicePropertyQueryDTO.setPropertyValueList(getBooleanPropertyValueList(item));
            } else {
                // 查询配置选项
                servicePropertyQueryDTO.setPropertyValueList(servicePropertyValueMapper.getByPropertyId(item.getId()));
            }
        }
        return servicePropertyQueryDTO;
    }

    /**
     * 封装BOOLEAN类型的属性配置值
     */
    private List<ServicePropertyValue> getBooleanPropertyValueList(ServiceProperty item) {
        ServicePropertyValue propertyValue = new ServicePropertyValue();
        propertyValue.setPropertyId(item.getId());
        propertyValue.setPropertyValue("true");
        propertyValue.setDisplayValue("是");
        ServicePropertyValue propertyValue2 = new ServicePropertyValue();
        propertyValue2.setPropertyId(item.getId());
        propertyValue2.setPropertyValue("false");
        propertyValue2.setDisplayValue("否");
        List<ServicePropertyValue> propertyValueList = Lists.newArrayList(propertyValue, propertyValue2);
        if (item.getIsRequired() == 0) {
            ServicePropertyValue propertyValue3 = new ServicePropertyValue();
            propertyValue3.setPropertyId(item.getId());
            propertyValue3.setPropertyValue("");
            propertyValue3.setDisplayValue("空");
            propertyValueList.add(propertyValue3);
        }
        return propertyValueList;
    }

    /**
     * 服务配置修改、实例配置修改
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveServiceInstanceConfig(ServicePropertySaveDTO configDTO, String realName) {
        Integer serviceId = configDTO.getServiceId();
        List<ServicePropertyValueRef> insertRefList = Lists.newArrayList();
        List<Integer> deleteRefList = Lists.newArrayList();
        configDTO.getPropertyQueryList().forEach(prop -> {
            Integer instanceId = prop.getInstanceId();
            prop.getPropertyList().forEach(item -> {
                if (!StringUtils.isEmpty(item.getCurrentValue())) {
                    Iterator<String> iteratorValue;
                    // 复选框，数据库存多条，其他类型存一条
                    if (item.getIsDependencies() == 0 && !ServicePropertyChooseTypeEnum.TEXT.name().equals(item.getChooseType())) {
                        iteratorValue = Arrays.asList(item.getCurrentValue().split(",")).iterator();
                    } else {
                        iteratorValue = Lists.newArrayList(item.getCurrentValue()).iterator();
                    }
                    if (StringUtils.isEmpty(item.getPropertyValueRefId())) {
                        // 新增配置值
                        iteratorValue.forEachRemaining(currentValue -> insertRefList.add(getInsertServicePropertyValueRef(serviceId, instanceId, item, currentValue, realName)));
                    } else {
                        // 对比数量
                        Iterator<String> iteratorRefId = Arrays.asList(item.getPropertyValueRefId().split(",")).iterator();
                        while (iteratorRefId.hasNext()) {
                            if (!iteratorValue.hasNext()) {
                                break;
                            }
                            // 修改配置值
                            updateServicePropertyValueRef(item, Integer.parseInt(iteratorRefId.next()), iteratorValue.next(), realName);
                        }
                        while (iteratorRefId.hasNext()) {
                            // 删除配置值
                            deleteRefList.add(Integer.parseInt(iteratorRefId.next()));
                        }
                        while (iteratorValue.hasNext()) {
                            // 新增配置值
                            insertRefList.add(getInsertServicePropertyValueRef(serviceId, instanceId, item, iteratorValue.next(), realName));
                        }
                    }
                } else if (!StringUtils.isEmpty(item.getPropertyValueRefId())) {
                    // 删除配置值
                    Arrays.stream(item.getPropertyValueRefId().split(",")).forEach(refId -> deleteRefList.add(Integer.parseInt(refId)));
                }
            });
        });
        if (!CollectionUtils.isEmpty(insertRefList)) {
            servicePropertyValueRefMapper.insertList(insertRefList);
        }
        if (!CollectionUtils.isEmpty(deleteRefList)) {
            servicePropertyValueRefMapper.deleteByIds(deleteRefList);
        }
    }

    /**
     * 新增配置值封装参数
     */
    private ServicePropertyValueRef getInsertServicePropertyValueRef(Integer serviceId, Integer instanceId,
                                                                     ServicePropertyQueryDTO item,
                                                                     String currentValue, String realName) {
        ServicePropertyValueRef propertyValueRef = new ServicePropertyValueRef();
        propertyValueRef.setServiceId(serviceId);
        propertyValueRef.setInstanceId(instanceId);
        propertyValueRef.setPropertyId(item.getId());
        propertyValueRef.setCurrentValue(currentValue);
        propertyValueRef.setCreator(realName);
        propertyValueRef.setModifier(realName);
        if (item.getIsDependencies() == 0 && !ServicePropertyChooseTypeEnum.TEXT.name().equals(item.getChooseType())) {
            // 匹配配置选项
            ServicePropertyValue servicePropertyValue = item.getPropertyValueList().stream()
                    .filter(value -> value.getPropertyValue().equals(currentValue)).findFirst().orElse(new ServicePropertyValue());
            propertyValueRef.setPropertyValueId(servicePropertyValue.getId());
        }
        return propertyValueRef;
    }

    /**
     * 修改配置值
     */
    private void updateServicePropertyValueRef(ServicePropertyQueryDTO item, Integer refId, String currentValue, String realName) {
        ServicePropertyValueRef propertyValueRef = new ServicePropertyValueRef();
        propertyValueRef.setId(refId);
        propertyValueRef.setCurrentValue(currentValue);
        propertyValueRef.setModifier(realName);
        if (item.getIsDependencies() == 0 && !ServicePropertyChooseTypeEnum.TEXT.name().equals(item.getChooseType())) {
            // 匹配配置选项
            ServicePropertyValue servicePropertyValue = item.getPropertyValueList().stream()
                    .filter(value -> value.getPropertyValue().equals(currentValue)).findFirst().orElse(new ServicePropertyValue());
            propertyValueRef.setPropertyValueId(servicePropertyValue.getId());
        }
        servicePropertyValueRefMapper.updateById(propertyValueRef);
    }

    /**
     * 添加服务
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Integer> addService(ServiceInstanceAddDTO serviceAddDTO, String realName) {
        // 1.zms_service表数据新增
        ZmsServiceEntity service = serviceAddDTO.getService();
        service.setServerType(ZmsServiceTypeEnum.getServiceType(service.getServerType()));
        service.setServerStatus(ZmsServiceStatusEnum.ENABLE.name());
        service.setCreator(realName);
        service.setModifier(realName);
        // 2.service_instance表数据新增
        zmsServiceMapper.insert(serviceAddDTO.getService());
        List<ServiceInstance> instances = getInstanceList(serviceAddDTO.getInstanceList(), service, realName);
        serviceInstanceMapper.insertList(instances);

        // 3.service_property_value_ref表数据新增
        List<ServicePropertyValueRef> servicePropertyValueRefs = getPropertyValueRefListForService(serviceAddDTO, service, instances, realName);
        List<Integer> instanceIds = instances.stream().map(ServiceInstance::getId).collect(Collectors.toList());
        List<AddInstanceAssembleDTO> consumeInstanceConfigs = addInstanceConsumeConfigAssemble.assembleAddInstance(service.getId(), instanceIds, servicePropertyValueRefs);
        updatesInstanceName(consumeInstanceConfigs);
        servicePropertyValueRefs = addConsumeInstanceConfig(consumeInstanceConfigs, servicePropertyValueRefs);
        servicePropertyValueRefMapper.insertList(servicePropertyValueRefs);
        return instanceIds;
    }

    private List<ServicePropertyValueRef> addConsumeInstanceConfig(List<AddInstanceAssembleDTO> consumeInstanceConfigs, List<ServicePropertyValueRef> servicePropertyValueRefs) {
        List<ServicePropertyValueRef> consumePropertyValueRefs = consumeInstanceConfigs.stream()
                .filter(item -> !StringUtils.isEmpty(item.getCurrentValue())).map(item -> {
                    ServicePropertyValueRef servicePropertyValueRef = new ServicePropertyValueRef();
                    BeanUtils.copyProperties(item, servicePropertyValueRef);
                    return servicePropertyValueRef;
                }).collect(Collectors.toList());
        List<String> customConfig = consumePropertyValueRefs.stream().map(item -> item.getPropertyId() + "|" + item.getInstanceId()).collect(Collectors.toList());
        List<ServicePropertyValueRef> servicePropertyValueRefList = servicePropertyValueRefs.stream()
                .filter(item -> !customConfig.contains(item.getPropertyId() + "|" + item.getInstanceId()))
                .collect(Collectors.toList());
        servicePropertyValueRefList.addAll(consumePropertyValueRefs);
        return servicePropertyValueRefList;
    }

    private void updatesInstanceName(List<AddInstanceAssembleDTO> consumeInstanceConfigs) {
        Map<Integer, String> instanceIdInstanceNameNameMap = consumeInstanceConfigs.stream()
                .collect(Collectors.toMap(AddInstanceAssembleDTO::getInstanceId, AddInstanceAssembleDTO::getInstanceName));
        //更新实例名称
        instanceIdInstanceNameNameMap.forEach((instanceId, instanceName) -> serviceInstanceMapper.updateInstanceNameById(instanceId, instanceName));
    }

    /**
     * 添加服务实例
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Integer> addServiceInstance(ServiceInstanceAddDTO serviceAddDTO, String realName) {
        Integer serviceId = serviceAddDTO.getService().getId();
        // 1.zms_service表数据查询
        ZmsServiceEntity service = zmsServiceMapper.getById(serviceId);
        // 2.service_instance表数据新增
        List<ServiceInstance> instances = getInstanceList(serviceAddDTO.getInstanceList(), service, realName);
        serviceInstanceMapper.insertList(instances);
        // 3.service_property_value_ref表数据新增
        List<ServicePropertyValueRef> servicePropertyValueRefs = getPropertyValueRefListForInstance(serviceAddDTO, service, instances, realName);
        List<Integer> instanceIds = instances.stream().map(ServiceInstance::getId).collect(Collectors.toList());
        //服务实例自定义初始化配置
        List<AddInstanceAssembleDTO> consumeInstanceConfigs = addInstanceConsumeConfigAssemble.assembleAddInstance(serviceId, instanceIds, servicePropertyValueRefs);
        //服务实例名称
        updatesInstanceName(consumeInstanceConfigs);
        //服务实例特殊配置
        servicePropertyValueRefs = addConsumeInstanceConfig(consumeInstanceConfigs, servicePropertyValueRefs);
        servicePropertyValueRefMapper.insertList(servicePropertyValueRefs);
        return instanceIds;
    }

    /**
     * 封装service_instance表新增数据
     */
    private List<ServiceInstance> getInstanceList(List<ServiceInstance> instances, ZmsServiceEntity service, String realName) {
        for (int i = 1; i < instances.size() + 1; i++) {
            ServiceInstance instance = instances.get(i - 1);
            instance.setServiceId(service.getId());
            instance.setInstanceStatus(InstanceStatusEnum.STOP.name());
            instance.setCreator(realName);
            instance.setModifier(realName);
            instance.setInstanceName(getInstanceName(service));
        }
        return instances;
    }

    private String getInstanceName(ZmsServiceEntity service) {
        if (ZmsServiceTypeEnum.ZMS_COLLECTOR.name().equals(service.getServerType())
                || ZmsServiceTypeEnum.ZMS_ALERT.name().equals(service.getServerType())
                || ZmsServiceTypeEnum.INFLUXDB.name().equals(service.getServerType())
                || ZmsServiceTypeEnum.ZMS_BACKUP_CLUSTER.name().equals(service.getServerType())) {
            return service.getServerName();
        }
        // ROCKETMQ、KAFKA、ZOOKEEPER实例名称在Assemble类中单独处理
        return "";
    }

    /**
     * 封装service_property_value_ref表新增数据(添加服务)
     */
    private List<ServicePropertyValueRef> getPropertyValueRefListForService(ServiceInstanceAddDTO serviceAddDTO, ZmsServiceEntity service, List<ServiceInstance> instances, String realName) {
        // service_property_value_ref表新增数据封装
        List<ServicePropertyValueRef> servicePropertyValueRefs = Lists.newArrayList();
        // 页面传入的ServicePropertyScopeEnum.SERVICE_CONFIG_GROUP配置
        Map<Integer, String> propertyListMap = serviceAddDTO.getPropertyList().stream()
                .collect(Collectors.toMap(ServicePropertyQueryDTO::getId, ServicePropertyQueryDTO::getCurrentValue));
        // 当前服务类型的所有配置
        List<ServicePropertyQueryDTO> servicePropertyList = getServiceInstanceConfig(service.getEnvironmentId(),
                service.getServerType(), null, null);
        servicePropertyList.forEach(item -> {
            if (propertyListMap.containsKey(item.getId())) {
                item.setCurrentValue(propertyListMap.get(item.getId()));
            }
        });
        // 服务配置数据
        List<ServicePropertyQueryDTO> propertyServiceList = servicePropertyList.stream()
                .filter(item -> InstanceTypeEnum.SERVICE.name().equalsIgnoreCase(item.getInstanceType())).collect(Collectors.toList());
        propertyServiceList.forEach(item -> servicePropertyValueRefs.addAll(getPropertyValueRefAddList(item, service.getId(), null, realName)));

        // 实例配置数据
        List<ServicePropertyQueryDTO> propertyInstanceList = servicePropertyList.stream()
                .filter(item -> !InstanceTypeEnum.SERVICE.name().equalsIgnoreCase(item.getInstanceType())).collect(Collectors.toList());
        servicePropertyValueRefs.addAll(getInstancePropertyValueRefAddList(instances, service.getId(), realName, propertyInstanceList));

        return servicePropertyValueRefs;
    }

    /**
     * 封装service_property_value_ref表新增数据(添加服务实例)
     */
    private List<ServicePropertyValueRef> getPropertyValueRefListForInstance(ServiceInstanceAddDTO serviceAddDTO, ZmsServiceEntity service, List<ServiceInstance> instances, String realName) {
        // 页面传入的ServicePropertyScopeEnum.SERVICE_CONFIG_GROUP、INSTANCE_CONFIG_GROUP配置
        Map<Integer, String> propertyListMap = serviceAddDTO.getPropertyList().stream()
                .collect(Collectors.toMap(ServicePropertyQueryDTO::getId, ServicePropertyQueryDTO::getCurrentValue));
        List<String> instanceTypes = instances.stream().map(ServiceInstance::getInstanceType).collect(Collectors.toList());
        // ServicePropertyScopeEnum.INSTANCE配置
        List<ServicePropertyQueryDTO> instancePropertyList = getServiceProperty(service.getEnvironmentId(),
                service.getServerType(), instanceTypes, null);
        instancePropertyList.forEach(item -> {
            if (propertyListMap.containsKey(item.getId())) {
                item.setCurrentValue(propertyListMap.get(item.getId()));
            }
        });
        // service_property_value_ref表新增数据封装
        return getInstancePropertyValueRefAddList(instances, service.getId(), realName, instancePropertyList);
    }

    /**
     * 封装service_property_value_ref表新增数据(实例配置数据)
     */
    private List<ServicePropertyValueRef> getInstancePropertyValueRefAddList(List<ServiceInstance> instances,
                                                                             Integer serviceId,
                                                                             String realName,
                                                                             List<ServicePropertyQueryDTO> propertyInstanceList) {
        List<ServicePropertyValueRef> instancePropertyValueRefAddList = Lists.newArrayList();
        instances.forEach(item -> propertyInstanceList.stream()
                .filter(prop -> item.getInstanceType().equalsIgnoreCase(prop.getInstanceType()))
                .forEach(prop -> instancePropertyValueRefAddList.addAll(getPropertyValueRefAddList(prop, serviceId, item.getId(), realName))));
        return instancePropertyValueRefAddList;
    }

    /**
     * 封装service_property_value_ref表新增数据(服务和实例配置数据)
     */
    private List<ServicePropertyValueRef> getPropertyValueRefAddList(ServicePropertyQueryDTO prop, Integer serviceId,
                                                                     Integer instanceId, String realName) {
        if (StringUtils.isEmpty(prop.getCurrentValue()) && StringUtils.isEmpty(prop.getDefaultValue())) {
            return Lists.newArrayList();
        }
        String currentValue = StringUtils.isEmpty(prop.getCurrentValue()) ? prop.getDefaultValue() : prop.getCurrentValue();
        List<String> valueList;
        // 复选框，数据库存多条，其他类型存一条
        if (prop.getIsDependencies() == 0 && !ServicePropertyChooseTypeEnum.TEXT.name().equals(prop.getChooseType())) {
            valueList = Arrays.asList(currentValue.split(","));
        } else {
            valueList = Lists.newArrayList(currentValue);
        }
        return valueList.stream().map(value -> getInsertServicePropertyValueRef(serviceId, instanceId, prop, value, realName)).collect(Collectors.toList());
    }

    /**
     * 生成启动的配置属性值
     */
    private List<com.zto.zms.dal.model.ServiceProcess> createProcess(Integer serviceId, List<ServiceInstance> instances, String realName) {
        // 1.service_process表数据封装
        List<com.zto.zms.dal.model.ServiceProcess> processList = instances.stream().map(instance -> {
            com.zto.zms.dal.model.ServiceProcess process = new com.zto.zms.dal.model.ServiceProcess();
            process.setServiceId(serviceId);
            process.setInstanceId(instance.getId());
            process.setCreator(realName);
            process.setModifier(realName);
            process.setStatus(ProcessStatusEnum.INIT.name());
            return process;
        }).collect(Collectors.toList());
        // 新增配置
        serviceProcessMapper.insertList(processList);

        // 2.process_property_value_ref表数据封装
        List<ProcessPropertyValueRef> processPropertyValueRefList = getProcessPropertyValueRefList(serviceId, instances, true);
        Map<Integer, Integer> processIds = processList.stream().collect(Collectors.toMap(com.zto.zms.dal.model.ServiceProcess::getInstanceId, com.zto.zms.dal.model.ServiceProcess::getId));
        processPropertyValueRefList.forEach(item -> {
            item.setServiceProcessId(processIds.get(item.getInstanceId()));
            item.setCreator(realName);
            item.setModifier(realName);
        });
        processPropertyValueRefMapper.insertList(processPropertyValueRefList);
        return processList;
    }

    /**
     * 封装process_property_value_ref表新增数据
     * flag: false为实例列表或重启对比配置，true为启动重启
     */
    private List<ProcessPropertyValueRef> getProcessPropertyValueRefList(Integer serviceId, List<ServiceInstance> instances, boolean flag) {
        String serviceType = zmsServiceMapper.getById(serviceId).getServerType();
        // 2.查询service_property_value_ref表
        List<ServicePropertyValueRefDTO> servicePropertyValueRefDTOList = servicePropertyValueRefMapper.queryByServiceId(serviceId);
        // 查询依赖服务真实值
        getDependenceRealValue(servicePropertyValueRefDTOList, flag);
        List<ProcessPropertyValueRef> servicePropertyValueRefList = servicePropertyValueRefDTOList.stream()
                .map(item -> {
                    ProcessPropertyValueRef processPropertyValueRef = new ProcessPropertyValueRef();
                    BeanUtils.copyProperties(item, processPropertyValueRef);
                    return processPropertyValueRef;
                }).collect(Collectors.toList());
        List<ProcessPropertyValueRef> processPropertyValueRefAddList = Lists.newArrayList();
        instances.forEach(instance -> {
            // 服务配置
            List<ProcessPropertyValueRef> serviceProcessPropertyValueRefList =
                    servicePropertyValueRefList.stream().map(item -> {
                        ProcessPropertyValueRef processPropertyValueRef = new ProcessPropertyValueRef();
                        BeanUtils.copyProperties(item, processPropertyValueRef);
                        processPropertyValueRef.setInstanceId(instance.getId());
                        return processPropertyValueRef;
                    }).collect(Collectors.toList());
            // 实例配置
            List<ServicePropertyValueRefDTO> instancePropertyValueRefDTOList = servicePropertyValueRefMapper.queryByInstanceId(instance.getId());
            List<ProcessPropertyValueRef> instanceProcessPropertyValueRefList = instancePropertyValueRefDTOList.stream()
                    .map(item -> {
                        ProcessPropertyValueRef processPropertyValueRef = new ProcessPropertyValueRef();
                        BeanUtils.copyProperties(item, processPropertyValueRef);
                        processPropertyValueRef.setRealValue(item.getCurrentValue());
                        return processPropertyValueRef;
                    }).collect(Collectors.toList());
            processPropertyValueRefAddList.addAll(serviceProcessPropertyValueRefList);
            processPropertyValueRefAddList.addAll(instanceProcessPropertyValueRefList);
            // 自定义配置
            List<ProcessPropertyValueRef> assemblePropertyValueRefs = adaptConfigFileAssemble.assembleConfigFile(serviceId, instance.getId(), processPropertyValueRefAddList);
            assemblePropertyValueRefs.forEach(ref -> {
                ref.setServiceId(serviceId);
                ref.setInstanceId(instance.getId());
                ref.setServiceType(serviceType);
                ref.setInstanceType(instance.getInstanceType());
            });
            processPropertyValueRefAddList.addAll(assemblePropertyValueRefs);
        });
        //去重
        return Lists.newArrayList(processPropertyValueRefAddList.stream()
                .collect(Collectors.toMap((item) -> item.getInstanceId() + "&" + item.getPropertyGroup() + "&" + item.getPropertyName(), item -> item, (o1, o2) -> o2))
                .values());
    }

    /**
     * 添加服务、服务实例返回数据
     */
    private List<ServiceInstanceStartVO> getServiceInstanceAddResult(List<HostServiceInstanceDTO> instances, List<com.zto.zms.dal.model.ServiceProcess> processList) {
        Map<Integer, HostServiceInstanceDTO> instanceMap = instances.stream().collect(Collectors.toMap(HostServiceInstanceDTO::getId, item -> item));
        return processList.stream().map(item -> {
            ServiceInstanceStartVO vo = new ServiceInstanceStartVO();
            vo.setProcessId(item.getId());
            vo.setInstanceId(item.getInstanceId());
            vo.setInstanceName(instanceMap.get(item.getInstanceId()).getInstanceName());
            vo.setHostName(instanceMap.get(item.getInstanceId()).getHostName());
            return vo;
        }).collect(Collectors.toList());
    }


    /**
     * 查询依赖服务真实值
     */
    private void getDependenceRealValue(List<ServicePropertyValueRefDTO> servicePropertyValueRefs, boolean flag) {
        servicePropertyValueRefs.forEach(ref -> {
            if (ref.getIsDependencies() == 1) {
                // 匹配对应的配置属性
                if (checkServiceProperty(ref, ServicePropertiesNameEnum.ZK_SERVICE)) {
                    // kafka、collector、alert服务依赖ZK
                    ref.setRealValue(getZKService(Integer.parseInt(ref.getCurrentValue()), flag));
                } else if (checkServiceProperty(ref, ServicePropertiesNameEnum.INFLUX_DB_SERVICE)) {
                    // collector、alert依赖influxDB服务
                    ref.setRealValue(getInfluxDbService(Integer.parseInt(ref.getCurrentValue()), flag));
                } else if (checkServiceProperty(ref, ServicePropertiesNameEnum.ENVID_SERVICE)) {
                    ref.setRealValue(ref.getCurrentValue());
                } else {
                    // 避免空指针
                    ref.setRealValue("");
                }
            } else {
                ref.setRealValue(ref.getCurrentValue());
            }
        });
    }

    /**
     * 查询ZK服务
     */
    private String getZKService(Integer zkServiceId, boolean flag) {
        // 根据服务ID查询服务实例
        List<ServiceInstance> instances = serviceInstanceMapper.listStartInstanceByServiceId(zkServiceId);
        if (CollectionUtils.isEmpty(instances)) {
            if (flag) {
                throw new IllegalArgumentException("请更换依赖的zookeeper服务");
            }
            return "";
        }
        Integer zkClientPortPropertyId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.ZOOKEEPER.name(), ServicePropertiesNameEnum.ZK_CLIENT_PORT.getInstanceType(), ServicePropertiesNameEnum.ZK_CLIENT_PORT.getGroup(), ServicePropertiesNameEnum.ZK_CLIENT_PORT.getName());
        // 查询ZK集群启动时的所有实例进程
        List<Integer> zkProcessIds = serviceProcessMapper.listLastSuccessProcess(zkServiceId);
        Map<Integer, String> zkPortPropertyValueRef = processPropertyValueRefMapper.listByProcessId(zkProcessIds, zkClientPortPropertyId).stream()
                .collect(Collectors.toMap(ProcessPropertyValueRef::getInstanceId, ProcessPropertyValueRef::getRealValue));
        return joinIpAndPort(instances, zkPortPropertyValueRef);
    }

    /**
     * 查询InfluxDB服务
     */
    private String getInfluxDbService(Integer influxDbServiceId, boolean flag) {
        // 根据服务ID查询服务实例
        List<ServiceInstance> instances = serviceInstanceMapper.listStartInstanceByServiceId(influxDbServiceId);
        if (CollectionUtils.isEmpty(instances)) {
            if (flag) {
                throw new IllegalArgumentException("请更换依赖的influxDB服务");
            }
            return "";
        }
        Integer influxDbPortPropertyId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.INFLUXDB.name(), ServicePropertiesNameEnum.INFLUX_DB_PORT.getInstanceType(), ServicePropertiesNameEnum.INFLUX_DB_PORT.getGroup(), ServicePropertiesNameEnum.INFLUX_DB_PORT.getName());
        // 查询InfluxDB的实例进程
        List<Integer> processIds = serviceProcessMapper.listLastSuccessProcess(influxDbServiceId);
        // influxDB服务特殊,使用currentValue
        Map<Integer, String> influxDbPortPropertyValueRef = processPropertyValueRefMapper.listByProcessId(processIds, influxDbPortPropertyId).stream()
                .collect(Collectors.toMap(ProcessPropertyValueRef::getInstanceId, ProcessPropertyValueRef::getCurrentValue));
        //获取host
        String hostIp = zmsHostMapper.getEnableIPById(instances.get(0).getHostId());
        //获取bind-address
        String bindAddress = influxDbPortPropertyValueRef.get(instances.get(0).getId());
        return MessageFormat.format("http://{0}:{1}", hostIp, bindAddress);
    }

    /**
     * 拼接ZK集群的启动地址和服务地址
     */
    private String joinIpAndPort(List<ServiceInstance> instances, Map<Integer, String> propertyValueRef) {
        StringBuilder address = new StringBuilder();
        instances.stream()
                .filter(instance -> propertyValueRef.containsKey(instance.getId()))
                .forEach(instance -> {
                    // 查询服务实例对应的主机
                    String hostIp = zmsHostMapper.getEnableIPById(instance.getHostId());
                    address.append(",").append(hostIp).append(":").append(propertyValueRef.get(instance.getId()));
                });
        return address.length() > 0 ? address.deleteCharAt(0).toString() : "";
    }

    /**
     * 判断是否是对应属性
     */
    private boolean checkServiceProperty(ServicePropertyValueRefDTO ref, ServicePropertiesNameEnum propertiesNameEnum) {
        return propertiesNameEnum.getInstanceType().equals(ref.getInstanceType())
                && propertiesNameEnum.getGroup().equals(ref.getPropertyGroup())
                && propertiesNameEnum.getName().equals(ref.getPropertyName());
    }

    /**
     * 服务实例重启配置对比
     */
    public List<ServicePropertyCompareDTO> compareServiceProperty(Integer serviceId, List<Integer> instanceIds) {
        // 查询启动的实例
        List<ServiceInstance> instanceList = serviceInstanceMapper.listStartInstanceByServiceId(serviceId);
        if (!CollectionUtils.isEmpty(instanceIds)) {
            instanceList = instanceList.stream().filter(item -> instanceIds.contains(item.getId())).collect(Collectors.toList());
        }
        // 预生成当前配置对应的发布配置
        Map<Integer, List<ProcessPropertyValueRef>> currentPropertyValueRefMap = getProcessPropertyValueRefList(serviceId, instanceList, false)
                .stream().collect(Collectors.groupingBy(ProcessPropertyValueRef::getInstanceId));
        // 对比实例的当前配置和发布配置，返回对比结果
        return compareServiceInstanceProperty(instanceList, currentPropertyValueRefMap);
    }

    /**
     * 对比实例的当前配置和发布配置，返回对比结果
     */
    private List<ServicePropertyCompareDTO> compareServiceInstanceProperty(List<ServiceInstance> instances,
                                                                           Map<Integer, List<ProcessPropertyValueRef>> currentPropertyValueRefMap) {
        List<ServicePropertyCompareDTO> servicePropertyCompareDTOList = Lists.newArrayList();
        Map<Integer, ServiceInstance> instanceMap = instances.stream().collect(Collectors.toMap(ServiceInstance::getId, item -> item));
        currentPropertyValueRefMap.forEach((instanceId, currentPropertyValueRefs) -> {
            // 当前服务实例对应的启动进程
            com.zto.zms.dal.model.ServiceProcess lastRunningProcess = serviceProcessMapper.listRunningProcessByInstance(instanceId);
            Integer processId = lastRunningProcess.getId();
            // 发布配置，包含服务配置和实例配置
            List<ProcessPropertyValueRef> processPropertyValueRefs = processPropertyValueRefMapper.listByServiceProcessId(processId);
            // 对比配置
            ServicePropertyCompareDTO servicePropertyCompareDTO = comparePropertyValueFile(instanceMap.get(instanceId), currentPropertyValueRefs, processPropertyValueRefs);
            servicePropertyCompareDTOList.add(servicePropertyCompareDTO);
        });
        return servicePropertyCompareDTOList;
    }

    /**
     * 对比单个实例的发布配置和现有配置
     */
    private ServicePropertyCompareDTO comparePropertyValueFile(ServiceInstance item, List<ProcessPropertyValueRef> currentPropertyValueRefs, List<ProcessPropertyValueRef> processPropertyValueRefs) {
        Map<String, List<ProcessPropertyValueRef>> currentValueRefConfApiKeyMap = currentPropertyValueRefs.stream().collect(Collectors.groupingBy(ProcessPropertyValueRef::getConfApiKey));
        Map<String, List<ProcessPropertyValueRef>> processValueRefConfApiKeyMap = processPropertyValueRefs.stream().collect(Collectors.groupingBy(ProcessPropertyValueRef::getConfApiKey));
        Set<String> confApiKeys = Sets.newHashSet();
        confApiKeys.addAll(currentValueRefConfApiKeyMap.keySet());
        confApiKeys.addAll(processValueRefConfApiKeyMap.keySet());
        List<ConfigApi> configApiList = configApiMapper.listByApiKeys(confApiKeys);
        List<ServicePropertyCompareFileDTO> servicePropertyCompareFileDTOList = Lists.newArrayList();
        // 1.文件
        configApiList.stream().filter(api -> !StringUtils.isEmpty(api.getFileName())).forEach(api -> {
            // 对当前文件配置进行排序, 封装配置name和value
            Map<String, String> currentPropertyValueRefMap = sortPropertyValueRefs(currentValueRefConfApiKeyMap.get(api.getApiKey()));
            // 对发布配置文件进行排序, 封装配置name和value
            Map<String, String> processPropertyValueRefMap = sortPropertyValueRefs(processValueRefConfApiKeyMap.get(api.getApiKey()));
            // 对比配置结果
            ServicePropertyCompareFileDTO servicePropertyCompareFileDTO = new ServicePropertyCompareFileDTO();
            servicePropertyCompareFileDTO.setFileName(api.getFileName());
            servicePropertyCompareFileDTO.setCurrentNum(currentPropertyValueRefMap.size());
            servicePropertyCompareFileDTO.setProcessNum(processPropertyValueRefMap.size());
            comparePropertyValueRefMap(servicePropertyCompareFileDTO, currentPropertyValueRefMap, processPropertyValueRefMap);
            servicePropertyCompareFileDTOList.add(servicePropertyCompareFileDTO);
        });
        // 2.系统资源
        compareEnvPropertyValueRef(currentValueRefConfApiKeyMap, processValueRefConfApiKeyMap, configApiList, servicePropertyCompareFileDTOList);
        // 返回数据
        ServicePropertyCompareDTO servicePropertyCompareDTO = new ServicePropertyCompareDTO();
        servicePropertyCompareDTO.setInstanceId(item.getId());
        servicePropertyCompareDTO.setInstanceName(item.getInstanceName());
        servicePropertyCompareDTO.setFileList(servicePropertyCompareFileDTOList);
        return servicePropertyCompareDTO;
    }

    /**
     * 对比系统资源文件（environment、jvm、jmx）
     */
    private void compareEnvPropertyValueRef(Map<String, List<ProcessPropertyValueRef>> currentValueRefConfApiKeyMap,
                                            Map<String, List<ProcessPropertyValueRef>> processValueRefConfApiKeyMap,
                                            List<ConfigApi> configApiList,
                                            List<ServicePropertyCompareFileDTO> servicePropertyCompareFileDTOList) {
        List<ProcessPropertyValueRef> currentEnvPropertyValueRefs = Lists.newArrayList();
        List<ProcessPropertyValueRef> processEnvPropertyValueRefs = Lists.newArrayList();
        configApiList.stream().filter(api -> StringUtils.isEmpty(api.getFileName())).forEach(api -> {
            if (currentValueRefConfApiKeyMap.containsKey(api.getApiKey())) {
                currentEnvPropertyValueRefs.addAll(currentValueRefConfApiKeyMap.get(api.getApiKey()));
            }
            if (processValueRefConfApiKeyMap.containsKey(api.getApiKey())) {
                processEnvPropertyValueRefs.addAll(processValueRefConfApiKeyMap.get(api.getApiKey()));
            }
        });
        if (currentEnvPropertyValueRefs.size() == 0 && processEnvPropertyValueRefs.size() == 0) {
            return;
        }
        // 对当前文件配置进行排序, 封装配置name和value
        Map<String, String> currentPropertyValueRefMap = sortPropertyValueRefs(currentEnvPropertyValueRefs);
        // 对发布配置文件进行排序, 封装配置name和value
        Map<String, String> processPropertyValueRefMap = sortPropertyValueRefs(processEnvPropertyValueRefs);
        // 对比配置结果
        ServicePropertyCompareFileDTO servicePropertyCompareFileDTO = new ServicePropertyCompareFileDTO();
        servicePropertyCompareFileDTO.setFileName("系统资源");
        servicePropertyCompareFileDTO.setCurrentNum(currentPropertyValueRefMap.size());
        servicePropertyCompareFileDTO.setProcessNum(processPropertyValueRefMap.size());
        comparePropertyValueRefMap(servicePropertyCompareFileDTO, currentPropertyValueRefMap, processPropertyValueRefMap);
        servicePropertyCompareFileDTOList.add(servicePropertyCompareFileDTO);
    }

    /**
     * 对配置进行排序, 封装配置name和value
     */
    private Map<String, String> sortPropertyValueRefs(List<ProcessPropertyValueRef> propertyValueRefs) {
        Map<String, String> propertyValueRefMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(propertyValueRefs)) {
            return propertyValueRefMap;
        }
        propertyValueRefs.sort(Comparator.comparing(ProcessPropertyValueRef::getPropertyId)
                .thenComparing(ProcessPropertyValueRef::getRealValue));
        Map<String, String> propertyNameRefMap = propertyValueRefs.stream()
                .collect(Collectors.toMap(item -> item.getPropertyId() + "&" + item.getPropertyName(), ProcessPropertyValueRef::getPropertyName, (v1, v2) -> v1));
        propertyValueRefMap = propertyValueRefs.stream()
                .collect(Collectors.toMap(item -> item.getPropertyId() + "&" + item.getPropertyName(), ProcessPropertyValueRef::getRealValue, (v1, v2) -> v1.concat(",".concat(v2))));
        propertyValueRefMap.entrySet().forEach(item -> item.setValue(propertyNameRefMap.get(item.getKey()).concat("=").concat(item.getValue())));
        return propertyValueRefMap;
    }

    /**
     * 对比当前配置和发布配置
     */
    private void comparePropertyValueRefMap(ServicePropertyCompareFileDTO servicePropertyCompareFileDTO,
                                            Map<String, String> currentPropertyValueRefMap,
                                            Map<String, String> processPropertyValueRefMap) {
        List<ServicePropertyCompareValueDTO> compareValueDTOList = Lists.newArrayList();
        TreeSet<String> propertyIdAndName = Sets.newTreeSet();
        propertyIdAndName.addAll(processPropertyValueRefMap.keySet());
        propertyIdAndName.addAll(currentPropertyValueRefMap.keySet());
        AtomicReference<Double> processId = new AtomicReference<>();
        AtomicReference<Double> currentId = new AtomicReference<>();
        AtomicReference<Double> initValue = new AtomicReference<>(10D);
        propertyIdAndName.forEach(prop -> {
            Double basicValue = initValue.getAndSet(initValue.get() + 10D);
            if (processPropertyValueRefMap.containsKey(prop) && currentPropertyValueRefMap.containsKey(prop)) {
                // 发布配置和当前配置都存在
                String processValue = processPropertyValueRefMap.get(prop);
                String currentValue = currentPropertyValueRefMap.get(prop);
                // 比较对应的值是否一致
                if (processValue.equals(currentValue)) {
                    processId.set(basicValue);
                    currentId.set(basicValue);
                    compareValueDTOList.add(getCompareValueDTO(basicValue, null, currentValue));
                } else {
                    // 减少配置
                    processId.set((null == processId.get() ? 0 : processId.get()) + 0.1D);
                    compareValueDTOList.add(getCompareValueDTO(processId.get(), "-", processValue));
                    // 增加配置
                    currentId.set((null == currentId.get() ? basicValue : currentId.get()) + 0.1D);
                    compareValueDTOList.add(getCompareValueDTO(currentId.get(), "+", currentValue));
                }
            } else {
                processId.set(basicValue);
                currentId.set(basicValue);
                if (processPropertyValueRefMap.containsKey(prop)) {
                    // 发布配置存在，当前配置不存在
                    compareValueDTOList.add(getCompareValueDTO(basicValue, "-", processPropertyValueRefMap.get(prop)));
                } else {
                    // 发布配置不存在，当前配置存在
                    compareValueDTOList.add(getCompareValueDTO(basicValue, "+", currentPropertyValueRefMap.get(prop)));
                }
            }
        });
        // 排序
        compareValueDTOList.sort(Comparator.comparing(ServicePropertyCompareValueDTO::getId));
        // 序号
        AtomicInteger leftNum = new AtomicInteger(1);
        AtomicInteger rightNum = new AtomicInteger(1);
        AtomicInteger addNum = new AtomicInteger(0);
        AtomicInteger reduceNum = new AtomicInteger(0);
        compareValueDTOList.forEach(item -> {
            if ("-".equals(item.getMark())) {
                item.setLeftNum(leftNum.getAndIncrement());
                reduceNum.incrementAndGet();
            } else if ("+".equals(item.getMark())) {
                item.setRightNum(rightNum.getAndIncrement());
                addNum.incrementAndGet();
            } else {
                item.setLeftNum(leftNum.getAndIncrement());
                item.setRightNum(rightNum.getAndIncrement());
            }
        });
        servicePropertyCompareFileDTO.setAddNum(addNum.get());
        servicePropertyCompareFileDTO.setReduceNum(reduceNum.get());
        servicePropertyCompareFileDTO.setCompareList(compareValueDTOList);
    }

    private ServicePropertyCompareValueDTO getCompareValueDTO(Double id, String mark, String value) {
        ServicePropertyCompareValueDTO DTO = new ServicePropertyCompareValueDTO();
        DTO.setId(id);
        DTO.setMark(mark);
        DTO.setValue(value);
        return DTO;
    }

    /**
     * 停止服务实例的进程
     */
    public List<ServiceInstanceStartVO> stopServiceInstance(List<Integer> instanceIds, String realName) {
        List<HostServiceInstanceDTO> serviceInstances = serviceInstanceMapper.listHostByInstanceIds(instanceIds);
        List<ServiceInstanceStartVO> serviceInstanceStartVOS = Lists.newArrayList();
        for (HostServiceInstanceDTO instance : serviceInstances) {
            //当前实例正在运行的进程
            List<Integer> processIds = serviceProcessMapper.queryLastRunningProcessByInstanceId(instance.getId());
            for (Integer processId : processIds) {
                try {
                    Result result = processService.stopProcess(processId, realName);
                    logger.info("停止服务进程:{},状态{},结果:{}", processId, result.isStatus(), result.getMessage());
                } catch (Exception e) {
                    logger.error(MessageFormat.format("停止服务进程:{0}异常:", processId), e);
                }
                ServiceInstanceStartVO serviceInstanceStartVo = new ServiceInstanceStartVO();
                serviceInstanceStartVo.setInstanceName(instance.getInstanceName());
                serviceInstanceStartVo.setHostName(instance.getHostName());
                serviceInstanceStartVo.setProcessId(processId);
                serviceInstanceStartVo.setInstanceId(instance.getId());
                serviceInstanceStartVOS.add(serviceInstanceStartVo);
            }
            //更新实例状态
            serviceInstanceMapper.stopInstance(instance.getId(), realName);
        }
        return serviceInstanceStartVOS;
    }

    /**
     * 启动服务实例
     */
    public List<ServiceInstanceStartVO> startServiceInstance(List<Integer> instanceIds, String realName) {
        //查询服务
        List<HostServiceInstanceDTO> hostInstances = serviceInstanceMapper.listHostByInstanceIds(instanceIds);

        //如果有正在启动、正在运行的实例，返回失败
        boolean checkSuccess = hostInstances.stream().noneMatch(item ->
                InstanceStatusEnum.STARTING.name().equals(item.getInstanceStatus()) ||
                        InstanceStatusEnum.START.name().equals(item.getInstanceStatus()));
        Assert.that(checkSuccess, "有正在启动或运行的服务实例");

        Integer serviceId = serviceInstanceMapper.listByInstanceIds(instanceIds).stream()
                .map(ServiceInstance::getServiceId).findFirst().orElseThrow(() -> new IllegalArgumentException("服务不存在！"));
        List<ServiceInstance> instances = hostInstances.stream().map(item -> {
            ServiceInstance instance = new ServiceInstance();
            instance.setId(item.getId());
            instance.setInstanceType(item.getInstanceType());
            return instance;
        }).collect(Collectors.toList());
        //创建process
        List<com.zto.zms.dal.model.ServiceProcess> serviceProcesses = createProcess(serviceId, instances, realName);
        for (com.zto.zms.dal.model.ServiceProcess serviceProcess : serviceProcesses) {
            try {
                processService.startProcess(serviceProcess.getInstanceId(), serviceProcess.getId());
            } catch (Exception e) {
                logger.error(MessageFormat.format("启动服务进程:{0}异常:", serviceProcess.getId()), e);
            }
        }
        return getServiceInstanceAddResult(hostInstances, serviceProcesses);
    }

    /**
     * 重启服务实例
     */
    public List<ServiceInstanceStartVO> restartServiceInstance(List<Integer> instanceIds, String realName) {
        // 停止服务实例
        stopServiceInstance(instanceIds, realName);
        // 启动服务实例
        return startServiceInstance(instanceIds, realName);
    }

}

