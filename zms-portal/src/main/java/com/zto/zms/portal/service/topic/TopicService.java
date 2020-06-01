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

package com.zto.zms.portal.service.topic;


import com.zto.zms.portal.dto.topic.ZmsTopicConfigInfo;
import com.zto.zms.portal.dto.topic.ZmsTopicDeleteInfo;
import com.zto.zms.service.manager.MessageAdminManagerAdapt;
import com.zto.zms.service.process.MiddlewareProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springside.modules.utils.collection.MapUtil;

import java.util.List;
import java.util.Map;


/**
 * <p> Description: </p>
 *
 * @author lidawei
 * @version 1.0
 * @date 2020/4/22
 */
@Service
public class TopicService {

    public static final Logger logger = LoggerFactory.getLogger(TopicService.class);

    @Autowired
    private MessageAdminManagerAdapt messageAdminManagerAdapt;

    public void createTopic(ZmsTopicConfigInfo topicCreateOrUpdateRequest) {
        logger.info("create topic info {}", topicCreateOrUpdateRequest.toString());
        MiddlewareProcess middlewareProcess = messageAdminManagerAdapt.getOrCreateAdmin(topicCreateOrUpdateRequest.getClusterName());
        Map<String, Integer> topics = topicCreateOrUpdateRequest.getTopics();
        if (MapUtil.isNotEmpty(topics)) {
            Map<String, Integer> replications = topicCreateOrUpdateRequest.getReplications();
            for (Map.Entry<String, Integer> topic : topics.entrySet()) {
                int replication = replications.get(topic.getKey());
                middlewareProcess.createTopic(topic.getKey(), topic.getValue(), replication);
            }
        }
    }

    public void updateTopic(ZmsTopicConfigInfo topicCreateOrUpdateRequest) {
        MiddlewareProcess middlewareProcess = messageAdminManagerAdapt.getOrCreateAdmin(topicCreateOrUpdateRequest.getClusterName());
        Map<String, Integer> topics = topicCreateOrUpdateRequest.getTopics();
        if (MapUtil.isNotEmpty(topics)) {
            Map<String, Integer> replications = topicCreateOrUpdateRequest.getReplications();
            for (Map.Entry<String, Integer> topic : topics.entrySet()) {
                String topicName = topic.getKey();
                int partitions = topic.getValue();
                int replication = replications.get(topic.getKey());
                if (middlewareProcess.isTopicExist(topicName)) {
                    logger.info(" Update the topic {}  in cluster {}", topic.getKey(), topicCreateOrUpdateRequest.getClusterName());
                    middlewareProcess.updateTopic(topicName, partitions);
                } else {
                    logger.info(" Create the topic {}  in cluster {}", topic.getKey(), topicCreateOrUpdateRequest.getClusterName());
                    middlewareProcess.createTopic(topic.getKey(), topic.getValue(), replication);
                }
            }
        }
    }

    public void deleteTopic(ZmsTopicDeleteInfo deleteInfo) {
        String clusterName = deleteInfo.getClusterName();
        List<String> topics = deleteInfo.getTopics();
        MiddlewareProcess middlewareProcess = messageAdminManagerAdapt.getOrCreateAdmin(clusterName);
        for (String topic : topics) {
            logger.info("Delete the topic {} in the cluster {}", topic, clusterName);
            middlewareProcess.deleteTopic(topic);
        }
    }

}

