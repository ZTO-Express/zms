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
import com.zto.zms.utils.Assert;
import com.zto.zms.dal.mapper.ServiceProcessMapper;
import com.zto.zms.portal.assemble.RunCommonConfigAssemble;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>Class: AdaptRunCommonAssembleImpl</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/11
 **/
@Component
public class AdaptRunCommonAssembleImpl implements RunCommonConfigAssemble {
    @Autowired
    private Map<String, RunCommonConfigAssemble> runCommonAssembleMap;
    @Autowired
    private ServiceProcessMapper serviceProcessMapper;

    @Override
    public RunCommonConfig assembleRunCommon(Integer processId) {
        String programType = serviceProcessMapper.getProgramType(processId);
        Assert.that(runCommonAssembleMap.containsKey(programType), "服务启动信息不存在");
        return runCommonAssembleMap.get(programType).assembleRunCommon(processId);
    }
}

