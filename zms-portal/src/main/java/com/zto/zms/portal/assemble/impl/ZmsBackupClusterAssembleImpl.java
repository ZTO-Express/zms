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

package com.zto.zms.portal.assemble.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.zto.zms.common.RunCommonConfig;
import com.zto.zms.common.ZmsServiceTypeEnum;
import com.zto.zms.dal.mapper.ServicePropertyMapper;
import com.zto.zms.dal.mapper.ZmsEnvironmentMapper;
import com.zto.zms.dal.mapper.ZmsServiceMapper;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.dal.model.ZmsEnvironment;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.assemble.ConfigFileAssemble;
import com.zto.zms.portal.assemble.RunCommonConfigAssemble;
import com.zto.zms.portal.common.ServiceStartUserEnum;
import com.zto.zms.portal.config.CommonConfig;
import com.zto.zms.portal.service.host.HostService;
import com.zto.zms.portal.service.serve.ProcessService;
import com.zto.zms.portal.service.serve.ServeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zto.zms.common.ZmsConst.ZMS_PORTAL;
import static com.zto.zms.common.ZmsConst.ZMS_TOKEN;
import static com.zto.zms.portal.common.ServicePropertiesNameEnum.ENVID_SERVICE;
import static com.zto.zms.portal.common.ServicePropertiesNameEnum.ZMS_BACKUP_CLUSTER_MAP;

/**
 * @author lidawei
 * @date 2020/5/12
 * @since
 **/
@Service("zms_backup_cluster-instance")
public class ZmsBackupClusterAssembleImpl implements RunCommonConfigAssemble, ConfigFileAssemble {

    private static final String ZMS_BACKUP_CLUSTER_START = "zmsBackupCluster/zms-backup-cluster-start.sh";
    private static final String ZMS_BACKUP_CLUSTER = "zmsBackupCluster";
    private static final String ZMS_BACKUP_CLUSTER_CONFIG = "zms-backup-cluster-config";
    private static final String ORIGIN_ZMS_ZK = "origin.zms_zk";
    private static final String ORIGIN_ENV_ID = "origin.envId";
    private static final String CURRENT_ENV_ID = "current.envId";
    private static final String CURRENT_ZMS_ZK = "current.zms_zk";

    @Autowired
    private CommonConfig commonConfig;
    @Autowired
    private ProcessService processService;
    @Autowired
    private ServicePropertyMapper servicePropertyMapper;
    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private HostService hostService;
    @Autowired
    private ServeService serveService;
    @Autowired
    private ZmsEnvironmentMapper environmentMapper;

    @Override
    public RunCommonConfig assembleRunCommon(Integer processId) {
        String programName = processService.getProgramName(processId);
        RunCommonConfig commonConfig = new RunCommonConfig();
        Map<String, String> environmentConfigMap = processService.environmentConfig(processId);
        commonConfig.setAutoRestart("unexpected");
        commonConfig.setCommand(ZMS_BACKUP_CLUSTER_START);
        commonConfig.setEnvironment(environmentConfigMap);
        commonConfig.setUser(ServiceStartUserEnum.ZMS_BACKUP_CLUSTER.getUser());
        commonConfig.setGroup(ServiceStartUserEnum.ZMS_BACKUP_CLUSTER.getGroup());
        commonConfig.setExitCodes("0,2");
        commonConfig.setKillAsGroup(true);
        commonConfig.setStopAsGroup(true);
        commonConfig.setStartSecs(15);
        commonConfig.setStopWaitSecs(30);
        commonConfig.setProgramName(programName);
        commonConfig.setLibDir(ZMS_BACKUP_CLUSTER);
        return commonConfig;
    }

    @Override
    public List<ProcessPropertyValueRef> assembleConfigFile(Integer serviceId, Integer instanceId, List<ProcessPropertyValueRef> propertyValueRefs) {
        ZmsServiceEntity zmsServiceEntity = zmsServiceMapper.getById(serviceId);
        Integer clusterMapPropertiesId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.ZMS_BACKUP_CLUSTER.name(),
                ZMS_BACKUP_CLUSTER_MAP.getInstanceType(), ZMS_BACKUP_CLUSTER_MAP.getGroup(), ZMS_BACKUP_CLUSTER_MAP.getName());
        //cluster.map
        Map<Integer, List<ProcessPropertyValueRef>> propertyIdMap = propertyValueRefs
                .stream()
                .collect(Collectors.groupingBy(ProcessPropertyValueRef::getPropertyId));
        List<ProcessPropertyValueRef> clusterMapPropertyValueRefs = propertyIdMap.get(clusterMapPropertiesId);
        JSONObject json = new JSONObject();
        for (ProcessPropertyValueRef valueRef : clusterMapPropertyValueRefs) {
            String currentValue = valueRef.getCurrentValue();
            String[] clusterMap = currentValue.split(":");
            String originCluster = clusterMap[0];
            String currentCluster = clusterMap[1];
            json.put(originCluster, currentCluster);
        }
        String clusterMapJson = json.toJSONString();
        List<ProcessPropertyValueRef> addPropertyValueRefs = Lists.newArrayList();
        addProperties(addPropertyValueRefs, clusterMapPropertiesId, ZMS_BACKUP_CLUSTER_MAP.getName(), clusterMapJson);
        //current.envId
        addProperties(addPropertyValueRefs, 0, CURRENT_ENV_ID, zmsServiceEntity.getEnvironmentId().toString());
        //current.zms_zk
        ZmsEnvironment currentEnv = environmentMapper.getEnv(zmsServiceEntity.getEnvironmentId());
        String currentZookeeper = getZookeeper(currentEnv.getZkServiceId());
        addProperties(addPropertyValueRefs, 0, CURRENT_ZMS_ZK, currentZookeeper);
        //token
        String token = hostService.getToken(zmsServiceEntity.getEnvironmentId());
        addProperties(addPropertyValueRefs, 0, ZMS_TOKEN, token);
        //portal
        addProperties(addPropertyValueRefs, 0, ZMS_PORTAL, commonConfig.getZmsPortalUrl());
        Integer originEnvIdPropertiesId = servicePropertyMapper.getIdByServiceTypeAndName(ZmsServiceTypeEnum.ZMS_BACKUP_CLUSTER.name(),
                ENVID_SERVICE.getInstanceType(), ENVID_SERVICE.getGroup(), ENVID_SERVICE.getName());
        ProcessPropertyValueRef originEnvIdPropertyValueRef = propertyIdMap.get(originEnvIdPropertiesId).get(0);
        String originEnvId = originEnvIdPropertyValueRef.getCurrentValue();
        ZmsEnvironment originEnv = environmentMapper.getEnv(Integer.valueOf(originEnvId));
        Integer zkServiceId = originEnv.getZkServiceId();
        String originZookeeper = getZookeeper(zkServiceId);
        addProperties(addPropertyValueRefs, 0, ORIGIN_ZMS_ZK, originZookeeper);
        //origin.envId
        originEnvIdPropertyValueRef.setPropertyName(ORIGIN_ENV_ID);
        return addPropertyValueRefs;
    }

    public String getZookeeper(Integer zkServiceId) {
        List<String> zookeepers = serveService.listZookeeperAddr(zkServiceId);
        //zk地址
        return Joiner.on(',').join(zookeepers);
    }

    private void addProperties(List<ProcessPropertyValueRef> valueRefs, Integer propertyId, String name, String value) {
        ProcessPropertyValueRef propertyValueRef = new ProcessPropertyValueRef();
        propertyValueRef.setPropertyName(name);
        propertyValueRef.setRealValue(value);
        propertyValueRef.setCurrentValue(value);
        propertyValueRef.setConfApiKey(ZMS_BACKUP_CLUSTER_CONFIG);
        propertyValueRef.setPropertyId(propertyId);
        propertyValueRef.setPropertyGroup("");
        valueRefs.add(propertyValueRef);
    }
}

