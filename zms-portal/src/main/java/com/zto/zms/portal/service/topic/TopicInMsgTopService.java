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

import com.zto.zms.portal.dto.topic.TopicInMsgDTO;
import com.zto.zms.service.mq.MqAdminManager;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.protocol.body.BrokerStatsData;
import org.apache.rocketmq.common.protocol.body.TopicList;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.store.stats.BrokerStatsManager;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by liangyong on 2019/3/12.
 */

@Service
public class TopicInMsgTopService {

    public static final Logger logger = LoggerFactory.getLogger(TopicInMsgTopService.class);

    @Resource
    private MqAdminManager mqAdmins;
    @Autowired
    private ExecutorService executorService;


    public List<TopicInMsgDTO> topicInMsgTopToday(String clusterName) {
        return execute(clusterName);
    }

    private List<TopicInMsgDTO> execute(String clusterName) {

        DefaultMQAdminExt defaultMqAdminExt = mqAdmins.getMqAdmin(clusterName);
        List<TopicInMsgDTO> resultLst = new ArrayList<>();
        try {
            TopicList topicList = defaultMqAdminExt.fetchAllTopicList();
            List<Future<TopicInMsgDTO>> futures = new ArrayList<>();
            for (String topic : topicList.getTopicList()) {
                if (topic.startsWith(MixAll.RETRY_GROUP_TOPIC_PREFIX) || topic.startsWith(MixAll.DLQ_GROUP_TOPIC_PREFIX)) {
                    continue;
                }
                Future<TopicInMsgDTO> future = executorService.submit(() -> topicDetail(defaultMqAdminExt, topic));
                futures.add(future);
            }
            for (Future<TopicInMsgDTO> future : futures) {
                resultLst.add(future.get());
            }
        } catch (Exception e) {
            logger.error("execute topicInMsgTopToday command error, clusterName={},errMsg={}",
                    clusterName, e);
        }
        Collections.sort(resultLst);
        int toIndex = Math.min(resultLst.size(), 50);
        return resultLst.subList(0, toIndex);
    }

    private TopicInMsgDTO topicDetail(final DefaultMQAdminExt admin,
                                      final String topic)
            throws RemotingException, MQClientException, InterruptedException {

        TopicInMsgDTO topicInMsgDto = new TopicInMsgDTO();
        TopicRouteData topicRouteData = admin.examineTopicRouteInfo(topic);
        long inMsgCntToday = 0;
        for (BrokerData bd : topicRouteData.getBrokerDatas()) {
            String masterAddr = bd.getBrokerAddrs().get(MixAll.MASTER_ID);
            if (masterAddr != null) {
                try {
                    BrokerStatsData bsd = admin.viewBrokerStatsData(masterAddr, BrokerStatsManager.TOPIC_PUT_NUMS, topic);
                    inMsgCntToday += compute24HourSum(bsd);
                } catch (Exception e) {
                    logger.error("查询broker节点topic数据异常", e);
                }
            }
        }
        topicInMsgDto.setInMsg24Hour(inMsgCntToday);
        topicInMsgDto.setTopic(topic);
        return topicInMsgDto;
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

