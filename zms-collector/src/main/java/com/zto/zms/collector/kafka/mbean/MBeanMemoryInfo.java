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

import java.lang.management.MemoryUsage;

public class MBeanMemoryInfo {

    public long commited;
    public long inited;
    public long used;
    public long max;
    public String name;


    public MBeanMemoryInfo(String name, MemoryUsage memoryUsage) {
        this.name = name;
        this.commited = memoryUsage.getCommitted();
        this.inited = memoryUsage.getInit();
        this.used = memoryUsage.getUsed();
        this.max = memoryUsage.getMax();
    }

    public long getCommited() {
        return commited;
    }

    public void setCommited(long commited) {
        this.commited = commited;
    }

    public long getInited() {
        return inited;
    }

    public void setInited(long inited) {
        this.inited = inited;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

