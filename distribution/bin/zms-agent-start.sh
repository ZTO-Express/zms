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


#zms-agent start
BASE_DIR="/opt/zms"
BASE_DIR=`cd "$BASE_DIR"; pwd`

BASE_RUN_DIR="$BASE_DIR/run"
BASE_LIB_DIR="$BASE_DIR/lib"
BASE_LOG_DIR="$BASE_DIR/log"

AGENT_DIR="$BASE_LIB_DIR/agent"
SERVICE_LIB_DIR="$BASE_LIB_DIR/service"
SERVICE_DIR="$BASE_RUN_DIR/service"
PROCESS_DIR="$BASE_RUN_DIR/process"
SUPERVISOR_DIR="$BASE_RUN_DIR/supervisor"

AGENT_LOG_DIR="$BASE_LOG_DIR/zms-agent"
AGENT_FILE="$AGENT_DIR/zms-agent.jar"

JAVA="java"

SHELL_DIR=`dirname $0`
SHELL_DIR=`cd "$SHELL_DIR"; pwd`

#Read config
while read line;do
    eval "$line"
done < $SHELL_DIR/config
echo $ZMS_TOKEN
echo $ZMS_PORTAL

if [ -z $ZMS_PORTAL ]; then
  echo "zms-portal url is not set"
  exec 1
fi
if [ -z $ZMS_TOKEN ];then
  echo "token is not set"
  exec 1
fi

if [ ! -d "$SUPERVISOR_DIR" ]; then
    mkdir -p $SUPERVISOR_DIR
fi

if [ ! -d "$PROCESS_DIR" ]; then
    mkdir -p $PROCESS_DIR
fi

if which $JAVA >/dev/null 2>&1; then #不打印错误输出信息
  info "Use internal java"
else
  JAVA="$BASE_LIB_DIR/jdk/bin/java"
fi

ENV_OPTS="-Dserver.port=18080 -Dzms.token=$ZMS_TOKEN -Dzms.portal=$ZMS_PORTAL -Dservice.dir=$SERVICE_DIR -Dservice.lib.dir=$SERVICE_LIB_DIR -Dagent.dir=$AGENT_DIR -Dsupervisor.dir=$SUPERVISOR_DIR -Dprocess.dir=$PROCESS_DIR -Dlogging.path=$AGENT_LOG_DIR -Dlogging.level.root=info"
JAVA_OPTS="-ms512m -mx512m -Xmn256m  -XX:MaxPermSize=128m -Dsun.net.http.allowRestrictedHeaders=true"


nohup $JAVA $ENV_OPTS $JAVA_OPTS -jar $AGENT_FILE > /dev/null 2>&1 &