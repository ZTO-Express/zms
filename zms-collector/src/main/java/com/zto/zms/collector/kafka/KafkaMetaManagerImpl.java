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

import com.google.common.collect.Maps;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.collector.model.ConsumerGroupInfo;
import com.zto.zms.service.kafka.KafkaAdminManage;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KafkaMetaManagerImpl implements KafkaMetaManager {

    private static Logger logger = LoggerFactory.getLogger(KafkaMetaManagerImpl.class);

    private final Map<String, Collection<Node>> clusterNodes = Maps.newConcurrentMap();

    private final Map<String, Node> clusterControllers = Maps.newConcurrentMap();

    private final Map<String,List<String>> topics = Maps.newConcurrentMap();

    private final Map<String,List<ConsumerGroupInfo>> consumers = Maps.newConcurrentMap();

    @Autowired
    KafkaAdminManage kafkaAdminManage;

    @Override
    public Collection<Node> getClusterNodes(ClusterMetadata cluster) {
        return clusterNodes.get(cluster.getClusterName());
    }

    @Override
    public void putClusterNodes(ClusterMetadata cluster){
        try {
            DescribeClusterResult clusterResult = kafkaAdminManage.getKafkaAdmin(cluster.getClusterName()).describeCluster();
            KafkaFuture<Collection<Node>> kafkaFuture = clusterResult.nodes();
            Collection<Node> nodes = kafkaFuture.get();
            clusterNodes.put(cluster.getClusterName(), nodes);
            logger.info(cluster.getClusterName() +" New clusterNodes created.");
        } catch (Exception e) {
            logger.error(cluster.getClusterName() +" New clusterNodes create failed.", e);
        }
    }

    @Override
    public Node getClusterController(ClusterMetadata cluster) {
        return clusterControllers.get(cluster.getClusterName());
    }

    @Override
    public void putClusterController(ClusterMetadata cluster) {
        try {
            DescribeClusterResult describeClusterResult = kafkaAdminManage.getKafkaAdmin(cluster.getClusterName()).describeCluster();
            Node controller = describeClusterResult.controller().get();
            clusterControllers.put(cluster.getClusterName(), controller);
            logger.info(cluster.getClusterName() +" New cluster Controller created.");
        }catch (Exception e) {
            logger.error(cluster.getClusterName() +" New cluster Controller create failed.", e);
        }
    }

    @Override
    public List<String> getClusterTopics(ClusterMetadata cluster) {
        return topics.get(cluster.getClusterName());
    }

    @Override
    public void putClusterTopics(ClusterMetadata cluster) {
        try {
            List<String> topicLst = new ArrayList<>();
            ListTopicsResult listTopicsResult = kafkaAdminManage.getKafkaAdmin(cluster.getClusterName()).listTopics();
            for (TopicListing topicListing : listTopicsResult.listings().get()) {
                topicLst.add(topicListing.name());
            }
            topics.put(cluster.getClusterName(),topicLst);
        } catch (Exception e) {
            logger.error(cluster.getClusterName() +" list topics failed.", e);
        }
    }

    @Override
    public List<ConsumerGroupInfo> getKafkaConsumers(ClusterMetadata cluster) {
      return consumers.get(cluster.getClusterName());
    }

    @Override
    public void putKafkaConsumers(ClusterMetadata cluster) {
        try {
            AdminClient adminClient = kafkaAdminManage.getKafkaAdmin(cluster.getClusterName());
            KafkaFuture<Collection<ConsumerGroupListing>> groupsFuture = adminClient.listConsumerGroups().all();
            Collection<ConsumerGroupListing> groupGroups = groupsFuture.get();

            List<ConsumerGroupInfo> consumerGroups = groupGroups.stream().map(item->{
                ConsumerGroupInfo consumerGroupInfo = new ConsumerGroupInfo();
                consumerGroupInfo.setGroup(item.groupId());
                return consumerGroupInfo;
            }).collect(Collectors.toList());

            consumers.put(cluster.getClusterName(),consumerGroups);
        } catch (Exception e) {
            logger.error(cluster.getClusterName() + " list consumers failed.", e);
        }
    }


}

