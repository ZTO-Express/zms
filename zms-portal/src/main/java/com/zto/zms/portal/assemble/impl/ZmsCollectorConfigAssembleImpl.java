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
import com.zto.zms.common.ZmsConst;
import com.zto.zms.common.ZmsServiceTypeEnum;
import com.zto.zms.portal.assemble.RunCommonConfigAssemble;
import com.zto.zms.portal.service.serve.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author yuhao.zhang
 * @description
 * @date 2020/3/11
 */
@Service("zms_collector-instance")
public class ZmsCollectorConfigAssembleImpl implements RunCommonConfigAssemble {

    private static final String ZMS_COLLECTOR_START = "collector/zms-collector-start.sh";
    private static final String ZMS_COLLECTOR = "collector";
    private static final String ENV_OPTS_KEY = "ZMSCOLLECTOR_ENV_OPTS";

    @Autowired
    private ProcessService processService;

    /**
     * supervisor配置拼装
     */
    @Override
    public RunCommonConfig assembleRunCommon(Integer processId) {
        String programName = processService.getProgramName(processId);
        RunCommonConfig commonConfig = new RunCommonConfig();
        Map<String, String> environmentConfigMap = processService.environmentConfig(processId);
        commonConfig.setAutoRestart("unexpected");
        commonConfig.setCommand(ZMS_COLLECTOR_START);
        String envParams = environmentConfigMap.get(ENV_OPTS_KEY);
        environmentConfigMap.put(ENV_OPTS_KEY,
                envParams.replace(ZmsServiceTypeEnum.ZOOKEEPER.name(), ZmsConst.ZK.ZMS_STARTUP_PARAM)
                        .replace(ZmsServiceTypeEnum.INFLUXDB.name(), ZmsConst.INFLUXDB));
        commonConfig.setEnvironment(environmentConfigMap);
        commonConfig.setExitCodes("0,2");
        commonConfig.setKillAsGroup(true);
        commonConfig.setStopAsGroup(true);
        commonConfig.setStartSecs(20);
        commonConfig.setStopWaitSecs(30);
        commonConfig.setProgramName(programName);
        commonConfig.setLibDir(ZMS_COLLECTOR);
        return commonConfig;
    }
}

