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

package com.zto.zms.metadata;

import com.google.common.base.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by superheizai on 2017/8/15.
 */
public class ConsumerGroupMetadata extends ZmsMetadata {

    private String bindingTopic;

    private String consumeFrom;

    private String broadcast;

    private String suspend = "false";


    public String getConsumeFrom() {
        return consumeFrom;
    }

    public void setConsumeFrom(String consumeFrom) {
        this.consumeFrom = consumeFrom;
    }

    public String getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(String broadcast) {
        this.broadcast = broadcast;
    }

    public String getBindingTopic() {
        return bindingTopic;
    }

    public void setBindingTopic(String bindingTopic) {
        this.bindingTopic = bindingTopic;
    }

    public String getSuspend() {
        return suspend;
    }

    public void setSuspend(String suspend) {
        this.suspend = suspend;
    }

    public boolean needSuspend() {
        return StringUtils.isNotBlank(this.suspend) && "true".equalsIgnoreCase(this.suspend);
    }


    public boolean suspendChange(ConsumerGroupMetadata metadata) {
        return this.bindingTopic.equals(metadata.getBindingTopic()) && this.consumeFrom.equals(metadata.getConsumeFrom()) &&
                this.broadcast.equals(metadata.getBroadcast()) && !this.suspend.equals(metadata.getSuspend()) && super.equals(metadata);
    }

    @Override
    public String toString() {
        return "ConsumerGroupMetadata{" +
                "bindingTopic='" + bindingTopic + '\'' +
                ", consumeFrom='" + consumeFrom + '\'' +
                ", broadcast='" + broadcast + '\'' +
                ", suspend='" + suspend + '\'' +
                '}' + super.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConsumerGroupMetadata that = (ConsumerGroupMetadata) o;

        return Objects.equal(this.bindingTopic, that.bindingTopic) &&
                Objects.equal(this.consumeFrom, that.consumeFrom) &&
                Objects.equal(this.broadcast, that.broadcast) &&
                Objects.equal(this.suspend, that.suspend) &&
                super.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(bindingTopic, consumeFrom, broadcast, suspend, this.getType(), this.getName(),
                this.getClusterMetadata(), this.getDomain());
    }
}

