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

package com.zto.zms.collector.rocketmq;


import com.google.common.collect.Maps;
import com.zto.zms.metadata.ClusterMetadata;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;

@Component
public class MqProducerManager {

    private static Logger logger = LoggerFactory.getLogger(MqProducerManager.class);

    private final Map<String, DefaultMQProducer> producers = Maps.newConcurrentMap();

    public DefaultMQProducer getMqProducer(ClusterMetadata cluster){
        String clusterName = cluster.getClusterName();
        DefaultMQProducer producer = this.producers.get(clusterName);
        if (null == producer) {
            producer = new DefaultMQProducer("rt_collect_" + clusterName);
            producer.setNamesrvAddr(cluster.getBootAddr());
            producer.setVipChannelEnabled(false);
            //同个clientIP会复用同个MqClient，所以根据集群名字来划分
            producer.setClientIP("mq producer-client-id-" + clusterName);
            try {
                producer.start();
            } catch (MQClientException e) {
                logger.error("mq producer {} start failed", clusterName, e);
                throw new RuntimeException(e);
            }
            producers.put(clusterName,producer);
            logger.info("New MQProducer created.");
        }
        return producer;
    }

    @PreDestroy
    public void shutdownResource() {
        for (Map.Entry<String, DefaultMQProducer> item : producers.entrySet()) {
            item.getValue().shutdown();
        }
        producers.clear();
    }
}

