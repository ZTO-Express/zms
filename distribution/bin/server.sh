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


WORK_HOME=`dirname $0`
WORK_HOME=`cd $WORK_HOME/..; pwd`
JAR_NAME=zms-portal.jar

SERVER_PORT="8088"
LOGGING_PATH="/data/logs/zms-portal"
LOGGING_LEVEL_ROOT="info"
ENV_OPTS="-Dserver.port=$SERVER_PORT -Dlogging.path=$LOGGING_PATH -Dlogging.level.root=$LOGGING_LEVEL_ROOT"

JAVA_OPTS="-Xms512m -Xmx1g -Xmn256m -Djava.awt.headless=true -XX:MaxPermSize=128m -Dsun.net.http.allowRestrictedHeaders=true"

# Which java to use
if [ -z "$JAVA_HOME" ]; then
  JAVA="java"
else
  JAVA="$JAVA_HOME/bin/java"
fi

JAR_ABSOLUTE_FILE=$WORK_HOME/$JAR_NAME

pid=0
checkpid() {
   	server_pid=$(pgrep -f $JAR_NAME)
 
   	if [ -n "$server_pid" ]; then
        pid=`echo $server_pid`
   	else
      	pid=0
   	fi
}

info() {
  echo -e "\033[32m $1 \033[0m"
}

error() {
  echo -e "\033[31m  $1 \033[0m"
}


start(){
  echo "server.port:$SERVER_PORT"
  echo "logging.path:$LOGGING_PATH"
	checkpid
	if [ 0 -ne $pid ];then
		error "Warn: server started already! Pid:[$pid]"
		exit 1
	fi
	if [ -f "$JAR_ABSOLUTE_FILE" ]; then
	  cd $WORK_HOME
		nohup $JAVA  $JAVA_OPTS $ENV_OPTS -jar $JAR_ABSOLUTE_FILE  >/dev/null 2>&1 &
		sleep 5s
		checkpid

		if [ 0 -ne $pid ]; then
			info "Start success,pid:[$pid]"
		else
			error "Warn: start failed"
		fi
	else
		error "Warn: JAR_FILE is not found! [$JAR_ABSOLUTE_FILE]"
		exit 6
  fi
}


stop(){
    checkpid
    if [ 0 -ne $pid ]
        then
        kill -9 $pid
        checkpid
        if [ 0 -eq $pid ]
            then
                info "Stop success"
            else
                error "Warn: server stoped failed! [$pid]"
            fi
        else
            echo 'server not started!'
        fi
}

status() {
	checkpid

	if [ 0 -ne $pid ]
	then
		info "server is started ,pid:[$pid]"
	else
		error "server is invalid"
	fi
}


case $1 in
    start)
    start
    ;;
    restart)
    stop
    start
    ;;
    stop)
    stop
    ;;
    status)
    status
    ;;
	  *)
	  echo $"Usage: $0 {start|stop|restart|status}"
    exit 2
esac

