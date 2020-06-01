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

/**
 * Created by liangyong on 2019/3/11.
 */
public class BrokerDataDTO {

    private String clusterName;
    private String brokerName;
    /**
     * Broker Id
     */
    private Long bid;
    private String addr;
    private String version;
    /**
     * Load inTps
     */
    private String inTps;
    /**
     * Load outTps
     */
    private String outTps;
    /**
     * Page Cache Lock Time Mills
     */
    private String pcWait;
    /**
     * 存储最早消息时间距现在多久
     * System.currentTimeMillis() - Long.valueOf(earliestMessageTimeStamp)
     */
    private String hour;
    /**
     * 分区的磁盘的使用率
     */
    private String space;

    private Long inTotalYest;

    private Long outTotalYest;

    private Long inTotalToday;

    private Long outTotalToday;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public Long getBid() {
        return bid;
    }

    public void setBid(Long bid) {
        this.bid = bid;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInTps() {
        return inTps;
    }

    public void setInTps(String inTps) {
        this.inTps = inTps;
    }

    public String getOutTps() {
        return outTps;
    }

    public void setOutTps(String outTps) {
        this.outTps = outTps;
    }

    public String getPcWait() {
        return pcWait;
    }

    public void setPcWait(String pcWait) {
        this.pcWait = pcWait;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }

    public Long getInTotalYest() {
        return inTotalYest;
    }

    public void setInTotalYest(Long inTotalYest) {
        this.inTotalYest = inTotalYest;
    }

    public Long getOutTotalYest() {
        return outTotalYest;
    }

    public void setOutTotalYest(Long outTotalYest) {
        this.outTotalYest = outTotalYest;
    }

    public Long getInTotalToday() {
        return inTotalToday;
    }

    public void setInTotalToday(Long inTotalToday) {
        this.inTotalToday = inTotalToday;
    }

    public Long getOutTotalToday() {
        return outTotalToday;
    }

    public void setOutTotalToday(Long outTotalToday) {
        this.outTotalToday = outTotalToday;
    }
}

