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

package com.zto.zms.portal.common;

import com.zto.zms.common.BrokerType;
import com.zto.zms.client.common.ConsumeFromWhere;
import com.zto.zms.common.ZmsType;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.metadata.ConsumerGroupMetadata;
import com.zto.zms.metadata.TopicMetadata;
import com.zto.zms.portal.dto.ClusterDTO;
import com.zto.zms.portal.dto.consumer.ConsumerDTO;
import com.zto.zms.service.router.ZkClientRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ZkRegister {

    public static final Logger logger = LoggerFactory.getLogger(ZkRegister.class);

    @Autowired
    private ZkClientRouter zkClientRouter;

    public void registerTopicToZk(String clusterName, String topicName, BrokerType brokerType) {
        TopicMetadata metadata = new TopicMetadata();

        ClusterMetadata clusterMetadata = new ClusterMetadata();
        clusterMetadata.setBrokerType(brokerType);
        clusterMetadata.setClusterName(clusterName);

        metadata.setClusterMetadata(clusterMetadata);
        metadata.setName(topicName);
        metadata.setType(ZmsType.TOPIC.getName());
        zkClientRouter.writeTopicInfo(metadata);
    }


    public void registerConsumerToZk(ConsumerDTO consumerDto, BrokerType brokerType) {
        ConsumerGroupMetadata metadata = new ConsumerGroupMetadata();
        metadata.setBindingTopic(consumerDto.getTopicName());
        metadata.setType(brokerType.getName());
        metadata.setName(consumerDto.getName());


        if (consumerDto.getConsumerFrom()) {
            metadata.setConsumeFrom(ConsumeFromWhere.EARLIEST.getName());

        } else {
            metadata.setConsumeFrom(ConsumeFromWhere.LATEST.getName());

        }
        metadata.setBroadcast(consumerDto.getBroadcast().toString());
        ClusterMetadata clusterMetadata = new ClusterMetadata();
        clusterMetadata.setClusterName(consumerDto.getClusterName());
        clusterMetadata.setBrokerType(brokerType);
        metadata.setClusterMetadata(clusterMetadata);
        zkClientRouter.writerConsumerInfo(metadata);
    }


    public void registerConsumerToZk(ConsumerGroupMetadata metadata) {
        zkClientRouter.writerConsumerInfo(metadata);
    }

    public void registerTopicToZk(TopicMetadata metadata) {
        zkClientRouter.writeTopicInfo(metadata);
    }

    public void registerClusterToZk(ClusterDTO dto) {
        ClusterMetadata metadata = new ClusterMetadata();
        metadata.setClusterName(dto.getName());
        metadata.setBrokerType(BrokerType.parse(dto.getClusterType()));
        metadata.setBootAddr(dto.getBootstraps());
        metadata.setServerIps(dto.getIps());
        zkClientRouter.writeClusterInfo(metadata);
    }


    public ConsumerGroupMetadata readConsumerInfo(String consumerName) {
        return zkClientRouter.readConsumerInfo(consumerName);
    }

    public TopicMetadata readTopicInfo(String topicName) {
        return zkClientRouter.readTopicInfo(topicName);
    }

    public boolean pathExist(String path) {
        return zkClientRouter.currentZkClient().exists(path);
    }


    public void deleteTopic(String topic) {
        zkClientRouter.deleteTopicInfo(topic);
    }

    public void deleteConsumer(String consumer) {
        zkClientRouter.deleteConsumerInfo(consumer);
    }


    public void deleteCluster(String cluster) {
        zkClientRouter.deleteClusterInfo(cluster);
    }
}

