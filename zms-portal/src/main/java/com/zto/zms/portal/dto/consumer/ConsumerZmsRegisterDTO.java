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

package com.zto.zms.portal.dto.consumer;

/**
 * Created by liangyong on 2019/3/1.
 */
public class ConsumerZmsRegisterDTO {

    private String zmsIP;
    private String instanceName;
    private String zmsVersion;
    private String startUpTime;
    private int threadLocalRandomInt;

    public String getZmsIP() {
        return zmsIP;
    }

    public void setZmsIP(String zmsIP) {
        this.zmsIP = zmsIP;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getZmsVersion() {
        return zmsVersion;
    }

    public void setZmsVersion(String zmsVersion) {
        this.zmsVersion = zmsVersion;
    }

    public String getStartUpTime() {
        return startUpTime;
    }

    public void setStartUpTime(String startUpTime) {
        this.startUpTime = startUpTime;
    }

    public int getThreadLocalRandomInt() {
        return threadLocalRandomInt;
    }

    public void setThreadLocalRandomInt(int threadLocalRandomInt) {
        this.threadLocalRandomInt = threadLocalRandomInt;
    }
}

