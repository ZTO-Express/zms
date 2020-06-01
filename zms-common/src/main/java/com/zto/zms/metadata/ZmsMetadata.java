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

import com.google.common.base.Objects;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.common.ZmsType;
import org.apache.commons.lang3.StringUtils;
import org.springside.modules.utils.net.NetUtil;



 /**
  * <p> Description: </p>
  *
  * @author superheizai
  * @date 2017/7/26
  * @since 1.0.0
  */
public class ZmsMetadata {
    private String type;

    private String name;

    private ClusterMetadata clusterMetadata;

    private String domain;

    private String gatedIps;

    private ClusterMetadata gatedCluster;

    private String statisticsLogger;

    public String getZmsClusterPath() {
        return String.join("/", ZmsConst.ZK.CLUSTER_ZKPATH, clusterMetadata.getClusterName());
    }

    public String getZmsPath() {
        if (isTopic()) {
            return String.join("/", ZmsConst.ZK.TOPIC_ZKPATH, name);
        } else {
            return String.join("/", ZmsConst.ZK.CONSUMERGROUP_ZKPATH, name);

        }
    }


    public boolean isTopic() {
        return ZmsType.TOPIC.getName().equalsIgnoreCase(type);
    }


    public String getStatisticsLogger() {
        return statisticsLogger;
    }

    public void setStatisticsLogger(String statisticsLogger) {
        this.statisticsLogger = statisticsLogger;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClusterMetadata getClusterMetadata() {
        return clusterMetadata;
    }

    public void setClusterMetadata(ClusterMetadata clusterMetadata) {
        this.clusterMetadata = clusterMetadata;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }


    public String getGatedIps() {
        return gatedIps;
    }

    public void setGatedIps(String gatedIps) {
        this.gatedIps = gatedIps;
    }

    public ClusterMetadata getGatedCluster() {
        return gatedCluster;
    }

    public void setGatedCluster(ClusterMetadata gatedCluster) {
        this.gatedCluster = gatedCluster;
    }

    public boolean isGatedLaunch() {
        return StringUtils.isNotBlank(gatedIps) && gatedIps.contains(NetUtil.getLocalHost());
    }

    @Override
    public String toString() {
        return "ZmsMetadata{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", clusterMetadata=" + clusterMetadata.toString() +
                ", domain='" + domain + '\'' +
                ", gatedCluster='" + (gatedCluster != null ? gatedCluster.toString() : "") + '\'' +
                ", gatedIps='" + gatedIps + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ZmsMetadata that = (ZmsMetadata) o;

        return Objects.equal(this.type, that.type) &&
                Objects.equal(this.name, that.name) &&
                Objects.equal(this.clusterMetadata, that.clusterMetadata) &&
                Objects.equal(this.domain, that.domain) &&
                Objects.equal(this.gatedIps, that.gatedIps) &&
                Objects.equal(this.gatedCluster, that.gatedCluster);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, name, clusterMetadata, domain, gatedIps, gatedCluster);
    }
}

