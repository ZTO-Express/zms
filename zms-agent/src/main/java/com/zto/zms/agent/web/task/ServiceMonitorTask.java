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

import ch.qos.logback.core.spi.LifeCycle;
import com.zto.zms.agent.web.dto.HostMonitorDTO;
import com.zto.zms.agent.web.dto.SupervisordStateDTO;
import com.zto.zms.agent.web.util.SysOperationUtil;
import com.zto.zms.common.ServiceProcess;
import com.zto.zms.agent.core.api.SupervisordApi;
import com.zto.zms.agent.web.api.ZmsPortalApi;
import com.zto.zms.agent.web.dto.SupervisordProcessInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springside.modules.utils.net.NetUtil;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.zto.zms.common.ZmsConst.ZMS_TOKEN;

/**
 * @author yuhao.zhang
 * @description
 * @date 2020/3/6
 */
public class ServiceMonitorTask {

	public static final Logger logger = LoggerFactory.getLogger(ServiceMonitorTask.class);
	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);


	public void start(){
		executor.scheduleAtFixedRate(() -> configureTasks(),10, 10, TimeUnit.SECONDS);
		executor.scheduleAtFixedRate(() -> agentMonitor(),10, 10, TimeUnit.SECONDS);
	}
	private void configureTasks(){
		logger.info("runningStatus task");
		try {
			List<ServiceProcess> serviceProcesses = new ArrayList<>();
			List<SupervisordProcessInfoDTO> supervisordProcessInfoDTOS = SupervisordApi.getInstance().getAllProcessInfo();
			for (SupervisordProcessInfoDTO processInfoDto : supervisordProcessInfoDTOS) {
				String programName = processInfoDto.getName();
				Integer processId = Integer.valueOf(programName.split("-")[0]);
				String stateName = processInfoDto.getStatename();
				ServiceProcess serviceProcess = new ServiceProcess();
				serviceProcess.setProcessId(processId);
				serviceProcess.setStateName(stateName);
				serviceProcesses.add(serviceProcess);
			}
			ZmsPortalApi.getInstance().runningStatus(serviceProcesses);
		}catch (Exception e){
			logger.error("task error:"+e.getMessage(),e);
		}

	}

	/**
	 * agent服务监测
	 *
	 */
	public void agentMonitor() {
		try {
			String token = System.getProperty(ZMS_TOKEN);
			InetAddress localAddress = NetUtil.getLocalAddress();
			String ip = localAddress.getHostAddress();
			String hostName = localAddress.getHostName();
			String hostStatus = "DISABLE";
			//验证supervisor是否正常启动
			SupervisordStateDTO supervisordStateDto = SupervisordApi.getInstance().getState();
			if (1 == supervisordStateDto.getStatecode()) {
				hostStatus = "ENABLE";
			}
			HostMonitorDTO hostMonitorDto = SysOperationUtil.getHostInfo();
			ZmsPortalApi.getInstance().addHost(ip, hostName, hostStatus, token, hostMonitorDto);
		} catch (Exception e) {
			logger.error("新增主机信息异常", e);
		}
	}

}

