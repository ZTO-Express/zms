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

import com.zto.zms.service.mq.MqAdminManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.admin.MQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Created by liangyong on 2019/2/14.
 */

@Service
public class BrokerConfigService {

    public static final Logger logger = LoggerFactory.getLogger(BrokerConfigService.class);

    @Resource
    private MqAdminManager mqAdmins;

    public Map<String, Object> getBrokerConfig(String nameSrvAddr, String clusterName, String brokerAddr)
            throws MQClientException, InterruptedException, RemotingConnectException, UnsupportedEncodingException,
            RemotingSendRequestException, RemotingTimeoutException, MQBrokerException {

        return execute(nameSrvAddr, clusterName, brokerAddr);
    }

    private Map<String, Object> execute(String nameSrvAddr, String clusterName, String brokerAddr)
            throws MQClientException, InterruptedException, RemotingConnectException, UnsupportedEncodingException,
            RemotingSendRequestException, RemotingTimeoutException, MQBrokerException {

        DefaultMQAdminExt defaultMqAdminExt;
        if (!StringUtils.isEmpty(clusterName)) {
            defaultMqAdminExt = mqAdmins.getMqAdmin(clusterName);
        } else {
            defaultMqAdminExt = new DefaultMQAdminExt();
            defaultMqAdminExt.setNamesrvAddr(nameSrvAddr);
            defaultMqAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
            try {
                defaultMqAdminExt.start();
            } catch (MQClientException e) {
                logger.error("execute getBrokerConfig command error, namesrvAddr={}, clusterName={}, brokerAddr={}, errMsg={}",
                        nameSrvAddr, clusterName, brokerAddr, e);
                throw e;
            }
        }
        try {
            return getAndPrint(defaultMqAdminExt, brokerAddr);
        } catch (Exception e) {
            logger.error("execute getBrokerConfig command error, namesrvAddr={}, clusterName={}, brokerAddr={}, errMsg={}",
                    nameSrvAddr, clusterName, brokerAddr, e);
            throw e;
        } finally {
            if (StringUtils.isEmpty(clusterName)) {
                defaultMqAdminExt.shutdown();
            }
        }
    }

    protected Map<String, Object> getAndPrint(final MQAdminExt defaultMqAdminExt, final String addr)
            throws InterruptedException, RemotingConnectException,
            UnsupportedEncodingException, RemotingTimeoutException,
            MQBrokerException, RemotingSendRequestException {

        Map<String, Object> resultMap = new TreeMap<>(Comparator.naturalOrder());

        Properties properties = defaultMqAdminExt.getBrokerConfig(addr);
        if (properties == null) {
            return new TreeMap<>();
        }
        for (Object key : properties.keySet()) {
            resultMap.put(key.toString(), properties.get(key));
        }
        return resultMap;
    }


}

