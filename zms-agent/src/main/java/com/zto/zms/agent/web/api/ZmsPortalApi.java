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

package com.zto.zms.agent.web.api;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.zto.zms.common.RunCommonConfig;
import com.zto.zms.common.ZmsHost;
import com.zto.zms.utils.Assert;
import com.zto.zms.utils.HttpClient;
import com.zto.zms.agent.web.dto.HostMonitorDTO;
import com.zto.zms.common.Result;
import com.zto.zms.common.ServiceProcess;
import com.zto.zms.agent.web.util.FileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import static com.zto.zms.common.ZmsConst.ZMS_PORTAL;
import static com.zto.zms.agent.core.constant.EnvironmentOptConstant.PROCESS_DIR;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/2/28
 **/
@Service
public class ZmsPortalApi {
	private static Logger logger = LoggerFactory.getLogger(ZmsPortalApi.class);

	private String uploadConfigFileUrl = "/api/process/{0}/serverConfig";

	private String environmentConfig = "/api/process/{0}/environmentConfig";

	private String runCommonConfig = "/api/process/{0}/runCommonConfig";

	private String runningStatusUrl = "/api/process/runningstatus";

	private String listConfig = "/api/process/listConfig";

	private String addHost = "/api/master/addHost";

	private String processDir = System.getProperty(PROCESS_DIR);

	@Autowired
	private FileHelper fileHelper;

	@Autowired
	private HttpClient httpClient;

	@PostConstruct
	public void init() {
		uploadConfigFileUrl = System.getProperty(ZMS_PORTAL) + uploadConfigFileUrl;
		environmentConfig = System.getProperty(ZMS_PORTAL) + environmentConfig;
		runningStatusUrl = System.getProperty(ZMS_PORTAL) + runningStatusUrl;
		listConfig = System.getProperty(ZMS_PORTAL) + listConfig;
		addHost = System.getProperty(ZMS_PORTAL) + addHost;
		runCommonConfig = System.getProperty(ZMS_PORTAL) + runCommonConfig;
	}


	/**
	 * 下载配置文件并解压到指定目录
	 *
	 * @param processId
	 * @throws IOException
	 */
	public void uploadConfigFile(Integer processId, String programName) throws IOException {
		String outDir = processDir + "/" + programName;
		String requestUrl = MessageFormat.format(uploadConfigFileUrl, processId);
		fileHelper.uploadUnZipFile(requestUrl, outDir);
		logger.info("upload config file to :{}", outDir);
	}


	/**
	 * 调用portal接口
	 * @param serviceProcesses
	 * @return
	 * @throws IOException
	 */
	public Result runningStatus(List<ServiceProcess> serviceProcesses) throws IOException {
		return httpClient.post(runningStatusUrl, serviceProcesses, Result.class);
	}


	public void addHost(String hostIp, String hostName, String hostStatus, String token, HostMonitorDTO hostMonitorDto) throws IOException {
		ZmsHost zmsHost = new ZmsHost();
		Map<String, String> params = Maps.newHashMap();
		zmsHost.setHostIp(hostIp);
		zmsHost.setToken(token);
		zmsHost.setHostName(hostName);
		zmsHost.setHostStatus(hostStatus);
		zmsHost.setTotalMem(String.valueOf(hostMonitorDto.getTotalMem()));
		zmsHost.setCpuRate(hostMonitorDto.getCpuRate());
		Result result = httpClient.post(addHost, params, Result.class);
		Assert.that(result.isStatus(), "addHost error:" + result.getMessage());
	}


	/**
	 * 运行指令信息
	 *
	 * @param processId
	 * @return
	 * @throws IOException
	 */
	public RunCommonConfig runCommon(Integer processId) throws IOException {
		String requestUrl = MessageFormat.format(runCommonConfig, processId);
		String envParam = httpClient.getWithString(requestUrl, Maps.newHashMap());
		Result result = JSON.parseObject(envParam, Result.class);

		Assert.that(result.isStatus(), "runCommon error:" + result.getMessage());
		return JSON.parseObject(JSON.toJSONString(result.getResult()), RunCommonConfig.class);
	}
}

