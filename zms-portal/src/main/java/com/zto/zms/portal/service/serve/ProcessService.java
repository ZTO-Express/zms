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

import com.alibaba.fastjson.JSON;
import com.zto.zms.common.RunCommonConfig;
import com.zto.zms.utils.Assert;
import com.zto.zms.writer.IniFileWriter;
import com.zto.zms.writer.PropertiesWriter;
import com.zto.zms.common.ServiceProcess;
import com.zto.zms.dal.mapper.*;
import com.zto.zms.dal.model.*;
import com.zto.zms.portal.assemble.impl.AdaptParamOptAssembleImpl;
import com.zto.zms.portal.assemble.impl.AdaptRunCommonAssembleImpl;
import com.zto.zms.portal.common.InstanceStatusEnum;
import com.zto.zms.portal.common.RunningStatusEnum;
import com.zto.zms.common.ProcessStdLog;
import com.zto.zms.portal.listener.impl.AdaptServiceInstanceListener;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.agent.AgentApiService;
import com.zto.zms.portal.service.host.HostService;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springside.modules.utils.collection.CollectionUtil;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Class: ServeConfigService</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/4
 **/
@Service
public class ProcessService {

    private static final String CONFIG_SCOPE_SERVER = "server";

    @Autowired
    private AdaptParamOptAssembleImpl adaptParamOptAssemble;
    @Autowired
    private ServiceProcessMapper serviceProcessMapper;
    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ProcessPropertyValueRefMapper processPropertyValueRefMapper;
    @Autowired
    private ConfigApiMapper configApiMapper;
    @Autowired
    private ZmsHostMapper zmsHostMapper;
    @Autowired
    private AgentApiService agentApiService;
    @Autowired
    private AdaptRunCommonAssembleImpl adaptRunCommonAssemble;
    @Autowired
    private AdaptServiceInstanceListener adaptServiceInstanceListener;
    @Autowired
    private HostService hostService;

    /**
     * 启动服务进程
     *
     * @param instanceId
     * @param processId
     * @throws IOException
     */
    public void startProcess(Integer instanceId, Integer processId) throws IOException {
        ServiceProcess serviceProcess = serviceProcessMapper.getByProcessId(processId);
        Assert.that(null != serviceProcess, "process is not exist");
        //当前process信息
        String programName = getProgramName(processId);
        String programType = getProgramType(processId);
        //上次process启动信息
        com.zto.zms.dal.model.ServiceProcess lastProcess = serviceProcessMapper.listRunningProcessByInstance(serviceProcess.getInstanceId());
        String lastProgramName = getProgramName(lastProcess.getId());
        //agent地址
        ZmsHost host = zmsHostMapper.getByInstanceId(serviceProcess.getInstanceId());
        Result result;
        try {
            String token = hostService.getToken(host.getEnvironmentId());
            //更新实例状态为启动中
            serviceInstanceMapper.updateInstanceStatus(instanceId, InstanceStatusEnum.STARTING.name());
            result = agentApiService.startProcess(host.getHostIp(), processId, programName, programType, lastProgramName, token);
        } catch (Exception e) {
            serviceProcessMapper.updateFailure(processId);
            throw e;
        }
        //启动成功
        if (result.isStatus()) {
            serviceProcessMapper.updateSuccess(processId);
            //启动监听
            adaptServiceInstanceListener.start(serviceProcess.getServiceId(), processId);
        } else {
            serviceProcessMapper.updateFailure(processId);
        }
        Assert.that(result.isStatus(), "启动服务进程失败:" + result.getMessage());
    }

    public Result stopProcess(Integer processId, String realName) throws IOException {
        String programName = getProgramName(processId);
        ServiceProcess serviceProcess = serviceProcessMapper.getByProcessId(processId);
        ZmsHost host = zmsHostMapper.getByInstanceId(serviceProcess.getInstanceId());
        String token = hostService.getToken(host.getEnvironmentId());
        Result result = agentApiService.stopProcess(host.getHostIp(), programName, token);
        //更新状态为停止
        if (result.isStatus()) {
            serviceProcessMapper.stopByAdmin(processId, realName);
        }
        return result;
    }

    /**
     * 生成process配置文件流，并压缩
     *
     * @param processId
     * @param outputStream
     * @throws IOException
     */
    public void configFile(Integer processId, OutputStream outputStream) throws IOException {
        ServiceProcess serviceProcess = serviceProcessMapper.getByProcessId(processId);
        Assert.that(null != serviceProcess, "process is not exist");
        String serviceType = serviceProcessMapper.getServiceType(processId);

        //查询配置
        List<ProcessPropertyValueRef> propertyValueRefs = processPropertyValueRefMapper.listByServiceProcessId(processId);
        //生成文件
        Map<String, List<ProcessPropertyValueRef>> valueRefConfApiKeyMap =
                propertyValueRefs.stream().collect(Collectors.groupingBy(ProcessPropertyValueRef::getConfApiKey));

        List<ConfigApi> configApiList = configApiMapper.listByApiKeys(valueRefConfApiKeyMap.keySet())
                .stream()
                .filter(item -> CONFIG_SCOPE_SERVER.equals(item.getConfigScope()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(configApiList)) {
            return;
        }
        //文件压缩
        try (TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(outputStream)) {
            //自定义文件配置
            processCustomConfig(valueRefConfApiKeyMap, configApiList, tarOutputStream);
            //默认配置文件模板
            processDefaultConfig(tarOutputStream, serviceType);
        }
    }

    /**
     * 服务默认配置文件，直接将文件压缩
     *
     * @param tarOutputStream
     * @param serviceType
     * @throws IOException
     */
    private void processDefaultConfig(TarArchiveOutputStream tarOutputStream, String serviceType) throws IOException {
        String path = Paths.get("conf/" + serviceType).toAbsolutePath().toString();
        if (null == path) {
            return;
        }
        File fileParent = new File(path);
        if (!fileParent.exists()) {
            return;
        }
        File[] files = fileParent.listFiles();
        if (null == files) {
            return;
        }
        for (File configFile : files) {
            try (FileInputStream fileInputStream = new FileInputStream(configFile);) {
                TarArchiveEntry entry = new TarArchiveEntry(configFile.getName());
                entry.setSize(fileInputStream.available());
                tarOutputStream.putArchiveEntry(entry);
                IOUtils.copy(fileInputStream, tarOutputStream);
                tarOutputStream.closeArchiveEntry();
            }
        }
    }

    /**
     * 可自定义配置文件
     *
     * @param valueRefConfApiKeyMap
     * @param configApiList
     * @param tarOutputStream
     * @throws IOException
     */
    private void processCustomConfig(Map<String, List<ProcessPropertyValueRef>> valueRefConfApiKeyMap, List<ConfigApi> configApiList, TarArchiveOutputStream tarOutputStream) throws IOException {
        for (ConfigApi configApi : configApiList) {
            ByteArrayOutputStream byteOutputStream = null;
            try {
                //.properties文件
                if ("properties".equals(configApi.getFileType())) {
                    PropertiesWriter propertiesWriter = PropertiesWriter.newInstance();
                    valueRefConfApiKeyMap.get(configApi.getApiKey()).forEach(item -> {
                        propertiesWriter.add(item.getPropertyName(), item.getRealValue());
                    });
                    byteOutputStream = new ByteArrayOutputStream();
                    propertiesWriter.store(byteOutputStream);
                }
                //win-ini格式文件
                if ("ini".equals(configApi.getFileType())) {
                    IniFileWriter iniFileWriter = IniFileWriter.newInstance();
                    Map<String, List<ProcessPropertyValueRef>> propertyGroupMap =
                            valueRefConfApiKeyMap.get(configApi.getApiKey()).stream()
                                    .collect(Collectors.groupingBy(ProcessPropertyValueRef::getPropertyGroup));
                    //转换成ini格式
                    propertyGroupMap.forEach((key, value) -> {
                        List<IniFileWriter.IniFileEntity> sections = value
                                .stream()
                                .map(item -> new IniFileWriter.IniFileEntity(item.getPropertyName(), item.getRealValue()))
                                .collect(Collectors.toList());
                        iniFileWriter.add(key, sections);
                    });
                    byteOutputStream = new ByteArrayOutputStream();
                    iniFileWriter.store(byteOutputStream);
                }
                if (null == byteOutputStream) {
                    continue;
                }
                //文件名
                String fileName = configApi.getFileName();
                //压缩文件
                tarWrite(tarOutputStream, byteOutputStream, fileName);
            } finally {
                IOUtils.closeQuietly(byteOutputStream);
            }
        }
    }

    private void tarWrite(TarArchiveOutputStream tarOutputStream, ByteArrayOutputStream byteOutputStream, String fileName) throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry(fileName);
        entry.setSize(byteOutputStream.toByteArray().length);
        tarOutputStream.putArchiveEntry(entry);
        tarOutputStream.write(byteOutputStream.toByteArray());
        tarOutputStream.closeArchiveEntry();
    }

    /**
     * 启动环境变量
     *
     * @param processId
     * @return
     */
    public Map<String, String> environmentConfig(Integer processId) {
        return adaptParamOptAssemble.assembleParamOpt(processId);
    }


    public String getProgramName(Integer processId) {
        return serviceProcessMapper.getProgramName(processId);
    }

    public String getProgramType(Integer processId) {
        return serviceProcessMapper.getProgramType(processId);
    }

    /**
     * 同步supervisor运行状态
     *
     * @return
     */
    public Boolean updateRunningStatus(List<ServiceProcess> serviceProcesses) {
        for (ServiceProcess serviceProcess : serviceProcesses) {
            serviceProcessMapper.updateRunningStatus(serviceProcess.getProcessId(), serviceProcess.getStateName());
            RunningStatusEnum statusEnum = RunningStatusEnum.getRunningStatusEnum(serviceProcess.getStateName());
            serviceProcess = serviceProcessMapper.getByProcessId(serviceProcess.getProcessId());
            if (null == statusEnum) {
                return true;
            }
            String instanceStatus;
            switch (statusEnum) {
                case RUNNING:
                    instanceStatus = InstanceStatusEnum.START.name();
                    break;
                case STARTING:
                    instanceStatus = InstanceStatusEnum.STARTING.name();
                    break;
                default:
                    instanceStatus = InstanceStatusEnum.STOP.name();
            }
            if (StringUtils.isNotEmpty(instanceStatus)) {
                serviceInstanceMapper.updateInstanceStatus(serviceProcess.getInstanceId(), instanceStatus);
            }
        }
        //处理该主机agent没有监控到的进程
        updateSupervisorNotExists(serviceProcesses);
        return true;
    }

    /**
     * 处理supervisor未监控到的进程
     *
     * @param serviceProcesses
     */
    public void updateSupervisorNotExists(List<ServiceProcess> serviceProcesses) {
        if (!CollectionUtils.isEmpty(serviceProcesses)) {
            List<ServiceInstance> services = processPropertyValueRefMapper.listRunningServiceByProcessId(serviceProcesses.get(0).getProcessId());
            if (CollectionUtil.isNotEmpty(services)) {
                List<Integer> serviceIds = services.stream().map(ServiceInstance::getServiceId).collect(Collectors.toList());
                List<ServiceProcess> allHostProcess = serviceProcessMapper.listLastSuccessProcessByServiceIdsAndHost(serviceIds, CollectionUtil.isNotEmpty(services) ? services.get(0).getHostId() : null);
                allHostProcess.forEach(process -> process.setProcessId(process.getServiceProcessId()));
                allHostProcess.removeAll(serviceProcesses);
                allHostProcess.forEach(remainProcess -> {
                    serviceProcessMapper.updateRunningStatus(remainProcess.getProcessId(), RunningStatusEnum.STOPPED.name());
                    serviceInstanceMapper.updateInstanceStatus(remainProcess.getInstanceId(), InstanceStatusEnum.STOP.name());
                });
            }
        }
    }

    /**
     * 服务进程配置
     *
     * @param processId
     * @param propertyName
     * @return
     */
    public Map<String, String> listConfig(Integer processId, String propertyName) {
        List<ProcessPropertyValueRef> propertyValueRefs = processPropertyValueRefMapper.listByProcessIdName(processId, propertyName);
        return propertyValueRefs.stream()
                .collect(Collectors.toMap(ProcessPropertyValueRef::getPropertyName, ProcessPropertyValueRef::getRealValue));
    }

    public Integer getProcessIdByProcessName(String processName) {
        return Integer.valueOf(processName.split("-")[0]);
    }


    /**
     * 环境变量
     *
     * @param processId
     * @param configApiScope
     * @return
     */
    public Map<String, List<ProcessPropertyValueRef>> processParamMap(Integer processId, String configApiScope) {
        List<ProcessPropertyValueRef> customEnvironment = processPropertyValueRefMapper.listByProcessIdAndApiKey(processId, configApiScope);
        return customEnvironment.stream()
                .collect(Collectors.groupingBy(ProcessPropertyValueRef::getConfApiKey));
    }

    public RunCommonConfig runCommonConfig(Integer processId) {
        return adaptRunCommonAssemble.assembleRunCommon(processId);
    }

    public String getEnvironmentName(Integer processId) {
        return serviceProcessMapper.getEnvironmentName(processId);
    }

    /**
     * 页面查询进程运行状态
     */
    public List<com.zto.zms.dal.model.ServiceProcess> getRunningStatus(List<Integer> processIds) {
        return serviceProcessMapper.getRunningStatus(processIds);
    }

    public ProcessStdLog tailProcessStdoutLog(Integer processId, int offset, int length) throws IOException {
        String programName = getProgramName(processId);
        ServiceProcess serviceProcess = serviceProcessMapper.getByProcessId(processId);
        String hostIp = zmsHostMapper.getHostIpByInstanceId(serviceProcess.getInstanceId());

        Result result = agentApiService.tailProcessStdoutLog(hostIp, programName, offset, length);
        Assert.that(result.isStatus(), "查询日志异常:" + result.getMessage());
        return JSON.parseObject(JSON.toJSONString(result.getResult()), ProcessStdLog.class);
    }

    public ProcessStdLog tailProcessStderrLog(Integer processId, int offset, int length) throws IOException {
        String programName = getProgramName(processId);
        ServiceProcess serviceProcess = serviceProcessMapper.getByProcessId(processId);
        String hostIp = zmsHostMapper.getHostIpByInstanceId(serviceProcess.getInstanceId());

        Result result = agentApiService.tailProcessStderrLog(hostIp, programName, offset, length);
        Assert.that(result.isStatus(), "查询日志异常:" + result.getMessage());
        return JSON.parseObject(JSON.toJSONString(result.getResult()), ProcessStdLog.class);
    }
}

