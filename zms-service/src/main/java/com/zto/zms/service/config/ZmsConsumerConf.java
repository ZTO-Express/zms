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

package com.zto.zms.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>Description: </p>
 *
 * @author xiaowenke@zto.cn
 * @version 1.0
 * @date 2019/9/16
 */
@ConfigurationProperties(prefix = "zms.queue")
@Configuration
public class ZmsConsumerConf {
    String consumerInfoConsumer;
    String producerInfoConsumer;
    String alertTopic;
    String alertConsumer;

    public String getConsumerInfoConsumer() {
        return consumerInfoConsumer;
    }

    public void setConsumerInfoConsumer(String consumerInfoConsumer) {
        this.consumerInfoConsumer = consumerInfoConsumer;
    }

    public String getProducerInfoConsumer() {
        return producerInfoConsumer;
    }

    public void setProducerInfoConsumer(String producerInfoConsumer) {
        this.producerInfoConsumer = producerInfoConsumer;
    }

    public String getAlertTopic() {
        return alertTopic;
    }

    public void setAlertTopic(String alertTopic) {
        this.alertTopic = alertTopic;
    }

    public String getAlertConsumer() {
        return alertConsumer;
    }

    public void setAlertConsumer(String alertConsumer) {
        this.alertConsumer = alertConsumer;
    }
}

