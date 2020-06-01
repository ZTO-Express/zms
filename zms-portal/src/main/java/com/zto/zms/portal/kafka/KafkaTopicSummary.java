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

package com.zto.zms.portal.kafka;


import com.zto.zms.portal.dto.topic.TopicSummaryDTO;
import org.apache.rocketmq.common.Pair;

import java.util.List;

public class KafkaTopicSummary extends TopicSummaryDTO {

    private List<KafkaTopicInfo> topicInfos;
    private List<Pair<String, String>> topicConfigs;
    private List<Name> consumers;

    public List<KafkaTopicInfo> getTopicInfos() {
        return topicInfos;
    }

    public void setTopicInfos(List<KafkaTopicInfo> topicInfos) {
        this.topicInfos = topicInfos;
    }

    public List<Pair<String, String>> getTopicConfigs() {
        return topicConfigs;
    }

    public void setTopicConfigs(List<Pair<String, String>> topicConfigs) {
        this.topicConfigs = topicConfigs;
    }

    public List<Name> getConsumers() {
        return consumers;
    }

    public void setConsumers(List<Name> consumers) {
        this.consumers = consumers;
    }
}

