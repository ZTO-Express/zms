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

package com.zto.zms.collector.rocketmq;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.service.mq.MqAdminManager;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class MqMetaManagerImpl implements  MqMetaManager{

    private static Logger logger = LoggerFactory.getLogger(MqMetaManagerImpl.class);
    private static final String RETRY_QUEUE = "%RETRY%";
    private static final String DLQ_QUEUE = "%DLQ%";
    private static final String TEMPLATE_QUEUE = "TBW012";
    private static final String BENCHMARK = "BenchmarkTest";

    @Autowired
    private MqAdminManager mqAdminManager;

    private final Map<String, ClusterInfo> clusterInfos = Maps.newConcurrentMap();

    private final Map<String,TopicList> clusterTopics = Maps.newConcurrentMap();


    @Override
    public ClusterInfo getClusterInfo(ClusterMetadata cluster) {
        return clusterInfos.get(cluster.getClusterName());
    }

    @Override
    public void putClusterInfo(ClusterMetadata cluster) {
        try {
            DefaultMQAdminExt mqAdmin = mqAdminManager.getMqAdmin(cluster.getClusterName());
            ClusterInfo clusterInfo = mqAdmin.examineBrokerClusterInfo();
            clusterInfos.put(cluster.getClusterName(),clusterInfo);
        } catch (InterruptedException | RemotingConnectException | RemotingTimeoutException |
                RemotingSendRequestException | MQBrokerException e) {
            logger.error("mq admin create failed", e);
        }
    }

    @Override
    public Set<String> getClusterTopics(ClusterMetadata cluster) {
         return filterUselessTopic(cluster.getClusterName(),clusterTopics.get(cluster.getClusterName()));
    }

    private boolean notMonitor(String topicName, String clusterName) {
        return topicName.equals(clusterName) || topicName.equals(TEMPLATE_QUEUE) || topicName.equals(BENCHMARK) || topicName.startsWith(RETRY_QUEUE) ;
    }

    private Set<String> filterUselessTopic(String clusterName, TopicList topicList) {
        Set<String> topics = Sets.newHashSet();
        for (String s : topicList.getTopicList()) {
            if (!notMonitor(s, clusterName)) {
                topics.add(s);
            }
        }
        return topics;
    }

    @Override
    public void putClusterTopics(ClusterMetadata cluster) {
        try {
            DefaultMQAdminExt mqAdminExt = mqAdminManager.getMqAdmin(cluster.getClusterName());
            TopicList topicList = mqAdminExt.fetchTopicsByCLuster(cluster.getClusterName());
            clusterTopics.put(cluster.getClusterName(),topicList);
        } catch (Exception e) {
            logger.error("mq put cluster topics failed", e);
        }
    }
}

