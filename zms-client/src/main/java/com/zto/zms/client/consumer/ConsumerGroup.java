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

package com.zto.zms.client.consumer;

import com.google.common.collect.Sets;
import com.zto.zms.common.ZmsConst;

import java.util.Set;

/**
 * Created by superheizai on 2017/11/2.
 */
public class ConsumerGroup {

    public ConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public ConsumerGroup(String consumerGroup, String consumerName) {
        this.consumerGroup = consumerGroup;
        this.consumerName = consumerName;
    }

    public ConsumerGroup(String consumerGroup, String consumerName, Set<String> tags) {
        this.consumerGroup = consumerGroup;
        this.consumerName = consumerName;
        this.tags = tags;
    }

    private String consumerGroup;

    private String consumerName = ZmsConst.DEFAULT_CONSUMER;

    private Set<String> tags = Sets.newHashSet();

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}

