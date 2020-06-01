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
import com.zto.zms.service.influx.InfluxdbClient;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private static final String ENV_NAME_DESC = "环境：";

    @Autowired
    private InfluxdbClient client;

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    Executor influxAlertService = Executors.newSingleThreadExecutor(r -> new Thread(r, "influx-write-alert-thread"));

    public void sendNotification(AlertRuleConfig rule, String envName) {
        if (!rule.getDescription().startsWith(ENV_NAME_DESC)) {
            rule.setDescription(ENV_NAME_DESC.concat(envName).concat("\n").concat(rule.getDescription()));
        }
        if (!StringUtils.isEmpty(rule.getAlertDingding())) {
            sendDingdingGroup(rule);
        }
        influxAlertService.execute(() -> {
            try {
                client.insertNotifications(rule.getName(),rule.getField(),rule.getType(),rule.getDescription());
            } catch (Exception ex) {
                logger.error("write alert {} into to influx error", rule.getRuleKey(), ex);
            }
        });
    }


    public void sendDingdingGroup(AlertRuleConfig rule) {

        HttpPost httppost = new HttpPost(rule.getAlertDingding());
        httppost.addHeader("Content-Type", "application/json; charset=utf-8");
        Map<String,Object> map = new HashMap<>();
        map.put("msgtype","text");
        Map<String,Object> content = new HashMap<>();
        content.put("content",rule.getDescription());
        map.put("text",content);
        String errorMsg = JSON.toJSONString(map);
        StringEntity se = new StringEntity(errorMsg, "utf-8");
        httppost.setEntity(se);

        HttpResponse response = null;
        try {
            response = httpClient.execute(httppost);
            logger.info("send {} dingding group msg:{} ", rule.getRuleKey(), response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            logger.error("send {} dingding group failed", rule.getRuleKey(), e);
        }finally {
            try {
                if (null != response) {
                    IOUtils.closeQuietly(response.getEntity().getContent());
                }
            } catch (IOException e) {
                logger.error("close error",e);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        client.close();
        try {
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

