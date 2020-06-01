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

import com.google.common.collect.Maps;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.dal.mapper.ConsumerMapper;
import com.zto.zms.dal.model.Consumer;
import com.zto.zms.portal.dto.consumer.ConsumerProgressDTO;
import com.zto.zms.portal.dto.consumer.ConsumerStatusAllDTO;
import com.zto.zms.portal.dto.consumer.ConsumerStatusDTO;
import com.zto.zms.portal.dto.consumer.ConsumerZmsRegisterDTO;
import com.zto.zms.service.mq.MqAdminManager;
import com.zto.zms.service.router.ZkClientRouter;
import org.apache.rocketmq.common.MQVersion;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.admin.OffsetWrapper;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.body.Connection;
import org.apache.rocketmq.common.protocol.body.ConsumerConnection;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by liangyong on 2019/3/1.
 */

@Service
public class ConsumerStatusService {
    public static final Logger logger = LoggerFactory.getLogger(ConsumerStatusService.class);

    @Resource
    private MqAdminManager mqAdmins;
    @Autowired
    private ConsumerMapper consumerMapper;
    @Autowired
    private ZkClientRouter zkClientRouter;


    public ConsumerStatusAllDTO consumerStatsAll(String clusterName, String consumerGroup) {
        ConsumerStatusAllDTO consumerStatusAll = new ConsumerStatusAllDTO();
        consumerStatus(clusterName, consumerGroup, consumerStatusAll);
        consumerProgress(clusterName, consumerGroup, consumerStatusAll);
        List<ConsumerZmsRegisterDTO> consumerZmsRegisterLst = consumerZmsRegister(consumerGroup);
        consumerStatusAll.setConsumerZmsRegisterList(consumerZmsRegisterLst);
        return consumerStatusAll;
    }

    /**
     * 消费详情
     *
     * @param envId      环境ID
     * @param consumerId 消费组ID
     */
    public ConsumerStatusAllDTO consumerStatsAllNew(int envId, Long consumerId) {
        ConsumerStatusAllDTO consumerStatusAll = new ConsumerStatusAllDTO();
        //获取对应集群和消费组
        List<Consumer> consumers = consumerMapper.selectConsumerGroupByEnv(envId, consumerId);
        if (CollectionUtils.isEmpty(consumers)) {
            return consumerStatusAll;
        }
        Consumer consumer = consumers.get(0);

        consumerStatus(consumer.getClusterName(), consumer.getName(), consumerStatusAll);
        consumerProgress(consumer.getClusterName(), consumer.getName(), consumerStatusAll);
        List<ConsumerZmsRegisterDTO> consumerZmsRegisterLst = consumerZmsRegister(consumer.getName());
        consumerStatusAll.setConsumerZmsRegisterList(consumerZmsRegisterLst);
        return consumerStatusAll;
    }

    /**
     * 获取MQ命令行consumerStatus信息
     */
    private void consumerStatus(String clusterName, String group, ConsumerStatusAllDTO consumerStatusAll) {
        List<ConsumerStatusDTO> consumerStatusLst = new ArrayList<>();
        try {
            DefaultMQAdminExt defaultMqAdminExt = mqAdmins.getMqAdmin(clusterName);
            ConsumerConnection cc = defaultMqAdminExt.examineConsumerConnectionInfo(group);

            for (Connection conn : cc.getConnectionSet()) {
                ConsumerStatusDTO consumerStatus = new ConsumerStatusDTO();
                consumerStatus.setClientId(conn.getClientId());
                consumerStatus.setClientAddr(conn.getClientAddr());
                consumerStatus.setLanguage(conn.getLanguage().name());
                consumerStatus.setVersion(MQVersion.getVersionDesc(conn.getVersion()));
                consumerStatusLst.add(consumerStatus);
            }
        } catch (Exception e) {
            logger.error("execute consumerStatus command error,  clusterName={},consumerGroup={}, errMsg={}",
                    clusterName, group, e);
        }
        consumerStatusAll.setConsumerStatusList(consumerStatusLst);
    }

    /**
     * 获取MQ命令行consumerProgress信息
     */
    private void consumerProgress(String clusterName, String consumerGroup, ConsumerStatusAllDTO consumerStatusAll) {
        List<ConsumerProgressDTO> consumerProgressList = new ArrayList<>();
        consumerStatusAll.setConsumerProgressList(consumerProgressList);
        try {
            DefaultMQAdminExt defaultMqAdminExt = mqAdmins.getMqAdmin(clusterName);
            ConsumeStats consumeStats = defaultMqAdminExt.examineConsumeStats(consumerGroup);
            List<MessageQueue> mqList = new LinkedList<>(consumeStats.getOffsetTable().keySet());
            Collections.sort(mqList);
            Map<MessageQueue, String> messageQueueAllocationResult = getMessageQueueAllocationResult(defaultMqAdminExt, consumerGroup);
            long diffTotal = 0L;
            for (MessageQueue mq : mqList) {
                OffsetWrapper offsetWrapper = consumeStats.getOffsetTable().get(mq);
                long diff = offsetWrapper.getBrokerOffset() - offsetWrapper.getConsumerOffset();
                diffTotal += diff;
                String lastTime = "";
                try {
                    lastTime = UtilAll.formatDate(new Date(offsetWrapper.getLastTimestamp()), UtilAll.YYYY_MM_DD_HH_MM_SS);
                } catch (Exception e) {
                    logger.error("日期格式化异常", e);
                }

                String clientIp = messageQueueAllocationResult.get(mq);
                ConsumerProgressDTO consumerProgress = new ConsumerProgressDTO();
                consumerProgress.setTopic(UtilAll.frontStringAtLeast(mq.getTopic(), 32));
                consumerProgress.setBrokerName(UtilAll.frontStringAtLeast(mq.getBrokerName(), 32));
                consumerProgress.setQid(mq.getQueueId());
                consumerProgress.setBrokerOffset(offsetWrapper.getBrokerOffset());
                consumerProgress.setConsumerOffset(offsetWrapper.getConsumerOffset());
                consumerProgress.setClientIP(null != clientIp ? clientIp : "NA");
                consumerProgress.setDiff(diff);
                consumerProgress.setLastTime(lastTime);
                consumerProgressList.add(consumerProgress);
            }
            Collections.sort(consumerProgressList);
            consumerStatusAll.setConsumerProgressList(consumerProgressList);
            consumerStatusAll.setConsumeTps(consumeStats.getConsumeTps());
            consumerStatusAll.setDiffTotal(diffTotal);
        } catch (Exception e) {
            logger.error("execute consumerProgress command error,  clusterName={},consumerGroup={}, errMsg={}",
                    clusterName, consumerGroup, e);
        }
    }

    private Map<MessageQueue, String> getMessageQueueAllocationResult(DefaultMQAdminExt defaultMqAdminExt,
                                                                      String groupName) {
        Map<MessageQueue, String> results = Maps.newHashMap();
        try {
            ConsumerConnection consumerConnection = defaultMqAdminExt.examineConsumerConnectionInfo(groupName);
            for (Connection connection : consumerConnection.getConnectionSet()) {
                String clientId = connection.getClientId();
                ConsumerRunningInfo consumerRunningInfo = defaultMqAdminExt.getConsumerRunningInfo(groupName, clientId,
                        false);
                for (MessageQueue messageQueue : consumerRunningInfo.getMqTable().keySet()) {
                    results.put(messageQueue, clientId.split("@")[0]);
                }
            }
        } catch (Exception ignore) {
        }
        return results;
    }

    /**
     * 从ZMS_ZK获取消费端注册信息
     *
     * @param consumerGroup 消费组
     */
    public List<ConsumerZmsRegisterDTO> consumerZmsRegister(String consumerGroup) {
        String zkPath = String.join("/", ZmsConst.ZK.CONSUMERGROUP_ZKPATH, consumerGroup);
        if (!zkClientRouter.currentZkClient().exists(zkPath)) {
            return new ArrayList<>();
        }
        List<ConsumerZmsRegisterDTO> consumerZmsRegisterLst = new ArrayList<>();

        List<String> subLst = zkClientRouter.currentZkClient().getChildren(zkPath);
        for (String clientInfo : subLst) {
            String[] clientArrS = clientInfo.split("\\|\\|");
            if (clientArrS.length == 4) {
                //老版本ZMS SDK
                ConsumerZmsRegisterDTO consumerZmsRegister = new ConsumerZmsRegisterDTO();
                consumerZmsRegister.setZmsIP(clientArrS[0]);
                consumerZmsRegister.setInstanceName(clientArrS[1]);
                consumerZmsRegister.setZmsVersion(clientArrS[2]);
                consumerZmsRegister.setThreadLocalRandomInt(Integer.parseInt(clientArrS[3]));
                consumerZmsRegisterLst.add(consumerZmsRegister);
            } else if (clientArrS.length == 5) {
                //新版本ZMS SDK
                ConsumerZmsRegisterDTO consumerZmsRegister = new ConsumerZmsRegisterDTO();
                consumerZmsRegister.setZmsIP(clientArrS[0]);
                consumerZmsRegister.setInstanceName(clientArrS[1]);
                consumerZmsRegister.setZmsVersion(clientArrS[2]);
                consumerZmsRegister.setStartUpTime(clientArrS[3]);
                consumerZmsRegister.setThreadLocalRandomInt(Integer.parseInt(clientArrS[4]));
                consumerZmsRegisterLst.add(consumerZmsRegister);
            } else {
                logger.warn("zms_zk /zms/consumergroup/{} sub nodes format error, subNode={} ",
                        consumerGroup, clientInfo);
            }
        }
        return consumerZmsRegisterLst;
    }

}

