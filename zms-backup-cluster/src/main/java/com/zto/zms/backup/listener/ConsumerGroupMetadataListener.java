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

package com.zto.zms.backup.listener;

import com.zto.zms.backup.api.ZmsPortalApi;
import com.zto.zms.backup.util.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * <p>消费组备份处理 </p>
 *
 * @author lidawei
 * @date 2020/5/11
 **/
public class ConsumerGroupMetadataListener implements MetadataListener {

	public static final Logger logger = LoggerFactory.getLogger(TopicMetadataListener.class);

	private Integer currentEnvId;
	private Integer originEnvId;
	private String name;
	private String clusterName;
	private String bindingTopic;

	public ConsumerGroupMetadataListener(String clusterName, String bindingTopic, String name) {
		this.currentEnvId = ConfigUtil.getCurrentEnvId();
		this.originEnvId = ConfigUtil.getOriginEnvId();
		this.name = name;
		this.bindingTopic = bindingTopic;
		this.clusterName = clusterName;
	}


	@Override
	public void backup() {
		try {
			ZmsPortalApi.backupConsumerMetaData(originEnvId, currentEnvId, clusterName, bindingTopic, name);
		} catch (Exception e) {
			logger.error(MessageFormat.format("Backup consumer metaData error:{0}", this.toString()), e);
		}
	}

	@Override
	public String toString() {
		return "ConsumerGroupMetadataListenerImpl{" +
				"currentEnvId=" + currentEnvId +
				", originEnvId=" + originEnvId +
				", name='" + name + '\'' +
				", clusterName='" + clusterName + '\'' +
				", bindingTopic='" + bindingTopic + '\'' +
				'}';
	}
}

