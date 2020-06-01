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

import com.zto.zms.common.RunCommonConfig;
import com.zto.zms.dal.domain.service.ServicePropertyValueRefDTO;
import com.zto.zms.dal.mapper.ServicePropertyValueRefMapper;
import com.zto.zms.dal.model.ProcessPropertyValueRef;
import com.zto.zms.portal.assemble.ConfigFileAssemble;
import com.zto.zms.portal.assemble.RunCommonConfigAssemble;
import com.zto.zms.portal.common.ServiceStartUserEnum;
import com.zto.zms.portal.service.serve.ProcessService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.zto.zms.portal.common.ServicePropertiesNameEnum.INFLUX_DB_PORT;

/**
 * influxDB自定义配置
 *
 * @author yuhao.zhang
 * @date 2020/3/13
 */
@Service("influxdb-instance")
public class InfluxdbConfigAssembleImpl implements ConfigFileAssemble, RunCommonConfigAssemble {

    private static final String INFLUX_DB_START = "influxdb/influxdb.sh";

    private static final String INFLUX_DB = "influxdb";

    @Autowired
    private ServicePropertyValueRefMapper servicePropertyValueRefMapper;
    @Autowired
    private ProcessService processService;

    /**
     * 生成配置文件
     */
    @Override
    public List<ProcessPropertyValueRef> assembleConfigFile(Integer serviceId, Integer instanceId, List<ProcessPropertyValueRef> propertyValueRefList) {
        //查询配置
        List<ServicePropertyValueRefDTO> propertyValueRefs = servicePropertyValueRefMapper.queryByInstanceId(instanceId);
        return propertyValueRefs.stream().map(item -> {
            item.setRealValue(item.getCurrentValue());
            if (StringUtils.isNotEmpty(item.getValueType())) {
                if ("STRING".equalsIgnoreCase(item.getValueType())) {
                    item.setRealValue("\"" + item.getRealValue() + "\"");
                }
                String checkGroupAndName = item.getPropertyGroup() + "&" + item.getPropertyName();
                String httpPort = INFLUX_DB_PORT.getGroup() + "&" + INFLUX_DB_PORT.getName();
                if (httpPort.equals(checkGroupAndName)) {
                    item.setRealValue(item.getRealValue().replaceFirst("\"", "\":"));
                }
            }
            ProcessPropertyValueRef propertyValueRef = new ProcessPropertyValueRef();
            BeanUtils.copyProperties(item, propertyValueRef);
            return propertyValueRef;
        }).collect(Collectors.toList());
    }

    @Override
    public RunCommonConfig assembleRunCommon(Integer processId) {
        String programName = processService.getProgramName(processId);
        RunCommonConfig commonConfig = new RunCommonConfig();
        commonConfig.setAutoRestart("unexpected");
        commonConfig.setCommand(INFLUX_DB_START);
        commonConfig.setExitCodes("0,2");
        commonConfig.setUser(ServiceStartUserEnum.INFLUXDB.getUser());
        commonConfig.setGroup(ServiceStartUserEnum.INFLUXDB.getGroup());
        commonConfig.setKillAsGroup(true);
        commonConfig.setStopAsGroup(true);
        commonConfig.setStartSecs(20);
        commonConfig.setStopWaitSecs(30);
        commonConfig.setProgramName(programName);
        commonConfig.setLibDir(INFLUX_DB);
        return commonConfig;
    }

}

