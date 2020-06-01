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

import com.google.common.collect.Lists;
import com.zto.zms.portal.rocketmq.StatsAllResultDto;
import com.zto.zms.service.mq.MqAdminManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.admin.ConsumeStats;
import org.apache.rocketmq.common.protocol.body.BrokerStatsData;
import org.apache.rocketmq.common.protocol.body.GroupList;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.store.stats.BrokerStatsManager;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangyong on 2019/2/14.
 */

@Service
public class StatsAllService {

    public static final Logger logger = LoggerFactory.getLogger(StatsAllService.class);

    @Resource
    private MqAdminManager mqAdmins;

    public List<StatsAllResultDto> statsAll(String clusterName,
                                            String topicName) throws InterruptedException, RemotingException, MQClientException, MQBrokerException {


        return execute(clusterName, null, topicName, null);

    }

    public List<StatsAllResultDto> execute(String clusterName,
                                           String nameSrvAddr,
                                           String topicName,
                                           String consumerGroup)
            throws MQClientException, RemotingException, InterruptedException, MQBrokerException {

        DefaultMQAdminExt defaultMqAdminExt;
        if (StringUtils.isNotEmpty(clusterName)) {
            defaultMqAdminExt = mqAdmins.getMqAdmin(clusterName);
        } else {
            defaultMqAdminExt = new DefaultMQAdminExt();
            defaultMqAdminExt.setNamesrvAddr(nameSrvAddr);
            defaultMqAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
            try {
                defaultMqAdminExt.start();
            } catch (MQClientException e) {
                logger.error("execute statsAll command error,  nameSrvAddr={}, clusterName={},topicName={},consumerGroup={}, errMsg={}",
                        nameSrvAddr, clusterName, topicName, consumerGroup, e);
                throw e;
            }
        }
        List<StatsAllResultDto> result = Lists.newArrayList();

        try {
            TopicList topicList = defaultMqAdminExt.fetchAllTopicList();
            boolean activeTopic = false;

            for (String topic : topicList.getTopicList()) {
                if (StringUtils.isNotEmpty(topicName) && !topic.equals(topicName)) {
                    continue;
                }
                result.addAll(printTopicDetail(defaultMqAdminExt, topic, activeTopic, consumerGroup));
            }
        } catch (Exception e) {
            logger.error("execute statsAll command error,  nameSrvAddr={}, clusterName={},topicName={},consumerGroup={}, errMsg={}",
                    nameSrvAddr, clusterName, topicName, consumerGroup, e);
            throw e;
        } finally {
            if (StringUtils.isEmpty(clusterName)) {
                defaultMqAdminExt.shutdown();
            }
        }
        return result;
    }

    private List<StatsAllResultDto> printTopicDetail(final DefaultMQAdminExt admin,
                                                     final String topic,
                                                     final boolean activeTopic,
                                                     final String consumerGroup)
            throws RemotingException, MQClientException, InterruptedException, MQBrokerException {

        List<StatsAllResultDto> resultLst = new ArrayList<>();
        TopicRouteData topicRouteData = admin.examineTopicRouteInfo(topic);
        GroupList groupList = admin.queryTopicConsumeByWho(topic);
        double inTps = 0;
        long inMsgCntToday = 0;
        for (BrokerData bd : topicRouteData.getBrokerDatas()) {
            String masterAddr = bd.getBrokerAddrs().get(MixAll.MASTER_ID);
            if (masterAddr != null) {
                try {
                    BrokerStatsData bsd = admin.viewBrokerStatsData(masterAddr, BrokerStatsManager.TOPIC_PUT_NUMS, topic);
                    inTps += bsd.getStatsMinute().getTps();
                    inMsgCntToday += compute24HourSum(bsd);
                } catch (Exception e) {
                    logger.error("viewBrokerStatsData error, brokerAddr:{}, topic:{}, err:{}", masterAddr, topic, e);
                }
            }
        }

        inTps = new BigDecimal(inTps).setScale(2, RoundingMode.UP).doubleValue();
        if (groupList != null && !groupList.getGroupList().isEmpty()) {
            for (String group : groupList.getGroupList()) {
                if (StringUtils.isNotEmpty(consumerGroup) && !consumerGroup.equalsIgnoreCase(group)) {
                    continue;
                }
                double outTps = 0;

                long outMsgCntToday = 0;
                for (BrokerData bd : topicRouteData.getBrokerDatas()) {
                    String masterAddr = bd.getBrokerAddrs().get(MixAll.MASTER_ID);
                    if (masterAddr != null) {
                        try {
                            String statsKey = String.format("%s@%s", topic, group);
                            BrokerStatsData bsd = admin.viewBrokerStatsData(masterAddr, BrokerStatsManager.GROUP_GET_NUMS, statsKey);
                            outTps += bsd.getStatsMinute().getTps();
                            outMsgCntToday += compute24HourSum(bsd);
                        } catch (Exception e) {
                            logger.error("viewBrokerStatsData error, brokerAddr:{}, topic:{}, group:{}, err:{}", masterAddr, topic, group, e);
                        }
                    }
                }

                long accumulate = 0;
                try {
                    ConsumeStats consumeStats = admin.examineConsumeStats(group, topic);
                    if (consumeStats != null) {
                        accumulate = consumeStats.computeTotalDiff();
                        if (accumulate < 0) {
                            accumulate = 0;
                        }
                    }
                } catch (Exception e) {
                    logger.error("examineConsumeStats error, group:{}, topic:{}, err:{}", group, topic, e);
                }

                if (!activeTopic || (inMsgCntToday > 0) ||
                        (outMsgCntToday > 0)) {
                    StatsAllResultDto statsResult = new StatsAllResultDto();
                    statsResult.setTopic(UtilAll.frontStringAtLeast(topic, 64));
                    statsResult.setConsumerGroup(UtilAll.frontStringAtLeast(group, 64));
                    statsResult.setAccumulation(accumulate);
                    statsResult.setInTPS(inTps);
                    statsResult.setOutTPS(new BigDecimal(outTps).setScale(2, RoundingMode.UP).doubleValue());
                    statsResult.setInMsg24Hour(inMsgCntToday);
                    statsResult.setOutMsg24Hour(outMsgCntToday);
                    resultLst.add(statsResult);
                }
            }
        } else {
            if (!activeTopic || (inMsgCntToday > 0)) {
                StatsAllResultDto statsResult = new StatsAllResultDto();
                statsResult.setTopic(UtilAll.frontStringAtLeast(topic, 32));
                statsResult.setConsumerGroup("");
                statsResult.setAccumulation(0L);
                statsResult.setInTPS(inTps);
                statsResult.setOutTPS(0d);
                statsResult.setInMsg24Hour(inMsgCntToday);
                statsResult.setOutMsg24Hour(0L);
                resultLst.add(statsResult);
            }
        }
        return resultLst;
    }

    public static long compute24HourSum(BrokerStatsData bsd) {
        if (bsd.getStatsDay().getSum() != 0) {
            return bsd.getStatsDay().getSum();
        }

        if (bsd.getStatsHour().getSum() != 0) {
            return bsd.getStatsHour().getSum();
        }

        if (bsd.getStatsMinute().getSum() != 0) {
            return bsd.getStatsMinute().getSum();
        }

        return 0;
    }


}

