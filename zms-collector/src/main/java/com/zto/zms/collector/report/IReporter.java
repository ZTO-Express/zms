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

/**
 * <p>Title: IReporter.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: www.zto.com</p>
 */
package com.zto.zms.collector.report;

import com.zto.zms.metadata.ClusterMetadata;

public interface IReporter {
    void refreshMetaOfCluster(ClusterMetadata cluster);

    void collectMqMetrics(ClusterMetadata cluster);

    /**
     * 记录
     * @param cluster
     */
    void collectRtTime(ClusterMetadata cluster);

    /**
     * 记录集群和topic的消息偏移量
     * @param cluster
     */
    void recordMaxOffset(ClusterMetadata cluster);

    void recordMaxOffsetInflux(ClusterMetadata cluster);

    /**
     * 记录集群中，主题tps前10个指标
     */
    void topicTpsTop10();

    /**
     * 消费延迟前10
     */
    void consumerLatencyTop10();
}

