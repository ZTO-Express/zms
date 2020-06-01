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

package com.zto.zms.collector.kafka.mbean;

/**
 * Created by liangyong on 2018/8/14.
 */
public class MBeanGCInfo {

    private long collectionCount;
    private long collectionTime;
    private long lastGcDuration;
    long usedBeforeGc;
    long usedAfterGc;
    long maxMemory;
    long startTime;
    private String name;

    public long getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(long collectionCount) {
        this.collectionCount = collectionCount;
    }

    public long getCollectionTime() {
        return collectionTime;
    }

    public void setCollectionTime(long collectionTime) {
        this.collectionTime = collectionTime;
    }

    public long getLastGcDuration() {
        return lastGcDuration;
    }

    public void setLastGcDuration(long lastGcDuration) {
        this.lastGcDuration = lastGcDuration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUsedBeforeGc() {
        return usedBeforeGc;
    }

    public void setUsedBeforeGc(long usedBeforeGc) {
        this.usedBeforeGc = usedBeforeGc;
    }

    public long getUsedAfterGc() {
        return usedAfterGc;
    }

    public void setUsedAfterGc(long usedAfterGc) {
        this.usedAfterGc = usedAfterGc;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}

