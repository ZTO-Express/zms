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


BASE_RUN_DIR="/opt/zms/run"
BASE_LIB_DIR="/opt/zms/lib/thirdpath"

SUPERVISORD="$BASE_LIB_DIR/python/bin/supervisord"
SUPERVISOR_CONF="$BASE_RUN_DIR/supervisor/supervisord.conf"


export PYTHONPATH=PYTHONPATH:$BASE_LIB_DIR/python/lib/python3.6/site-packages
#start
$SUPERVISORD -c $SUPERVISOR_CONF