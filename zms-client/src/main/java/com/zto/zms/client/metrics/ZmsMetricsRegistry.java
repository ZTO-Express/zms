/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zto.zms.client.metrics;

import com.codahale.metrics.MetricRegistry;
import com.zto.zms.common.ZmsConst;

/**
 * Created by superheizai on 2017/9/5.
 */
class ZmsMetricsRegistry {

    static final MetricRegistry REGISTRY = new MetricRegistry();

    static final String buildName(String producerMetricGroup, String type, String clientName, String zmsName) {
        return producerMetricGroup + "--" + type + "--" + zmsName + "--" + ZmsConst.ZMS_IP.replace(".", "_") + "--" + clientName;
    }

}

