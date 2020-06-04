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

package com.zto.zms.alert;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.zto.zms.client.Zms;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.client.consumer.MsgConsumedStatus;
import com.zto.zms.client.consumer.RocketmqMessageListener;
import com.zto.zms.utils.Assert;
import com.zto.zms.utils.HttpClient;
import com.zto.zms.common.Result;
import com.zto.zms.service.config.ZmsConsumerConf;
import com.zto.zms.service.influx.InfluxdbClient;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springside.modules.utils.net.NetUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AlertTask implements CommandLineRunner, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(AlertTask.class);

    @Autowired
    private InfluxdbClient client;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private DLQAlertService dlqAlertService;
    @Autowired
    private ZmsConsumerConf zmsConsumerConf;

    private volatile boolean working = false;
    private volatile String envName;
    private String getEffectAlertRulesByEnvUrl = "/api/alert/getEffectAlertRulesByEnv";
    Map<String, AlertRuleConfig> ruleMap = Maps.newConcurrentMap();

    @Override
    public void run(String... args) throws Exception {
        envName = System.getProperty(ZmsConst.ZK.ENV);
        getEffectAlertRulesByEnvUrl = System.getProperty(ZmsConst.ZMS_PORTAL) + getEffectAlertRulesByEnvUrl;

        dlqAlertService.start();
        List<AlertRuleConfig> rules = getEffectAlertRulesByEnv();
        rules.forEach(alertRule -> ruleMap.put(alertRule.getRuleKey(), alertRule));

        alertRuleRefresh();
        logger.info("{} start to startup", NetUtil.getLocalHost());
        working = true;
        ExecutorService service = Executors.newFixedThreadPool(20, new ThreadFactory() {
            AtomicLong index = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Rule-Match-thread-" + index.incrementAndGet());
            }
        });
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            AtomicLong index = new AtomicLong(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "submit-matchrule-scheduled-thread-" + index.incrementAndGet());
            }
        });
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (working) {
                synchronized (ruleMap) {
                    Collection<AlertRuleConfig> values = ruleMap.values();
                    for (AlertRuleConfig rule : values) {
                        if (rule.getEffect() && rule.isInEffectTimePeriod()) {
                            service.submit(() -> {
                                try {
                                    logger.debug("start to check rule {}", rule.getRuleKey());
                                    check(rule);
                                } catch (Throwable ex) {
                                    logger.error("check rule {}  error", rule.getRuleKey(), ex);
                                }
                            });
                        }
                    }
                }
            }
        }, 1, 5, TimeUnit.MINUTES);
    }

    /**
     * 启动时调用portal查询当前环境下所有的告警规则
     */
    private List<AlertRuleConfig> getEffectAlertRulesByEnv() throws IOException {
        Map<String, String> params = Maps.newHashMap();
        params.put("env", envName);
        String resp = HttpClient.getWithString(getEffectAlertRulesByEnvUrl, params);
        Result result = JSON.parseObject(resp, Result.class);
        Assert.that(result.isStatus(), "query config error:" + result.getMessage());
        return JSON.parseArray(JSON.toJSONString(result.getResult()), AlertRuleConfig.class);
    }

    public void check(AlertRuleConfig rule) {
        // 1.获取rule对应的sql
        String sql = rule.getInfluxSql();
        // 2.获取rule对应的值
        int count = client.executeInfluxSql(sql);
        logger.debug(rule.getRuleKey() + " is " + count);
        boolean triggered = operate(rule.getTriggerOperator(), count, rule.getTriggerTimes());
        if (triggered) {
            logger.info("rule {} triggered", rule.getRuleKey());
            notificationService.sendNotification(rule, envName);
        }
    }

    public boolean operate(String opearator, int src, int target) {
        if (">".equalsIgnoreCase(opearator)) {
            return src > target;
        } else if ("=".equalsIgnoreCase(opearator)) {
            return src == target;
        } else {
            return src < target;
        }
    }

    @Override
    public void destroy() throws Exception {
        client.close();
    }

    private void alertRuleRefresh() {
        Zms.subscribe(zmsConsumerConf.getAlertConsumer(), new RocketmqMessageListener() {
            @Override
            public MsgConsumedStatus onMessage(List<MessageExt> msgs) {
                for (MessageExt msg : msgs) {
                    try {
                        String json = new String(msg.getBody(), "UTF-8");
                        AlertRuleConfig alertRule = JSON.parseObject(json, AlertRuleConfig.class);
                        if ("ADD".equalsIgnoreCase(msg.getTags())) {
                            logger.info("ADD {} rule msg consumed", envName);
                            ruleMap.put(alertRule.getRuleKey(), alertRule);
                        } else if ("UPDATE".equalsIgnoreCase(msg.getTags())){
                            logger.info("UPDATE {} rule msg consumed", envName);
                            if (alertRule.getEffect()) {
                                ruleMap.values().stream()
                                        .filter(item -> item.getId().equals(alertRule.getId()))
                                        .forEach(item -> ruleMap.remove(item.getRuleKey()));

                                ruleMap.put(alertRule.getRuleKey(), alertRule);
                            } else {
                                ruleMap.remove(alertRule.getRuleKey());
                            }
                        } else {
                            logger.info("DELETE {} rule msg consumed", envName);
                            ruleMap.remove(alertRule.getRuleKey());
                        }
                    } catch (UnsupportedEncodingException e) {
                        logger.error("parse {} string error {}", envName, JSON.toJSON(msg), e);
                    }
                }

                logger.info("{} rules updated after are:", envName);
                for (AlertRuleConfig alertRule : ruleMap.values()) {
                    logger.info(alertRule.toString());
                }
                return MsgConsumedStatus.SUCCEED;
            }
        });
    }

}

