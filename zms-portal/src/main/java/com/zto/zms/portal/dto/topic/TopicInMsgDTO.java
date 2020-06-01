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

package com.zto.zms.portal.dto.topic;

/**
 * Created by liangyong on 2019/3/12.
 */
public class TopicInMsgDTO implements Comparable<TopicInMsgDTO> {

    private String topic;

    private Long inMsg24Hour;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Long getInMsg24Hour() {
        return inMsg24Hour;
    }

    public void setInMsg24Hour(Long inMsg24Hour) {
        this.inMsg24Hour = inMsg24Hour;
    }

    @Override
    public int compareTo(TopicInMsgDTO o) {
        return o.getInMsg24Hour().compareTo(this.getInMsg24Hour());
    }
}

