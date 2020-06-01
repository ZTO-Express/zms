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

package com.zto.zms.collector.report;

import com.google.common.collect.Lists;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.collector.model.ClusterRtTime;
import com.zto.zms.service.domain.MetricsDo;
import com.zto.zms.service.domain.influxdb.ClusterDailyOffsetsInfo;
import com.zto.zms.service.influx.TopicDailyOffsetsInfo;
import com.zto.zms.service.repository.ClusterDailyOffsetsInfoRepository;
import com.zto.zms.service.repository.TopicDailyOffsetsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

public abstract class AbstractReporter implements IReporter{

	@Autowired
	protected ClusterDailyOffsetsInfoRepository clusterDailyOffsetsInfoRepository;
    @Autowired
	protected TopicDailyOffsetsRepository topicDailyOffsetsRepository;
	@Autowired
	protected MetricBuffer metricBuffer;
	@Autowired
	protected MetricsTransformService metricsTransformService;


	protected void saveStatisticClusterOffset(String clusterName, long maxClusterOffset) {
		ClusterDailyOffsetsInfo latestStatisticClusterOffset = clusterDailyOffsetsInfoRepository.getLastClusterMaxOffset(clusterName);
		long clusterIncrease = 0L;
		if (null != latestStatisticClusterOffset) {
			clusterIncrease = maxClusterOffset - Double.valueOf(latestStatisticClusterOffset.getValue()).longValue();
		}
        List<MetricsDo> metricsList = Lists.newArrayList();
		MetricsDo metricsDo = new MetricsDo();
		Map<String, String> tagOptions = metricsDo.getTagOptions();
		Map<String, Double> segmentMap = metricsDo.getSegmentMap();
		tagOptions.put("clusterName", clusterName);
		segmentMap.put("incrementVal", (double) clusterIncrease);
		segmentMap.put("maxOffset", (double) maxClusterOffset);
        metricsList.add(metricsDo);

		metricBuffer.put(ZmsConst.Measurement.STATISTIC_CLUSTER_DAILY_OFFSETS_INFO,metricsList);
	}

	/**
	 * 记录主题日消息偏移量
	 * @param clusterName
	 * @param topic
	 * @param maxTopicOffset
	 */
	protected void saveStatisticTopicOffset(String clusterName, String topic, long maxTopicOffset) {
		long increaseVal = 0L;
        TopicDailyOffsetsInfo latestStatisticTopicOffset = topicDailyOffsetsRepository.getLastTopicMaxOffset(clusterName,topic);
		if (null != latestStatisticTopicOffset) {
			increaseVal = maxTopicOffset - Double.valueOf(latestStatisticTopicOffset.getValue()).longValue();
		}
        List<MetricsDo> metricsList = Lists.newArrayList();

        MetricsDo metricsDo = new MetricsDo();
        Map<String, String> tagOptions = metricsDo.getTagOptions();
        Map<String, Double> segmentMap = metricsDo.getSegmentMap();
        tagOptions.put("clusterName", clusterName);
        tagOptions.put("topicName", topic);
        segmentMap.put("incrementVal", (double) increaseVal);
        segmentMap.put("maxOffset", (double) maxTopicOffset);
        metricsList.add(metricsDo);

		metricBuffer.put(ZmsConst.Measurement.STATISTIC_TOPIC_DAILY_OFFSETS_INFO,metricsList);
	}


	public void saveRtMetrics(ClusterRtTime clusterRtTime) {
		List<MetricsDo> metricsDoList = metricsTransformService.transformMqRtInfo(clusterRtTime);
		metricBuffer.put(ZmsConst.Measurement.RT_TIME, metricsDoList);
		List<MetricsDo> clusterMetrics = metricsTransformService.transformClusterMetrics(clusterRtTime);
		metricBuffer.put(ZmsConst.Measurement.CLUSTER_NUMBER_INFO, clusterMetrics);
	}
}

