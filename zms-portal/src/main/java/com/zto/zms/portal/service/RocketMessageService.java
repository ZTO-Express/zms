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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.utils.Assert;
import com.zto.zms.dal.mapper.TopicEnvironmentRefMapper;
import com.zto.zms.portal.dto.MessageView;
import com.zto.zms.service.mq.MqAdminManager;
import com.zto.zms.service.router.ZkClientRouter;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.route.QueueData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.api.MessageTrack;
import org.apache.rocketmq.tools.command.CommandUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Service
public class RocketMessageService {

    private Logger logger = LoggerFactory.getLogger(RocketMessageService.class);

    @Autowired
    private TopicEnvironmentRefMapper topicEnvironmentRefMapper;
    @Resource
    private MqAdminManager mqAdmins;
    @Autowired
    private ZkClientRouter zkClientRouter;


    public List<MessageView> viewMessage(Integer envId, String topicName, final String msgId) {
        String clusterName = topicEnvironmentRefMapper.getByEnvIdAndTopicName(envId, topicName);
        Assert.that(StringUtils.isNotEmpty(clusterName), "当前环境主题集群不存在");
        DefaultMQAdminExt mqAdminExt = mqAdmins.getMqAdmin(clusterName);

        try {
            MessageExt messageExt = mqAdminExt.viewMessage(topicName, msgId);
            MessageView messageView = MessageView.fromMessageExt(messageExt);
            List<MessageTrack> messageTrackList = mqAdminExt.messageTrackDetail(messageExt);
            messageView.setMessageTrackList(messageTrackList);
            return Lists.newArrayList(messageView);
        } catch (Exception e) {
            try {
                MessageExt messageExt = mqAdminExt.viewMessage(msgId);
                MessageView messageView = MessageView.fromMessageExt(messageExt);
                List<MessageTrack> messageTrackList = mqAdminExt.messageTrackDetail(messageExt);
                messageView.setMessageTrackList(messageTrackList);
                return Lists.newArrayList(messageView);
            } catch (Exception e1) {

                return Lists.newArrayList();
            }
        }
    }

    public List<MessageView> queryMessageByTopicAndKey(Integer envId, String topicName, String key) {

        String clusterName = topicEnvironmentRefMapper.getByEnvIdAndTopicName(envId, topicName);
        Assert.that(StringUtils.isNotEmpty(clusterName), "当前环境主题集群不存在");
        try {
            DefaultMQAdminExt mqAdminExt = mqAdmins.getMqAdmin(clusterName);
            return Lists.transform(mqAdminExt.queryMessage(topicName, key, 64, 0, System.currentTimeMillis()).getMessageList(),
                    messageExt -> {
                        MessageView messageView = MessageView.fromMessageExt(messageExt);
                        try {
                            List<MessageTrack> messageTrackList = mqAdminExt.messageTrackDetail(messageExt);
                            messageView.setMessageTrackList(messageTrackList);
                        } catch (Exception e) {
                            logger.warn(e.getMessage());
                        }
                        return messageView;
                    });
        } catch (Exception err) {
            return Lists.newArrayList();
        }
    }


    public List<MessageView> queryMessageByTime(Integer envId, String topicName, final long beginTime, final long endTime) {
        String clusterName = topicEnvironmentRefMapper.getByEnvIdAndTopicName(envId, topicName);
        Assert.that(StringUtils.isNotEmpty(clusterName), "当前环境主题集群不存在");

        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer(MixAll.TOOLS_CONSUMER_GROUP, null);
        ClusterMetadata clusterMetadata = zkClientRouter.currentZkClient().readClusterMetadata(clusterName);
        consumer.setNamesrvAddr(clusterMetadata.getBootAddr());
        try {
            consumer.start();
            return fetchMsgLst(clusterName, topicName, beginTime, endTime, consumer);
        } catch (MQClientException e) {
            if (topicName.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX) && e.getMessage().contains("Can not find Message Queue for this topic")) {
                try {
                    DefaultMQAdminExt mqAdminExt = mqAdmins.getMqAdmin(clusterName);
                    TopicRouteData topicRouteData = mqAdminExt.examineTopicRouteInfo(topicName);
                    if (topicRouteData != null) {
                        List<QueueData> queueDataS = topicRouteData.getQueueDatas();
                        QueueData queueData = queueDataS.get(0);
                        TopicConfig topicConfig = new TopicConfig();
                        topicConfig.setTopicName(topicName);
                        topicConfig.setWriteQueueNums(queueData.getWriteQueueNums());
                        topicConfig.setReadQueueNums(queueData.getReadQueueNums());
                        topicConfig.setTopicSysFlag(queueData.getTopicSynFlag());
                        Set<String> masterSet = CommandUtil.fetchMasterAddrByClusterName(mqAdminExt, clusterName);
                        for (String addr : masterSet) {
                            mqAdminExt.createAndUpdateTopicConfig(addr, topicConfig);
                        }
                        logger.info("change topic {} perm success.", topicName);
                        //每隔30秒Broker向nameServer上报Topic注册信息
                        Thread.sleep(6 * 1000);
                        //重试一次
                        return fetchMsgLst(clusterName, topicName, beginTime, endTime, consumer);
                    }
                } catch (Exception ex) {
                    logger.error("change topic {} perm fail, err:{}", topicName, e.getMessage());
                }
            }
        } finally {
            consumer.shutdown();
        }
        return Lists.newArrayList();
    }

    private List<MessageView> fetchMsgLst(String clusterName, String topic, long begin,
                                          long end, DefaultMQPullConsumer consumer) throws MQClientException {
        DefaultMQAdminExt mqAdminExt = mqAdmins.getMqAdmin(clusterName);
        List<MessageView> msgLst = Lists.newArrayList();
        String subExpression = "*";
        Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(topic);
        for (MessageQueue mq : mqs) {
            long minOffset = consumer.searchOffset(mq, begin);
            long maxOffset = consumer.searchOffset(mq, end);
            read:
            for (long offset = minOffset; offset <= maxOffset; ) {
                try {
                    if (msgLst.size() > 50) {
                        break;
                    }
                    PullResult pullResult = consumer.pull(mq, subExpression, offset, 32);
                    offset = pullResult.getNextBeginOffset();
                    switch (pullResult.getPullStatus()) {
                        case FOUND:
                            List<MessageView> msgLstByQuery = Lists.transform(pullResult.getMsgFoundList(), messageExt -> {
                                MessageView messageView = MessageView.fromMessageExt(messageExt);
                                try {
                                    List<MessageTrack> messageTrackList = mqAdminExt.messageTrackDetail(messageExt);
                                    messageView.setMessageTrackList(messageTrackList);
                                } catch (Exception e) {
                                    logger.warn(e.getMessage());
                                }
                                return messageView;
                            });
                            List<MessageView> filteredList = Lists.newArrayList(Iterables.filter(msgLstByQuery, messageView -> {
                                if (messageView.getStoreTimestamp() < begin || messageView.getStoreTimestamp() > end) {
                                    logger.info("begin={} end={} time not in range {} {}", begin, end, messageView.getStoreTimestamp(), new Date(messageView.getStoreTimestamp()).toString());
                                }
                                return messageView.getStoreTimestamp() >= begin && messageView.getStoreTimestamp() <= end;
                            }));
                            msgLst.addAll(filteredList);
                            break;
                        case NO_MATCHED_MSG:
                        case NO_NEW_MSG:
                        case OFFSET_ILLEGAL:
                            break read;
                        default:
                    }
                } catch (Exception e) {
                    logger.error("Fetch message error, e=" + e.getMessage());
                    break;
                }
            }
        }
        Collections.sort(msgLst, (o1, o2) -> {
            if (o1.getStoreTimestamp() - o2.getStoreTimestamp() == 0) {
                return 0;
            }
            return (o1.getStoreTimestamp() > o2.getStoreTimestamp()) ? -1 : 1;
        });
        return msgLst;
    }


}

