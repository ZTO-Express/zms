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

package com.zto.zms.common;

import java.util.Date;
import java.util.Objects;

/**
 * 服务实例对应进程ID
 *
 * @author sun kai
 * @date 2020/02/22
 */
public class ServiceProcess {

    private Integer serviceId;

    private Integer instanceId;

    private Integer serviceProcessId;

    private Integer processId;

    private String stateName;

    private Date lastMonitorTime;

    public Date getLastMonitorTime() {
        return lastMonitorTime;
    }

    public void setLastMonitorTime(Date lastMonitorTime) {
        this.lastMonitorTime = lastMonitorTime;
    }

    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getServiceProcessId() {
        return serviceProcessId;
    }

    public void setServiceProcessId(Integer serviceProcessId) {
        this.serviceProcessId = serviceProcessId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServiceProcess that = (ServiceProcess) o;
        return processId.equals(that.processId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processId);
    }
}

