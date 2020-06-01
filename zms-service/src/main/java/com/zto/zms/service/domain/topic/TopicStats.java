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

package com.zto.zms.service.domain.topic;

import com.google.common.collect.Lists;
import org.apache.rocketmq.common.admin.TopicOffset;
import org.apache.rocketmq.common.admin.TopicStatsTable;
import org.apache.rocketmq.common.message.MessageQueue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TopicStats {
    private int queueId;
    private String topic;
    private String brokerName;
    private long minOffset;
    private long maxOffset;
    private String time;

    public static List<TopicStats> fromTopicStats(TopicStatsTable table) {
        List<TopicStats> stats = Lists.newArrayList();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


        for (Map.Entry<MessageQueue, TopicOffset> entry : table.getOffsetTable().entrySet()) {
            TopicStats stat = new TopicStats();
            stat.setBrokerName(entry.getKey().getBrokerName());
            stat.setQueueId(entry.getKey().getQueueId());
            stat.setTopic(entry.getKey().getTopic());
            stat.setMaxOffset(entry.getValue().getMaxOffset());
            stat.setMinOffset(entry.getValue().getMinOffset());

            stat.setTime(format.format(new Date(entry.getValue().getLastUpdateTimestamp())));
            stats.add(stat);
        }

        return stats;
    }


    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public long getMinOffset() {
        return minOffset;
    }

    public void setMinOffset(long minOffset) {
        this.minOffset = minOffset;
    }

    public long getMaxOffset() {
        return maxOffset;
    }

    public void setMaxOffset(long maxOffset) {
        this.maxOffset = maxOffset;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

