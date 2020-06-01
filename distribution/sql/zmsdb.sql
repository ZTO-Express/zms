-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at

--     http://www.apache.org/licenses/LICENSE-2.0

-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

create database zms DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

use zms;

CREATE TABLE if not exists `topic`
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT,
    `create_date`   timestamp    NOT NULL                            DEFAULT CURRENT_TIMESTAMP,
    `modify_date`   timestamp    NOT NULL                            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `applicant`     varchar(255) NOT NULL                            DEFAULT '' COMMENT '申请人',
    `domain`        varchar(255) NOT NULL                            DEFAULT '' COMMENT '申请域(appId)',
    `name`          varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '主题名称',
    `partitions`    int(10)                                          DEFAULT '0' COMMENT 'rocketMq主题队列;kafka主题分区',
    `cluster_id`    bigint(20)                                       DEFAULT '0' COMMENT '集群ID,废弃字段',
    `create_from`   int(10)                                          DEFAULT NULL COMMENT '',
    `status`        int(11)      NOT NULL                            DEFAULT '0' COMMENT '状态',
    `tps`           int(11)      NOT NULL                            DEFAULT '0' COMMENT '发送速度',
    `msgSzie`       int(11)      NOT NULL                            DEFAULT '0' COMMENT '消息体(字节)',
    `memo`          varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '备注字段',
    `gated_ips`     varchar(255)                                     DEFAULT '' COMMENT '灰度集群IP,废弃字段',
    `gated_cluster` bigint(20)                                       DEFAULT NULL COMMENT '灰度集群ID,废弃字段',
    `alert_emails`  varchar(2048)                                    DEFAULT NULL COMMENT '邮件通知列表',
    `applicant_no`  varchar(64)                                      DEFAULT '' COMMENT '申请人工号',
    `replication`   tinyint(4)   NOT NULL                            DEFAULT '3' COMMENT 'kafka主题副本数',
    PRIMARY KEY (`id`),
    UNIQUE KEY `t_unique_key` (`name`),
    KEY `index_applicant` (`applicant`),
    KEY `index_topic_cluster_id` (`cluster_id`),
    KEY `index_domain` (`domain`),
    KEY `index_name` (`name`),
    KEY `index_topic_meta_id` (`create_from`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='主题表';

CREATE TABLE if not exists `topic_environment_ref`
(
    `id`               int(11)    NOT NULL AUTO_INCREMENT,
    `topic_id`         bigint(20) NOT NULL COMMENT '主题id',
    `service_id`       int(11)      DEFAULT NULL COMMENT '服务id',
    `environment_id`   int(11)      DEFAULT NULL COMMENT '环境id',
    `gated_ips`        varchar(256) DEFAULT '' COMMENT '灰度集群ip',
    `gated_service_id` int(11)      DEFAULT NULL COMMENT '灰度集群服务id',
    `creator`          varchar(32)  DEFAULT 'system',
    `modifier`         varchar(32)  DEFAULT 'system',
    `gmt_create`       datetime     DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_topic_id` (`topic_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='主题环境管理表';
alter table topic_environment_ref add unique unidx_topicid_envid(topic_id,environment_id);

CREATE TABLE if not exists `consumer`
(
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT,
    `create_date`      timestamp    NOT NULL                            DEFAULT CURRENT_TIMESTAMP,
    `modify_date`      timestamp    NOT NULL                            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `applicant`        varchar(255) NOT NULL                            DEFAULT '' COMMENT '申请人',
    `broadcast`        tinyint(1)   NOT NULL                            DEFAULT '0' COMMENT '是否广播消费',
    `domain`           varchar(255) NOT NULL                            DEFAULT '' COMMENT '申请域(appId)',
    `name`             varchar(255)                                     DEFAULT NULL COMMENT '消费组名称',
    `topic_id`         bigint(20)   NOT NULL                            DEFAULT '0' COMMENT '主题ID',
    `consumer_from`    tinyint(1)   NOT NULL                            DEFAULT '0' COMMENT '是否最早消费',
    `status`           int(11)      NOT NULL                            DEFAULT '0' COMMENT '状态',
    `delay_threadhold` bigint(15)   NOT NULL                            DEFAULT '10000' COMMENT '积压阈值',
    `cluster_id`       bigint(20)                                       DEFAULT NULL COMMENT '集群id,废弃字段',
    `memo`             varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '备注字段',
    `update_server`    tinyint(1)                                       DEFAULT '0' COMMENT '是否需要更新到集群',
    `update_zk`        tinyint(1)                                       DEFAULT '0' COMMENT '是否需要更新到zk',
    `gated_ips`        varchar(255)                                     DEFAULT NULL COMMENT '灰度集群IP,废弃字段',
    `gated_cluster`    bigint(20)                                       DEFAULT NULL COMMENT '灰度集群ID,废弃字段',
    `applicant_no`     varchar(64)                                      DEFAULT '' COMMENT '申请人工号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `t_unique_key` (`name`),
    KEY `index_applicant` (`applicant`),
    KEY `index_domain` (`domain`),
    KEY `index_name` (`name`),
    KEY `index_topic_id` (`topic_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='消费组表';

CREATE TABLE if not exists `consumer_environment_ref`
(
    `id`               int(11)    NOT NULL AUTO_INCREMENT,
    `consumer_id`      bigint(20) NOT NULL COMMENT '消费组id',
    `environment_id`   int(11)    NOT NULL COMMENT '环境id',
    `service_id`       int(11)      DEFAULT NULL COMMENT '服务id',
    `gated_ips`        varchar(255) DEFAULT NULL COMMENT '灰度集群ip',
    `gated_service_id` int(11)      DEFAULT NULL COMMENT '灰度集群ID',
    `creator`          varchar(32)  DEFAULT 'system',
    `modifier`         varchar(32)  DEFAULT 'system',
    `gmt_create`       datetime     DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`     datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_consumer_id` (`consumer_id`),
    KEY `idx_environment_id` (`environment_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='消费组环境管理表';
alter table consumer_environment_ref add unique unidx_consumerid_envid(consumer_id,environment_id);

CREATE TABLE if not exists `alert_rule`
(
    `id`               bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `create_date`      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    `modify_date`      timestamp    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `field`            varchar(255) NOT NULL DEFAULT '' COMMENT '告警字段',
    `name`             varchar(255) NOT NULL DEFAULT '' COMMENT 'consumer/topic/cluster名字',
    `type`             varchar(50)  NOT NULL DEFAULT '' COMMENT 'consumer/topic/cluster',
    `operator`         varchar(50)  NOT NULL DEFAULT '' COMMENT '比较符号,> < ==',
    `target`           int(11)      NOT NULL DEFAULT '0' COMMENT '触发阈值',
    `scope`            varchar(50)  NOT NULL DEFAULT '' COMMENT '检测区间',
    `trigger_times`    int(11)      NOT NULL DEFAULT '0' COMMENT '检测时间啊区间内，触发几次',
    `description`      varchar(255) NOT NULL DEFAULT '' COMMENT '说明',
    `influx_sql`       varchar(500)          DEFAULT '' COMMENT '对应rule的sql',
    `trigger_operator` varchar(50)           DEFAULT '' COMMENT '阈值次数比较符号，> < == !=',
    `cluster`          varchar(50)  NOT NULL DEFAULT '' COMMENT '所在集群',
    `alert_user`       varchar(50)  NOT NULL DEFAULT '' COMMENT '告警接收人',
    `alert_email`      varchar(50)           DEFAULT '' COMMENT '告警接收邮箱',
    `alert_mobile`     varchar(50)  NOT NULL DEFAULT '' COMMENT '告警接收电话',
    `alert_dingding`   varchar(500)          DEFAULT '' COMMENT '告警接收钉钉hook',
    `effect`           tinyint(1)   NOT NULL DEFAULT '1' COMMENT '是否生效,默认生效',
    `effect_from`      varchar(10)  NOT NULL DEFAULT '00:00' COMMENT '告警生效开始时间',
    `effect_to`        varchar(10)  NOT NULL DEFAULT '23:59' COMMENT '告警生效结束时间',
    `alert_user_no`    varchar(64)           DEFAULT '' COMMENT '告警接收人工号',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`alert_user`),
    KEY `idx_name` (`name`),
    KEY `idx_cluster` (`cluster`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='告警规则表';

CREATE TABLE if not exists `alert_rule_environment_ref`
(
    `id`             int(11)    NOT NULL AUTO_INCREMENT,
    `alert_rule_id`  bigint(20) NOT NULL COMMENT '告警规则id',
    `environment_id` int(11)    NOT NULL COMMENT '环境id',
    `creator`        varchar(32) DEFAULT 'system',
    `modifier`       varchar(32) DEFAULT 'system',
    `gmt_create`     datetime    DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`   datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_alert_rule_id` (`alert_rule_id`),
    KEY `idx_environment_id` (`environment_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='告警规则环境管理表';

CREATE TABLE if not exists `operation_log`
(
    `id`          bigint(20)    NOT NULL AUTO_INCREMENT,
    `create_date` timestamp     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `opeartor`    varchar(255)  NOT NULL DEFAULT '' COMMENT '操作人',
    `name`        varchar(255)  NOT NULL DEFAULT '' COMMENT '路径',
    `operation`   varchar(255)  NOT NULL DEFAULT '' COMMENT '操作类型',
    `content`     varchar(2000) NOT NULL DEFAULT '' COMMENT '内容',
    PRIMARY KEY (`id`),
    KEY `index_opeartor` (`opeartor`),
    KEY `index_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='操作日志记录表';

CREATE TABLE if not exists `zms_environment`
(
    `id`                  int(11)      NOT NULL AUTO_INCREMENT,
    `environment_name`    varchar(128) NOT NULL DEFAULT '' COMMENT '环境名称',
    `environment_status`  varchar(16)  NOT NULL COMMENT 'ENABLE,DISABLE',
    `zk_service_id`       int(11)               DEFAULT NULL COMMENT '用于存储源数据',
    `influxdb_service_id` int(11)               DEFAULT NULL COMMENT '用于存储集群采集数据',
    `order_num`           int(11)               DEFAULT '0' COMMENT '排序编号',
    `creator`             varchar(32)           DEFAULT 'system',
    `modifier`            varchar(32)           DEFAULT 'system',
    `gmt_create`          datetime              DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`        datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unidx_environment_name_` (`environment_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='环境表';

CREATE TABLE if not exists `zms_host`
(
    `id`                int(11)      NOT NULL AUTO_INCREMENT,
    `environment_id`    int(11)      NOT NULL COMMENT '环境id',
    `host_ip`           varchar(64)  NOT NULL DEFAULT '' COMMENT 'ip',
    `host_user`         varchar(64)           DEFAULT '' COMMENT '用户',
    `host_name`         varchar(128) NOT NULL DEFAULT '' COMMENT '主机名称',
    `host_status`       varchar(16)  NOT NULL DEFAULT 'DISABLE' COMMENT '主机状态',
    `ssh_port`          varchar(8)            DEFAULT NULL COMMENT 'ssh端口',
    `remark`            varchar(128)          DEFAULT '' COMMENT '备注',
    `last_monitor_time` datetime              DEFAULT NULL COMMENT '最后监听时间',
    `total_mem`         varchar(64)           DEFAULT NULL COMMENT '内存大小',
    `cpu_rate`          varchar(64)           DEFAULT NULL COMMENT 'cpu占用率',
    `creator`           varchar(32)           DEFAULT 'system',
    `modifier`          varchar(32)           DEFAULT 'system',
    `gmt_create`        datetime              DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`      datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unidx_envid_hostip` (`environment_id`, `host_ip`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='主机';

CREATE TABLE if not exists `zms_service`
(
    `id`             int(11)      NOT NULL AUTO_INCREMENT,
    `environment_id` int(11)      NOT NULL COMMENT '环境id',
    `server_name`    varchar(128) NOT NULL COMMENT '服务名称',
    `server_type`    varchar(32)  NOT NULL COMMENT '服务类型,ZMS_COLLECTOR,ZMS_ALERT,ZOOKEEPER,INFLUX_DB,KAFKA,ROCKETMQ',
    `server_status`  varchar(16)  NOT NULL DEFAULT 'DISABLE' COMMENT 'ENABLE,DISABLE',
    `is_deleted`     smallint(6)           DEFAULT '0' COMMENT '是否删除',
    `creator`        varchar(32)           DEFAULT 'system',
    `modifier`       varchar(32)           DEFAULT 'system',
    `gmt_create`     datetime              DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`   datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_env_id_server_name` (`environment_id`, `server_name`),
    KEY `idx_env_id_server_type` (`environment_id`, `server_type`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='服务表';

CREATE TABLE if not exists `service_instance`
(
    `id`              int(11)      NOT NULL AUTO_INCREMENT,
    `service_id`      int(11)      NOT NULL COMMENT '服务id',
    `instance_type`   varchar(16)  NOT NULL COMMENT '服务实例类型,BROKER,NAME_SERVER,INSTANCE',
    `instance_name`   varchar(128) NOT NULL COMMENT '服务实例名',
    `host_id`         int(11)      NOT NULL COMMENT '主机id',
    `instance_status` varchar(16)  NOT NULL DEFAULT 'STOP' COMMENT 'START,STOP',
    `is_deleted`      smallint(6)           DEFAULT '0' COMMENT '是否删除',
    `creator`         varchar(32)           DEFAULT 'system',
    `modifier`        varchar(32)           DEFAULT 'system',
    `gmt_create`      datetime              DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`    datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_service_id` (`service_id`),
    KEY `idx_host_id` (`host_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='服务实例表';

CREATE TABLE if not exists `service_property`
(
    `id`              int(11)      NOT NULL AUTO_INCREMENT,
    `service_type`    varchar(32)  NOT NULL COMMENT '服务类型,ZMS_COLLECTOR,ZMS_ALERT,ZOOKEEPER,INFLUXDB,KAFKA,ROCKETMQ,ZMS-BACKUP-CLUSTER',
    `instance_type`   varchar(16)           DEFAULT NULL COMMENT '服务类型,BROKER,NAME_SERVER,INSTANCE',
    `property_name`   varchar(128) NOT NULL COMMENT '属性名称',
    `property_group`  varchar(64)           DEFAULT '' COMMENT '配置分组',
    `is_dependencies` smallint(6)           DEFAULT '0' COMMENT '是否依赖服务，如果是通过property_name指定依赖服务类型',
    `choose_type`     varchar(16)  NOT NULL COMMENT 'TEXT,CHECKBOX,RADIO,LIST',
    `description`     varchar(512)          DEFAULT '' COMMENT '属性描述',
    `display_group`   varchar(128) NOT NULL DEFAULT '' COMMENT '展示组',
    `display_name`    varchar(128) NOT NULL DEFAULT '' COMMENT '展示名称',
    `default_value`   varchar(1024)         DEFAULT '' COMMENT '默认值',
    `is_required`     smallint(6)           DEFAULT '0' COMMENT '是否必须',
    `is_read_only`    smallint(6)           DEFAULT '0' COMMENT '是否只读',
    `value_type`      varchar(16)  NOT NULL DEFAULT 'STRING' COMMENT 'STRING,NUMBER,DOUBLE',
    `min_len`         int(11)               DEFAULT NULL COMMENT '最小长度',
    `max_len`         int(11)               DEFAULT NULL COMMENT '最大长度',
    `min_value`       int(11)               DEFAULT NULL COMMENT '最小值',
    `max_value`       int(11)               DEFAULT NULL COMMENT '最大值',
    `scope`           varchar(128) NOT NULL COMMENT '配置范围，SERVICE_CONFIG_GROUP:添加服务配置,SERVICE：服务配置,INSTANCE_CONFIG_GROUP:添加实例配置,INSTANCE：实例配置',
    `order_num`       int(11)               DEFAULT '0',
    `creator`         varchar(32)           DEFAULT 'system',
    `modifier`        varchar(32)           DEFAULT 'system',
    `gmt_create`      datetime              DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`    datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `conf_api_key`    varchar(64)           DEFAULT '',
    PRIMARY KEY (`id`),
    KEY `idx_service_type_property_name` (`service_type`, `property_name`),
    KEY `idx_property_name` (`property_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='服务属性表';

CREATE TABLE if not exists `service_property_value`
(
    `id`                int(11)       NOT NULL AUTO_INCREMENT,
    `property_id`       int(11)       NOT NULL COMMENT '属性',
    `property_value`    varchar(1024) NOT NULL DEFAULT '' COMMENT '可配置值',
    `display_value`     varchar(1024) NOT NULL DEFAULT '' COMMENT '显示值',
    `is_override_scope` smallint(6)            DEFAULT '0' COMMENT '是否覆盖',
    `creator`           varchar(32)            DEFAULT 'system',
    `modifier`          varchar(32)            DEFAULT 'system',
    `gmt_create`        datetime               DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`      datetime               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_property_id` (`property_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='属性配置值';

CREATE TABLE if not exists `service_property_value_ref`
(
    `id`                int(11)       NOT NULL AUTO_INCREMENT,
    `service_id`        int(11)       NOT NULL COMMENT '服务id',
    `instance_id`       int(11)                DEFAULT NULL COMMENT '实例id',
    `property_id`       int(11)       NOT NULL COMMENT '属性id',
    `property_value_id` int(11)                DEFAULT NULL COMMENT '属性值id',
    `current_value`     varchar(1024) NOT NULL COMMENT '配置值',
    `started_value`     varchar(1024) NOT NULL DEFAULT '' COMMENT '上次启动的值',
    `creator`           varchar(32)            DEFAULT 'system',
    `modifier`          varchar(32)            DEFAULT 'system',
    `gmt_create`        datetime               DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`      datetime               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_service_id_property_id` (`service_id`, `property_id`),
    KEY `idx_instance_id_property_id` (`instance_id`, `property_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='服务属性，属性值记录表';

CREATE TABLE if not exists `service_process`
(
    `id`                int(11) NOT NULL AUTO_INCREMENT,
    `service_id`        int(11) NOT NULL COMMENT '服务id',
    `instance_id`       int(11) NOT NULL COMMENT '实例id',
    `running_status`    varchar(32) DEFAULT 'STOPPED' COMMENT '运行状态(RunningStatusEnum),STARTING,RUNNING,STOPPING,STOPPED,BACKOFF,FATAL,EXITED,UNKNOWN',
    `last_monitor_time` datetime    DEFAULT NULL COMMENT '服务检测时间',
    `status`            varchar(16) DEFAULT 'INIT' COMMENT '配置状态,INIT,SUCCESS,FAILURE',
    `creator`           varchar(32) DEFAULT 'system',
    `modifier`          varchar(32) DEFAULT 'system',
    `gmt_create`        datetime    DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`      datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_service_id_instance_id` (`service_id`, `instance_id`),
    KEY `idx_instance_id` (`instance_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='服务进程表';

CREATE TABLE if not exists `process_property_value_ref`
(
    `id`                 int(11)       NOT NULL AUTO_INCREMENT,
    `service_id`         int(11)       NOT NULL,
    `instance_id`        int(11)                DEFAULT NULL,
    `service_process_id` int(11)       NOT NULL COMMENT '服务进程id',
    `property_id`        int(11)       NOT NULL,
    `property_value_id`  int(11)                DEFAULT NULL,
    `current_value`      varchar(1024) NOT NULL COMMENT '配置值',
    `real_value`         varchar(1024) NOT NULL DEFAULT '' COMMENT '配置文件值',
    `creator`            varchar(32)            DEFAULT 'system',
    `modifier`           varchar(32)            DEFAULT 'system',
    `gmt_create`         datetime               DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified`       datetime               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `service_type`    varchar(32)  NOT NULL COMMENT '服务类型,ZMS_COLLECTOR,ZMS_ALERT,ZOOKEEPER,INFLUXDB,KAFKA,ROCKETMQ,ZMS-BACKUP-CLUSTER',
    `instance_type`      varchar(16)            DEFAULT NULL COMMENT '服务类型,BROKER,NAMESVR,INSTANCE',
    `property_name`      varchar(128)  NOT NULL DEFAULT '' COMMENT '属性名称',
    `conf_api_key`       varchar(64)   NOT NULL DEFAULT '',
    `property_group`     varchar(64)            DEFAULT '' COMMENT '配置分组',
    PRIMARY KEY (`id`),
    KEY `idx_process_id_property_id` (`service_process_id`, `property_id`),
    KEY `idx_property_id` (`property_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='服务进程启动参数记录';

CREATE TABLE if not exists `config_api`
(
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT,
    `api_key`      varchar(64)  NOT NULL DEFAULT '' COMMENT 'api键值',
    `file_name`    varchar(128) NOT NULL DEFAULT '' COMMENT '文件名',
    `file_type`    varchar(32)  NOT NULL DEFAULT '' COMMENT '文件类型:ini,properties',
    `config_scope` varchar(32)  NOT NULL DEFAULT '' COMMENT 'file,environment',
    `creator`      varchar(32)           DEFAULT 'system',
    `modifier`     varchar(32)           DEFAULT 'system',
    `gmt_create`   datetime              DEFAULT CURRENT_TIMESTAMP,
    `gmt_modified` datetime              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_api_key` (`api_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8 COMMENT ='生成文件配置api';




-- init data




-- 服务配置config_api
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('zookeeper-server-config', 'zoo.cfg', 'properties', 'server');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('ZOOKEEPER_SERVER_ENV_OPTS', '', '', 'environment');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('ZOOKEEPER_SERVER_JVM_OPTS', '', '', 'jvm');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('cluster', '', '', 'cluster');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('KAFKA_LOG4J_OPTS', '', '', 'environment');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('KAFKA_HEAP_OPTS', '', '', 'jvm');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('kafka-server-config', 'server.properties', 'properties', 'server');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('KAFKA_JMX_OPTS', '', '', 'jmx');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('ZMSCOLLECTOR_ENV_OPTS', '', '', 'environment');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('rocketmq-broker-config', 'broker.properties', 'properties', 'server');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('rocketmq-namesvr-config', 'namesrv.properties', 'properties', 'server');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('influxdb-server-config', 'influxdb.conf', 'ini', 'server');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('ZMSALERT_ENV_OPTS', '', '', 'environment');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('JAVA_OPT_EXT', '', '', 'jvm');
INSERT INTO config_api (api_key, file_name, file_type, config_scope) VALUES ('zms-backup-cluster-config', 'config.properties', 'properties', 'server');


-- 服务属性、属性值
-- =======rocketmq=========
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'brokerRole', '', 0, 'RADIO', 'Broker 角色类型。如果对消息的可靠性要求比较严格，可以采用 SYNC_MASTER加SLAVE的部署方式。如果对消息可靠性要求不高，可以采用ASYNC_MASTER加SLAVE的部署方式 Broker 角色分为 ASYNC_MASTER（异步主机）、SYNC_MASTER（同步主机）以及SLAVE（从机）。如果对消息的可靠性要求比较严格，可以采用 SYNC_MASTER加SLAVE的部署方式。如果对消息可靠性要求不高，可以采用ASYNC_MASTER加SLAVE的部署方式', 'SERVICE', '服务范围', 'ASYNC_MASTER', 0, 0, 'STRING', null, null, null, null, 'INSTANCE_CONFIG_GROUP', 0, 'rocketmq-broker-config');
INSERT INTO service_property_value (property_id, property_value, display_value, is_override_scope) VALUES ((select max(id) from service_property), 'ASYNC_MASTER', '异步主机', 0);
INSERT INTO service_property_value (property_id, property_value, display_value, is_override_scope) VALUES ((select max(id) from service_property), 'SYNC_MASTER', '同步主机', 0);

INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'flushDiskType', '', 0, 'RADIO', '刷盘类型。SYNC_FLUSH 模式下的 broker 保证在收到确认生产者之前将消息刷盘。ASYNC_FLUSH 模式下的 broker 则利用刷盘一组消息的模式，可以取得更好的性能', 'SERVICE', '服务范围', 'ASYNC_FLUSH', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property_value (property_id, property_value, display_value, is_override_scope) VALUES ((select max(id) from service_property), 'SLAVE', '从机', 0);
INSERT INTO service_property_value (property_id, property_value, display_value, is_override_scope) VALUES ((select max(id) from service_property), 'SYNC_FLUSH', '同步刷新', 0);
INSERT INTO service_property_value (property_id, property_value, display_value, is_override_scope) VALUES ((select max(id) from service_property), 'ASYNC_FLUSH', '异步处理', 0);

INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-Xmn', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '4g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-Xms', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '8g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-Xmx', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '8g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'abortFile', '', 0, 'TEXT', 'abort 文件存储路径', 'SERVICE', '服务范围', '/data/rocketmq/store/abort', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'autoCreateSubscriptionGroup', '', 0, 'TEXT', '是否允许 Broker 自动创建订阅组', 'SERVICE', '服务范围', 'false', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'autoCreateTopicEnable', '', 0, 'TEXT', '是否允许 Broker 自动创建Topic', 'SERVICE', '服务范围', 'false', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'brokerId', '', 0, 'TEXT', 'broker id, 0 表示 master, 其他的正整数表示 slave', 'SERVICE', '服务范围', '0', 1, 0, 'STRING', null, null, null, null, 'INSTANCE_CONFIG_GROUP', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'brokerName', '', 0, 'TEXT', 'broker 所属的 Cluser 名称', 'SERVICE', '服务范围', '', 1, 0, 'STRING', null, null, null, null, 'INSTANCE_CONFIG_GROUP', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'checkTransactionMessageEnable', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'deleteWhen', '', 0, 'TEXT', '在每天的什么时间删除已经超过文件保留时间的 commit log', 'SERVICE', '服务范围', '04', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'diskMaxUsedSpaceRatio', '', 0, 'TEXT', '检测物理文件磁盘空间', 'SERVICE', '服务范围', '88', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'fileReservedTime', '', 0, 'TEXT', '以小时计算的文件保留时间', 'SERVICE', '服务范围', '48', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'listenPort', '', 0, 'TEXT', 'listenPort', 'SERVICE', '服务范围', '10911', 1, 0, 'NUMBER', null, null, 0, 65535, 'INSTANCE_CONFIG_GROUP', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', 'listenPort', '', 0, 'TEXT', 'listenPort', 'SERVICE', '服务范围', '9876', 1, 0, 'NUMBER', null, null, 0, 65535, 'INSTANCE_CONFIG_GROUP', 0, 'rocketmq-namesvr-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'mapedFileSizeConsumeQueue', '', 0, 'TEXT', 'ConsumeQueue每个文件默认存多少条，根据业务情况调整', 'SERVICE', '服务范围', '', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'maxMessageSize', '', 0, 'TEXT', '限制的消息大小 ', 'SERVICE', '服务范围', '65536', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'maxTransferCountOnMessageInMemory', '', 0, 'TEXT', '单次pull消息（内存）传输的最大条数', 'SERVICE', '服务范围', '', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'pullMessageThreadPoolNums', '', 0, 'TEXT', '服务端处理消息拉取线程池线程数量 默认为16加上当前操作系统CPU核数的两倍', 'SERVICE', '服务范围', '', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'storeCheckpoint', '', 0, 'TEXT', 'checkpoint 文件存储路径', 'SERVICE', '服务范围', '/data/rocketmq/store/checkpoint', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'storePathCommitLog', '', 0, 'TEXT', 'commitLog存储路径', 'SERVICE', '服务范围', '/data/rocketmq/store/commitlog', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'storePathConsumeQueue', '', 0, 'TEXT', '消费队列存储路径', 'SERVICE', '服务范围', '/data/rocketmq/store/consumequeue', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'storePathIndex', '', 0, 'TEXT', '消息索引存储路径', 'SERVICE', '服务范围', '/data/rocketmq/store/index', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'storePathRootDir', '', 0, 'TEXT', '存储路径', 'SERVICE', '服务范围', '/data/rocketmq/store', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'transferMsgByHeap', '', 0, 'TEXT', '消息传输是否使用堆内存', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'transientStorePoolEnable', '', 0, 'TEXT', 'Commitlog是否开启 transientStorePool机制，默认为 false', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'SERVICE', 'version', '', 0, 'TEXT', 'ROCKETMQ版本', 'SERVICE', '服务范围', '4.1', 1, 0, 'TEXT', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, '');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', 'warmMapedFileEnable', '', 0, 'TEXT', '是否温和地使用 MappedFile如果为true,将不强制将内存映射文件锁定在内存中', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'rocketmq-broker-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-Xmx', '', 0, 'TEXT', '堆内存最大值（namesvr）', 'SERVICE', '服务范围', '4g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-Xms', '', 0, 'TEXT', '堆内存初始值（namesvr）', 'SERVICE', '服务范围', '4g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-Xmn', '', 0, 'TEXT', '年轻代大小（namesvr）', 'SERVICE', '服务范围', '2g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-XX:PermSize', '', 0, 'TEXT', '持久代初始值', 'SERVICE', '服务范围', '320m', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-XX:NewSize', '', 0, 'TEXT', '年轻代大小', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-XX:NewRatio', '', 0, 'TEXT', '年轻代(包括Eden和两个Survivor区)与年老代的比值(除去持久代)', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-XX:MetaspaceSize', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-XX:MaxPermSize', '', 0, 'TEXT', '持久代最大值', 'SERVICE', '服务范围', '128m', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-XX:MaxNewSize', '', 0, 'TEXT', '年轻代最大值', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-XX:MaxMetaspaceSize', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-XX:MaxDirectMemorySize', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '15g', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'NAMESVR', '-Xss', '', 0, 'TEXT', '每个线程的堆栈大小', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-XX:MaxDirectMemorySize', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '15g', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-XX:PermSize', '', 0, 'TEXT', '持久代初始值（broker）', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-XX:NewSize', '', 0, 'TEXT', '年轻代大小', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-XX:NewRatio', '', 0, 'TEXT', '年轻代(包括Eden和两个Survivor区)与年老代的比值(除去持久代)', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-XX:MetaspaceSize', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-XX:MaxPermSize', '', 0, 'TEXT', '持久代最大值（broker）', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-XX:MaxNewSize', '', 0, 'TEXT', '年轻代最大值', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-XX:MaxMetaspaceSize', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ROCKETMQ', 'BROKER', '-Xss', '', 0, 'TEXT', '每个线程的堆栈大小', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'JAVA_OPT_EXT');

-- =======rocketmq=========

-- ========kafka=========
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'listeners', '', 0, 'TEXT', 'broker监听端口', 'SERVICE', '服务范围', '9092', 1, 0, 'NUMBER', null, null, 0, 65535, 'INSTANCE_CONFIG_GROUP', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'SERVICE', 'version', '', 0, 'TEXT', 'KAFKA版本', 'SERVICE', '服务范围', '2.1', 1, 0, 'TEXT', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, '');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'SERVICE', 'ZOOKEEPER', '', 1, 'RADIO', 'zookeeper:zookeeper.connect', 'SERVICE', '服务范围', '', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'cluster');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'broker.id', '', 0, 'TEXT', '此服务器的代理id。如果未设置，将生成唯一的代理id。为了避免zookeeper生成的代理id与用户配置的代理id之间的冲突，生成的代理id从reserved.broker.max开始。id + 1。', '', '服务范围', '1', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE_CONFIG_GROUP', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'num.network.threads', '', 0, 'TEXT', '服务器用于从网络接收请求并向网络发送响应的线程数', '', '服务范围', '6', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'num.io.threads', '', 0, 'TEXT', '服务器用于处理请求的线程数，可能包括磁盘I/O', '', '服务范围', '16', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'socket.send.buffer.bytes', '', 0, 'TEXT', '套接字服务器套接字的SO_SNDBUF缓冲区。如果值为-1，则使用OS默认值。', '', '服务范围', '102400', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'socket.receive.buffer.bytes', '', 0, 'TEXT', '套接字服务器套接字的SO_RCVBUF缓冲区。如果值为-1，则使用OS默认值。', '', '服务范围', '102400', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'socket.request.max.bytes', '', 0, 'TEXT', '套接字请求中的最大字节数', '', '服务范围', '104857600', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'log.dirs', '', 0, 'TEXT', '保存日志数据的目录。如果未设置，则使用log中的值。使用dir', '', '服务范围', '/data/kafka', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'num.partitions', '', 0, 'TEXT', '每个主题的默认日志分区数', '', '服务范围', '10', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'num.recovery.threads.per.data.dir', '', 0, 'TEXT', '在启动时用于日志恢复和在关闭时用于刷新的每个数据目录的线程数', '', '服务范围', '4', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'offsets.topic.replication.factor', '', 0, 'TEXT', '偏移量主题的复制因子(设置得更高以确保可用性)。在集群大小满足此复制因子要求之前，内部主题创建将失败。', '', '服务范围', '3', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'transaction.state.log.min.isr', '', 0, 'TEXT', '覆盖min.insync。事务主题的副本配置。', '', '服务范围', '1', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'transaction.state.log.replication.factor', '', 0, 'TEXT', '事务主题的复制因子(设置得更高以确保可用性)。在集群大小满足此复制因子要求之前，内部主题创建将失败。', '', '服务范围', '3', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'log.retention.hours', '', 0, 'TEXT', '删除日志文件之前保留日志文件的小时数(以小时为单位)', '', '服务范围', '72', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'log.segment.bytes', '', 0, 'TEXT', '单个日志文件的最大大小', '', '服务范围', '1073741824', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'log.retention.check.interval.ms', '', 0, 'TEXT', '日志清理器检查日志是否符合删除条件的频率(以毫秒为单位)', '', '服务范围', '300000', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'zookeeper.connection.timeout.ms', '', 0, 'TEXT', '客户端等待与Zookeeper建立连接的最长时间。 如果未设置，则使用zookeeper.session.timeout.ms中的值', '', '服务范围', '6000', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'group.initial.rebalance.delay.ms', '', 0, 'TEXT', '在执行第一次重新平衡之前，组协调器将等待更多消费者加入新组的时间。更长的延迟意味着可能更少的重新平衡，但是增加了处理开始之前的时间。', '', '服务范围', '3000', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'com.sun.management.jmxremote.port', '', 0, 'TEXT', '', '', '服务范围', '9999', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'KAFKA_JMX_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'com.sun.management.jmxremote.authenticate', '', 0, 'RADIO', '', '', '服务范围', 'false', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'KAFKA_JMX_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'com.sun.management.jmxremote.ssl', '', 0, 'RADIO', '', '', '服务范围', 'false', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'KAFKA_JMX_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'delete.topic.enable', '', 0, 'TEXT', '启用删除主题。 如果关闭此配置，则通过管理工具删除主题将无效。', '', '服务范围', 'true', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'log.cleaner.enable', '', 0, 'TEXT', '启用日志清除器进程以在服务器上运行', '', '服务范围', 'true', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'controlled.shutdown.enable', '', 0, 'TEXT', '启用日志清除器进程以在服务器上运行', '', '服务范围', 'true', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'unclean.leader.election.enable', '', 0, 'TEXT', '指示是否启用不在ISR集中的副本作为最后的选择方法，使其被选作领导者，即使这样做可能会导致数据丢失。', '', '服务范围', 'false', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'default.replication.factor', '', 0, 'TEXT', '自动创建的主题的默认复制因子', '', '服务范围', '3', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'auto.create.topics.enable', '', 0, 'TEXT', '在服务器上启用主题自动创建', '', '服务范围', 'false', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'num.replica.fetchers', '', 0, 'TEXT', '用于复制来自源代理的消息的访存线程的数量。 增大此值可以增加关注代理中的I / O并行度。', '', '服务范围', '2', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', 'offsets.retention.minutes', '', 0, 'TEXT', '消费群体失去所有消费群体（即变空）后，其抵销将在此保留期内保留，直到被丢弃。 对于独立使用者（使用手动分配），偏移量将在上次提交时间加上此保留期后过期。', '', '服务范围', '10080', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'kafka-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', '-Xmx', '', 0, 'TEXT', '堆内存最大值', 'SERVICE', '服务范围', '8g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'KAFKA_HEAP_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', '-Xms', '', 0, 'TEXT', '堆内存初始值', 'SERVICE', '服务范围', '8g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'KAFKA_HEAP_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', '-Xmn', '', 0, 'TEXT', '年轻代大小', 'SERVICE', '服务范围', '4g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'KAFKA_HEAP_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', '-XX:NewRatio', '', 0, 'TEXT', '年轻代(包括Eden和两个Survivor区)与年老代的比值(除去持久代)', '', '', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'KAFKA_HEAP_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', '-Xss', '', 0, 'TEXT', '每个线程的堆栈大小', '', '', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'KAFKA_HEAP_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', '-XX:MaxPermSize', '', 0, 'TEXT', '持久代最大值', '', '', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'KAFKA_HEAP_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', '-XX:PermSize', '', 0, 'TEXT', '持久代初始值', '', '', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'KAFKA_HEAP_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', '-XX:MaxNewSize', '', 0, 'TEXT', '年轻代最大值', '', '', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'KAFKA_HEAP_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', '-XX:NewSize', '', 0, 'TEXT', '年轻代大小', '', '', '', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'KAFKA_HEAP_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', '-XX:MetaspaceSize', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '512m', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'KAFKA_HEAP_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('KAFKA', 'BROKER', '-XX:MaxMetaspaceSize', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '512m', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'KAFKA_HEAP_OPTS');

-- ========kafka=========

-- =======zookeeper=======
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'clientPort', '', 0, 'TEXT', 'ZK端口', 'SERVICE', '服务范围', '2181', 1, 0, 'NUMBER', null, null, 0, 65535, 'SERVICE_CONFIG_GROUP', 0, 'zookeeper-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'tickTime', '', 0, 'TEXT', 'ZooKeeper使用的基本时间单位（毫秒）。 它用于做心跳，并且最小会话超时将是tickTime的两倍。', 'SERVICE', '服务范围', '2000', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'zookeeper-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'initLimit', '', 0, 'TEXT', 'initLimit是ZooKeeper用来限制quorum里的ZooKeeper服务器必须连接到leader的超时时间', 'SERVICE', '服务范围', '10', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'zookeeper-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'syncLimit', '', 0, 'TEXT', 'syncLimit限制了服务器与领导者之间的过时距离。', 'SERVICE', '服务范围', '5', 1, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'zookeeper-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'dataDir', '', 0, 'TEXT', '存储内存中数据库快照的位置，以及数据库更新的事务日志(除非另外指定)。', 'SERVICE', '服务范围', '/data/zookeeper/datadir', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'zookeeper-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'dataLogDir', '', 0, 'TEXT', '此选项将指示计算机将事务日志写入dataLogDir而不是dataDir。 这允许使用专用的日志设备，并有助于避免日志记录和快照之间的竞争。', 'SERVICE', '服务范围', '/data/zookeeper/logdir', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'ZOOKEEPER_SERVER_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'com.sun.management.jmxremote.port', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'SERVICE', 0, 'ZOOKEEPER_SERVER_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'com.sun.management.jmxremote.authenticate', '', 0, 'TEXT', '', 'SERVICE', '服务范围', 'false', 0, 0, 'STRING', null, null, null, null, 'SERVICE', 0, 'ZOOKEEPER_SERVER_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'com.sun.management.jmxremote.ssl', '', 0, 'TEXT', '', 'SERVICE', '服务范围', 'false', 0, 0, 'STRING', null, null, null, null, 'SERVICE', 0, 'ZOOKEEPER_SERVER_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'jute.maxbuffer', '', 0, 'TEXT', ' 它指定可以存储在znode中的数据的最大大小。 默认值为0xfffff，或不到1M。 如果更改此选项，则必须在所有服务器和客户端上设置系统属性，否则会出现问题。 这确实是一个健全性检查。 ZooKeeper旨在存储大小为千字节的数据。', 'SERVICE', '服务范围', '4194304', 0, 0, 'NUMBER', null, null, null, null, 'SERVICE', 0, 'ZOOKEEPER_SERVER_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'zookeeper.datadir.autocreate', '', 0, 'TEXT', '自动创建数据目录', 'SERVICE', '服务范围', 'false', 0, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'ZOOKEEPER_SERVER_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', '-Xms', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '52428800', 0, 0, 'NUMBER', null, null, null, null, 'SERVICE', 0, 'ZOOKEEPER_SERVER_JVM_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', '-Xmx', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '52428800', 0, 0, 'NUMBER', null, null, null, null, 'SERVICE', 0, 'ZOOKEEPER_SERVER_JVM_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'quorumPort', '', 0, 'TEXT', '仲裁端口', 'SERVICE', '服务范围', '3181', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'zookeeper-cluster-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'myid', '', 0, 'TEXT', '服务ID', 'SERVICE', '服务范围', '', 1, 0, 'STRING', null, null, null, null, 'INSTANCE', 0, 'zookeeper-cluster-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'electionPort', '', 0, 'TEXT', '选举端口', 'SERVICE', '服务范围', '4181', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'zookeeper-cluster-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'maxClientCnxns', '', 0, 'TEXT', '将单个客户端（由IP地址标识）可以与ZooKeeper集成的单个成员建立的并发连接数（在套接字级别）受到限制。 这用于防止某些类的DoS攻击，包括文件描述符耗尽。 默认值为60。将其设置为0将完全消除并发连接的限制。', 'SERVICE', '服务范围', '100000', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'zookeeper-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'autopurge.snapRetainCoun', '', 0, 'TEXT', '启用后，ZooKeeper自动清除功能会在dataDir和dataLogDir中分别保留autopurge.snapRetainCount最新快照和相应的事务日志，并删除其余快照。 默认值为3。最小值为3。', 'SERVICE', '服务范围', '10', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'zookeeper-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZOOKEEPER', 'INSTANCE', 'autopurge.purgeInterval', '', 0, 'TEXT', '必须触发清除任务的时间间隔(以小时为单位)。设置为正整数(1及以上)以启用自动清除。默认值为0。', 'SERVICE', '服务范围', '72', 0, 0, 'NUMBER', null, null, null, null, 'INSTANCE', 0, 'zookeeper-server-config');
-- =======zookeeper=======

-- ========influxdb========
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('INFLUXDB', 'INSTANCE', 'bind-address', 'http', 0, 'TEXT', 'The bind address used by the HTTP service.', 'SERVICE', '服务范围', '8086', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'influxdb-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('INFLUXDB', 'INSTANCE', 'wal-dir', 'data', 0, 'TEXT', 'WAL文件存储的路径', 'SERVICE', '服务范围', '/data/influxdb/wal', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'influxdb-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('INFLUXDB', 'INSTANCE', 'dir', 'data', 0, 'TEXT', 'TSM文件存储的路径', 'SERVICE', '服务范围', '/data/influxdb/data', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'influxdb-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('INFLUXDB', 'INSTANCE', 'dir', 'meta', 0, 'TEXT', '存储元数据信息的路径', 'SERVICE', '服务范围', '/data/influxdb/meta', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'influxdb-server-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('INFLUXDB', 'INSTANCE', 'series-id-set-cache-size', 'data', 0, 'TEXT', '内存缓存的 series 集的大小', 'SERVICE', '服务范围', '100', 1, 0, 'NUMBER', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'influxdb-server-config');
-- ========influxdb========

-- =========zms-alert=======
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_ALERT', 'SERVICE', 'INFLUXDB', '', 1, 'RADIO', 'influxDB', 'SERVICE', '服务范围', '', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 2, 'ZMSALERT_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_ALERT', 'SERVICE', 'logging.level.root', '', 0, 'TEXT', 'logging.level.root', 'SERVICE', '服务范围', 'info', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 4, 'ZMSALERT_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_ALERT', 'SERVICE', 'ZOOKEEPER', '', 1, 'RADIO', 'zms_zk', 'SERVICE', '服务范围', '', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 1, 'ZMSALERT_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_ALERT', 'INSTANCE', '-ms', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '1G', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 5, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_ALERT', 'INSTANCE', '-mx', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '2G', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 6, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_ALERT', 'INSTANCE', '-Xmn', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '512m', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 7, 'JAVA_OPT_EXT');
-- ========zms-alert=========


-- ========zms-collector======
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_COLLECTOR', 'SERVICE', 'INFLUXDB', '', 1, 'RADIO', 'influxDB', 'SERVICE', '服务范围', '', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'ZMSCOLLECTOR_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_COLLECTOR', 'SERVICE', 'logging.level.root', '', 0, 'TEXT', 'logging.level.root', 'SERVICE', '服务范围', 'info', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'ZMSCOLLECTOR_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_COLLECTOR', 'SERVICE', 'ZOOKEEPER', '', 1, 'RADIO', 'zms_zk', 'SERVICE', '服务范围', '', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'ZMSCOLLECTOR_ENV_OPTS');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_COLLECTOR', 'INSTANCE', '-ms', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '1G', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 4, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_COLLECTOR', 'INSTANCE', '-mx', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '2G', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 5, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_COLLECTOR', 'INSTANCE', '-Xmn', '', 0, 'TEXT', '', 'SERVICE', '服务范围', '512m', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 6, 'JAVA_OPT_EXT');
-- ========zms-collector======


-- ========zms-backup-cluster======
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_BACKUP_CLUSTER', 'SERVICE', 'envId', '', 1, 'RADIO', '需要备份的环境', 'SERVICE', '服务范围', '', 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 0, 'zms-backup-cluster-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_BACKUP_CLUSTER', 'SERVICE', 'backup.cluster.map', '', 0, 'CLUSTER_MAP', '源集群  ：目标集群，多个集群以逗号分隔', 'SERVICE', '服务范围', null, 1, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 1, 'zms-backup-cluster-config');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_BACKUP_CLUSTER', 'SERVICE', '-Xmn', '', 0, 'TEXT', '年轻代大小', 'SERVICE', '服务范围', '1g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 2, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_BACKUP_CLUSTER', 'SERVICE', '-Xms', '', 0, 'TEXT', '堆内存初始值', 'SERVICE', '服务范围', '1g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 3, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_BACKUP_CLUSTER', 'SERVICE', '-Xmx', '', 0, 'TEXT', '堆内存最大值', 'SERVICE', '服务范围', '2g', 0, 0, 'STRING', null, null, null, null, 'SERVICE_CONFIG_GROUP', 4, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_BACKUP_CLUSTER', 'SERVICE', '-XX:NewSize', '', 0, 'TEXT', '年轻代大小', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'SERVICE', 5, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_BACKUP_CLUSTER', 'SERVICE', '-XX:MaxNewSize', '', 0, 'TEXT', '年轻代最大值', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'SERVICE', 6, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_BACKUP_CLUSTER', 'SERVICE', '-XX:PermSize', '', 0, 'TEXT', '持久代初始值', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'SERVICE', 7, 'JAVA_OPT_EXT');
INSERT INTO service_property (service_type, instance_type, property_name, property_group, is_dependencies, choose_type, description, display_group, display_name, default_value, is_required, is_read_only, value_type, min_len, max_len, min_value, max_value, scope, order_num, conf_api_key) VALUES ('ZMS_BACKUP_CLUSTER', 'SERVICE', '-XX:MaxPermSize', '', 0, 'TEXT', '持久代最大值', 'SERVICE', '服务范围', '', 0, 0, 'STRING', null, null, null, null, 'SERVICE', 8, 'JAVA_OPT_EXT');
-- ========zms-backup-cluster======