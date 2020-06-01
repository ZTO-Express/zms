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

package com.zto.zms.collector.report;

import com.google.common.collect.Maps;
import com.zto.zms.common.BrokerType;
import com.zto.zms.utils.ExecutorServiceUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Class: ExtensionReporter</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2019/12/3
 **/
@Component
public class ExtensionReporter implements DisposableBean {

    private Map<String, ScheduledExecutorService> serviceMap = Maps.newHashMap();

    @Autowired
    private Map<String, IReporter> reporterMap;

    private AtomicInteger threadNum = new AtomicInteger();

    public void start(){
        this.topicTpsTop10();
        this.consumerLatencyTop10();
    }


    public void topicTpsTop10() {
        for (BrokerType brokerType : BrokerType.values()) {
            String taskName = "TopicTopService-" + brokerType.getName() + threadNum.addAndGet(1);
            submit(taskName, () -> this.reporterMap.get(brokerType.getName()).topicTpsTop10(), 60,
                    60,
                    TimeUnit.SECONDS);
        }
    }

    public void consumerLatencyTop10() {
        for (BrokerType brokerType : BrokerType.values()) {
            String taskName = "ConsumerLatencyService-" + brokerType.getName() + threadNum.addAndGet(1);
            submit(taskName, () -> this.reporterMap.get(brokerType.getName()).consumerLatencyTop10(), 60,
                    60,
                    TimeUnit.SECONDS);
        }
    }


    public void submit(final String serviceName, Runnable runnable, long initialDelay,
                       long period, TimeUnit unit) {
        serviceMap.put(serviceName, Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, serviceName + threadNum.addAndGet(1))));
        serviceMap.get(serviceName).scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }


    @Override
    public void destroy() throws Exception {
        serviceMap.forEach((key, value) -> ExecutorServiceUtils.gracefullyShutdown(serviceMap.remove(key)));
    }


}

