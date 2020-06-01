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


JAR_SOURCE="alert"
JAR_NAME="zms-alert.jar"

zms_config=`dirname $0`
zms_config=`cd "$zms_config/../common"; pwd`
. ${zms_config}/zms-config.sh
local_zms_java_home

zms_portal_config=`cd "$PORTAL_DIR"; pwd`
#Read config
while read line;do
    eval "$line"
done < $zms_portal_config/config

ALERT_LOG_DIR="$BASE_LOG_DIR/zms-alert"

echo "ZMS_PORTAL:$ZMS_PORTAL"
echo "ALERT_LOG_DIR:$ALERT_LOG_DIR"
echo "ZMSALERT_ENV_OPTS:$ZMSALERT_ENV_OPTS"
echo "JAVA_OPT_EXT:$JAVA_OPT_EXT"

ENV_OPTS="$ZMSALERT_ENV_OPTS -Dzms.portal=$ZMS_PORTAL -Dlogging.path=$ALERT_LOG_DIR"
JAVA_OPTS="$JAVA_OPT_EXT -Djava.awt.headless=true -XX:MaxPermSize=128m -Dsun.net.http.allowRestrictedHeaders=true"

JAR_ABSOLUTE_FILE=$SERVICE_LIB_DIR/$JAR_SOURCE/$JAR_NAME

if [ -f "$JAR_ABSOLUTE_FILE" ]; then
		$JAVA $JAVA_OPTS $ENV_OPTS -jar $JAR_ABSOLUTE_FILE
	else
		echo "warn: JAR_FILE is not found! [$JAR_ABSOLUTE_FILE]"
fi