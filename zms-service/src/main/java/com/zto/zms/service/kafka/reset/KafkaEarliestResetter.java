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

import org.apache.kafka.common.TopicPartition;

import java.util.Map;
import java.util.Set;

public class KafkaEarliestResetter extends AbstractKafkaResetter {


    @Override
    Map<TopicPartition, Long> findOffsets(String server, String group, Set<TopicPartition> keys) {
        return getConsumer(server, group).beginningOffsets(keys);
    }
}

