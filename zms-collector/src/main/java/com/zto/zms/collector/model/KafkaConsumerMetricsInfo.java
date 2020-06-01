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

package com.zto.zms.collector.model;

import com.google.common.collect.Lists;
import java.util.List;

 /**
  * <p> Description: </p>
  *
  * @author liangyong
  * @date 2018/10/30
  * @since 1.0.0
  */
public class KafkaConsumerMetricsInfo {

    private String clusterName;

    private List<ConsumerStatus> kafkaLagInfos = Lists.newArrayList();

    private long timestamp;


    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public List<ConsumerStatus> getKafkaLagInfos() {
        return kafkaLagInfos;
    }

    public void setKafkaLagInfos(List<ConsumerStatus> kafkaLagInfos) {
        this.kafkaLagInfos = kafkaLagInfos;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

