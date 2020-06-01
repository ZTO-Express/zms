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

package com.zto.zms.agent.web.task;

import com.zto.zms.common.ServiceProcess;
import com.zto.zms.agent.core.api.SupervisordApi;
import com.zto.zms.agent.web.api.ZmsPortalApi;
import com.zto.zms.agent.web.dto.SupervisordProcessInfoDTO;
import com.zto.zms.agent.web.service.ZmsPortalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuhao.zhang
 * @description
 * @date 2020/3/6
 */
@Configuration
@EnableScheduling
public class ServiceMonitorTask {

	public static final Logger logger = LoggerFactory.getLogger(ServiceMonitorTask.class);

	@Autowired
	private SupervisordApi supervisordApi;

	@Autowired
	private ZmsPortalService zmsPortalService;

	@Autowired
	private ZmsPortalApi zmsPortalApi;

	@Scheduled(cron = "0/10 * * * * ?")
	private void configureTasks() throws Exception {
		logger.info("runningStatus task");
		List<ServiceProcess> serviceProcesses = new ArrayList<>();
		List<SupervisordProcessInfoDTO> supervisordProcessInfoDTOS = supervisordApi.getAllProcessInfo();
		for (SupervisordProcessInfoDTO processInfoDto : supervisordProcessInfoDTOS) {
			String programName = processInfoDto.getName();
			Integer processId = Integer.valueOf(programName.split("-")[0]);
			String stateName = processInfoDto.getStatename();
			ServiceProcess serviceProcess = new ServiceProcess();
			serviceProcess.setProcessId(processId);
			serviceProcess.setStateName(stateName);
			serviceProcesses.add(serviceProcess);
		}
		zmsPortalApi.runningStatus(serviceProcesses);
	}

	/**
	 * agent服务监测
	 *
	 */
	@Scheduled(cron = "0/10 * * * * ?")
	public void agentMonitor() {
		try {
			zmsPortalService.addHost();
		} catch (Exception e) {
			logger.error("新增主机信息异常", e);
		}
	}

}

