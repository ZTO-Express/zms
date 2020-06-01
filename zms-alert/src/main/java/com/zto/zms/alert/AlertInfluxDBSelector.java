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

package com.zto.zms.alert;

import com.zto.zms.common.ZmsConst;
import com.zto.zms.service.selector.InfluxDBSelector;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * <p> Description: </p>
 *
 * @author lidawei
 * @version 1.0
 * @date 2020/1/20
 */
@Component
public class AlertInfluxDBSelector implements InfluxDBSelector, DisposableBean {


	private static Logger logger = LoggerFactory.getLogger(AlertInfluxDBSelector.class);

	private InfluxDB influxDB;

	@PostConstruct
	public void init() {
		String influxDBUrl = System.getProperty(ZmsConst.INFLUXDB);
		influxDB = InfluxDBFactory.connect(influxDBUrl);
		influxDB.enableBatch(200, 1, TimeUnit.SECONDS);
	}

	@Override
	public InfluxDB currentInfluxDB() {
		return influxDB;
	}

	@Override
	public void destroy() throws Exception {
		if (null != this.influxDB) {
			try {
				this.influxDB.flush();
				this.influxDB.close();
				this.influxDB = null;
			} catch (Exception e) {
				logger.error("Destroy error", e);
			}
		}
	}
}

