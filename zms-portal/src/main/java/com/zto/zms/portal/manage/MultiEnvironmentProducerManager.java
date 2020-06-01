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

package com.zto.zms.portal.manage;

import com.zto.zms.common.BrokerType;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.common.ZmsException;
import com.zto.zms.metadata.TopicMetadata;
import com.zto.zms.client.producer.*;
import com.zto.zms.service.manager.ZmsContextManager;
import com.zto.zms.service.router.ZkClientRouter;
import com.zto.zms.zookeeper.ZmsZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Description: 多环境消息生产者管理类</p>
 *
 * @author lidawei
 * @date 2020/2/7
 **/
@Service
public class MultiEnvironmentProducerManager {

    public static final Logger logger = LoggerFactory.getLogger(MultiEnvironmentProducerManager.class);

    private static Map<String, ZmsProducerProxy> topicProducers = new ConcurrentHashMap<>();

    @Autowired
    private ZkClientRouter zkClientRouter;

    /**
     * 获取当前环境消息生产者
     *
     * @param envId
     * @param topic
     * @return
     */
    public Producer getCurrentProducer(Integer envId, String topic) {
        ZmsContextManager.setEnv(envId);
        String cacheName = envId + "_" + topic + "_" + ZmsConst.DEFAULT_PRODUCER;
        if (topicProducers.get(cacheName) == null) {
            synchronized (ProducerFactory.class) {
                if (topicProducers.get(cacheName) == null) {
                    ZmsProducerProxy producer;
                    TopicMetadata metadata;
                    try {
                        metadata = zkClientRouter.currentZkClient().readTopicMetadata(topic);
                    } catch (Exception ex) {
                        logger.error("read topic {} metadata error", topic, ex);
                        throw ZmsException.METAINFO_EXCEPTION;
                    }
                    logger.info("Producer create: topic metadata is {}", metadata.toString());

                    if (BrokerType.ROCKETMQ.equals(metadata.getClusterMetadata().getBrokerType())) {
                        producer = new RocketmqProducerProxy(metadata, ZmsConst.DEFAULT_PRODUCER) {
                            @Override
                            public ZmsZkClient getZkInstance() {
                                return zkClientRouter.currentZkClient();
                            }
                        };
                    } else {
                        producer = new KafkaProducerProxy(metadata, ZmsConst.DEFAULT_PRODUCER) {
                            @Override
                            public ZmsZkClient getZkInstance() {
                                return zkClientRouter.currentZkClient();
                            }
                        };
                    }
                    topicProducers.putIfAbsent(cacheName, producer);
                }
            }
        }
        return topicProducers.get(cacheName);
    }
}

