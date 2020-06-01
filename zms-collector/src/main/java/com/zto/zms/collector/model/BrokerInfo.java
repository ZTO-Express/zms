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

public class BrokerInfo {

    private long brokerId;
    private String brokerName;
    private String ip;
    private String runtime;
    private String earliestMsgTime;
    private int pageCacheLockTimeMillis;
    private long msgPutTotalTodayNow;
    private long msgGetTotalTodayNow;
    private double putTps;
    private double getTotalTps;
    private String putMessageDistributeTime;
    private Double bootTime;
    private int sendThreadPoolQueueSize;
    private int pullThreadPoolQueueSize;
    private int pullThreadPoolQueueCapacity;
    private int sendThreadPoolQueueCapacity;
    private int pullThreadPoolQueueHeadWaitTimeMills;


    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getEarliestMsgTime() {
        return earliestMsgTime;
    }

    public void setEarliestMsgTime(String earliestMsgTime) {
        this.earliestMsgTime = earliestMsgTime;
    }

    public int getPageCacheLockTimeMillis() {
        return pageCacheLockTimeMillis;
    }

    public void setPageCacheLockTimeMillis(int pageCacheLockTimeMillis) {
        this.pageCacheLockTimeMillis = pageCacheLockTimeMillis;
    }

    public long getMsgPutTotalTodayNow() {
        return msgPutTotalTodayNow;
    }

    public void setMsgPutTotalTodayNow(long msgPutTotalTodayNow) {
        this.msgPutTotalTodayNow = msgPutTotalTodayNow;
    }

    public long getMsgGetTotalTodayNow() {
        return msgGetTotalTodayNow;
    }

    public void setMsgGetTotalTodayNow(long msgGetTotalTodayNow) {
        this.msgGetTotalTodayNow = msgGetTotalTodayNow;
    }

    public double getPutTps() {
        return putTps;
    }

    public void setPutTps(double putTps) {
        this.putTps = putTps;
    }

    public double getGetTotalTps() {
        return getTotalTps;
    }

    public void setGetTotalTps(double getTotalTps) {
        this.getTotalTps = getTotalTps;
    }

    public String getPutMessageDistributeTime() {
        return putMessageDistributeTime;
    }

    public void setPutMessageDistributeTime(String putMessageDistributeTime) {
        this.putMessageDistributeTime = putMessageDistributeTime;
    }

    public Double getBootTime() {
        return bootTime;
    }

    public void setBootTime(Double bootTime) {
        this.bootTime = bootTime;
    }

    public int getSendThreadPoolQueueSize() {
        return sendThreadPoolQueueSize;
    }

    public void setSendThreadPoolQueueSize(int sendThreadPoolQueueSize) {
        this.sendThreadPoolQueueSize = sendThreadPoolQueueSize;
    }

    public int getPullThreadPoolQueueSize() {
        return pullThreadPoolQueueSize;
    }

    public void setPullThreadPoolQueueSize(int pullThreadPoolQueueSize) {
        this.pullThreadPoolQueueSize = pullThreadPoolQueueSize;
    }

    public int getPullThreadPoolQueueCapacity() {
        return pullThreadPoolQueueCapacity;
    }

    public void setPullThreadPoolQueueCapacity(int pullThreadPoolQueueCapacity) {
        this.pullThreadPoolQueueCapacity = pullThreadPoolQueueCapacity;
    }

    public int getSendThreadPoolQueueCapacity() {
        return sendThreadPoolQueueCapacity;
    }

    public void setSendThreadPoolQueueCapacity(int sendThreadPoolQueueCapacity) {
        this.sendThreadPoolQueueCapacity = sendThreadPoolQueueCapacity;
    }

    public int getPullThreadPoolQueueHeadWaitTimeMills() {
        return pullThreadPoolQueueHeadWaitTimeMills;
    }

    public void setPullThreadPoolQueueHeadWaitTimeMills(int pullThreadPoolQueueHeadWaitTimeMills) {
        this.pullThreadPoolQueueHeadWaitTimeMills = pullThreadPoolQueueHeadWaitTimeMills;
    }

    public long getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(long brokerId) {
        this.brokerId = brokerId;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}

