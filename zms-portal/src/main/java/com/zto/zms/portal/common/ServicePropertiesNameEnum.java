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

package com.zto.zms.portal.common;


/**
 * <p> Description: 服务属性组-属性名关联</p>
 *
 * @author lidawei9103@zto.com
 * @version 1.0
 * @date 2020/1/9
 */
public enum ServicePropertiesNameEnum {

    //kafka、collector、alert依赖zk服务
    ZK_SERVICE("SERVICE", "", "ZOOKEEPER"),
    //collector、alert依赖influxDb服务
    INFLUX_DB_SERVICE("SERVICE", "", "INFLUXDB"),
    //环境依赖
    ENVID_SERVICE("SERVICE", "", "envId"),

    ZK_CLIENT_PORT("INSTANCE", "", "clientPort"),
    INFLUX_DB_PORT("INSTANCE", "http", "bind-address"),
    INFLUX_DB_WAL_DIR("INSTANCE", "data", "wal-dir"),
    INFLUX_DB_DATA_DIR("INSTANCE", "data", "dir"),
    INFLUX_DB_META_DIR("INSTANCE", "meta", "dir"),
    INFLUX_DB_SERIES_ID_SET_CACHE_SIZE("INSTANCE", "data", "series-id-set-cache-size"),
    CLUSTER_VERSION("SERVICE", "", "version"),
    ROCKETMQ_NAMESERVER_PORT("NAMESVR", "", "listenPort"),
    ROCKETMQ_BROKER_PORT("BROKER", "", "listenPort"),
    ROCKETMQ_BROKER_NAME("BROKER", "", "brokerName"),
    ROCKETMQ_BROKER_ID("BROKER", "", "brokerId"),
    KAFKA_PORT("BROKER", "", "listeners"),
    KAFKA_BROKERID("BROKER", "", "broker.id"),
    KAFKA_ZOOKEEPER_CHROOT("SERVICE", "", "zookeeper.chroot"),
    ZOOKEEPER_QUORUM("INSTANCE", "", "quorumPort"),
    ZOOKEEPER_MYID("INSTANCE", "", "myid"),
    ZOOKEEPER_DATADIR("INSTANCE", "", "dataDir"),
    ZOOKEEPER_ELECTION_PORT("INSTANCE", "", "electionPort"),
    ZMS_BACKUP_CLUSTER_MAP("SERVICE", "", "backup.cluster.map");

    private String instanceType;
    private String group;
    private String name;

    ServicePropertiesNameEnum(String instanceType, String propertyGroup, String propertyName) {
        this.instanceType = instanceType;
        this.group = propertyGroup;
        this.name = propertyName;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getInstanceType() {
        return instanceType;
    }
}

