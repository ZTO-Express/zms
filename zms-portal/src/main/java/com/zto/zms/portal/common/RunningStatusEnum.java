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

package com.zto.zms.portal.common;

import org.apache.commons.lang3.StringUtils;

/**
 * 进程状态
 *
 * @author yuhao.zhang
 * @description
 * @date 2020/3/19
 */
public enum RunningStatusEnum {

    STARTING("Supervisor 收到启动请求后，进程正在启动"),
    RUNNING("进程启动成功，处于运行状态"),
    STOPPING("Supervisor 收到停止请求后，进程正在停止"),
    STOPPED("进程停止成功，处于停止状态"),
    BACKOFF("进程进入 starting 状态后，由于马上就退出导致没能进入 running 状态"),
    FATAL("进程没有正常启动"),
    EXITED("进程从 running 状态退出"),
    UNKNOWN("进程处于未知状态(supervisord 程序错误)");

    private String statusDesc;

    RunningStatusEnum(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public static RunningStatusEnum getRunningStatusEnum(String value) {
        for (RunningStatusEnum statusEnum : RunningStatusEnum.values()) {
            if (StringUtils.isNotEmpty(value) && value.equalsIgnoreCase(statusEnum.name())) {
                return statusEnum;
            }
        }
        return null;
    }
}

