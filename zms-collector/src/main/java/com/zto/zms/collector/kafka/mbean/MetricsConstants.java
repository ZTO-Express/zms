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
public class MetricsConstants {
    public interface MBean {
        String COUNT = "Count";
        String FIFTEEN_MINUTE_RATE = "FifteenMinuteRate";
        String FIVE_MINUTE_RATE = "FiveMinuteRate";
        String MEAN_RATE = "MeanRate";
        String ONE_MINUTE_RATE = "OneMinuteRate";
        String VALUE = "Value";
        String MIN = "Min";
        String MAX = "Max";
        String MEAN = "Mean";
        String STD_DEV = "StdDev";
        String PERCENTILE_50TH = "50thPercentile";
        String PERCENTILE_75TH = "75thPercentile";
        String PERCENTILE_95TH = "95thPercentile";
        String PERCENTILE_98TH = "98thPercentile";
        String PERCENTILE_99TH = "99thPercentile";
        String PERCENTILE_999TH = "999thPercentile";
    }

    public interface GCBean{
        String COLLECTION_COUNT = "CollectionCount";
        String COLLECTION_TIME = "CollectionTime";
    }

    public interface OSBean{
        String OPEN_FILE_DESCRIPTOR_COUNT = "OpenFileDescriptorCount";
        String MAX_FILE_DESCRIPTOR_COUNT = "MaxFileDescriptorCount";
        String FREE_PHYSICAL_MEMORY_SIZE = "FreePhysicalMemorySize";
        String PROCESS_CPU_LOAD = "ProcessCpuLoad";
        String SYSTEM_CPU_LOAD = "SystemCpuLoad";
        String SYSTEM_LOAD_AVERAGE = "SystemLoadAverage";
    }

    public interface ThreadBean{
        String THREAD_COUNT = "ThreadCount";
        String PEAK_THREAD_COUNT = "PeakThreadCount";
    }


}

