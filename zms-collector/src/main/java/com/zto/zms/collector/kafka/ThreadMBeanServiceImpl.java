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


import com.zto.zms.collector.kafka.mbean.MBeanThreadInfo;
import com.zto.zms.collector.kafka.mbean.MetricsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;

 /**
  * <p> Description: </p>
  *
  * @author liangyong
  * @date 2018/8/14
  * @since 1.0.0
  */
@Service
public class ThreadMBeanServiceImpl implements ThreadMBeanService {


    private static Logger logger = LoggerFactory.getLogger(ThreadMBeanServiceImpl.class);

    @Override
    public MBeanThreadInfo collectThread(String uri) {
        String mbean = "java.lang:type=Threading";
        return getMbeanThreadInfo(uri, mbean);
    }

    private MBeanThreadInfo getMbeanThreadInfo(String uri, String mbean) {
        try {
            MBeanThreadInfo mBeanThreadInfo = new MBeanThreadInfo();
            MBeanServerConnection mbeanSvrConn = MBeanConnector.getJmxConnection(uri);
            ObjectName objectName = new ObjectName(mbean);
            int peakThreadCount = (Integer) (mbeanSvrConn.getAttribute(objectName, MetricsConstants.ThreadBean.PEAK_THREAD_COUNT));
            int threadCount = (Integer) mbeanSvrConn.getAttribute(objectName, MetricsConstants.ThreadBean.THREAD_COUNT);
            mBeanThreadInfo.setPeakThreadCount(peakThreadCount);
            mBeanThreadInfo.setThreadCount(threadCount);
            return mBeanThreadInfo;
        } catch (IOException e) {
            logger.error("getMbeanThreadInfo  error", e);
        } catch (Exception e) {
            logger.error("getMbeanThreadInfo error", e);
        }
        return null;
    }


}

