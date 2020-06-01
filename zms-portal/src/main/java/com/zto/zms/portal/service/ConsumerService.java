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

import com.zto.zms.dal.mapper.ConsumerMapper;
import com.zto.zms.dal.model.Consumer;
import com.zto.zms.service.manager.MessageAdminManagerAdapt;
import com.zto.zms.service.process.MiddlewareProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springside.modules.utils.collection.CollectionUtil;

import java.util.List;

/**
 * <p> Description: </p>
 *
 * @author superheizai
 * @version 1.0
 * @date 2017/9/13.
 */
@Service
public class ConsumerService {

    @Autowired
    private MessageAdminManagerAdapt messageAdminManagerAdapt;

    @Autowired
    private ConsumerMapper consumerMapper;

    public static final Logger logger = LoggerFactory.getLogger(ConsumerService.class);

    /**
     * 获取该环境下对应消费组名称和集群名称
     */
    public Consumer selectConsumerGroupByEnv(int envId, Long consumerId) {
        Consumer consumer = new Consumer();
        //获取对应集群和消费组和topic
        List<Consumer> consumers = consumerMapper.selectConsumerGroupByEnv(envId, consumerId);
        return CollectionUtil.isNotEmpty(consumers) ? consumers.get(0) : consumer;
    }

    public void createAndUpdateSubscriptionGroupConfig(String clusterName, String consumerGroup, Boolean broadcast, Boolean consumerFrom) {
        MiddlewareProcess middlewareProcess = messageAdminManagerAdapt.getOrCreateAdmin(clusterName);
        middlewareProcess.createConsumerGroup(consumerGroup, broadcast, consumerFrom);
    }

    public void deleteSubGroup(String clusterName, String consumerName) {
        MiddlewareProcess middlewareProcess = messageAdminManagerAdapt.getOrCreateAdmin(clusterName);
        middlewareProcess.deleteConsumerGroup(consumerName);
    }

    public void resetToEarliest(String cluster, String consumer, String topic) {
        MiddlewareProcess middlewareProcess = messageAdminManagerAdapt.getOrCreateAdmin(cluster);
        middlewareProcess.resetMessageToEarliest(consumer, topic);
    }

    public void resetToLatest(String cluster, String consumer, String topic) {
        MiddlewareProcess middlewareProcess = messageAdminManagerAdapt.getOrCreateAdmin(cluster);
        middlewareProcess.resetMessageToLatest(consumer, topic);
    }

    public void resetToTime(String cluster, String consumer, String topic, long time) {
        MiddlewareProcess middlewareProcess = messageAdminManagerAdapt.getOrCreateAdmin(cluster);
        middlewareProcess.resetMessageToTime(consumer, topic, time);
    }

    public void resetToOffset(String cluster, String consumer, String topic, long offset) {
        MiddlewareProcess middlewareProcess = messageAdminManagerAdapt.getOrCreateAdmin(cluster);
        middlewareProcess.resetMessageToOffset(consumer, topic, offset);
    }
}




