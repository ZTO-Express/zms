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


package com.zto.zms.service.influx;

import com.google.common.collect.Maps;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.stats.*;
import com.zto.zms.utils.Assert;
import com.zto.zms.service.domain.MetricsDo;
import com.zto.zms.service.domain.influxdb.ConsumerInfo;
import com.zto.zms.service.domain.influxdb.DLQAlertRule;
import com.zto.zms.service.domain.influxdb.InfluxCountValue;
import com.zto.zms.service.selector.InfluxDBSelector;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created by superheizai on 2017/9/14.
 */
@Service
public class InfluxdbClient {

	public static final Logger logger = LoggerFactory.getLogger(InfluxdbClient.class);

	@Autowired(required = false)
	private InfluxDBSelector influxdbSelector;

	private static final String MONITOR_DB = "zms_monitor";

	public InfluxDB currentInfluxDB() {
		InfluxDB influxdb = influxdbSelector.currentInfluxDB();
		Assert.that(null != influxdb, "未配置influxDB数据源");
		return influxdb;
	}

	public List<ConsumerInfo> read(List<String> clusters, String template) {
		List<ConsumerInfo> result = new ArrayList<>();
		for (String cluster : clusters) {
			String format = String.format(template, cluster);
			List<ConsumerInfo> consumerLatency = queryList(format, ConsumerInfo.class);
			for (ConsumerInfo latency : consumerLatency) {
				latency.setClusterName(cluster);
				result.add(latency);
			}
		}
		return result;
	}

	public <T> List<T> queryList(String template, Class<T> resultType) {
		QueryResult zto_monitor = this.currentInfluxDB().query(new Query(template
				, MONITOR_DB));
		return InfluxdbModelTransformer.InstanceHolder.getInstance().toPOJOList(zto_monitor, resultType);
	}

	public <T> T query(String template, Class<T> resultType) {
		QueryResult zto_monitor = this.currentInfluxDB().query(new Query(template
				, MONITOR_DB));
		return InfluxdbModelTransformer.InstanceHolder.getInstance().toPOJO(zto_monitor, resultType);
	}

	public void write(ProducerStats producerStats) {

		ClientInfo clientInfo = producerStats.getClientInfo();
		BatchPoints batchPoints = BatchPoints.database(MONITOR_DB).tag("clientName", clientInfo.getClientName()).tag("zmsName", clientInfo.getZmsName()).tag("ip", clientInfo.getIp()).build();

		for (MeterInfo info : producerStats.getMeters()) {
			batchPoints.point(Point.measurement(info.getType()).fields(buildMeterFields(info)).build());
		}

		for (TimerInfo info : producerStats.getTimers()) {
			batchPoints.point(Point.measurement(info.getType()).fields(buildTimerFields(info)).build());

		}
		for (DistributionInfo info : producerStats.getDistributions()) {
			batchPoints.point(Point.measurement(info.getType()).fields(buildDistributionFields(info)).build());

		}

		try {
			this.currentInfluxDB().write(batchPoints);
		} catch (Exception ex) {
			logger.error("write influx error", ex);
		}

	}

	public void write(ConsumerStats consumerStats) {

		ClientInfo clientInfo = consumerStats.getClientInfo();
		BatchPoints batchPoints = BatchPoints.database(MONITOR_DB).tag("clientName", clientInfo.getClientName()).tag("zmsName", clientInfo.getZmsName()).tag("ip", clientInfo.getIp()).build();

		for (MeterInfo info : consumerStats.getMeters()) {
			batchPoints.point(Point.measurement(info.getType()).fields(buildMeterFields(info)).build());
		}

		for (TimerInfo info : consumerStats.getTimers()) {
			batchPoints.point(Point.measurement(info.getType()).fields(buildTimerFields(info)).build());

		}
		try {
			this.currentInfluxDB().write(batchPoints);
		} catch (Exception ex) {
			logger.error("write influx error", ex);
		}

	}

	private Map<String, Object> buildMeterFields(MeterInfo meter) {
		Map<String, Object> fields = Maps.newHashMap();
		fields.put("count", meter.getCount());
		fields.put("mean", meter.getMean());
		fields.put("min1rate", meter.getMin1Rate());
		fields.put("min5rate", meter.getMin5Rate());
		fields.put("min15rate", meter.getMin15Rate());
		return fields;
	}


	private Map<String, Object> buildTimerFields(TimerInfo timer) {
		Map<String, Object> fields = Maps.newHashMap();
		fields.put("median", timer.getMedian() / 1000000);
		fields.put("mean", timer.getMean() / 1000000);
		fields.put("max", timer.getMax() / 1000000);
		fields.put("min", timer.getMin() / 1000000);
		fields.put("percent75", timer.getPercent75() / 1000000);
		fields.put("percent90", timer.getPercent90() / 1000000);
		fields.put("percent95", timer.getPercent95() / 1000000);
		fields.put("percent98", timer.getPercent98() / 1000000);
		fields.put("percent99", timer.getPercent99() / 1000000);
		fields.put("percent999", timer.getPercent999() / 1000000);
		return fields;
	}

	private Map<String, Object> buildDistributionFields(DistributionInfo distribution) {
		Map<String, Object> fields = Maps.newHashMap();
		fields.put("less1", distribution.getLessThan1Ms());
		fields.put("less5", distribution.getLessThan5Ms());
		fields.put("less10", distribution.getLessThan10Ms());
		fields.put("less50", distribution.getLessThan50Ms());
		fields.put("less100", distribution.getLessThan100Ms());
		fields.put("less500", distribution.getLessThan500Ms());
		fields.put("less1000", distribution.getLessThan1000Ms());
		fields.put("more1000", distribution.getMoreThan1000Ms());
		return fields;
	}

	public void insertNotifications(String name, String field, String type, String description) {

		Point point = Point.measurement(ZmsConst.Measurement.TRIGGER_RULES)
				.tag("name", name)
				.tag("field", field)
				.tag("type", type)
				.addField("desc", description).build();
		this.currentInfluxDB().setDatabase(MONITOR_DB);
		this.currentInfluxDB().write(point);
	}

	public void insertDLQNotifications(DLQAlertRule alertMsg) {

		Point point = Point.measurement(ZmsConst.Measurement.DLQ_ALERT_MSG_INFO)
				.tag("cluster", alertMsg.getClusterName())
				.tag("topicName", alertMsg.getTopicName())
				.addField("msg", alertMsg.getMsg()).build();
		this.currentInfluxDB().setDatabase(MONITOR_DB);
		this.currentInfluxDB().write(point);
	}

	@PreDestroy
	public void close() {
		if (null != influxdbSelector) {
			influxdbSelector.currentInfluxDB().close();
		}
	}

	public int executeInfluxSql(String influxSql) {
		QueryResult results = currentInfluxDB().query(new Query(influxSql, MONITOR_DB));
		if (results.getResults() == null) {
			return 0;
		}
		InfluxCountValue count = InfluxdbModelTransformer.InstanceHolder.getInstance().toPOJO(results, InfluxCountValue.class);
		if (count == null) {
			return 0;
		}
		return count.getCount();
	}

	public QueryResult query(String sql) {
		return this.currentInfluxDB().query(new Query(sql, MONITOR_DB));
	}


	public void writeData(List<MetricsDo> metricsList, String measurement, Long timestamp) {
		BatchPoints batchPoints = BatchPoints.database(MONITOR_DB).consistency(InfluxDB.ConsistencyLevel.ALL).build();
		for (MetricsDo metrics : metricsList) {
			if (null == metrics) {
				continue;
			}
			Map<String, Object> segmentMap = metrics.getSegmentMap();
			for (Map.Entry<String, Object> entry : segmentMap.entrySet()) {
				Point.Builder builder = Point.measurement(measurement)
						.time(timestamp, TimeUnit.MILLISECONDS);
				builder.tag(metrics.getTagOptions());
                if(entry.getValue() instanceof String){
                    builder.tag("name", entry.getKey())
                            .addField("value", valueOf((String) entry.getValue()));
                }else {
                    builder.tag("name", entry.getKey())
                            .addField("value", valueOf((Number) entry.getValue()));
                }
				batchPoints.point(builder.build());
			}
		}
		this.currentInfluxDB().write(batchPoints);
	}

	private Double valueOf(Number value) {
		return BigDecimal.valueOf(value.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	private String valueOf(String value) {
		return value;
	}

}


