#!/bin/bash

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#####################################################
#                                                   #
#                   ZMS Agent                       #
# Install the dependent environment: JDK,python     #
# Install agents :zms-agent,supervisor              #
# Install the service and open the firewall port    #
#                                                   #
#####################################################
AGENT_BASE_DIR="/opt/zms"
SHELL_BASE_DIR="agentshell"
PYTHON_SHELL_FILE="python-install.sh"
AGENT_SHELL_FILE="zms-agent-start.sh"
SUPERVISOR_SHELL_FILE="supervisord-start.sh"
FIREWALL_SHELL_FILE="firewall.sh"
ALERT_SHELL_FILE="zms-alert-start.sh"
COLLECTOR_SHELL_FILE="zms-collector-start.sh"


KAFKA_SERVER_SHELL="kafka-server-start.sh"
ROCKETMQ_BROKER_SHELL_FILE="rocketmq-broker-start.sh"
ROCKETMQ_NAMESVR_SHELL_FILE="rocketmq-namesvr-start.sh"
ZOOKEEPER_SERVER_SHELL="zookeeper-server-start.sh"
ZMS_CONFIG_SHELL="zms-config.sh"
INFLUXDB_SERVER_SHELL="influxdb.sh"
BACKUP_CLUSTER_SERVER_SHELL="zms-backup-cluster-start.sh"


NTPDATE_NODES_SHELL_FILE="nodeslist"
AGENT_FILE_NAME="zms-agent.jar"
PYTHON_FILE_NAME="Python-3.6.5.tgz"
SUPERVISOR_FILE_NAME="supervisor-4.1.0.tar.gz"
JRE_FILE_NAME="jdk-8u221-linux-x64.tar.gz"
ROCKETMQ_FILE_NAME="rocketmq-all-4.1.0-incubating-bin-release.zip"
KAFKA_FILE_NAME="kafka_2.11-2.2.1.tgz"
ZOOKEEPER_FILE_NAME="zookeeper-3.4.10.tar.gz"
UNZIP_FILE_NAME="unzip-6.0-20.el7.x86_64.rpm"
RELY_RPM="rely-rpm.tar.gz"
INFLUXDB_FILE_NAME="influxdb-1.4.2_linux_amd64.tar.gz"
ALERT_FILE_NAME="zms-alert.jar"
COLLECTOR_FILE_NAME="zms-collector.jar"
BACKUP_CLUSTER_FILE_NAME="zms-backup-cluster.jar"

PYTHON_THIRD_DOWNLOAD="https://www.python.org/ftp/python/3.6.5/Python-3.6.5.tgz"
KAFKA_THIRD_DOWNLOAD="http://archive.apache.org/dist/kafka/2.2.1/kafka_2.11-2.2.1.tgz"
ROCKETMQ_THIRD_DOWNLOAD="https://archive.apache.org/dist/rocketmq/4.1.0-incubating/rocketmq-all-4.1.0-incubating-bin-release.zip"
SUPERVISOR_THIRD_DOWNLOAD="https://files.pythonhosted.org/packages/de/87/ee1ad8fa533a4b5f2c7623f4a2b585d3c1947af7bed8e65bc7772274320e/supervisor-4.1.0.tar.gz"
ZOOKEEPER_THIRD_DOWNLOAD="http://archive.apache.org/dist/zookeeper/zookeeper-3.4.10/zookeeper-3.4.10.tar.gz"
INFLUXDB_THIRD_DOWNLOAD="https://dl.influxdata.com/influxdb/releases/influxdb-1.4.2_linux_amd64.tar.gz"
UNZIP_THIRD_DOWNLOAD="http://mirror.centos.org/centos/7/os/x86_64/Packages/unzip-6.0-21.el7.x86_64.rpm"

SUPERVISORD_CONFIG_FILE_NAME="supervisord.conf"

PID_FILENAME="supervisor.pid"
PID_FILE="$BASE_DIR/$PID_FILENAME"
DOWNLOAD_URL="$2/api/package/script/download"

DOWNLOAD_LIB_URL="$DOWNLOAD_URL/lib"
DOWNLOAD_BIN_URL="$DOWNLOAD_URL/bin"
DOWNLOAD_CONF_URL="$DOWNLOAD_URL/conf"

ZMS_TEM_DIR="$AGENT_BASE_DIR/tmp"
ZMS_LIB_DIR="$AGENT_BASE_DIR/lib"
ZMS_RUN_DIR="$AGENT_BASE_DIR/run"
ZMS_LOG_DIR="$AGENT_BASE_DIR/log"

PYTHON_HONE="$ZMS_LIB_DIR/thirdpath/python"

USER="baseuser"
GROUP="baseuser"

AGENT="agent"
SUPERVISORD="supervisor"
PYTHOD="Python"
JAVA="java"
SERVICE="service"
ZOOKEEPER="zookeeper"
INFLUXDB="influxdb"
KAFKA="kafka"
ROCKETMQ="rocketmq"
COMMON="common"
PYTHON="python3"
ALERT="alert"
COLLECTOR="collector"

ZMS_BACKUP_CLUSTER="zmsBackupCluster"

DATA_DIR="/data"

# avoid shell-init: error retrieving current directory

info() {
  echo -e "\033[32m $1 \033[0m"
}

error() {
  echo -e "\033[31m  $1 \033[0m"
}

if [ -z $1 ];then
  error "token is required"
  exec 1
fi

if [ -z $2 ];then
  error "Portal url is required"
  exec 1
fi


#Create user if not exist
if [ `cat /etc/passwd|grep '^baseuser:' -c`  -ne 1 ]; then
  groupadd $GROUP
  useradd -g $GROUP $USER
  if [ $? -eq 0 ]; then
    echo "Create user $USER:$GROUP"
  else
    error "Create user fail"
    exit 1
  fi
else
   echo "$USER is exist"
fi

if [ ! -d $AGENT_BASE_DIR ];
then
    mkdir -p $AGENT_BASE_DIR
    mkdir $AGENT_BASE_DIR/$SHELL_BASE_DIR
    mkdir -p ${ZMS_LIB_DIR}/${AGENT}
    mkdir -p ${ZMS_RUN_DIR}/${SUPERVISORD}
    mkdir -p ${ZMS_TEM_DIR}
    mkdir -p ${ZMS_LOG_DIR}
    mkdir -p ${ZMS_LIB_DIR}/${SERVICE}
    mkdir -p ${ZMS_LIB_DIR}/${SERVICE}/${ALERT}
    mkdir -p ${ZMS_LIB_DIR}/${SERVICE}/${COLLECTOR}
    mkdir -p ${ZMS_LIB_DIR}/${SERVICE}/${ZMS_BACKUP_CLUSTER}

    mkdir -p ${ZMS_RUN_DIR}/${SERVICE}/${ZOOKEEPER}
    mkdir -p ${ZMS_RUN_DIR}/${SERVICE}/${INFLUXDB}
    mkdir -p ${ZMS_RUN_DIR}/${SERVICE}/${KAFKA}
    mkdir -p ${ZMS_RUN_DIR}/${SERVICE}/${ROCKETMQ}
    mkdir -p ${ZMS_RUN_DIR}/${SERVICE}/${COMMON}
    mkdir -p ${ZMS_RUN_DIR}/${SERVICE}/${ALERT}
    mkdir -p ${ZMS_RUN_DIR}/${SERVICE}/${COLLECTOR}
    mkdir -p ${ZMS_RUN_DIR}/${SERVICE}/${ZMS_BACKUP_CLUSTER}
fi

#Initializes the data directory
if [ ! -d $DATA_DIR ]; then
  mkdir -p $DATA_DIR
fi

info "Data dir:$DATA_DIR"
info "User:$USER"
info "Group:$GROUP"


# Write config
echo "ZMS_TOKEN=$1" > ${AGENT_BASE_DIR}/${SHELL_BASE_DIR}/config
echo "ZMS_PORTAL=$2" >> ${AGENT_BASE_DIR}/${SHELL_BASE_DIR}/config

download(){
  LOCAL_URL=$1
  THIRD_PARTY_URL=$2
  OUTPUT_FILE=$3
  wget $LOCAL_URL -O $OUTPUT_FILE
  if [ $? -ne 0 ]; then
    echo "The local library does not exist: $LOCAL_URL"
    wget $THIRD_PARTY_URL -O $OUTPUT_FILE
    if [ $? -ne 0 ]; then
      error "Download failed: $THIRD_PARTY_URL"
    fi
  fi
}


echo "====== DOWNLOAD FILES ======="
wget ${DOWNLOAD_LIB_URL}/${AGENT_FILE_NAME} -O "${ZMS_LIB_DIR}/${AGENT}/${AGENT_FILE_NAME}"
download ${DOWNLOAD_LIB_URL}/${ROCKETMQ_FILE_NAME} ${ROCKETMQ_THIRD_DOWNLOAD} "${ZMS_TEM_DIR}/${ROCKETMQ_FILE_NAME}"
download ${DOWNLOAD_LIB_URL}/${KAFKA_FILE_NAME} ${KAFKA_THIRD_DOWNLOAD} "${ZMS_TEM_DIR}/${KAFKA_FILE_NAME}"
download ${DOWNLOAD_LIB_URL}/${ZOOKEEPER_FILE_NAME} ${ZOOKEEPER_THIRD_DOWNLOAD} "${ZMS_TEM_DIR}/${ZOOKEEPER_FILE_NAME}"
download ${DOWNLOAD_LIB_URL}/${INFLUXDB_FILE_NAME} ${INFLUXDB_THIRD_DOWNLOAD} "${ZMS_TEM_DIR}/${INFLUXDB_FILE_NAME}"
wget ${DOWNLOAD_LIB_URL}/${ALERT_FILE_NAME} -O "${ZMS_LIB_DIR}/${SERVICE}/${ALERT}/${ALERT_FILE_NAME}"
wget ${DOWNLOAD_LIB_URL}/${COLLECTOR_FILE_NAME} -O "${ZMS_LIB_DIR}/${SERVICE}/${COLLECTOR}/${COLLECTOR_FILE_NAME}"
wget ${DOWNLOAD_LIB_URL}/${BACKUP_CLUSTER_FILE_NAME} -O "${ZMS_LIB_DIR}/${SERVICE}/${ZMS_BACKUP_CLUSTER}/${BACKUP_CLUSTER_FILE_NAME}"


wget ${DOWNLOAD_BIN_URL}/${AGENT_SHELL_FILE} -O "${AGENT_BASE_DIR}/${SHELL_BASE_DIR}/${AGENT_SHELL_FILE}"
wget ${DOWNLOAD_BIN_URL}/${SUPERVISOR_SHELL_FILE} -O "${AGENT_BASE_DIR}/${SHELL_BASE_DIR}/${SUPERVISOR_SHELL_FILE}"
wget ${DOWNLOAD_BIN_URL}/${FIREWALL_SHELL_FILE} -O "${AGENT_BASE_DIR}/${SHELL_BASE_DIR}/${FIREWALL_SHELL_FILE}"
wget ${DOWNLOAD_BIN_URL}/${KAFKA_SERVER_SHELL} -O "${ZMS_RUN_DIR}/${SERVICE}/${KAFKA}/${KAFKA_SERVER_SHELL}"
wget ${DOWNLOAD_BIN_URL}/${ZOOKEEPER_SERVER_SHELL} -O "${ZMS_RUN_DIR}/${SERVICE}/${ZOOKEEPER}/${ZOOKEEPER_SERVER_SHELL}"
wget ${DOWNLOAD_BIN_URL}/${ROCKETMQ_BROKER_SHELL_FILE} -O "${ZMS_RUN_DIR}/${SERVICE}/${ROCKETMQ}/${ROCKETMQ_BROKER_SHELL_FILE}"
wget ${DOWNLOAD_BIN_URL}/${ROCKETMQ_NAMESVR_SHELL_FILE} -O "${ZMS_RUN_DIR}/${SERVICE}/${ROCKETMQ}/${ROCKETMQ_NAMESVR_SHELL_FILE}"
wget ${DOWNLOAD_BIN_URL}/${INFLUXDB_SERVER_SHELL} -O "${ZMS_RUN_DIR}/${SERVICE}/${INFLUXDB}/${INFLUXDB_SERVER_SHELL}"

wget ${DOWNLOAD_BIN_URL}/${ZMS_CONFIG_SHELL} -O "${ZMS_RUN_DIR}/${SERVICE}/${COMMON}/${ZMS_CONFIG_SHELL}"
wget ${DOWNLOAD_BIN_URL}/${ALERT_SHELL_FILE} -O "${ZMS_RUN_DIR}/${SERVICE}/${ALERT}/${ALERT_SHELL_FILE}"
wget ${DOWNLOAD_BIN_URL}/${COLLECTOR_SHELL_FILE} -O "${ZMS_RUN_DIR}/${SERVICE}/${COLLECTOR}/${COLLECTOR_SHELL_FILE}"
wget ${DOWNLOAD_BIN_URL}/${BACKUP_CLUSTER_SERVER_SHELL} -O "${ZMS_RUN_DIR}/${SERVICE}/${ZMS_BACKUP_CLUSTER}/${BACKUP_CLUSTER_SERVER_SHELL}"

wget ${DOWNLOAD_CONF_URL}/${SUPERVISORD_CONFIG_FILE_NAME} -O "${ZMS_RUN_DIR}/${SUPERVISORD}/${SUPERVISORD_CONFIG_FILE_NAME}"


echo "======= Setup JDK ==========="
if which $JAVA >/dev/null 2>&1; then #不打印错误输出信息
  info "Use internal java"
else
  wget ${DOWNLOAD_LIB_URL}/${JRE_FILE_NAME} -O "${ZMS_TEM_DIR}/${JRE_FILE_NAME}"
  if [ $? -ne 0 ]; then
      error "No Java environment is installed"
      exit 2
  fi
  tar -xvf "${ZMS_TEM_DIR}/${JRE_FILE_NAME}" -C ${ZMS_TEM_DIR} >/dev/null 2>&1
  mv ${ZMS_TEM_DIR}/jdk1.8.0_221 ${ZMS_LIB_DIR}/jdk
  JAVA=${ZMS_LIB_DIR}/jdk/bin/java
fi

echo "======= Setup Python ==========="
if which $PYTHON >/dev/null 2>&1; then
  info "Use internal python3"
else
  download ${DOWNLOAD_LIB_URL}/${PYTHON_FILE_NAME} ${PYTHON_THIRD_DOWNLOAD} "${ZMS_TEM_DIR}/${PYTHON_FILE_NAME}"
  tar -xvf "${ZMS_TEM_DIR}/${PYTHON_FILE_NAME}" -C ${ZMS_TEM_DIR}  >/dev/null 2>&1
  mv ${ZMS_TEM_DIR}/Python-3.6.5 ${ZMS_TEM_DIR}/${PYTHOD}
  #rpm install
  #python reply
  wget ${DOWNLOAD_LIB_URL}/${RELY_RPM} -O "${ZMS_TEM_DIR}/${RELY_RPM}"
  if [ $? -eq 0 ]; then
    mkdir -p ${ZMS_TEM_DIR}/rpm
    tar -zxvf "${ZMS_TEM_DIR}/${RELY_RPM}" -C ${ZMS_TEM_DIR}/rpm  >/dev/null 2>&1
    cd ${ZMS_TEM_DIR}/rpm
    yum localinstall *.rpm -y --skip-broken >/dev/null 2>&1
  else
    yum -y install zlib-devel bzip2-devel openssl-devel ncurses-devel sqlite-devel readline-devel tk-devel gdbm-devel db4-devel libpcap-devel xz-devel libffi-devel >/dev/null 2>&1
  fi
	mkdir -p $PYTHON_HONE
  cd ${ZMS_TEM_DIR}/${PYTHOD}
  ./configure --prefix=$PYTHON_HONE >/dev/null 2>&1
  make
  make install >/dev/null 2>&1

	if [ $? -eq 0 ];then
    info "[Python] install success"
    PYTHON="$PYTHON_HONE/bin/python3"
  else
    error "[Python] install failture"
    exec 1
  fi
fi

echo "======= Setup Supervisord ==========="
if [ ! -f "$PYTHON_HONE/bin/supervisord" ];then

  if [ ! -d "$PYTHON_HONE/lib/python3.6/site-packages" ];then
    mkdir -p $PYTHON_HONE/lib/python3.6/site-packages
  fi
  export PYTHONPATH=$PYTHON_HONE/lib/python3.6/site-packages
  download ${DOWNLOAD_LIB_URL}/${SUPERVISOR_FILE_NAME} ${SUPERVISOR_THIRD_DOWNLOAD} "${ZMS_TEM_DIR}/${SUPERVISOR_FILE_NAME}"
  tar -xvf "${ZMS_TEM_DIR}/${SUPERVISOR_FILE_NAME}" -C ${AGENT_BASE_DIR}  >/dev/null 2>&1
  mv ${AGENT_BASE_DIR}/supervisor-4.1.0 ${ZMS_TEM_DIR}/${SUPERVISORD}
  cd ${ZMS_TEM_DIR}/${SUPERVISORD} && $PYTHON setup.py install --prefix=$PYTHON_HONE >/dev/null 2>&1
  if [ $? -eq 0 ];then
    info "[Supervisord] install success"
  else
    error "[Supervisord] install failture"
    exec 1
  fi
fi


echo "======== UNZIP FILES ========"

if ! which unzip >/dev/null 2>&1; then
  download ${DOWNLOAD_LIB_URL}/${UNZIP_FILE_NAME} ${UNZIP_THIRD_DOWNLOAD} "${ZMS_TEM_DIR}/${UNZIP_FILE_NAME}"
  yum localinstall ${ZMS_TEM_DIR}/${UNZIP_FILE_NAME} -y --skip-broken >/dev/null 2>&1
fi

unzip -d ${ZMS_TEM_DIR} "${ZMS_TEM_DIR}/${ROCKETMQ_FILE_NAME}" >/dev/null 2>&1
mv ${ZMS_TEM_DIR}/rocketmq-all-4.1.0-incubating ${ZMS_LIB_DIR}/${SERVICE}/${ROCKETMQ}

tar -xvf "${ZMS_TEM_DIR}/${KAFKA_FILE_NAME}" -C ${ZMS_TEM_DIR}  >/dev/null 2>&1
mv ${ZMS_TEM_DIR}/kafka_2.11-2.2.1 ${ZMS_LIB_DIR}/${SERVICE}/${KAFKA}

tar -xvf "${ZMS_TEM_DIR}/${ZOOKEEPER_FILE_NAME}" -C ${ZMS_TEM_DIR}  >/dev/null 2>&1
mv ${ZMS_TEM_DIR}/zookeeper-3.4.10 ${ZMS_LIB_DIR}/${SERVICE}/${ZOOKEEPER}

tar -xvf "${ZMS_TEM_DIR}/${INFLUXDB_FILE_NAME}" -C ${ZMS_TEM_DIR}  >/dev/null 2>&1
mv ${ZMS_TEM_DIR}/influxdb-1.4.2-1 ${ZMS_LIB_DIR}/${SERVICE}/${INFLUXDB}

chown -R $USER:$GROUP $DATA_DIR
chown -R $USER:$GROUP $AGENT_BASE_DIR
chmod -R 755 $DATA_DIR
chmod -R 755 $AGENT_BASE_DIR

echo "======== START DAEMON ======="
# 添加服务
#supervisord
SUPERVISORD_SERVICE_NAME=supervisord.service
SUPERVISORD_SERVICE="/usr/lib/systemd/system/$SUPERVISORD_SERVICE_NAME"
echo "[Unit]" >  $SUPERVISORD_SERVICE
echo "Description=Service for supervisord" >> $SUPERVISORD_SERVICE
echo "[Service]" >> $SUPERVISORD_SERVICE
echo "ExecStart=${AGENT_BASE_DIR}/${SHELL_BASE_DIR}/${SUPERVISOR_SHELL_FILE}" >> $SUPERVISORD_SERVICE
echo "Type=forking" >> $SUPERVISORD_SERVICE
echo "TimeoutStopSec=10" >> $SUPERVISORD_SERVICE
echo "PrivateTmp=true" >> $SUPERVISORD_SERVICE
echo "User=root" >> $SUPERVISORD_SERVICE
echo "Group=root" >> $SUPERVISORD_SERVICE
#echo "PIDFile=$ZMS_RUN_DIR/$SUPERVISORD/supervisord.pid" >> $SUPERVISORD_SERVICE
echo "[Install]" >> $SUPERVISORD_SERVICE
echo "WantedBy=multi-user.target" >> $SUPERVISORD_SERVICE

systemctl daemon-reload
systemctl enable $SUPERVISORD_SERVICE_NAME
systemctl start $SUPERVISORD_SERVICE_NAME
systemctl status $SUPERVISORD_SERVICE_NAME

# 添加服务
#zms-agent
ZMS_AGENT_SERVICE_NAME="zms-agent.service"
ZMS_AGENT_SERVICE="/usr/lib/systemd/system/$ZMS_AGENT_SERVICE_NAME"
echo "[Unit]" >  $ZMS_AGENT_SERVICE
echo "Description=Service for zms agent" >> $ZMS_AGENT_SERVICE
echo "[Service]" >> $ZMS_AGENT_SERVICE
echo "ExecStart=${AGENT_BASE_DIR}/${SHELL_BASE_DIR}/${AGENT_SHELL_FILE}" >> $ZMS_AGENT_SERVICE
echo "Type=forking" >> $ZMS_AGENT_SERVICE
echo "TimeoutStopSec=10" >> $ZMS_AGENT_SERVICE
echo "PrivateTmp=true" >> $ZMS_AGENT_SERVICE
echo "User=$USER" >> $ZMS_AGENT_SERVICE
echo "Group=$GROUP" >> $ZMS_AGENT_SERVICE
#echo "PIDFile=$ZMS_RUN_DIR/$AGENT/zms-agent.pid" >> $ZMS_AGENT_SERVICE
echo "[Install]" >> $ZMS_AGENT_SERVICE
echo "WantedBy=multi-user.target" >> $ZMS_AGENT_SERVICE

systemctl daemon-reload
systemctl enable $ZMS_AGENT_SERVICE_NAME
systemctl start $ZMS_AGENT_SERVICE_NAME
systemctl status $ZMS_AGENT_SERVICE_NAME


${AGENT_BASE_DIR}/${SHELL_BASE_DIR}/${FIREWALL_SHELL_FILE} $1


sleep 5
echo "======== INSTALL END ======="