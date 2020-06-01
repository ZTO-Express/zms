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

/**
 * Created by superheizai on 2017/11/2.
 */
public class SimpleMessage {
    // 排序和分片key
    private String key;

    private String tags;

    private int delayLevel = 0;
    // 消息体
    private byte[] payload;

    public SimpleMessage() {
    }

    public SimpleMessage(byte[] payload) {
        this.payload = payload;
    }

    public SimpleMessage(String key, String tags, int delayLevel, byte[] payload) {
        this.key = key;
        this.tags = tags;
        this.delayLevel = delayLevel;
        this.payload = payload;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public int getDelayLevel() {
        return delayLevel;
    }

    public void setDelayLevel(int delayLevel) {
        this.delayLevel = delayLevel;
    }
}

