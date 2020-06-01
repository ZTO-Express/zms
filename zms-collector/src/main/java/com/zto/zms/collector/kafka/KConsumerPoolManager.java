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

import com.zto.zms.metadata.ClusterMetadata;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

@Component
public final class KConsumerPoolManager {

    private static Logger logger = LoggerFactory.getLogger(KConsumerPoolManager.class);

    private static Map<String, Vector<KafkaConsumer>> kConsumerPools = new HashMap<>();

    private final static int K_CONSUMER_POOL_SIZE = 2;

    public static synchronized KafkaConsumer getKafkaConsumerClient(ClusterMetadata cluster) {
        Vector<KafkaConsumer> kConsumerPool = kConsumerPools.get(cluster.getClusterName());
        KafkaConsumer kConsumer = null;
        try {
            if (!CollectionUtils.isEmpty(kConsumerPool)) {
                kConsumer = kConsumerPool.get(0);
                kConsumerPool.remove(0);
            } else {
                kConsumerPool = new Vector<>();
                for (int i = 0; i < K_CONSUMER_POOL_SIZE; i++) {
                    try {
                        kConsumer = getKafkaConsumer(cluster);
                        kConsumerPool.add(kConsumer);
                    } catch (Exception ex) {

                    }
                }
                kConsumer = kConsumerPool.get(0);
                kConsumerPool.remove(0);
                kConsumerPools.put(cluster.getClusterName(),kConsumerPool);
            }
        } catch (Exception e) {
            logger.error("KConsumer init has error,msg is " + e.getMessage());
        }
        return kConsumer;
    }

    public static synchronized void release(ClusterMetadata cluster, KafkaConsumer kConsumer) {
        Vector<KafkaConsumer> kConsumerPool = kConsumerPools.get(cluster.getClusterName());
        if (null != kConsumerPool && kConsumerPool.size() < K_CONSUMER_POOL_SIZE) {
            kConsumerPool.add(kConsumer);
        }else{
            kConsumer.close();
        }
    }

    private static KafkaConsumer getKafkaConsumer(ClusterMetadata cluster){
        Properties props = new Properties();
        props.put("bootstrap.servers", cluster.getBootAddr());
        props.put("group.id", "kafka_consumer_metrics_group");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        return new KafkaConsumer(props);
    }

    @PreDestroy
    public void shutdownResource(){
        for (Vector<KafkaConsumer> vector : kConsumerPools.values()) {
            for(KafkaConsumer consumer : vector){
                consumer.close();
            }
        }
    }

}

