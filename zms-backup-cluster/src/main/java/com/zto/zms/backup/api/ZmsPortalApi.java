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

package com.zto.zms.backup.api;

import com.zto.zms.utils.Assert;
import com.zto.zms.utils.HttpClient;
import com.zto.zms.common.Result;
import com.zto.zms.backup.util.ConfigUtil;
import com.zto.zms.service.domain.consumer.BackupConsumerMetadataDTO;
import com.zto.zms.service.domain.topic.BackupTopicMetadataDTO;
import com.zto.zms.utils.HMacVerifyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;

import static com.zto.zms.common.ZmsConst.ZMS_PORTAL;
import static com.zto.zms.common.ZmsConst.ZMS_TOKEN;


/**
 *
 * @author lidawei
 * @since 1.0.0
 */
public class ZmsPortalApi {

	private static Logger logger = LoggerFactory.getLogger(ZmsPortalApi.class);

	private static String backupTopicMetaDataUrl = "/api/topic/backupMetadata?sign={0}";

	private static String backupConsumerMetaDataUrl = "/api/consumer/backupMetadata?sign={0}";
	private static String PORTAL_URL = ConfigUtil.getConfig(ZMS_PORTAL);
	private static String TOKEN = ConfigUtil.getConfig(ZMS_TOKEN);

	static {
		Assert.that(StringUtils.isNotBlank(PORTAL_URL), "zms.portal is not configured");
		Assert.that(StringUtils.isNotBlank(TOKEN), "zms.token is not configured");

		backupTopicMetaDataUrl = PORTAL_URL + backupTopicMetaDataUrl;
		backupConsumerMetaDataUrl = PORTAL_URL + backupConsumerMetaDataUrl;
		logger.info("Init HttpClientHelper,portal:{}", PORTAL_URL);
	}


	public static void backupTopicMetaData(Integer originEnvId, Integer currentEnvId, String clusterName, String topic) throws IOException {
		BackupTopicMetadataDTO backupTopicMetadataDTO =
				new BackupTopicMetadataDTO(originEnvId, currentEnvId, clusterName, topic);
		String url = sign(backupTopicMetaDataUrl, backupTopicMetadataDTO);
		Result result = HttpClient.post(url, backupTopicMetadataDTO, Result.class);
		Assert.that(result.isStatus(), "Backup topic metaData error:" + result.getMessage());
	}


	public static void backupConsumerMetaData(Integer originEnvId, Integer currentEnvId, String clusterName, String bindingTopic, String consumerName) throws IOException {
		BackupConsumerMetadataDTO backupConsumerMetadataDTO =
				new BackupConsumerMetadataDTO(originEnvId, currentEnvId, clusterName, bindingTopic, consumerName);
		String url = sign(backupConsumerMetaDataUrl, backupConsumerMetadataDTO);
		Result result = HttpClient.post(url, backupConsumerMetadataDTO, Result.class);
		Assert.that(result.isStatus(), "Backup consumer metaData error:" + result.getMessage());
	}

	private static String sign(String url, Object object) {
		String sign = HMacVerifyUtil.generateSign(object, TOKEN);
		return MessageFormat.format(url, sign);
	}
}

