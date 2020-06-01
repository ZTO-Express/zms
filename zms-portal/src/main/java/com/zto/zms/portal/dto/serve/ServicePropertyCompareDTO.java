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

package com.zto.zms.portal.dto.serve;

import java.util.List;

/**
 * 配置对比封装返回
 *
 * @author sun kai
 * @date 2020/03/04
 */
public class ServicePropertyCompareDTO {

    /**
     * 实例ID
     */
    private Integer instanceId;

    /**
     * 实例名称
     */
    private String instanceName;

    /**
     * 实例修改的文件
     */
    private List<ServicePropertyCompareFileDTO> fileList;

    public Integer getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public List<ServicePropertyCompareFileDTO> getFileList() {
        return fileList;
    }

    public void setFileList(List<ServicePropertyCompareFileDTO> fileList) {
        this.fileList = fileList;
    }
}

