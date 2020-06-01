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

import com.zto.zms.portal.rocketmq.TopicStatusDto;
import com.zto.zms.portal.service.ClusterListService;
import com.zto.zms.service.mq.MqAdminManager;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.admin.TopicOffset;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by liangyong on 2019/3/11.
 */

@Service
public class TopicStatusService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterListService.class);

    @Resource
    private MqAdminManager mqAdmins;

    public List<TopicStatusDto> topicStatus(String clusterName, String topicName)
            throws InterruptedException, RemotingException, MQClientException, MQBrokerException {
        return execute(clusterName, topicName);
    }

    private List<TopicStatusDto> execute(String clusterName, String topicName)
            throws InterruptedException, RemotingException, MQClientException, MQBrokerException {

        List<TopicStatusDto> resultLst = new ArrayList<>();
        try {
            DefaultMQAdminExt defaultMqAdminExt = mqAdmins.getMqAdmin(clusterName);
            TopicStatsTable topicStatsTable = defaultMqAdminExt.examineTopicStats(topicName);

            List<MessageQueue> mqList = new LinkedList<>(topicStatsTable.getOffsetTable().keySet());
            Collections.sort(mqList);

            for (MessageQueue mq : mqList) {
                TopicStatusDto topicStatusDto = new TopicStatusDto();
                TopicOffset topicOffset = topicStatsTable.getOffsetTable().get(mq);

                String humanTimestamp = "";
                if (topicOffset.getLastUpdateTimestamp() > 0) {
                    humanTimestamp = UtilAll.timeMillisToHumanString2(topicOffset.getLastUpdateTimestamp());
                }
                topicStatusDto.setBrokerName(UtilAll.frontStringAtLeast(mq.getBrokerName(), 32));
                topicStatusDto.setQid(mq.getQueueId());
                topicStatusDto.setMinOffset(topicOffset.getMinOffset());
                topicStatusDto.setMaxOffset(topicOffset.getMaxOffset());
                topicStatusDto.setLastUpdated(humanTimestamp);
                resultLst.add(topicStatusDto);
            }

        } catch (Exception e) {
            logger.error("execute clusterList command error,  clusterName={}", clusterName, e);
            throw e;
        }
        return resultLst;
    }

}

