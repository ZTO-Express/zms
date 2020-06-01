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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.Properties;

/**
 * Created by superheizai on 2017/8/16.
 */
public class ConsumeMessage {
    private String topic;

    private int partition;

    private long offset;

    private byte[] payload;

    private String msgId;

    private String tag;

    private Properties properties = new Properties();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public static ConsumeMessage parse(MessageExt rocketmqMsg) {
        ConsumeMessage message = new ConsumeMessage();
        message.setTopic(rocketmqMsg.getTopic());
        message.setMsgId(rocketmqMsg.getMsgId());
        message.setOffset(rocketmqMsg.getQueueOffset());
        message.setPartition(rocketmqMsg.getQueueId());
        message.setPayload(rocketmqMsg.getBody());
        message.setTag(rocketmqMsg.getTags());
        message.getProperties().put("reconsumeTimes", String.valueOf(rocketmqMsg.getReconsumeTimes()));
        message.getProperties().put("broker", rocketmqMsg.getStoreHost().toString());
        message.getProperties().put("bornTime", rocketmqMsg.getBornTimestamp());
        message.getProperties().put("brokerHost", rocketmqMsg.getBornHostNameString());
        message.getProperties().put("boydCrc", rocketmqMsg.getBodyCRC());
        return message;
    }

    public static ConsumeMessage parse(ConsumerRecord kafkaMsg) {
        ConsumeMessage message = new ConsumeMessage();
        message.setTopic(kafkaMsg.topic());
        message.setMsgId("");
        message.setOffset(kafkaMsg.offset());
        message.setPartition(kafkaMsg.partition());
        message.setPayload((byte[]) kafkaMsg.value());
        return message;
    }
}

