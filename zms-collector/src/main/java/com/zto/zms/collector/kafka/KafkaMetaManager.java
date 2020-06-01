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
import com.zto.zms.collector.model.ConsumerGroupInfo;
import org.apache.kafka.common.Node;

import java.util.Collection;
import java.util.List;

public interface KafkaMetaManager {

    /**
     * Get kafka cluster nodes
     * @param cluster
     * @return
     */
    Collection<Node> getClusterNodes(ClusterMetadata cluster);

    /**
     * Update cache nodes info
     * @param cluster
     */
    void putClusterNodes(ClusterMetadata cluster);

    /**
     * Get kafka controller
     * @param cluster
     * @return
     */
    Node getClusterController(ClusterMetadata cluster);

    /**
     * Update cache controller info
     * @param cluster
     */
    void putClusterController(ClusterMetadata cluster);

    /**
     * List all cluster topics
     * @param cluster
     * @return
     */
    List<String> getClusterTopics(ClusterMetadata cluster);

    /**
     * Update cached topics
     * @param cluster
     */
    void putClusterTopics(ClusterMetadata cluster);

    /**
     * List all cluster consumers
     * @param cluster
     */
    List<ConsumerGroupInfo> getKafkaConsumers(ClusterMetadata cluster);

    /**
     * Update cached consumers
     * @param cluster
     */
    void putKafkaConsumers(ClusterMetadata cluster);




}

