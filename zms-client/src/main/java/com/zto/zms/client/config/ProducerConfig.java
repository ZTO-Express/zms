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

package com.zto.zms.client.config;

import java.util.Properties;

/**
 * Created by superheizai on 2017/7/31.
 */
public class ProducerConfig {

    public static final String TIMEOUT = "timeout";
    public static final String RETRIES = "retries";

    public static final class KAFKA {
        public static final Properties KAFKA_CONFIG = new Properties();

        static {
            KAFKA_CONFIG.put("acks", "all");
            KAFKA_CONFIG.put(RETRIES, 0);
            KAFKA_CONFIG.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            KAFKA_CONFIG.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");
            KAFKA_CONFIG.put("max.in.flight.requests.per.connection", "1");
        }
    }

}


