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


BASE_DIR="/opt/zms"
BASE_DIR=`echo $BASE_DIR`
BASE_LOG_DIR="$BASE_DIR/log"

PORTAL_DIR="$BASE_DIR/agentshell"
AGENT_DIR="$BASE_DIR/lib/agent"
SERVICE_LIB_DIR="$BASE_DIR/lib/service"
SERVICE_RUN_DIR="$BASE_DIR/run/service"
PROCESS_RUN_DIR="$BASE_DIR/run/process"

JAVA=java

export BASE_LOG_DIR=$BASE_LOG_DIR
export PORTAL_DIR=$PORTAL_DIR
export AGENT_DIR=$AGENT_DIR
export SERVICE_LIB_DIR=$SERVICE_LIB_DIR
export SERVICE_RUN_DIR=$SERVICE_RUN_DIR
export PROCESS_RUN_DIR=$PROCESS_RUN_DIR


local_zms_java_home(){
    if which $JAVA >/dev/null 2>&1; then
      echo "Use internal java"
      export JAVA=$JAVA
    else
      JAVA_HOME="$BASE_DIR/lib/jdk"
      JAVA=$JAVA_HOME/bin/java
      export JAVA_HOME=$JAVA_HOME
      export JAVA=$JAVA_HOME/bin/java
    fi
    echo "JAVA_HOME:$JAVA_HOME"
    echo "JAVA:$JAVA"
}

local_zms_java_home
