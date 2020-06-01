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

package com.zto.zms.portal.dto;


import com.zto.zms.portal.dto.serve.ZmsServiceVO;

import java.util.List;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/1/6
 **/
public class ZmsEnvironmentRespDTO {

    private Integer id;

    private String environmentName;

    private String environmentStatus;

    private Integer zkServiceId;

    private Integer influxdbServiceId;

    /**
     * 0 zk和influxdb已配置，1 zk配置,influxdb没有配置，2 zk没有配置
     */
    private Integer healthState;

    private int hostCount;

    private List<ZmsServiceVO> services;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getEnvironmentStatus() {
        return environmentStatus;
    }

    public void setEnvironmentStatus(String environmentStatus) {
        this.environmentStatus = environmentStatus;
    }

    public Integer getZkServiceId() {
        return zkServiceId;
    }

    public void setZkServiceId(Integer zkServiceId) {
        this.zkServiceId = zkServiceId;
    }

    public int getHostCount() {
        return hostCount;
    }

    public void setHostCount(int hostCount) {
        this.hostCount = hostCount;
    }

    public List<ZmsServiceVO> getServices() {
        return services;
    }

    public void setServices(List<ZmsServiceVO> services) {
        this.services = services;
    }

    public Integer getInfluxdbServiceId() {
        return influxdbServiceId;
    }

    public void setInfluxdbServiceId(Integer influxdbServiceId) {
        this.influxdbServiceId = influxdbServiceId;
    }

    public Integer getHealthState() {
        return healthState;
    }

    public void setHealthState(Integer healthState) {
        this.healthState = healthState;
    }
}

