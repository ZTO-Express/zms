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

package com.zto.zms.service.kafka;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by superheizai on 2017/9/11.
 */
@Component
public class KafkaAdminClient {

    private static final Logger logger = LoggerFactory.getLogger(KafkaAdminClient.class);

    @Autowired
    private KafkaAdminManage kafkaAdminManage;

    /**
     * Create topic
     */
    public void createInitTopic(String clusterName) {
        AdminClient client = kafkaAdminManage.getKafkaAdmin(clusterName);
        DescribeClusterResult ret = client.describeCluster();

        KafkaFuture<Collection<Node>> nodes = ret.nodes();
        try {
            Set<String> allTopics = client.listTopics().names().get(60, TimeUnit.SECONDS);
            List<NewTopic> topics = nodes.get().stream()
                    .map((node) -> {
                        Map<Integer, List<Integer>> assignment = Maps.newHashMap();
                        assignment.put(0, Lists.newArrayList(node.id()));
                        String topicName = buildTopicName(node, clusterName);
                        return new NewTopic(topicName, assignment);
                    }).filter((newTopic) -> !allTopics.contains(newTopic.name()))
                    .collect(Collectors.toList());
            client.createTopics(topics);
        } catch (Exception e) {
            logger.error("create kafka cluster init topic error", e);
        }

    }

    public static String buildTopicName(Node node, String clusterName) {
        String topic;
        if (StringUtils.isNotBlank(node.host())) {
            String[] ips = node.host().split("\\.");
            if (ips.length > 3) {
                topic = clusterName + "_" + ips[2] + "_" + ips[3];
            } else {
                topic = clusterName + "broker-" + node.id();
            }
        } else {
            topic = clusterName + "broker-" + node.id();
        }
        return topic;
    }

}

