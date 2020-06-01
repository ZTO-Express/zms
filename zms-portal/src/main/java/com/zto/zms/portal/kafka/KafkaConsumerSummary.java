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

package com.zto.zms.portal.kafka;

import java.util.List;

public class KafkaConsumerSummary {
    private long totalLag;

    private List<KafkaConsumerInfo> consumerInfos;

    public long getTotalLag() {
        return totalLag;
    }

    public void setTotalLag(long totalLag) {
        this.totalLag = totalLag;
    }

    public List<KafkaConsumerInfo> getConsumerInfos() {
        return consumerInfos;
    }

    public void setConsumerInfos(List<KafkaConsumerInfo> consumerInfos) {
        this.consumerInfos = consumerInfos;
    }
}

