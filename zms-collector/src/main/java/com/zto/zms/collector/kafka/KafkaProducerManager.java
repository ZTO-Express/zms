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
import com.zto.zms.client.config.ProducerConfig;
import com.zto.zms.metadata.ClusterMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springside.modules.utils.net.NetUtil;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;


@Component
public class KafkaProducerManager {

    private static Logger logger = LoggerFactory.getLogger(KafkaProducerManager.class);

    private final Map<String, KafkaProducer> producers = Maps.newConcurrentMap();


    public KafkaProducer getKafkaProducer(ClusterMetadata cluster){
        KafkaProducer kafkaProducer = this.producers.get(cluster.getClusterName());
        if (null == kafkaProducer) {
            Properties kafkaProperties = new Properties();
            kafkaProperties.putAll(ProducerConfig.KAFKA.KAFKA_CONFIG);
            kafkaProperties.put("bootstrap.servers", cluster.getBootAddr());
            kafkaProperties.put("client.id", cluster.getClusterName() + "--" + NetUtil.getLocalHost() + "--" + ThreadLocalRandom.current().nextInt(100000));
            kafkaProducer = new KafkaProducer(kafkaProperties);
            producers.put(cluster.getClusterName(),kafkaProducer);
            logger.info("New KafkaProducer created.");
        }
        return kafkaProducer;
    }

    @PreDestroy
    public void shutdownResource() {
        for (Map.Entry<String, KafkaProducer> item : producers.entrySet()) {
            item.getValue().close();
        }
        producers.clear();
    }


}


