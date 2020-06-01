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

package com.zto.zms.collector.selector;

import com.zto.zms.common.ZmsConst;
import com.zto.zms.service.selector.ZookeeperSelector;
import com.zto.zms.zookeeper.ZmsZkClient;
import kafka.utils.ZKStringSerializer$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/1/20
 **/
@Component
public class CollectorZookeeperSelector implements ZookeeperSelector, DisposableBean {

	private static Logger logger = LoggerFactory.getLogger(CollectorZookeeperSelector.class);

	private ZmsZkClient zkClient;

	@PostConstruct
	public void init() {
		String zmsZk = System.getProperty(ZmsConst.ZK.ZMS_STARTUP_PARAM);
		zkClient = new ZmsZkClient(zmsZk, 20 * 1000, 10 * 1000, ZKStringSerializer$.MODULE$);
		logger.info("Initialization zookeeper successful:{}", zmsZk);
	}

	@Override
	public ZmsZkClient currentZkClient() {
		return zkClient;
	}

	@Override
	public void destroy() throws Exception {
		if (null != this.zkClient) {
			try {
				this.zkClient.close();
				this.zkClient = null;
			} catch (Exception e) {
				logger.error("ZkClient Destroy error",e);
			}
		}
	}
}

