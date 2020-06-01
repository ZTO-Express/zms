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
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zto.zms.logger;

import com.zto.zms.common.ZmsException;
import org.apache.rocketmq.client.log.ClientLogger;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;

public class ZmsClientLogger {


    private static final String ZMS_LOG_ROOT = "zms.client.logging.path";
    private static final String ZMS_LOG_LEVEL = "zms.client.logging.level";

    static Logger createLogger(final String loggerName) {
        boolean isLoadConfig =
                Boolean.parseBoolean(System.getProperty("zms.client.log.loadconfig", "true"));

        final String log4jResourceFile =
                System.getProperty("zms.client.log4j.resource.fileName", "log4j_zms_client.xml");

        final String logbackResourceFile =
                System.getProperty("zms.client.logback.resource.fileName", "logback_zms_client.xml");

        final String log4j2ResourceFile =
                System.getProperty("zms.client.log4j2.resource.fileName", "log4j2_zms_client.xml");


        String zmsClientLogRoot = System.getProperty(ZMS_LOG_ROOT, "/data/logs/zms");
        System.setProperty(ZMS_LOG_ROOT, zmsClientLogRoot);

        String zmsClientLogLevel = System.getProperty(ZMS_LOG_LEVEL, "info");
        System.setProperty(ZMS_LOG_LEVEL, zmsClientLogLevel);

        if (isLoadConfig) {
            try {
                ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
                Class classType = loggerFactory.getClass();
                if ("org.slf4j.impl.Log4jLoggerFactory".equals(classType.getName())) {
                    Class<?> domConfigurator;
                    Object domConfiguratorobj;
                    domConfigurator = Class.forName("org.apache.log4j.xml.DOMConfigurator");
                    domConfiguratorobj = domConfigurator.newInstance();
                    Method configure = domConfiguratorobj.getClass().getMethod("configure", URL.class);
                    URL url = ClientLogger.class.getClassLoader().getResource(log4jResourceFile);
                    configure.invoke(domConfiguratorobj, url);
                } else if ("ch.qos.logback.classic.LoggerContext".equals(classType.getName())) {
                    Class<?> joranConfigurator;
                    Class<?> context = Class.forName("ch.qos.logback.core.Context");
                    Object joranConfiguratoroObj;
                    joranConfigurator = Class.forName("ch.qos.logback.classic.joran.JoranConfigurator");
                    joranConfiguratoroObj = joranConfigurator.newInstance();
                    Method setContext = joranConfiguratoroObj.getClass().getMethod("setContext", context);
                    setContext.invoke(joranConfiguratoroObj, loggerFactory);
                    URL url = ClientLogger.class.getClassLoader().getResource(logbackResourceFile);
                    Method doConfigure =
                            joranConfiguratoroObj.getClass().getMethod("doConfigure", URL.class);
                    doConfigure.invoke(joranConfiguratoroObj, url);


                } else if ("org.apache.logging.slf4j.Log4jLoggerFactory".equals(classType.getName())) {
                    Class<?> joranConfigurator = Class.forName("org.apache.logging.log4j.core.config.Configurator");
                    Method initialize = joranConfigurator.getDeclaredMethod("initialize", String.class, String.class);
                    initialize.invoke(joranConfigurator, "log4j2", log4j2ResourceFile);

                }
            } catch (Exception e) {
                throw new ZmsException("创建logger失败", e);
            }
        }
        return LoggerFactory.getLogger(loggerName);
    }


}

