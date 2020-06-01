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

package com.zto.zms.backup.util;

import com.alibaba.fastjson.JSON;
import com.zto.zms.common.ZmsException;
import com.zto.zms.utils.Assert;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author lidawei
 * @date 2020/5/11
 * @since 1.0.0
 **/
public class ConfigUtil {

	private static Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

	private static final String CONFIG_FILE = "config";
	private static final String CONFIG_FILE_NAME = "config.properties";

	public static final String BACKUP_CLUSTER_MAP = "backup.cluster.map";
	public static final String ORIGIN_ZMZ_ZK = "origin.zms_zk";
	public static final String ORIGIN_ENV_ID = "origin.envId";
	public static final String CURRENT_ENV_ID = "current.envId";
	public static final String CURRENT_ZMS_ZK = "current.zms_zk";

	private static Properties properties = new Properties();

	static {
		String configFile = System.getProperty(CONFIG_FILE);
		Assert.that(StringUtils.isNotBlank(configFile), "No configuration file was specified");

		File file = new File(String.join("/", configFile, CONFIG_FILE_NAME));
		try (FileInputStream inputStream = new FileInputStream(file)) {
			properties.load(inputStream);
		} catch (Exception e) {
			logger.error("Load configuration file exception", e);
			throw new ZmsException("Load configuration file  error", 401);
		}
	}

	public static String getConfig(String key) {
		return properties.getProperty(key);
	}

	public static Integer getOriginEnvId() {
		return Integer.valueOf(getConfig(ORIGIN_ENV_ID));
	}

	public static Integer getCurrentEnvId() {
		return Integer.valueOf(getConfig(CURRENT_ENV_ID));
	}

	/**
	 * 集群备份映射关系
	 * key: 需要备份集群
	 * value: 备份到的目标集群
	 *
	 * @return
	 */
	public static Map<String, String> getBackupClusterMap() {
		//初始化，环境的集群元数据进行同步
		String backupClusterMap = getConfig(BACKUP_CLUSTER_MAP);
		return JSON.parseObject(backupClusterMap, Map.class);
	}


}

