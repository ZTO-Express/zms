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

package com.zto.zms.collector.kafka;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;


/**
 * <p> Description: </p>
 *
 * @author liangyong
 * @date  2018/8/14
 */
public class MBeanConnector {


    private static final Logger logger = LoggerFactory.getLogger(MBeanConnector.class);

    private static final String JMX = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";

    private final static BiMap<String, JMXConnector> JMS_CONNECTOR_MAP = HashBiMap.create();
    public static final Object object = new Object();


    protected static MBeanServerConnection getJmxConnection(final String uri) {
        try {
            return getJmxConnector(uri).getMBeanServerConnection();
        } catch (IOException e) {
            JMS_CONNECTOR_MAP.remove(uri);
            try {
                return getJmxConnector(uri).getMBeanServerConnection();
            } catch (IOException e1) {
                logger.error("catch exception and try still failed", e1);
                throw new RuntimeException(e1);

            }
        }
    }

    private static JMXConnector getJmxConnector(final String uri) {
        JMXConnector workingConnector = JMS_CONNECTOR_MAP.get(uri);
        if (workingConnector == null) {
            try {
                JMXServiceURL jmxServiceUrl = new JMXServiceURL(String.format(JMX, uri));
                final JMXConnector connector = JMXConnectorFactory.connect(jmxServiceUrl);
                connector.getConnectionId();
                JMS_CONNECTOR_MAP.put(uri, connector);
                connector.addConnectionNotificationListener(
                        (notification, handback) -> {

                            if (notification instanceof JMXConnectionNotification) {
                                if (notification.getType().equals(JMXConnectionNotification.OPENED)
                                        || notification.getType().equals(JMXConnectionNotification.CLOSED)
                                        || notification.getType().equals(JMXConnectionNotification.FAILED)) {

                                    logger.warn("JMXConnection {} notified event:{}", uri, notification.getType());
                                    synchronized (object) {
                                        JMS_CONNECTOR_MAP.inverse().remove(connector);

                                    }

                                }
                            }
                        },
                        (NotificationFilter) notification -> false,
                        null);
                workingConnector = connector;
            } catch (IOException e) {
                logger.error("connect to remote jmx {} error", uri, e);
            }
        }
        if (workingConnector == null) {
            synchronized (object) {
                if (workingConnector == null) {
                    JMXServiceURL jmxServiceURL = null;
                    try {
                        jmxServiceURL = new JMXServiceURL(String.format(JMX, uri));
                        final JMXConnector connector = JMXConnectorFactory.connect(jmxServiceURL);
                        JMS_CONNECTOR_MAP.put(uri, connector);
                    } catch (MalformedURLException e) {
                        logger.error(" jmx connection url error", e);
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        logger.error("can't get jmx connection {} ", jmxServiceURL, e);
                        throw new RuntimeException(e);
                    }


                }
            }

        }
        return workingConnector;
    }

}

