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

package com.zto.zms.zookeeper;

import com.zto.zms.common.BrokerType;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.common.ZmsException;
import com.zto.zms.common.ZmsType;
import com.zto.zms.logger.ZmsLogger;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.metadata.ConsumerGroupMetadata;
import com.zto.zms.metadata.TopicMetadata;
import com.zto.zms.metadata.ZmsMetadata;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by superheizai on 2017/10/12.
 */
public class ZmsZkClient extends ZkClient {

    public static final Logger logger = LoggerFactory.getLogger(ZmsZkClient.class);


    public ZmsZkClient(String zkServers, int sessionTimeout, int connectionTimeout, ZkSerializer zkSerializer) {
        super(zkServers, sessionTimeout, connectionTimeout, zkSerializer);
    }


    public static class Builder {
        private static final ZmsZkClient instance;

        static {
            String zmsServer = System.getProperty(ZmsConst.ZK.ZMS_STARTUP_PARAM);
            String effectiveParam = "ZMS_STARTUP_PARAM";

            if (StringUtils.isEmpty(zmsServer)) {
                throw ZmsException.NO_ZK_EXCEPTION;
            }
            instance = new ZmsZkClient(zmsServer, 20 * 1000, 10 * 1000, ZKStringSerializer$.MODULE$);
            logger.info("zk connected to {} by parameter {}", zmsServer, effectiveParam);
        }
    }

    public static ZmsZkClient getInstance() {
        return Builder.instance;
    }

    public void deleteTopic(String topicName) {
        delete(String.join("/", ZmsConst.ZK.TOPIC_ZKPATH, topicName));
    }

    public void deleteConsumerGroup(String consumerGroupName) {
        delete(String.join("/", ZmsConst.ZK.CONSUMERGROUP_ZKPATH, consumerGroupName));
    }

    public void deleteCluster(String clusterName) {
        delete(String.join("/", ZmsConst.ZK.CLUSTER_ZKPATH, clusterName));
    }

    public ClusterMetadata readClusterMetadata(String clusterName) {
        String clusterData = readData(String.join("/", ZmsConst.ZK.CLUSTER_ZKPATH, clusterName));
        if (StringUtils.isEmpty(clusterData)) {
            ZmsLogger.log.error("cluster {} metadata is empty", clusterName);
            throw ZmsException.CLUSTER_INFO_EXCEPTION;
        }

        Properties clusterProperties = null;
        ClusterMetadata metadata = new ClusterMetadata();
        try {
            clusterProperties = parseProperties(clusterData);
            metadata.setBootAddr(clusterProperties.getProperty(ZmsConst.ZK.ZK_FIELD_BOOTADDDR));
            metadata.setBrokerType(BrokerType.parse(clusterProperties.getProperty(ZmsConst.ZK.ZK_FIELD_BROKERTYPE)));
            metadata.setClusterName(clusterName);
            metadata.setServerIps(clusterProperties.getProperty(ZmsConst.ZK.ZK_FIELD_SERVERIPS));
        } catch (IOException e) {
            ZmsLogger.log.error("parse cluster {}  data info {} error", clusterName, clusterData);
            throw ZmsException.CLUSTER_INFO_EXCEPTION;
        }
        return metadata;
    }

    private Properties parseProperties(String source) throws IOException {
        Properties properties = new Properties();
        if (source == null || source.isEmpty()) {
            return properties;
        }
        try {
            properties.load(new ByteArrayInputStream(source.getBytes(ZmsConst.CHAR_ENCODING)));
        } catch (IOException e) {
            throw e;
        }
        return properties;
    }

    public boolean checkPath(ZmsType type, String name) {
        boolean isTopic = ZmsType.TOPIC.getName().equalsIgnoreCase(type.getName());
        String zkPath = isTopic ?
                String.join("/", ZmsConst.ZK.TOPIC_ZKPATH, name) :
                String.join("/", ZmsConst.ZK.CONSUMERGROUP_ZKPATH, name);
        return exists(zkPath);
    }

    public TopicMetadata readTopicMetadata(String name) {
        return (TopicMetadata) readZkInfo(ZmsType.TOPIC.getName(), name);
    }

    public ConsumerGroupMetadata readConsumerGroupMetadata(String name) {
        return (ConsumerGroupMetadata) readZkInfo(ZmsType.CONSUMER_GRUOP.getName(), name);
    }

    public void writeClusterMetadata(ClusterMetadata clusterMetadata) {
        String zkMeta = buildClusterData(clusterMetadata);
        writeClusterMetadata(clusterMetadata.getClusterName(), zkMeta);
    }

    public void writeTopicMetadata(TopicMetadata topicMetadata) {
        String topicZkData = buildTopicData(topicMetadata);
        writeTopicMetadata(topicMetadata.getName(), topicZkData);
    }

    public void writeConsumerGroupMetadata(ConsumerGroupMetadata cgMetadata) {
        String cgZkData = buildConsumerGroupData(cgMetadata);
        writeConsumerGroupMetadata(cgMetadata.getName(), cgZkData);
    }

    private void writeClusterMetadata(String clusterName, String zkMeta) {
        String clusterPath = String.join("/", ZmsConst.ZK.CLUSTER_ZKPATH, clusterName);
        if (!exists(clusterPath)) {
            createPersistent(clusterPath, true);
        }
        writeData(clusterPath, zkMeta);
    }

    private void writeTopicMetadata(String topicName, String topicZkData) {
        String topicPath = String.join("/", ZmsConst.ZK.TOPIC_ZKPATH, topicName);
        if (!exists(topicPath)) {
            createPersistent(topicPath, true);
        }
        writeData(topicPath, topicZkData);
    }

    private void writeConsumerGroupMetadata(String cgName, String cgZkData) {
        String consumerPath = String.join("/", ZmsConst.ZK.CONSUMERGROUP_ZKPATH, cgName);
        if (!exists(consumerPath)) {
            createPersistent(consumerPath, true);
        }

        writeData(consumerPath, cgZkData);
    }

    private ZmsMetadata readZkInfo(String type, String name) {


        boolean isTopic = ZmsType.TOPIC.getName().equalsIgnoreCase(type);
        ZmsMetadata metadata = isTopic ?
                new TopicMetadata() :
                new ConsumerGroupMetadata();


        String zkPath = isTopic ?
                String.join("/", ZmsConst.ZK.TOPIC_ZKPATH, name) :
                String.join("/", ZmsConst.ZK.CONSUMERGROUP_ZKPATH, name);
        if (!exists(zkPath)) {
            logger.error("zk path not existed: " + zkPath);
            return null;
        }
        String zkData = readData(zkPath);
        Properties properties = null;
        try {
            properties = parseProperties(zkData);
        } catch (IOException e) {
            ZmsLogger.log.error("parse {} data info {} error", type, zkData);
            return null;
        }
        metadata.setName(name);
        metadata.setType(type);
        metadata.setGatedIps(properties.getProperty(ZmsConst.ZK.ZK_FIELD_GATED_IP));
        metadata.setStatisticsLogger(properties.getProperty(ZmsConst.STATISTICS.STATISTICS_METADATA_NAME));
        if (!isTopic) {
            ((ConsumerGroupMetadata) metadata).setBindingTopic(properties.getProperty(ZmsConst.ZK.ZK_FIELD_BINDING_TOPIC));
            ((ConsumerGroupMetadata) metadata).setBroadcast(properties.getProperty(ZmsConst.ZK.ZK_FIELD_BROADCAST));
            ((ConsumerGroupMetadata) metadata).setConsumeFrom(properties.getProperty(ZmsConst.ZK.ZK_FIELD_CONSUME_FROM));
            ((ConsumerGroupMetadata) metadata).setSuspend(properties.getProperty(ZmsConst.ZK.ZK_FIELD_SUSPEND));
        }
        String clusterName = properties.getProperty(ZmsConst.ZK.ZK_FIELD_CLUSTERNAME);
        ClusterMetadata clusterMetadata = readClusterMetadata(clusterName);
        metadata.setClusterMetadata(clusterMetadata);

        String gatedClusterName = properties.getProperty(ZmsConst.ZK.ZK_FIELD_GATED_CLUSTER);
        if (StringUtils.isNotBlank(gatedClusterName)) {
            ClusterMetadata gatedCluster = readClusterMetadata(gatedClusterName);
            metadata.setGatedCluster(gatedCluster);
        }
        return metadata;
    }

    private String buildTopicData(TopicMetadata topicMetadata) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_CLUSTERNAME).append("=").append(topicMetadata.getClusterMetadata().getClusterName());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_TYPE).append("=").append(ZmsType.TOPIC.getName());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_GATED_IP).append("=").append((topicMetadata.getGatedIps() == null ? "" : topicMetadata.getGatedIps()));
        stringBuilder.append(System.lineSeparator());
        if (topicMetadata.getGatedCluster() != null && StringUtils.isNotBlank(topicMetadata.getGatedCluster().getClusterName())) {
            stringBuilder.append(ZmsConst.ZK.ZK_FIELD_GATED_CLUSTER).append("=").append((topicMetadata.getGatedCluster() == null ? "" : topicMetadata.getGatedCluster().getClusterName()));
        } else {
            stringBuilder.append(ZmsConst.ZK.ZK_FIELD_GATED_CLUSTER).append("=");
        }
        return stringBuilder.toString();

    }

    private String buildConsumerGroupData(ConsumerGroupMetadata cgMetadata) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_CLUSTERNAME).append("=").append(cgMetadata.getClusterMetadata().getClusterName());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_TYPE).append("=").append(ZmsType.CONSUMER_GRUOP.getName());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_BINDING_TOPIC).append("=").append(cgMetadata.getBindingTopic());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_BROADCAST).append("=").append(cgMetadata.getBroadcast());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_CONSUME_FROM).append("=").append(cgMetadata.getConsumeFrom());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_SUSPEND).append("=").append(cgMetadata.getSuspend());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_GATED_IP).append("=").append(cgMetadata.getGatedIps() == null ? "" : cgMetadata.getGatedIps());
        stringBuilder.append(System.lineSeparator());
        if (cgMetadata.getGatedCluster() != null && StringUtils.isNotBlank(cgMetadata.getGatedCluster().getClusterName())) {
            stringBuilder.append(ZmsConst.ZK.ZK_FIELD_GATED_CLUSTER).append("=").append(cgMetadata.getGatedCluster() == null ? "" : cgMetadata.getGatedCluster().getClusterName());
        } else {
            stringBuilder.append(ZmsConst.ZK.ZK_FIELD_GATED_CLUSTER).append("=");
        }
        return stringBuilder.toString();
    }


    private String buildClusterData(ClusterMetadata clusterMetadata) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_BOOTADDDR).append("=").append(clusterMetadata.getBootAddr());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_BROKERTYPE).append("=").append(clusterMetadata.getBrokerType().getName());
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(ZmsConst.ZK.ZK_FIELD_SERVERIPS).append("=").append(clusterMetadata.getServerIps());
        return stringBuilder.toString();
    }

}
