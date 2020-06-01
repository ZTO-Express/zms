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

/**
 * <p>Title: ZmsConf.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: www.zto.com</p>
 */
package com.zto.zms.service.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@ConfigurationProperties(prefix = "zms")
@Configuration
public class ZmsConf {
    private Map<String, String> zk;

    private Map<String, String> rocketMq;

    private Map<String, String> kafka;

    private String influxdb;

    private String adminUser;

    private String ignoreCluster;

    public Map<String, String> getZk() {
        return zk;
    }

    public void setZk(Map<String, String> zk) {
        this.zk = zk;
    }

    public Map<String, String> getRocketMq() {
        return rocketMq;
    }

    public void setRocketMq(Map<String, String> rocketMq) {
        this.rocketMq = rocketMq;
    }

    public Map<String, String> getKafka() {
        return kafka;
    }

    public void setKafka(Map<String, String> kafka) {
        this.kafka = kafka;
    }

    public String getInfluxdb() {
        return influxdb;
    }

    public void setInfluxdb(String influxdb) {
        this.influxdb = influxdb;
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getIgnoreCluster() {
        return ignoreCluster;
    }

    public void setIgnoreCluster(String ignoreCluster) {
        this.ignoreCluster = ignoreCluster;
    }

    public boolean isAdminUser(String username, String password) {
        if (null == adminUser) {
            return false;
        }
        for (String adminUser : adminUser.split(",")) {
            String[] userPass = adminUser.split(":");
            if (null != password) {
                if (userPass.length > 1 && username.equals(userPass[0]) && password.equals(userPass[1])) {
                    return true;
                }
            } else {
                if (StringUtils.compare(userPass[0], username) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAdminUser(String user) {
        return isAdminUser(user, null);
    }

    public boolean isIgnoreCluster(String clusterName) {
        if (StringUtils.isBlank(ignoreCluster)) {
            return false;
        }
        for (String s : ignoreCluster.split(",")) {
            if (clusterName.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }
}

