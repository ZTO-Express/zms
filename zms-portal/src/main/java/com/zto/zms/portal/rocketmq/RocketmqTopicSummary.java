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

package com.zto.zms.portal.rocketmq;


import com.zto.zms.portal.dto.topic.TopicSummaryDTO;

import java.util.List;

/**
 * Created by liangyong on 2019/4/8.
 */
public class RocketmqTopicSummary extends TopicSummaryDTO {

    private List<TopicStatusDto> topicStatusDtoList;
    private List<StatsAllResultDto> statsAllResultDtoList;

    public List<TopicStatusDto> getTopicStatusDtoList() {
        return topicStatusDtoList;
    }

    public void setTopicStatusDtoList(List<TopicStatusDto> topicStatusDtoList) {
        this.topicStatusDtoList = topicStatusDtoList;
    }

    public List<StatsAllResultDto> getStatsAllResultDtoList() {
        return statsAllResultDtoList;
    }

    public void setStatsAllResultDtoList(List<StatsAllResultDto> statsAllResultDtoList) {
        this.statsAllResultDtoList = statsAllResultDtoList;
    }
}

