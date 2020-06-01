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

package com.zto.zms.collector.model;


import java.util.List;

public class ClusterRtTime {
    private String cluster;

    private int clusterNums;

    private List<RtTime> times;

    private long timestamp = System.currentTimeMillis();


    public int getClusterNums() {
        return clusterNums;
    }

    public void setClusterNums(int clusterNums) {
        this.clusterNums = clusterNums;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public List<RtTime> getTimes() {
        return times;
    }

    public void setTimes(List<RtTime> times) {
        this.times = times;
    }
}

