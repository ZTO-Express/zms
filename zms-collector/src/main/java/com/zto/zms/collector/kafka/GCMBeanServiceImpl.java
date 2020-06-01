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

import com.google.common.collect.Maps;
import com.zto.zms.collector.kafka.mbean.MBeanGCInfo;
import com.zto.zms.collector.kafka.mbean.MetricsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.TabularDataSupport;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by liangyong on 2018/8/14.
 */
@Service
public class GCMBeanServiceImpl implements GCMBeanService {


    private static Logger logger = LoggerFactory.getLogger(GCMBeanServiceImpl.class);

    // lastGarbageCollectorCount && lastGarbageCollectorTime用于记录上次的收集到的数据。那本次减去上次，就是
    // 这段时间内发生GC次数和时间
    Map<String, EasyGCInfo> youngLastGcInfo = Maps.newHashMap();
    Map<String, EasyGCInfo> oldLastGcInfo = Maps.newHashMap();
    Map<String, Long> startTimes = Maps.newHashMap();

    @Override
    public MBeanGCInfo oldGenerationOfG1(String uri) {
        String mbean = "java.lang:type=GarbageCollector,name=G1 Old Generation";
        return getMbeanGCInfo(uri, mbean, "G1 Old Generation");
    }

    @Override
    public MBeanGCInfo youngGenerationOfG1(String uri) {
        String mbean = "java.lang:type=GarbageCollector,name=G1 Young Generation";
        return getMbeanGCInfo(uri, mbean, "G1 Young Generation");
    }

    private MBeanGCInfo getMbeanGCInfo(String uri, String mbean, String name) {

        MBeanGCInfo gcInfo = new MBeanGCInfo();
        gcInfo.setName(name);


        try {
            MBeanServerConnection mbeanSvrConn = MBeanConnector.getJmxConnection(uri);

            ObjectName objectName = new ObjectName(mbean);
            long collectionCount = (Long) mbeanSvrConn.getAttribute(objectName, MetricsConstants.GCBean.COLLECTION_COUNT);
            long collectionTime = (Long) mbeanSvrConn.getAttribute(objectName, MetricsConstants.GCBean.COLLECTION_TIME);

            Map<String, EasyGCInfo> workingMap;
            if ("G1 Young Generation".equals(name)) {
                workingMap = youngLastGcInfo;
            } else {
                workingMap = oldLastGcInfo;
                Object result = mbeanSvrConn.getAttribute(objectName, "LastGcInfo");
                CompositeDataSupport lastGCInfoData = (CompositeDataSupport) result;
                if (result == null) {
                    return gcInfo;
                }
                Long durationOfLastGc = (Long) lastGCInfoData.get("duration");
                Long usedMemoryBeforeGc = (Long) getMemoryMetric(lastGCInfoData, "memoryUsageBeforeGc", "G1 Old Gen", "used");
                Long usedMemoryAfterGc = (Long) getMemoryMetric(lastGCInfoData, "memoryUsageAfterGc", "G1 Old Gen", "used");
                Long maxMemory = (Long) getMemoryMetric(lastGCInfoData, "memoryUsageAfterGc", "G1 Old Gen", "max");
                Long startTime = (Long) lastGCInfoData.get("startTime");

                gcInfo.setLastGcDuration(durationOfLastGc);
                gcInfo.setUsedAfterGc(usedMemoryAfterGc);
                gcInfo.setUsedBeforeGc(usedMemoryBeforeGc);
                gcInfo.setMaxMemory(maxMemory);
                if (startTimes.containsKey(uri)) {
                    gcInfo.setStartTime(startTimes.get(uri) + startTime);
                } else {
                    List<RuntimeMXBean> platformMXBeans = ManagementFactory.getPlatformMXBeans(mbeanSvrConn,
                            RuntimeMXBean.class);
                    if (!CollectionUtils.isEmpty(platformMXBeans)) {
                        startTimes.put(uri, platformMXBeans.get(0).getStartTime());
                        gcInfo.setStartTime(platformMXBeans.get(0).getStartTime() + startTime);
                    }
                }

            }

            String key = uri + name;
            if (workingMap.get(key) != null) {
                gcInfo.setCollectionCount(collectionCount - workingMap.get(key).collectionCount);
                gcInfo.setCollectionTime(collectionTime - workingMap.get(key).collectionTime);

            } else {
                gcInfo.setCollectionCount(collectionCount);
                gcInfo.setCollectionTime(collectionTime);
            }
            workingMap.put(key, new EasyGCInfo(collectionTime, collectionCount));

            return gcInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gcInfo;
    }


    private static Object getMemoryMetric(CompositeDataSupport data, String diagnostic, String heapGeneration, String type) {
        Object memoryMetric = null;
        TabularDataSupport tabularData = (TabularDataSupport) data.get(diagnostic);
        Iterator tabularDataIterator = tabularData.entrySet().iterator();
        while (tabularDataIterator.hasNext()) {
            Map.Entry tabularDataEntry = (Map.Entry) tabularDataIterator.next();
            if (((List) tabularDataEntry.getKey()).get(0).equals(heapGeneration)) {
                CompositeDataSupport compositeData = (CompositeDataSupport) tabularDataEntry.getValue();
                Iterator compositeDataIterator = compositeData.values().iterator();
                while (compositeDataIterator.hasNext()) {
                    Object object = compositeDataIterator.next();
                    if (!(object instanceof String)) {
                        CompositeDataSupport memoryUsage = (CompositeDataSupport) object;
                        memoryMetric = memoryUsage.get(type);
                    }
                }
            }
        }
        return memoryMetric;
    }

    private class EasyGCInfo {
        long collectionTime;
        long collectionCount;


        public EasyGCInfo(long collectionTime, long collectionCount) {
            this.collectionTime = collectionTime;
            this.collectionCount = collectionCount;
        }


    }
}

