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

package com.zto.zms.collector.kafka;

import com.google.common.collect.Lists;
import com.zto.zms.collector.kafka.mbean.MBeanBufferPoolInfo;
import com.zto.zms.collector.kafka.mbean.MBeanMemoryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.Collections;
import java.util.List;


/**
 * <p> Description: </p>
 *
 * @author liangyong
 * @date  2018/8/14
 * @since 1.0.0
 */
@Service
public class MemoryMBeanServiceImpl implements MemoryMBeanService {
    private static Logger logger = LoggerFactory.getLogger(MemoryMBeanServiceImpl.class);

    @Override
    public List<MBeanBufferPoolInfo> getMemoryBufferPoolInfo(String uri) {

        MBeanServerConnection connection = null;
        List<MBeanBufferPoolInfo> bufferPoolInfos = Lists.newArrayList();

        try {
            connection = MBeanConnector.getJmxConnection(uri);
            List<BufferPoolMXBean> bufferPoolBeans = ManagementFactory.getPlatformMXBeans(connection,
                    BufferPoolMXBean.class);
            for (BufferPoolMXBean bufferPoolBean : bufferPoolBeans) {
                bufferPoolInfos.add(new MBeanBufferPoolInfo(bufferPoolBean));
            }
        } catch (IOException e) {
            logger.error("getMemoryBufferPoolInfo error", e);
            return  Collections.emptyList();
        }
        return bufferPoolInfos;
    }

    @Override
    public List<MBeanMemoryInfo> getJvmMemoryInfo(String uri) {
        List<MBeanMemoryInfo> memoryUsages = Lists.newArrayList();

        MBeanServerConnection connection = null;
        List<MemoryMXBean> garbageCollectorMXBeans = null;
        try {
            connection =MBeanConnector.getJmxConnection(uri);
            garbageCollectorMXBeans = ManagementFactory.getPlatformMXBeans(connection,
                    MemoryMXBean.class);
        } catch (IOException e) {
            logger.error("getJvmMemoryInfo error", e);
            return Collections.emptyList();
        }
        for (MemoryMXBean memoryBean : garbageCollectorMXBeans) {
            memoryUsages.add(new MBeanMemoryInfo("heapMemoryUsage", memoryBean.getHeapMemoryUsage()));
            memoryUsages.add(new MBeanMemoryInfo("nonHeapMemoryUsage", memoryBean.getNonHeapMemoryUsage()));
        }
        // 各代空间
        try {
            List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getPlatformMXBeans(connection,
                    MemoryPoolMXBean.class);
            for (MemoryPoolMXBean memoryPool : memoryPoolBeans) {
                String name = memoryPool.getName().trim();
                String lowerCaseName = name.toLowerCase();

                System.out.println(lowerCaseName);
                System.out.println(memoryPool.getUsage());
                memoryUsages.add(new MBeanMemoryInfo(lowerCaseName, memoryPool.getUsage()));
                memoryUsages.add(new MBeanMemoryInfo(lowerCaseName + "_peak", memoryPool.getPeakUsage()));
            }

        } catch (IOException e) {
            logger.error("getJvmMemoryInfo error", e);
            return Collections.emptyList();
        }
        return memoryUsages;
    }

}

