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

package com.zto.zms.service.kafka.reset;

import com.google.common.collect.Maps;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import org.springside.modules.utils.collection.MapUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KafkaTimeResetter extends AbstractKafkaResetter {


    private Long time;

    public KafkaTimeResetter(Long targetTime) {
        this.time = targetTime;
    }

    @Override
    Map<TopicPartition, Long> findOffsets(String cluster, String group, Set<TopicPartition> keys) {

        Map<TopicPartition, Long> timestamps = new HashMap<>();
        keys.forEach(topicPartition -> timestamps.put(topicPartition, time));
        Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes = getConsumer(cluster, group).offsetsForTimes(timestamps);
        Map<TopicPartition, Long> result = Maps.newHashMap();
        if (MapUtil.isNotEmpty(offsetsForTimes)) {
            for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : offsetsForTimes.entrySet()) {
                if (null != entry.getValue()) {
                    result.put(entry.getKey(), entry.getValue().offset());
                }
            }


        }
        return result;
    }
}

