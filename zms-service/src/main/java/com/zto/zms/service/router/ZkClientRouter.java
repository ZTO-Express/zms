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

package com.zto.zms.service.router;

import com.google.common.collect.Maps;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.metadata.ConsumerGroupMetadata;
import com.zto.zms.metadata.TopicMetadata;
import com.zto.zms.utils.Assert;
import com.zto.zms.service.manager.ZmsContextManager;
import com.zto.zms.service.selector.ZookeeperSelector;
import com.zto.zms.zookeeper.ZmsZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PreDestroy;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;


/**
 * <p> Description: zookeeper数据源</p>
 *
 * @author lidawei
 * @version 1.0
 * @date 2020/1/10
 */
@Component
public class ZkClientRouter {
    public static final Logger logger = LoggerFactory.getLogger(ZkClientRouter.class);

    @Autowired(required = false)
    private ZookeeperSelector zookeeperSelector;

    /**
     * 当前zk客户端
     *
     * @return
     */
    public ZmsZkClient currentZkClient() {
        ZmsZkClient zmsZkClient = zookeeperSelector.currentZkClient();
        Assert.that(null != zmsZkClient, "当前环境没有配置zookeeper数据源");
        return zmsZkClient;
    }

    public Map<String, ClusterMetadata> readClusters() {
        List<String> children = currentZkClient().getChildren(ZmsConst.ZK.CLUSTER_ZKPATH);

        if (CollectionUtils.isEmpty(children)) {
            return Maps.newHashMap();
        }

        Map<String, ClusterMetadata> map = Maps.newHashMap();
        children.forEach(child -> {
            ClusterMetadata clusterMetadata = currentZkClient().readClusterMetadata(child);
            map.put(child, clusterMetadata);
        });
        return map;
    }

    public ClusterMetadata readClusterMetadata(String clusterName) {
        return currentZkClient().readClusterMetadata(clusterName);
    }


    public void writeTopicInfo(TopicMetadata metadata) {
        try {
            currentZkClient().writeTopicMetadata(metadata);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("write envId:{0} zk  topic {1} error", ZmsContextManager.getEnv(), metadata.toString()), ex);
            throw new RuntimeException(ex);
        }

    }


    public void writerConsumerInfo(ConsumerGroupMetadata metadata) {
        try {
            currentZkClient().writeConsumerGroupMetadata(metadata);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("write env:{0} zk consumer {1} error", ZmsContextManager.getEnv(), metadata.toString()), ex);
            throw new RuntimeException(ex);
        }

    }


    public void writeClusterInfo(ClusterMetadata metadata) {
        try {
            currentZkClient().writeClusterMetadata(metadata);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("write env:{0} zk cluster {1} error", ZmsContextManager.getEnv(), metadata.toString()), ex);
            throw new RuntimeException(ex);

        }
    }

    public void writeClusterInfoNecessary(ClusterMetadata metadata) {
        if (existClusterInfo(metadata.getClusterName())) {
            ClusterMetadata clusterMetadata = readClusterMetadata(metadata.getClusterName());
            if (clusterMetadata.equals(metadata)) {
                return;
            }
        }
        try {
            currentZkClient().writeClusterMetadata(metadata);
            logger.info("Write zookeeper node:cluster:{}", metadata);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("write env:{0} zk cluster {1} error", ZmsContextManager.getEnv(), metadata.toString()), ex);
            throw new RuntimeException(ex);
        }
    }


    public void deleteClusterInfo(String clusterName) {
        try {
            currentZkClient().deleteCluster(clusterName);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("delete  env:{0} zk cluster {1} error :", ZmsContextManager.getEnv(), clusterName), ex);
            throw new RuntimeException(ex);
        }

    }

    public void deleteTopicInfo(String topicName) {
        try {
            currentZkClient().deleteTopic(topicName);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("delete envId:{0} topic {1}  error:", ZmsContextManager.getEnv(), topicName), ex);
            throw new RuntimeException(ex);
        }
    }

    public void deleteConsumerInfo(String consumerName) {
        try {
            currentZkClient().deleteConsumerGroup(consumerName);
        } catch (Exception ex) {
            logger.error(MessageFormat.format("delete envId:{0} consumer {1} error :", ZmsContextManager.getEnv(), consumerName), ex);
            throw new RuntimeException(ex);
        }

    }


    public ConsumerGroupMetadata readConsumerInfo(String consumerName) {
        try {
            return currentZkClient().readConsumerGroupMetadata(consumerName);
        } catch (Exception ex) {
            logger.error("read consumer error :" + consumerName, ex);
            throw new RuntimeException(ex);
        }
    }

    public TopicMetadata readTopicInfo(String topicName) {
        try {
            return currentZkClient().readTopicMetadata(topicName);
        } catch (Exception ex) {
            logger.error("read consumer error :" + topicName, ex);
            throw new RuntimeException(ex);
        }
    }

    public boolean existClusterInfo(String clusterName) {
        String clusterPath = String.join("/", ZmsConst.ZK.CLUSTER_ZKPATH, clusterName);
        return currentZkClient().exists(clusterPath);
    }

    @PreDestroy
    public void close() {
        if (null != zookeeperSelector) {
            zookeeperSelector.currentZkClient().close();
        }
    }
}

