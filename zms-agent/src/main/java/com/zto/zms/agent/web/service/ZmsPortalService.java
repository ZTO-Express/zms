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

package com.zto.zms.agent.web.service;

import com.zto.zms.agent.core.api.SupervisordApi;
import com.zto.zms.agent.web.api.ZmsPortalApi;
import com.zto.zms.agent.web.dto.HostMonitorDTO;
import com.zto.zms.agent.web.dto.SupervisordStateDTO;
import com.zto.zms.agent.web.util.SysOperationUtil;
import org.apache.xmlrpc.XmlRpcException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springside.modules.utils.net.NetUtil;

import java.io.IOException;
import java.net.InetAddress;

import static com.zto.zms.common.ZmsConst.ZMS_TOKEN;

/**
 * <p>Class: ZmsPortalProcess</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/10
 **/

@Service
public class ZmsPortalService {

	@Autowired
	private ZmsPortalApi zmsPortalApi;

	@Autowired
	private SupervisordApi supervisordApi;

	@Autowired
	private SysOperationUtil sysOperationUtil;

	/**
	 * 新增主机
	 *
	 * @throws IOException
	 */
	public void addHost() throws IOException, InterruptedException, XmlRpcException {
		String token = System.getProperty(ZMS_TOKEN);
		InetAddress localAddress = NetUtil.getLocalAddress();
		String ip = localAddress.getHostAddress();
		String hostName = localAddress.getHostName();
		String hostStatus = "DISABLE";
		//验证supervisor是否正常启动
		SupervisordStateDTO supervisordStateDto = supervisordApi.getState();
		if (1 == supervisordStateDto.getStatecode()) {
			hostStatus = "ENABLE";
		}
		HostMonitorDTO hostMonitorDto = sysOperationUtil.getHostInfo();
		zmsPortalApi.addHost(ip, hostName, hostStatus, token, hostMonitorDto);
	}

}

