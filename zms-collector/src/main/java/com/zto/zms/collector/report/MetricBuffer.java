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

/**
 * <p>Title: DataCarrier.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: www.zto.com</p>
 */
package com.zto.zms.collector.report;

import com.zto.zms.utils.ExecutorServiceUtils;
import com.zto.zms.service.domain.MetricsDo;
import com.zto.zms.service.influx.InfluxdbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MetricBuffer implements DisposableBean {
    private Logger logger = LoggerFactory.getLogger(MetricBuffer.class);
    @Autowired
    private InfluxdbClient influxdbClient;
    private static final Integer DEFAULT_QUEUE_SIZE = 10240;
    private final Map<String, ArrayBlockingQueue<List<MetricsDo>>> queueMap = new ConcurrentHashMap<>();
    private final Object locked = new Object();
    private ExecutorService executor = Executors.newFixedThreadPool(20, r -> new Thread(r, "MetricBuffer-Consumer-Thread"));

    public boolean put(final String topic, List<MetricsDo> data) {
        if (!queueMap.containsKey(topic)) {
            synchronized (locked) {
                if (!queueMap.containsKey(topic)) {
                    queueMap.put(topic, new ArrayBlockingQueue(DEFAULT_QUEUE_SIZE));
                    executor.submit((Runnable) () -> {
                        while (true) {
                            try {
                                consume(topic, queueMap.get(topic).take());
                            } catch (Exception e) {
                                logger.error(MessageFormat.format("consume Metrics error:{0}", topic), e);
                            }
                        }
                    });
                }
            }
        }
        return queueMap.get(topic).offer(data);
    }

    private void consume(String topic, List<MetricsDo> msg) {
        influxdbClient.writeData(msg, topic, System.currentTimeMillis());
    }


    @Override
    public void destroy() throws Exception {
        ExecutorServiceUtils.gracefullyShutdown(executor);
    }
}

