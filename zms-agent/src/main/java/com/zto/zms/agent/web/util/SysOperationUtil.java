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

package com.zto.zms.agent.web.util;

import com.zto.zms.agent.web.dto.HostMonitorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

/**
 * @author yuhao.zhang
 * @date 2020/3/26
 */
@Component
public class SysOperationUtil {

    public static final Logger logger = LoggerFactory.getLogger(SysOperationUtil.class);

    /**
     * get memory by used info
     *
     * @return int[] result
     * result.length==4;int[0]=MemTotal;int[1]=MemFree;int[2]=SwapTotal;int[3]=SwapFree;
     * @throws IOException
     * @throws InterruptedException
     */
    public static int[] getMemInfo() throws IOException, InterruptedException
    {
        File file = new File("/proc/meminfo");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)));
        int[] result = new int[4];
        String str = null;
        StringTokenizer token = null;
        while((str = br.readLine()) != null)
        {
            token = new StringTokenizer(str);
            if(!token.hasMoreTokens()) {
                continue;
            }

            str = token.nextToken();
            if(!token.hasMoreTokens()) {
                continue;
            }

            if("MemTotal:".equalsIgnoreCase(str)) {
                result[0] = Integer.parseInt(token.nextToken());
            } else if("MemFree:".equalsIgnoreCase(str)) {
                result[1] = Integer.parseInt(token.nextToken());
            } else if("SwapTotal:".equalsIgnoreCase(str)) {
                result[2] = Integer.parseInt(token.nextToken());
            } else if("SwapFree:".equalsIgnoreCase(str)) {
                result[3] = Integer.parseInt(token.nextToken());
            }
        }

        return result;
    }

    /**
     * get memory by used info
     *
     * @return float efficiency
     * @throws IOException
     * @throws InterruptedException
     */
    public static float getCpuInfo() throws IOException, InterruptedException
    {
        File file = new File("/proc/stat");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)));
        StringTokenizer token = new StringTokenizer(br.readLine());
        token.nextToken();
        long user1 = Long.parseLong(token.nextToken());
        long nice1 = Long.parseLong(token.nextToken());
        long sys1 = Long.parseLong(token.nextToken());
        long idle1 = Long.parseLong(token.nextToken());

        Thread.sleep(1000);

        br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file)));
        token = new StringTokenizer(br.readLine());
        token.nextToken();
        long user2 = Long.parseLong(token.nextToken());
        long nice2 = Long.parseLong(token.nextToken());
        long sys2 = Long.parseLong(token.nextToken());
        long idle2 = Long.parseLong(token.nextToken());

        return (float)((user2 + sys2 + nice2) - (user1 + sys1 + nice1)) / (float)((user2 + nice2 + sys2 + idle2) - (user1 + nice1 + sys1 + idle1));
    }

    public HostMonitorDTO getHostInfo() throws IOException, InterruptedException {
        int[] memInfo = SysOperationUtil.getMemInfo();
        HostMonitorDTO hostMonitorDTO = new HostMonitorDTO();
        logger.debug("MemTotal：" + memInfo[0]);
        logger.debug("MemFree：" + memInfo[1]);
        logger.debug("SwapTotal：" + memInfo[2]);
        logger.debug("SwapFree：" + memInfo[3]);
        logger.debug("CPU：" + SysOperationUtil.getCpuInfo());
        hostMonitorDTO.setTotalMem((long) memInfo[0]);
        DecimalFormat df = new DecimalFormat("#0.00");
        hostMonitorDTO.setCpuRate(df.format(SysOperationUtil.getCpuInfo()*100)+"%");

        return hostMonitorDTO;
    }
}

