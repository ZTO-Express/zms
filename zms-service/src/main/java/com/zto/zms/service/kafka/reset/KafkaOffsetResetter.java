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
import org.apache.kafka.common.TopicPartition;

import java.util.Map;
import java.util.Set;

/**
 * 根据偏移量重置
 *
 * @author liangyong
 * @date 2019-06-21
 */
public class KafkaOffsetResetter extends AbstractKafkaResetter {


    private Long offset;

    public KafkaOffsetResetter(Long offset) {
        this.offset = offset;
    }

    @Override
    Map<TopicPartition, Long> findOffsets(String server, String group, Set<TopicPartition> keys) {
        Map<TopicPartition, Long> targetOffsets = Maps.newHashMap();
        Map<TopicPartition, Long> beginningOffsets = getConsumer(server, group).beginningOffsets(keys);
        Map<TopicPartition, Long> endOffsets = getConsumer(server, group).endOffsets(keys);
        for (Map.Entry<TopicPartition, Long> entry : endOffsets.entrySet()) {
            if (entry.getValue() < offset) {
                targetOffsets.put(entry.getKey(), entry.getValue());
            } else if (offset < beginningOffsets.get(entry.getKey())) {
                targetOffsets.put(entry.getKey(), beginningOffsets.get(entry.getKey()));
            } else {
                targetOffsets.put(entry.getKey(), offset);
            }
        }
        return targetOffsets;
    }
}

