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

package com.zto.zms.service.process;


import com.zto.zms.client.Zms;

/**
 * <p> Description: Message middleware processing classes</p>
 * <p>If you need to send and consume messages {@link Zms}</p>
 *
 * @author lidawei
 * @date 2020/4/22
 * @since 1.0.0
 */
public interface MiddlewareProcess {


    /**
     * Create a topic in the cluster
     *
     * @param topic
     * @param partitions
     * @param replication
     */
    void createTopic(String topic, int partitions, Integer replication);

    /**
     * Whether the cluster exists or not
     *
     * @param topic
     * @return
     */
    boolean isTopicExist(String topic);

    /**
     * Update the theme
     *
     * @param topic
     * @param partitions
     */
    void updateTopic(String topic, int partitions);

    /**
     * Delete the topic
     *
     * @param topic
     */
    void deleteTopic(String topic);


    /**
     * Create a consumer in the cluster
     *
     * @param consumerGroup
     * @param broadcast
     * @param consumerFrom  true
     */
    void createConsumerGroup(String consumerGroup, Boolean broadcast, Boolean consumerFrom);

    /**
     * Update the topic
     *
     * @param consumerGroup
     * @param broadcast
     * @param consumerFrom
     */
    void updateConsumerGroup(String consumerGroup, Boolean broadcast, Boolean consumerFrom);

    /**
     * Delete the topic
     *
     * @param consumerGroup
     * @return
     */
    void deleteConsumerGroup(String consumerGroup);


    /**
     * Reset the consume group message offset to the earliest point
     *
     * @param consumerGroup
     * @param topic
     * @return
     */
    void resetMessageToEarliest(String consumerGroup, String topic);

    /**
     * Reset the consume group message offset to the latest point
     *
     * @param consumerGroup
     * @param topic
     * @return
     */
    void resetMessageToLatest(String consumerGroup, String topic);


    /**
     * Reset the consume group message offset to the specified offset
     *
     * @param consumerGroup
     * @param topic
     * @param offset
     * @return
     */
    void resetMessageToOffset(String consumerGroup, String topic, long offset);

    /**
     * Reset the consume group message offset to the specified time
     *
     * @param consumerGroup
     * @param topic
     * @param time
     * @return
     */
    void resetMessageToTime(String consumerGroup, String topic, long time);


    /**
     * The target cluster rebuilds the topic
     *
     * @param topic
     * @param partitions
     * @param replication
     */
    void recoverTopic(String topic, int partitions, Integer replication);

    /**
     * The target cluster rebuilds the topic
     *
     * @param consumerGroup
     * @param broadcast
     * @param consumerFrom
     */
    void recoverConsumerGroup(String consumerGroup, Boolean broadcast, Boolean consumerFrom);

}

