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

package com.zto.zms.client.common;

import com.zto.zms.client.producer.SendType;

import java.util.Map;

/**
 * Created by superheizai on 2017/8/7.
 */


public class ZmsMessage extends SimpleMessage {

    //同步，异步和oneway
    private SendType sendType;


    // 消息属性列表，目前只支持 RocketMQ
    private Map<String, String> properties;

    public ZmsMessage() {
        this.sendType = SendType.SYNC;
    }

    public ZmsMessage(SimpleMessage simpleMessage) {
        super(simpleMessage.getKey(), simpleMessage.getTags(), simpleMessage.getDelayLevel(), simpleMessage.getPayload());
    }

    public ZmsMessage(String key, String tags, int delayLevel, byte[] payload, SendType sendType) {
        super(key, tags, delayLevel, payload);
        this.sendType = sendType;
    }
    public ZmsMessage(String key, String tags, int delayLevel, byte[] payload, SendType sendType, Map<String, String> properties) {
        super(key, tags, delayLevel, payload);
        this.sendType = sendType;
        this.properties = properties;
    }

    public void setSendType(SendType sendType) {
        this.sendType = sendType;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public SendType getSendType() {
        return sendType;
    }

    public Map<String, String> getProperties() {
        return properties;
    }


}


