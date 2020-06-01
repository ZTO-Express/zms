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

package com.zto.zms.service.manager;

import com.google.common.collect.Maps;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.service.router.ZkClientRouter;
import com.zto.zms.zookeeper.ZmsZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;

import static com.zto.zms.common.ZmsException.ILLEGAL_CLUSTER_TYPE;

/**
 * <p>Class: MessageAdminManagerAdapt</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/4/2
 **/
@Component
public class MessageAdminManagerAdapt {

    private Logger logger = LoggerFactory.getLogger(MessageAdminManagerAdapt.class);

    private final Map<String, AbstractMessageMiddlewareManager> middlewareManagerMap = Maps.newConcurrentMap();

    @Autowired
    private ZkClientRouter zkClientRouter;


    /**
     * 获取或新建消息管理类
     *
     * @param clusterName
     * @return
     */
    public AbstractMessageMiddlewareManager getOrCreateAdmin(String clusterName) {
        String generateKey = generateKey(clusterName);
        if (!middlewareManagerMap.containsKey(generateKey)) {
            synchronized (middlewareManagerMap) {
                if (!middlewareManagerMap.containsKey(generateKey)) {
                    AbstractMessageMiddlewareManager middlewareManager = createAdmin(clusterName);
                    middlewareManagerMap.put(generateKey, middlewareManager);
                }
            }
        }
        return middlewareManagerMap.get(generateKey);
    }

    /**
     * 新建消息管理类
     *
     * @param clusterName
     * @return
     */
    public AbstractMessageMiddlewareManager createAdmin(String clusterName) {
        ZmsZkClient zmsZkClient = zkClientRouter.currentZkClient();
        ClusterMetadata clusterMetadata = zkClientRouter.currentZkClient().readClusterMetadata(clusterName);
        AbstractMessageMiddlewareManager middlewareManager;
        Integer envId = ZmsContextManager.getEnv();
        switch (clusterMetadata.getBrokerType()) {
            case ROCKETMQ:
                middlewareManager = new RocketMqMiddlewareManager(zmsZkClient, clusterMetadata, () -> {
                    ZmsContextManager.setEnv(envId);
                    remove(clusterName);
                });
                break;
            case KAFKA:
                middlewareManager = new KafkaMiddlewareManager(zmsZkClient, clusterMetadata, () -> {
                    ZmsContextManager.setEnv(envId);
                    remove(clusterName);
                });
                break;
            default:
                throw ILLEGAL_CLUSTER_TYPE;
        }
        logger.info("Create MessageMiddlewareAdmin:{}", clusterMetadata);
        return middlewareManager;
    }


    /**
     * 重新加载消息管理类
     *
     * @param clusterName
     * @return
     */
    public AbstractMessageMiddlewareManager reload(String clusterName) {
        String generateKey = generateKey(clusterName);
        synchronized (middlewareManagerMap) {
            if (middlewareManagerMap.containsKey(generateKey)) {
                middlewareManagerMap.remove(generateKey).destroy();
            }
            AbstractMessageMiddlewareManager middlewareManager = createAdmin(clusterName);
            middlewareManagerMap.put(generateKey, middlewareManager);
        }
        return middlewareManagerMap.get(generateKey);
    }

    /**
     * 移除消息管理类
     *
     * @param clusterName
     */
    public void remove(String clusterName) {
        String generateKey = generateKey(clusterName);
        synchronized (middlewareManagerMap) {
            if (middlewareManagerMap.containsKey(generateKey)) {
                middlewareManagerMap.remove(generateKey).destroy();
            }
        }
    }


    private String generateKey(String clusterName) {
        Integer envId = ZmsContextManager.getEnv();
        return null == envId ? "&" + clusterName : envId + "&" + clusterName;
    }


    @PreDestroy
    public void destroy() {
        for (Map.Entry<String, AbstractMessageMiddlewareManager> item : middlewareManagerMap.entrySet()) {
            try {
                item.getValue().destroy();
            } catch (Exception e) {
                logger.warn("Destroy error", e);
            }
        }
        middlewareManagerMap.clear();
    }
}

