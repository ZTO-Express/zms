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


zms_config=`dirname $0`
zms_config=`cd "$zms_config/../common"; pwd`
. ${zms_config}/zms-config.sh
local_zms_java_home

echo "KAFKA_HOME:$KAFKA_HOME"
echo "JAVA_HOME:$JAVA_HOME"
echo "KAFKA_LOG4J_OPTS:$KAFKA_LOG4J_OPTS"
echo "KAFKA_HEAP_OPTS:$KAFKA_HEAP_OPTS"
echo "KAFKA_JMX_OPTS:$KAFKA_JMX_OPTS"
echo "CONF_DIR:$CONF_DIR"

if [ "x$KAFKA_LOG4J_OPTS" = "x" ]; then
    export KAFKA_LOG4J_OPTS="-Dlog4j.configuration=file:$CONF_DIR/log4j.properties"
fi

if [ "x$KAFKA_HEAP_OPTS" = "x" ]; then
    export KAFKA_HEAP_OPTS="-Xmx8G -Xm8G -XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=512m"
fi

EXTRA_ARGS=${EXTRA_ARGS-'-name kafkaServer -loggc'}


exec $KAFKA_HOME/bin/kafka-run-class.sh $EXTRA_ARGS kafka.Kafka  $CONF_DIR/server.properties