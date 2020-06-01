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

import com.zto.zms.portal.dto.BrokerDataDTO;
import com.zto.zms.service.mq.MqAdminManager;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.common.protocol.body.ClusterInfo;
import org.apache.rocketmq.common.protocol.body.KVTable;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by liangyong on 2019/3/11.
 */

@Service
public class ClusterListService {

    public static final Logger logger = LoggerFactory.getLogger(ClusterListService.class);

    @Resource
    private MqAdminManager mqAdmins;

    public List<BrokerDataDTO> clusterList(String clusterName)
            throws InterruptedException, RemotingConnectException, RemotingTimeoutException, RemotingSendRequestException, MQBrokerException {

        return execute(clusterName);
    }

    private List<BrokerDataDTO> execute(String clusterName)
            throws InterruptedException, MQBrokerException, RemotingTimeoutException, RemotingSendRequestException, RemotingConnectException {
        List<BrokerDataDTO> resultLst = new ArrayList<>();
        try {
            DefaultMQAdminExt defaultMqAdminExt = mqAdmins.getMqAdmin(clusterName);
            ClusterInfo clusterInfoSerializeWrapper = defaultMqAdminExt.examineBrokerClusterInfo();
            Iterator<Map.Entry<String, Set<String>>> itCluster = clusterInfoSerializeWrapper.getClusterAddrTable().entrySet().iterator();
            if (itCluster.hasNext()) {
                Map.Entry<String, Set<String>> next = itCluster.next();
                TreeSet<String> brokerNameSet = new TreeSet<>(next.getValue());
                for (String brokerName : brokerNameSet) {
                    BrokerData brokerData = clusterInfoSerializeWrapper.getBrokerAddrTable().get(brokerName);
                    if (brokerData != null) {
                        for (Map.Entry<Long, String> entry : brokerData.getBrokerAddrs().entrySet()) {
                            BrokerDataDTO brokerDataDto = new BrokerDataDTO();
                            double in = 0;
                            double out = 0;
                            String version = "";
                            String sendThreadPoolQueueSize = "";
                            String pullThreadPoolQueueSize = "";
                            String sendThreadPoolQueueHeadWaitTimeMills = "";
                            String pullThreadPoolQueueHeadWaitTimeMills = "";
                            String pageCacheLockTimeMills = "";
                            String earliestMessageTimeStamp = "";
                            String commitLogDiskRatio = "";

                            long inTotalYest = 0;
                            long outTotalYest = 0;
                            long inTotalToday = 0;
                            long outTotalToday = 0;

                            try {
                                KVTable kvTable = defaultMqAdminExt.fetchBrokerRuntimeStats(entry.getValue());
                                String putTps = kvTable.getTable().get("putTps");
                                String getTransferedTps = kvTable.getTable().get("getTransferedTps");
                                sendThreadPoolQueueSize = kvTable.getTable().get("sendThreadPoolQueueSize");
                                pullThreadPoolQueueSize = kvTable.getTable().get("pullThreadPoolQueueSize");

                                sendThreadPoolQueueSize = kvTable.getTable().get("sendThreadPoolQueueSize");
                                pullThreadPoolQueueSize = kvTable.getTable().get("pullThreadPoolQueueSize");

                                sendThreadPoolQueueHeadWaitTimeMills = kvTable.getTable().get("sendThreadPoolQueueHeadWaitTimeMills");
                                pullThreadPoolQueueHeadWaitTimeMills = kvTable.getTable().get("pullThreadPoolQueueHeadWaitTimeMills");
                                pageCacheLockTimeMills = kvTable.getTable().get("pageCacheLockTimeMills");
                                earliestMessageTimeStamp = kvTable.getTable().get("earliestMessageTimeStamp");
                                commitLogDiskRatio = kvTable.getTable().get("commitLogDiskRatio");

                                version = kvTable.getTable().get("brokerVersionDesc");
                                {
                                    String[] tpss = putTps.split(" ");
                                    if (tpss.length > 0) {
                                        in = Double.parseDouble(tpss[0]);
                                    }
                                }

                                {
                                    String[] tpss = getTransferedTps.split(" ");
                                    if (tpss.length > 0) {
                                        out = Double.parseDouble(tpss[0]);
                                    }
                                }

                                String msgPutTotalYesterdayMorning = kvTable.getTable().get("msgPutTotalYesterdayMorning");
                                String msgPutTotalTodayMorning = kvTable.getTable().get("msgPutTotalTodayMorning");
                                String msgPutTotalTodayNow = kvTable.getTable().get("msgPutTotalTodayNow");
                                String msgGetTotalYesterdayMorning = kvTable.getTable().get("msgGetTotalYesterdayMorning");
                                String msgGetTotalTodayMorning = kvTable.getTable().get("msgGetTotalTodayMorning");
                                String msgGetTotalTodayNow = kvTable.getTable().get("msgGetTotalTodayNow");

                                inTotalYest = Long.parseLong(msgPutTotalTodayMorning) - Long.parseLong(msgPutTotalYesterdayMorning);
                                outTotalYest = Long.parseLong(msgGetTotalTodayMorning) - Long.parseLong(msgGetTotalYesterdayMorning);

                                inTotalToday = Long.parseLong(msgPutTotalTodayNow) - Long.parseLong(msgPutTotalTodayMorning);
                                outTotalToday = Long.parseLong(msgGetTotalTodayNow) - Long.parseLong(msgGetTotalTodayMorning);


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            double hour = 0.0;
                            double space = 0.0;

                            if (earliestMessageTimeStamp != null && earliestMessageTimeStamp.length() > 0) {
                                long mills = System.currentTimeMillis() - Long.parseLong(earliestMessageTimeStamp);
                                hour = mills / 1000.0 / 60.0 / 60.0;
                            }

                            if (commitLogDiskRatio != null && commitLogDiskRatio.length() > 0) {
                                space = Double.parseDouble(commitLogDiskRatio);
                            }
                            brokerDataDto.setClusterName(clusterName);
                            brokerDataDto.setBrokerName(brokerName);
                            brokerDataDto.setBid(entry.getKey());
                            brokerDataDto.setAddr(entry.getValue());
                            brokerDataDto.setVersion(version);
                            brokerDataDto.setInTps(String.format("%9.2f(%s,%sms)", in, sendThreadPoolQueueSize, sendThreadPoolQueueHeadWaitTimeMills));
                            brokerDataDto.setOutTps(String.format("%9.2f(%s,%sms)", out, pullThreadPoolQueueSize, pullThreadPoolQueueHeadWaitTimeMills));
                            brokerDataDto.setPcWait(pageCacheLockTimeMills);
                            brokerDataDto.setHour(String.format("%2.2f", hour));
                            brokerDataDto.setSpace(String.format("%.4f", space));
                            brokerDataDto.setInTotalToday(inTotalToday);
                            brokerDataDto.setOutTotalToday(outTotalToday);
                            brokerDataDto.setInTotalYest(inTotalYest);
                            brokerDataDto.setOutTotalYest(outTotalYest);

                            resultLst.add(brokerDataDto);
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("execute clusterList command error,  clusterName={}", clusterName, e);
            throw e;
        }
        return resultLst;
    }


}



