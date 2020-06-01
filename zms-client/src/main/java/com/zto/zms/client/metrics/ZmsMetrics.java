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

package com.zto.zms.client.metrics;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.zto.zms.stats.*;
import org.springside.modules.utils.net.NetUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by superheizai on 2017/8/9.
 */
public abstract class ZmsMetrics {
	private String clientName;
	private String zmsName;

	private ClientInfo clientInfo;


	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getZmsName() {
		return zmsName;
	}

	public void setZmsName(String zmsName) {
		this.zmsName = zmsName;
	}

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	public void setClientInfo(ClientInfo clientInfo) {
		this.clientInfo = clientInfo;
	}

	ZmsMetrics(String clientName, String zmsName) {
		this.clientName = clientName;
		this.zmsName = zmsName;
		clientInfo = buildClientInfo(clientName, zmsName);

	}

	public MeterInfo transfer(Meter meter, String type) {
		MeterInfo info = new MeterInfo();
		info.setCount(meter.getCount());
		info.setMean(meter.getMeanRate());
		info.setMin1Rate(meter.getOneMinuteRate());
		info.setMin5Rate(meter.getFiveMinuteRate());
		info.setMin15Rate(meter.getFifteenMinuteRate());
		info.setType(type);
		return info;
	}

	public TimerInfo transfer(Timer timer, String type) {
		TimerInfo info = new TimerInfo();
		Snapshot snapshot = timer.getSnapshot();
		info.setMean(transferToMilliseconds(snapshot.getMean()));
		info.setMin(transferToMilliseconds(snapshot.getMin()));
		info.setMax(transferToMilliseconds(snapshot.getMax()));
		info.setMedian(transferToMilliseconds(snapshot.getMedian()));
		info.setStddev(transferToMilliseconds(snapshot.getStdDev()));
		info.setPercent75(transferToMilliseconds(snapshot.get75thPercentile()));
		info.setPercent90(transferToMilliseconds(snapshot.getValue(0.9)));
		info.setPercent95(transferToMilliseconds(snapshot.get95thPercentile()));
		info.setPercent98(transferToMilliseconds(snapshot.get98thPercentile()));
		info.setPercent99(transferToMilliseconds(snapshot.get99thPercentile()));
		info.setPercent999(transferToMilliseconds(snapshot.get999thPercentile()));
		info.setType(type);
		return info;
	}

	public DistributionInfo transfer(Distribution distribution, String type) {
		DistributionInfo info = new DistributionInfo();
		info.setLessThan1Ms(distribution.getLessThan1Ms().longValue());
		info.setLessThan5Ms(distribution.getLessThan5Ms().longValue());
		info.setLessThan10Ms(distribution.getLessThan10Ms().longValue());
		info.setLessThan50Ms(distribution.getLessThan50Ms().longValue());
		info.setLessThan100Ms(distribution.getLessThan100Ms().longValue());
		info.setLessThan500Ms(distribution.getLessThan500Ms().longValue());
		info.setLessThan1000Ms(distribution.getLessThan1000Ms().longValue());
		info.setMoreThan1000Ms(distribution.getMoreThan1000Ms().longValue());
		info.setType(type);
		return info;
	}

	public abstract StatsInfo reportMessageStatistics();

	public abstract String reportLogStatistics();

	void processMeter(Metered meter, StringBuilder context) throws Exception {
		context.append(String.format("             count = %d\n", meter.getCount()));
		context.append(String.format("         mean rate = %.2f \n", meter.getMeanRate()));
		context.append(String.format("     1-minute rate = %.2f \n", meter.getOneMinuteRate()));
		context.append(String.format("     5-minute rate = %.2f \n", meter.getFiveMinuteRate()));
		context.append(String.format("    15-minute rate = %.2f \n", meter.getFifteenMinuteRate()));
	}

	void processTimer(Timer timer, StringBuilder context) throws Exception {
		processMeter(timer, context);
		final Snapshot snapshot = timer.getSnapshot();
		context.append(String.format("               min = %d\n", snapshot.getMin()));
		context.append(String.format("               max = %d\n", snapshot.getMax()));
		context.append(String.format("              mean = %.2f\n", snapshot.getMean()));
		context.append(String.format("            stddev = %.2f\n", snapshot.getStdDev()));
		context.append(String.format("            median = %.2f\n", snapshot.getMedian()));
		context.append(String.format("              75%% <= %.2f\n", snapshot.get75thPercentile()));
		context.append(String.format("              90%% <= %.2f\n", snapshot.getValue(0.90)));
		context.append(String.format("              95%% <= %.2f\n", snapshot.get95thPercentile()));
		context.append(String.format("              98%% <= %.2f\n", snapshot.get98thPercentile()));
		context.append(String.format("              99%% <= %.2f\n", snapshot.get99thPercentile()));
		context.append(String.format("              999%% <= %.2f\n", snapshot.get999thPercentile()));
	}

	private ClientInfo buildClientInfo(String clientName, String zmsName) {
		ClientInfo clientInfo = new ClientInfo();
		clientInfo.setClientName(clientName);
		clientInfo.setZmsName(zmsName);
		clientInfo.setIp(NetUtil.getLocalHost());
		return clientInfo;
	}

	private double transferToMilliseconds(double costTime) {
		BigDecimal data = new BigDecimal(costTime);
		BigDecimal bigDecimal = data.divide(new BigDecimal(1000000), RoundingMode.HALF_DOWN);
		return bigDecimal.doubleValue();
	}

}

