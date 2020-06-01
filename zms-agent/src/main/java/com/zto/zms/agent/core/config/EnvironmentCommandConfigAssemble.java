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

package com.zto.zms.agent.core.config;

import com.google.common.collect.Maps;
import com.zto.zms.writer.IniFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/4
 **/
public class EnvironmentCommandConfigAssemble implements CommandConfigAssemble {

	private static Logger logger = LoggerFactory.getLogger(EnvironmentCommandConfigAssemble.class);

	private Map<String, String> environmentMap = Maps.newTreeMap();

	public static EnvironmentCommandConfigAssemble newInstance() {
		return new EnvironmentCommandConfigAssemble();
	}

	public EnvironmentCommandConfigAssemble addEnvironment(String name, String value) {
		this.environmentMap.put(name, value);
		return this;
	}

	public EnvironmentCommandConfigAssemble addAllEnvironment(Map<String,String> environmentMap){
		logger.info("---------environmentMap"+environmentMap);
		if(CollectionUtils.isEmpty(environmentMap)){
			return this;
		}
		this.environmentMap.putAll(environmentMap);
		return this;
	}

	@Override
	public List<IniFileWriter.IniFileEntity> assemble(List<IniFileWriter.IniFileEntity> options) {
		StringBuilder environment = new StringBuilder();
		environmentMap.entrySet().stream().filter(item -> null != item.getValue())
				.forEach(item ->
						environment
								.append(",")
								.append(item.getKey())
								.append("=")
								.append("'")
								.append(item.getValue())
								.append("'")
				);
		options.add(new IniFileWriter.IniFileEntity("environment", environment.substring(1)));
		return options;
	}
}

