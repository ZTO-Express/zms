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


import com.alibaba.fastjson.JSON;
import com.zto.zms.dal.domain.service.ZmsClusterServiceDTO;
import com.zto.zms.dal.mapper.ZmsServiceMapper;
import com.zto.zms.portal.dto.MigrationDTO;
import com.zto.zms.service.domain.topic.TopicLog;
import com.zto.zms.service.manager.MessageAdminManagerAdapt;
import com.zto.zms.service.manager.ZmsContextManager;
import com.zto.zms.service.process.MiddlewareProcess;
import org.apache.rocketmq.common.MixAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecoverService {

    private Logger logger = LoggerFactory.getLogger(RecoverService.class);

    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private MessageAdminManagerAdapt messageAdminManagerAdapt;

    /**
     * 集群重建
     */
    public void clusterRecover(MigrationDTO dto) {
        checkCluster(dto);
        // 设置ZK环境
        ZmsContextManager.setEnv(dto.getEnvId());
        logger.info("begin to recover src cluster {} to target cluster {}", dto.getSrcCluster(), dto.getTargetCluster());
        List<TopicLog> topicLogs = loadTopics(dto.getSrcCluster());
        if (!CollectionUtils.isEmpty(topicLogs)) {
            logger.info("begin to create topics");
            MiddlewareProcess middlewareProcess = messageAdminManagerAdapt.getOrCreateAdmin(dto.getTargetCluster());
            for (TopicLog topicLog : topicLogs) {
                //todo replication
                middlewareProcess.recoverTopic(topicLog.getTopicName(), topicLog.getPartitions(), 3);
            }
            List<String> consumers = loadConsumers(dto.getSrcCluster());
            for (String consumer : consumers) {
                middlewareProcess.recoverConsumerGroup(consumer, false, true);
            }
        }
    }

    /**
     * 校验原集群和目标集群
     */
    private void checkCluster(MigrationDTO dto) {
        ZmsClusterServiceDTO targetCluster = zmsServiceMapper.getServiceAndEnvById(dto.getTargetClusterId());
        ZmsClusterServiceDTO srcCluster = zmsServiceMapper.getServiceAndEnvById(dto.getSrcClusterId());
        if (targetCluster == null) {
            throw new RuntimeException("targetCluster is not exist, clusterId=" + dto.getTargetClusterId());
        }
        if (srcCluster == null) {
            throw new RuntimeException("srcCluster is not exist, clusterId=" + dto.getTargetClusterId());
        }
        if (!targetCluster.getEnvironmentId().equals(srcCluster.getEnvironmentId())) {
            throw new RuntimeException("srcCluster and targetCluster's env must be the same, " +
                    "srcEnvName=" + srcCluster.getEnvironmentName() + ",targetEnvName=" + targetCluster.getEnvironmentName());
        }
        if (!targetCluster.getServerType().equalsIgnoreCase(srcCluster.getServerType())) {
            throw new RuntimeException("srcCluster and targetCluster's clusterType must be the same, " +
                    "srcClusterType=" + srcCluster.getServerType() + ",targetClusterType=" + targetCluster.getServerType());
        }
        dto.setTargetCluster(targetCluster.getServerName());
        dto.setSrcCluster(srcCluster.getServerName());
        dto.setEnvId(targetCluster.getEnvironmentId());
    }


    private List<TopicLog> loadTopics(String clusterName) {
        return load("/data/logs/zms-backup/" + clusterName + "_topic", TopicLog.class);
    }

    private List<String> loadConsumers(String clusterName) {
        return load("/data/logs/zms-backup/" + clusterName + "_consumer", String.class);
    }

    private <T> List<T> load(String fileName, Class<T> clz) {
        try {
            String jsonString = MixAll.file2String(fileName);
            if (null == jsonString || jsonString.length() == 0) {
                return this.loadBak(fileName, clz);
            } else {
                logger.info("load {} OK", fileName);
                return this.decode(jsonString, clz);
            }
        } catch (Exception e) {
            logger.error("load " + fileName + " Failed, and try to load backup file", e);
            return this.loadBak(fileName, clz);
        }
    }

    private <T> List<T> loadBak(String fileName, Class<T> clz) {
        try {
            String jsonString = MixAll.file2String(fileName + ".bak");
            if (jsonString != null && jsonString.length() > 0) {
                logger.info("load " + fileName + " OK");
                return this.decode(jsonString, clz);
            }
        } catch (Exception e) {
            logger.error("load " + fileName + " Failed", e);
        }
        return new ArrayList<>();
    }

    private <T> List<T> decode(String jsonString, Class<T> clz) {
        return JSON.parseArray(jsonString, clz);
    }
}

