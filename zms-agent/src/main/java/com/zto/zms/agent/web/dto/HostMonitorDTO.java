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

package com.zto.zms.agent.web.dto;

/**
 * @author yuhao.zhang
 * @date 2020/3/26
 */
public class HostMonitorDTO {
    /**
     * 核心数
     */
    private Integer cpuCores;
    /**
     * cpu个数
     */
    private Integer cpuNum;
    /**
     * 内存总量
     */
    private Long totalMem;
    /**
     * 内存使用量
     */
    private Long usedMem;
    /**
     * 操作系统
     */
    private String sysArch;
    /**
     * 操作系统名称
     */
    private String osVendorName;

    /**
     * 操作系统版本号
     */
    private String osVersion;

    /**
     * cpu利用率
     */
    private String cpuRate;

    public String getCpuRate() {
        return cpuRate;
    }

    public void setCpuRate(String cpuRate) {
        this.cpuRate = cpuRate;
    }

    public Integer getCpuCores() {
        return cpuCores;
    }

    public void setCpuCores(Integer cpuCores) {
        this.cpuCores = cpuCores;
    }

    public Integer getCpuNum() {
        return cpuNum;
    }

    public void setCpuNum(Integer cpuNum) {
        this.cpuNum = cpuNum;
    }

    public Long getTotalMem() {
        return totalMem;
    }

    public void setTotalMem(Long totalMem) {
        this.totalMem = totalMem;
    }

    public Long getUsedMem() {
        return usedMem;
    }

    public void setUsedMem(Long usedMem) {
        this.usedMem = usedMem;
    }

    public String getSysArch() {
        return sysArch;
    }

    public void setSysArch(String sysArch) {
        this.sysArch = sysArch;
    }

    public String getOsVendorName() {
        return osVendorName;
    }

    public void setOsVendorName(String osVendorName) {
        this.osVendorName = osVendorName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }
}

