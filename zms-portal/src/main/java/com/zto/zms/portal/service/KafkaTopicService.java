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

package com.zto.zms.portal.service;

import com.zto.zms.common.ZmsException;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.dal.mapper.ConsumerMapper;
import com.zto.zms.dal.model.Consumer;
import com.zto.zms.portal.kafka.ConsumerGroupLoader;
import com.zto.zms.portal.kafka.KafkaConsumerSummary;
import com.zto.zms.portal.kafka.KafkaTopicSummary;
import com.zto.zms.portal.kafka.TopicLoader;
import com.zto.zms.service.router.ZkClientRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springside.modules.utils.collection.CollectionUtil;

import java.util.List;

@Service
public class KafkaTopicService {

    public static final Logger logger = LoggerFactory.getLogger(KafkaTopicService.class);
    @Autowired
    private ZkClientRouter zkClientRouter;
    @Autowired
    private ConsumerMapper consumerMapper;

    public KafkaConsumerSummary consumerGroupSummary(String cluster, String group) {
        ConsumerGroupLoader loader = new ConsumerGroupLoader();
        logger.info("consumerGroupSummary cluster {} topic {} consumer", cluster, group);

        ClusterMetadata clusterMetadata = zkClientRouter.currentZkClient().readClusterMetadata(cluster);
        return loader.load(clusterMetadata.getBootAddr(), group);
    }

    /**
     * kafka消费组详情
     */
    public KafkaConsumerSummary consumerGroupSummary(int envId, Long consumerId) {
        ConsumerGroupLoader loader = new ConsumerGroupLoader();
        //获取对应集群和消费组
        List<Consumer> consumers = consumerMapper.selectConsumerGroupByEnv(envId, consumerId);
        if (CollectionUtil.isEmpty(consumers)) {
            throw ZmsException.CONSUMER_NOT_EXISTS_EXCEPTION;
        }
        Consumer consumer = consumers.get(0);
        logger.info("consumerGroupSummary cluster {} topic {} consumer", consumer.getClusterName(), consumer.getName());
        ClusterMetadata clusterMetadata = zkClientRouter.currentZkClient().readClusterMetadata(consumer.getClusterName());
        return loader.load(clusterMetadata.getBootAddr(), consumer.getName());
    }


    public KafkaTopicSummary topicSummary(String clusterName, String topicName) throws Exception {
        TopicLoader loader = new TopicLoader();
        ClusterMetadata clusterMetadata = zkClientRouter.currentZkClient().readClusterMetadata(clusterName);
        return loader.load(clusterName, clusterMetadata.getBootAddr(), clusterMetadata.getServerIps(), topicName);
    }

}


