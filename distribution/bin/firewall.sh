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
#                   ZMS firewall                      #
#                                                   #
#####################################################

fire="firewall-cmd --state"
if which $fire >/dev/null 2>&1; then
    echo "firewall-cmd is already install"
else
    yum -y install firewalld
fi

openFirewall(){
  b3=`firewall-cmd --query-port=$1/tcp`
  if [ "$b3" = "no" ]; then
      firewall-cmd --permanent --add-port=$1/tcp
      firewall-cmd --reload
      echo "the port start success:[$1]"
  else
      echo "the port is already start:[$1]"
  fi
}

openFirewall "8080"
openFirewall "19001"
openFirewall "18080"