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


import com.zto.zms.collector.kafka.mbean.MBeanOSInfo;
import com.zto.zms.collector.kafka.mbean.MetricsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

 /**
  * <p> Description: </p>
  * 
  * @author liangyong
  * @date 2018/8/14
  * @since 1.0.0
  */
@Service
public class OSMBeanServiceImpl implements OSMBeanService {

    private static Logger logger = LoggerFactory.getLogger(OSMBeanServiceImpl.class);

    @Override
    public MBeanOSInfo operatingSystem(String uri) {
        String mbean = "java.lang:type=OperatingSystem";
        return getMbeanOSInfo(uri, mbean);
    }

    private MBeanOSInfo getMbeanOSInfo(String uri, String mbean) {
        try {
            MBeanOSInfo mBeanOSInfo = new MBeanOSInfo();
            MBeanServerConnection mbeanSvrConn = MBeanConnector.getJmxConnection(uri);
            long freePhysicalMemorySize = (Long) mbeanSvrConn.getAttribute(new ObjectName(mbean), MetricsConstants.OSBean.FREE_PHYSICAL_MEMORY_SIZE);
            long maxFileDescriptorCount = (Long) mbeanSvrConn.getAttribute(new ObjectName(mbean), MetricsConstants.OSBean.MAX_FILE_DESCRIPTOR_COUNT);
            long openFileDescriptorCount = (Long) mbeanSvrConn.getAttribute(new ObjectName(mbean), MetricsConstants.OSBean.OPEN_FILE_DESCRIPTOR_COUNT);
            double processCpuLoad = (Double) mbeanSvrConn.getAttribute(new ObjectName(mbean), MetricsConstants.OSBean.PROCESS_CPU_LOAD);
            double systemCpuLoad = (Double) mbeanSvrConn.getAttribute(new ObjectName(mbean), MetricsConstants.OSBean.SYSTEM_CPU_LOAD);
            double systemLoadAverage = (Double) mbeanSvrConn.getAttribute(new ObjectName(mbean), MetricsConstants.OSBean.SYSTEM_LOAD_AVERAGE);
            mBeanOSInfo.setFreePhysicalMemorySize(freePhysicalMemorySize);
            mBeanOSInfo.setMaxFileDescriptorCount(maxFileDescriptorCount);
            mBeanOSInfo.setOpenFileDescriptorCount(openFileDescriptorCount);
            mBeanOSInfo.setProcessCpuLoad(processCpuLoad);
            mBeanOSInfo.setSystemCpuLoad(systemCpuLoad);
            mBeanOSInfo.setSystemLoadAverage(systemLoadAverage);
            return mBeanOSInfo;
        } catch (Exception e) {
            logger.error("getMbeanOSInfo error", e);
        }
        return null;
    }

}

