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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.zto.zms.service.mq;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.MQClientAPIImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.TopicConfig;
import org.apache.rocketmq.common.message.MessageClientIDSetter;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.RequestCode;
import org.apache.rocketmq.common.protocol.ResponseCode;
import org.apache.rocketmq.common.protocol.body.SubscriptionGroupWrapper;
import org.apache.rocketmq.common.protocol.body.TopicConfigSerializeWrapper;
import org.apache.rocketmq.common.subscription.SubscriptionGroupConfig;
import org.apache.rocketmq.remoting.RemotingClient;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExtImpl;
import org.joor.Reflect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.rocketmq.remoting.protocol.RemotingSerializable.decode;

public class MQAdminExtImpl extends DefaultMQAdminExt {
    private Logger logger = LoggerFactory.getLogger(MQAdminExtImpl.class);

    public MQAdminExtImpl() {
    }

    private RemotingClient remotingClient;

    private final Object remotingClientLock = new Object();

    private RemotingClient getRemotingClient() {
        if (remotingClient == null) {
            synchronized (remotingClientLock) {
                if (remotingClient == null) {
                    DefaultMQAdminExtImpl defaultMqAdminExtImpl = Reflect.on(this).get("defaultMQAdminExtImpl");
                    MQClientInstance mqClientInstance = Reflect.on(defaultMqAdminExtImpl).get("mqClientInstance");
                    MQClientAPIImpl mqClientApiImpl = Reflect.on(mqClientInstance).get("mQClientAPIImpl");
                    remotingClient = Reflect.on(mqClientApiImpl).get("remotingClient");
                }
            }
        }
        return remotingClient;
    }

    @Override
    public SubscriptionGroupConfig examineSubscriptionGroupConfig(String addr, String group) {
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.GET_ALL_SUBSCRIPTIONGROUP_CONFIG, null);
        RemotingCommand response;
        try {
            response = getRemotingClient().invokeSync(addr, request, 3000);
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
        assert response != null;
        if (response.getCode() == ResponseCode.SUCCESS) {
            SubscriptionGroupWrapper subscriptionGroupWrapper = decode(response.getBody(), SubscriptionGroupWrapper.class);
            return subscriptionGroupWrapper.getSubscriptionGroupTable().get(group);
        }
        throw new RuntimeException(new MQBrokerException(response.getCode(), response.getRemark()));
    }

    @Override
    public TopicConfig examineTopicConfig(String addr, String topic) {
        RemotingCommand request = RemotingCommand.createRequestCommand(RequestCode.GET_ALL_TOPIC_CONFIG, null);
        RemotingCommand response;
        try {
            response = getRemotingClient().invokeSync(addr, request, 3000);
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
        if (response.getCode() == ResponseCode.SUCCESS) {
            TopicConfigSerializeWrapper topicConfigSerializeWrapper = decode(response.getBody(), TopicConfigSerializeWrapper.class);
            return topicConfigSerializeWrapper.getTopicConfigTable().get(topic);
        }
        throw new RuntimeException(new MQBrokerException(response.getCode(), response.getRemark()));
    }


    @Override
    public MessageExt viewMessage(String topic,
                                  String msgId) throws RemotingException, MQBrokerException, InterruptedException, MQClientException {
        logger.info("MessageClientIDSetter.getNearlyTimeFromID(msgId)={} msgId={}", MessageClientIDSetter.getNearlyTimeFromID(msgId), msgId);
        try {
            return viewMessage(msgId);
        } catch (Exception e) {
            logger.error("viewMessage error, msgId = {}, err {}", msgId, e);
        }
        return super.viewMessage(topic, msgId);
    }
}

