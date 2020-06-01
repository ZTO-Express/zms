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


MYID=$1
DATA_DIR=$2

zms_config=`dirname $0`
zms_config=`cd "$zms_config/../common"; pwd`
. ${zms_config}/zms-config.sh
local_zms_java_home

echo "MYID:$MYID"
echo "DATA_DIR:$DATA_DIR"
echo "CONF_DIR:$CONF_DIR"
echo "ZOOKEEPER_HOME:$ZOOKEEPER_HOME"
echo "ZOOKEEPER_SERVER_ENV_OPTS:$ZOOKEEPER_SERVER_ENV_OPTS"
echo "ZOOKEEPER_SERVER_JVM_OPTS:$ZOOKEEPER_SERVER_JVM_OPTS"

if [ ! -d "$DATA_DIR" ]; then
    mkdir -p $DATA_DIR
fi


echo $MYID > $DATA_DIR/myid

exec $JAVA -cp $CONF_DIR:$ZOOKEEPER_HOME/lib/log4j.jar:$ZOOKEEPER_HOME/build/*:$ZOOKEEPER_HOME/build/lib/*:$ZOOKEEPER_HOME/*:$ZOOKEEPER_HOME/lib/*:$ZOOKEEPER_CLASSPATH $ZOOKEEPER_SERVER_ENV_OPTS $ZOOKEEPER_SERVER_JVM_OPTS org.apache.zookeeper.server.quorum.QuorumPeerMain $CONF_DIR/zoo.cfg