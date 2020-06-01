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

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import com.google.common.collect.Maps;
import com.zto.zms.service.mq.MqAdminManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.QueueData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UpdateTopicPermService {

    public static final Logger logger = LoggerFactory.getLogger(UpdateTopicPermService.class);

    private DefaultMQAdminExt defaultMQAdminExt;

    @Resource
    private MqAdminManager mqAdmins;

    public void execute(String cluster, String topic, String broker, int newPerm) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {

        defaultMQAdminExt = mqAdmins.getMqAdmin(cluster);
        TopicRouteData topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(topic);
        assert topicRouteData != null;
        List<QueueData> queueDataS = topicRouteData.getQueueDatas();
        assert queueDataS != null && queueDataS.size() > 0;

        ClusterInfo clusterInfo = defaultMQAdminExt.examineBrokerClusterInfo();

        HashMap<String, BrokerData> brokerAddrTable = clusterInfo.getBrokerAddrTable();

        if (StringUtils.isNotEmpty(broker)) {
            updatePerm(queueDataS, brokerAddrTable, broker, newPerm, topic);
        } else {
            Set<String> masterSet =
                    CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, cluster);
            for (String addr : masterSet) {
                updatePerm(queueDataS, brokerAddrTable, addr, newPerm, topic);

            }
        }

    }


    public Map<String, TopicConfig> get(String cluster, String topic) throws RemotingException, MQClientException, InterruptedException, MQBrokerException {

        defaultMQAdminExt = mqAdmins.getMqAdmin(cluster);
        TopicRouteData topicRouteData = defaultMQAdminExt.examineTopicRouteInfo(topic);
        assert topicRouteData != null;
        List<QueueData> queueDataS = topicRouteData.getQueueDatas();
        assert queueDataS != null && queueDataS.size() > 0;
        Set<String> masterSet =
                CommandUtil.fetchMasterAddrByClusterName(defaultMQAdminExt, cluster);
        Map<String, TopicConfig> configs = Maps.newHashMap();
        for (String addr : masterSet) {

            configs.put(addr, defaultMQAdminExt.examineTopicConfig(addr, topic));

        }

        return configs;

    }


    private QueueData getQueueData(List<QueueData> queueDataS, HashMap<String, BrokerData> brokerAddrTable, String addr) {
        for (Map.Entry<String, BrokerData> entry : brokerAddrTable.entrySet()) {
            String name = entry.getValue().getBrokerName();
            String master = entry.getValue().getBrokerAddrs().get(MixAll.MASTER_ID);
            if (addr.equalsIgnoreCase(master)) {
                for (QueueData data : queueDataS) {
                    if (data.getBrokerName().equalsIgnoreCase(name)) {
                        return data;
                    }
                }
            }
        }
        return null;
    }


    private void updatePerm(List<QueueData> queueDatas, HashMap<String, BrokerData> brokerAddrTable, String addr, int newPerm, String topic) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        QueueData data = getQueueData(queueDatas, brokerAddrTable, addr);
        if (data == null) {
            logger.warn("no master broker can be found to update {}", addr);
            return;
        }
        if (data.getPerm() == newPerm) {
            logger.warn("new perm equals to the old one on {}", addr);
            return;
        }
        TopicConfig topicConfig = new TopicConfig();
        topicConfig.setTopicName(topic);
        topicConfig.setPerm(newPerm);
        topicConfig.setTopicSysFlag(data.getTopicSynFlag());
        topicConfig.setWriteQueueNums(data.getWriteQueueNums());
        topicConfig.setReadQueueNums(data.getReadQueueNums());

        defaultMQAdminExt.createAndUpdateTopicConfig(addr, topicConfig);
        logger.info("update topic perm to {} in {} success.%n", newPerm, addr);
        logger.info("{}%n", topicConfig);
    }

}

