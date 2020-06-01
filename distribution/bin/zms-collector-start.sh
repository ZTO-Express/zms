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


JAR_NAME=zms-collector.jar

DATE=`date +%Y%m%d%H%M%S`

zms_config=`dirname $0`
zms_config=`cd "$zms_config/../common"; pwd`
. ${zms_config}/zms-config.sh
local_zms_java_home

COLLECTOR_LOG_DIR="$BASE_LOG_DIR/zms-collector"

echo "COLLECTOR_LOG_DIR:$COLLECTOR_LOG_DIR"
echo "ZMSCOLLECTOR_ENV_OPTS:$ZMSCOLLECTOR_ENV_OPTS"
echo "JAVA_OPT_EXT:$JAVA_OPT_EXT"

ENV_OPTS="$ZMSCOLLECTOR_ENV_OPTS -Dlogging.path=$COLLECTOR_LOG_DIR"

JAVA_OPTS="$JAVA_OPT_EXT -Djava.awt.headless=true -XX:MaxPermSize=128m -Dsun.net.http.allowRestrictedHeaders=true"

export WORK_HOME=$SERVICE_LIB_DIR/collector

JAR_ABSOLUTE_FILE=$WORK_HOME/$JAR_NAME
if [ -f "$JAR_ABSOLUTE_FILE" ]; then
		$JAVA $JAVA_OPTS $ENV_OPTS -jar $JAR_ABSOLUTE_FILE
	else
		echo "warn: JAR_FILE is not found! [$JAR_ABSOLUTE_FILE]"
fi
