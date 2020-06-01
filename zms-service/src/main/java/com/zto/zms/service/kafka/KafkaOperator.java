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

package com.zto.zms.service.kafka;

import com.zto.zms.client.config.ConsumerConfig;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Properties;

public class KafkaOperator {

    KafkaConsumer kafkaConsumer;
    AdminClient adminClient;

    public KafkaConsumer getConsumer(String btServer, String consumerName) {
        if (kafkaConsumer == null) {
            Properties kafkaProperties = new Properties();

            kafkaProperties.putAll(ConsumerConfig.KAFKA.KAFKA_CONFIG);
            kafkaProperties.put("bootstrap.servers", btServer);
            kafkaProperties.put("group.id", consumerName);
            kafkaProperties.put("client.id", "zms_console_ops");
            kafkaConsumer = new KafkaConsumer<>(kafkaProperties);
        }
        return kafkaConsumer;
    }


    public AdminClient getClient(String server) {
        if (adminClient == null) {
            Properties properties = new Properties();
            properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, server);
            adminClient = AdminClient.create(properties);
        }
        return adminClient;
    }


    public void closeOperator() {
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
        }
        if (adminClient != null) {
            adminClient.close();
        }
    }

}

