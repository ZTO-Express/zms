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

import java.lang.management.BufferPoolMXBean;

/**
 * Created by liangyong on 2018/8/14.
 */
public class MBeanBufferPoolInfo {

    private long count;
    private long capacity;
    private long used;

    private String name;

    public MBeanBufferPoolInfo(BufferPoolMXBean mbean) {
        this.capacity = mbean.getTotalCapacity();
        this.name = mbean.getName();
        this.count = mbean.getCount();
        this.used = mbean.getMemoryUsed();
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

