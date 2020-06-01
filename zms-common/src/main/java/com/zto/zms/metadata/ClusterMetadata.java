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

package com.zto.zms.metadata;

import com.zto.zms.common.BrokerType;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Created by superheizai on 2017/9/11.
 */
public class ClusterMetadata {
    private String clusterName;
    private String bootAddr;
    private BrokerType brokerType;
    private String serverIps;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getBootAddr() {
        return bootAddr;
    }

    public void setBootAddr(String bootAddr) {
        this.bootAddr = bootAddr;
    }

    public BrokerType getBrokerType() {
        return brokerType;
    }

    public void setBrokerType(BrokerType brokerType) {
        this.brokerType = brokerType;
    }


    public String getServerIps() {
        return serverIps;
    }

    public void setServerIps(String serverIps) {
        this.serverIps = serverIps;
    }

    @Override
    public String toString() {
        return "ClusterMetadata{" +
                "clusterName='" + clusterName + '\'' +
                ", bootAddr='" + bootAddr + '\'' +
                ", brokerType=" + brokerType +
                ", serverIps='" + serverIps + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ClusterMetadata)) {
            return false;
        }

        ClusterMetadata metadata = (ClusterMetadata) o;

        if (ObjectUtils.notEqual(clusterName, metadata.clusterName)) {
            return false;
        }

        if (ObjectUtils.notEqual(bootAddr, metadata.bootAddr)) {
            return false;
        }

        if (brokerType != metadata.brokerType) {
            return false;
        }

        return !ObjectUtils.notEqual(serverIps, metadata.serverIps);
    }

    @Override
    public int hashCode() {
        int result = clusterName != null ? clusterName.hashCode() : 0;
        result = 31 * result + (bootAddr != null ? bootAddr.hashCode() : 0);
        result = 31 * result + (brokerType != null ? brokerType.hashCode() : 0);
        result = 31 * result + (serverIps != null ? serverIps.hashCode() : 0);
        return result;
    }
}

