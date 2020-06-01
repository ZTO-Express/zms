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
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by liangyong on 2018/10/30.
 */
public interface KafkaService {


    /**
     * 获取消费组的订阅主题
     * @param cluster
     * @param group
     * @return
     */
    Set<String> getKafkaConsumerTopics(ClusterMetadata cluster, String group);

    /**
     * 主题分片
     * @param cluster
     * @param topic
     * @return
     */
    Map<String, List<Integer>> findTopicPartitions(ClusterMetadata cluster, Collection<String> topic);

    /**
     * 主题分区偏消息移量
     * @param cluster
     * @param topic
     * @param partition
     * @return
     */
    long getLogSize(ClusterMetadata cluster, String topic, int partition);

    /**
     * 消费组消息偏移量
     * @param cluster
     * @param consumerGroup
     * @return
     */
    Map<TopicPartition, Long> getKafkaOffset(ClusterMetadata cluster, String consumerGroup);

}

