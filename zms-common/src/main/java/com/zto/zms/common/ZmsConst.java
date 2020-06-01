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

package com.zto.zms.common;

import org.springside.modules.utils.net.NetUtil;

/**
 * Created by superheizai on 2017/7/26.
 */
public class ZmsConst {

    public static final String DEFAULT_NAME = "DefaultName";
    public static final String DEFAULT_PRODUCER = "DEFAULT_PRODUCER";
    public static final String DEFAULT_CONSUMER = "DEFAULT_CONSUMER";
    public final static String CHAR_ENCODING = "UTF-8";
    public final static String ZMS = "ZMS";
    public static final String ZMS_PORTAL = "zms.portal";
    public static final String ZMS_TOKEN = "zms.token";
    public static final String INFLUXDB = "influxDB";

    public static final String ZMS_VERSION = "1.0.0";
    public static final String ZMS_IP = NetUtil.getLocalHost();

    private ZmsConst() {
    }

    public static class STATISTICS {
        public static final String STATISTICS_CLUSTER_NAME = "statistic_cluster";
        public static final String STATISTICS_METADATA_NAME = "statisticsLogger";
        public static final String STATISTICS_TOPIC_PRODUCERINFO = "statistic_topic_producerinfo";
        public static final String STATISTICS_TOPIC_CONSUMERINFO = "statistic_topic_consumerinfo";
        public static final String STATISTICS_TOPIC_KAFKA_CONSUMERINFO = "statistic_topic_kafka_consumerinfo";
        public static final String STATISTICS_TOPIC_KAFKA_PRODUCERINFO = "statistic_topic_kafka_producerinfo";
        public static final String CHECKSTATUS_TOPIC_NAME = "checkstatus_topic";
        public static final String STATISTICS_CONSUMER_KAFKA_PRODUCERINFO = "statistic_consumer_kafka_producerinfo";
        public static final String STATISTICS_CONSUMER_KAFKA_CONSUMERINFO = "statistic_consumer_kafka_consumerinfo";
        public static final String STATISTICS_CONSUMER_PRODUCERINFO = "statistic_consumer_producerInfo";
        public static final String STATISTICS_CONSUMER_CONSUMERINFO = "statistic_consumer_consumerInfo";
        public static final String PING_TOPIC_NAME = "statistic_ping_topic";
        public static final String PING_CONSUMER_NAME = "statistic_ping_consumer";
    }

    public static class ZK {
        public static final String ZMS_STARTUP_PARAM = "zms_zk";
        public static final String ENV = "env";
        public static final String CLUSTER_ZKPATH = "/zms/cluster";
        public static final String TOPIC_ZKPATH = "/zms/topic";
        public static final String CONSUMERGROUP_ZKPATH = "/zms/consumergroup";
        public static final String ZK_FIELD_CLUSTERNAME = "clusterName";
        public static final String ZK_FIELD_BOOTADDDR = "bootAddr";
        public static final String ZK_FIELD_BROKERTYPE = "brokerType";
        public static final String ZK_FIELD_BINDING_TOPIC = "bindingTopic";
        public static final String ZK_FIELD_BROADCAST = "broadcast";
        public static final String ZK_FIELD_CONSUME_FROM = "consumeFrom";
        public static final String ZK_FIELD_TYPE = "type";
        public static final String ZK_FIELD_SERVERIPS = "serverIps";
        public static final String ZK_FIELD_SUSPEND = "suspend";
        public static final String ZK_FIELD_GATED_IP = "gatedIps";
        public static final String ZK_FIELD_GATED_CLUSTER = "gatedCluster";
    }


    public static class CLIENT_CONFIG {
        public static final String CONSUME_THREAD_MIN = "consumeThreadMin";
        public static final String CONSUME_THREAD_MAX = "consumeThreadMax";
        public static final String CONSUME_MESSAGES_SIZE = "consumeMessagesSize";
        public static final String ROCKETMQ_CONSUME_BATCH = "rocketmqConsumeBatchSize";
        public static final String CONSUME_ORDERLY = "isOrderly";
    }

    public static class Measurement {
        public static final String CLUSTER_NUMBER_INFO = "cluster_number_info";
        public static final String CLUSTER_STRING_INFO = "cluster_string_info";
        public static final String MQ_BROKER_NUMBER_INFO = "mq_broker_number_info";
        public static final String MQ_BROKER_STRING_INFO = "mq_broker_string_info";
        public static final String MQ_CONSUMER_NUMBER_INFO = "mq_consumer_number_info";
        public static final String MQ_CONSUMER_STRING_INFO = "mq_consumer_string_info";
        public static final String MQ_TOPIC_INFO = "mq_topic_info";
        public static final String STATISTIC_TOPIC_PRODUCER_INFO = "statistic_topic_producer_info";
        public static final String STATISTIC_TOPIC_CONSUMER_INFO = "statistic_topic_consumer_info";
        public static final String STATISTIC_TOPIC_OFFSETS_INFO = "statistic_topic_offsets_info";
        public static final String STATISTIC_TOPIC_DAILY_OFFSETS_INFO = "statistic_topic_daily_offsets_info";
        public static final String STATISTIC_CLUSTER_DAILY_OFFSETS_INFO = "statistic_cluster_daily_offsets_info";
        public static final String STATISTIC_DLQ_TOPIC_OFFSETS_INFO = "statistic_dlq_topic_offsets_info";
        public static final String KAFKA_CONSUMER_NUMBER_INFO = "kafka_consumer_number_info";
        public static final String KAFKA_BROKER_INFO = "kafka_broker_info";
        public static final String KAFKA_TOPIC_INFO = "kafka_topic_info";
        public static final String KAFKA_ENV_INFO = "kafka_env_info";
        public static final String TOPIC_TOP_INFO = "topic_top_info";
        public static final String CONSUMER_TOP_INFO = "consumer_top_info";
        public static final String RT_TIME = "rt_time";
        public static final String DLQ_ALERT_MSG_INFO = "dlq_alert_msg_info";
        public static final String TRIGGER_RULES = "trigger_rules";
    }

}

