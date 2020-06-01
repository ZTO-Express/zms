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

package com.zto.zms.common;

import java.util.List;
import java.util.Map;

/**
 * <p>Class: RunCommonConfig</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/11
 **/
public class RunCommonConfig {
    private String programName;
    private Map<String, String> environment;
    private List<String> args;
    private String command;
    private String libDir;
    private String autoRestart;
    private String exitCodes;
    private Integer startSecs;
    private Integer stopWaitSecs;
    private Boolean killAsGroup;
    private String stdoutLogfileMaxBytes;
    private String stdoutLogfileBackups;
    private String stderrLogfileMaxBytes;
    private String stderrLogfileBackups;
    private String stdoutLogfile;
    private String stderrLogfile;
    private String user;
    private String group;
    private Boolean stopAsGroup;
    private Integer startRetries;
    private String stopSignal;
    private Boolean redirectStderr;
    private Integer stdoutCaptureMaxBytes;
    private Boolean stdoutEventsEnabled;
    private Boolean stdoutSyslog;
    private Integer stderrCaptureMaxBytes;
    private Boolean stderrEventsEnabled;
    private Boolean stderrSyslog;

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getLibDir() {
        return libDir;
    }

    public void setLibDir(String libDir) {
        this.libDir = libDir;
    }

    public String getAutoRestart() {
        return autoRestart;
    }

    public void setAutoRestart(String autoRestart) {
        this.autoRestart = autoRestart;
    }

    public String getExitCodes() {
        return exitCodes;
    }

    public void setExitCodes(String exitCodes) {
        this.exitCodes = exitCodes;
    }

    public Integer getStartSecs() {
        return startSecs;
    }

    public void setStartSecs(Integer startSecs) {
        this.startSecs = startSecs;
    }

    public Integer getStopWaitSecs() {
        return stopWaitSecs;
    }

    public void setStopWaitSecs(Integer stopWaitSecs) {
        this.stopWaitSecs = stopWaitSecs;
    }

    public Boolean getKillAsGroup() {
        return killAsGroup;
    }

    public void setKillAsGroup(Boolean killAsGroup) {
        this.killAsGroup = killAsGroup;
    }

    public Boolean getStopAsGroup() {
        return stopAsGroup;
    }

    public void setStopAsGroup(Boolean stopAsGroup) {
        this.stopAsGroup = stopAsGroup;
    }

    public Integer getStartRetries() {
        return startRetries;
    }

    public void setStartRetries(Integer startRetries) {
        this.startRetries = startRetries;
    }

    public String getStopSignal() {
        return stopSignal;
    }

    public void setStopSignal(String stopSignal) {
        this.stopSignal = stopSignal;
    }

    public Boolean getRedirectStderr() {
        return redirectStderr;
    }

    public void setRedirectStderr(Boolean redirectStderr) {
        this.redirectStderr = redirectStderr;
    }

    public Integer getStdoutCaptureMaxBytes() {
        return stdoutCaptureMaxBytes;
    }

    public void setStdoutCaptureMaxBytes(Integer stdoutCaptureMaxBytes) {
        this.stdoutCaptureMaxBytes = stdoutCaptureMaxBytes;
    }

    public Boolean getStdoutEventsEnabled() {
        return stdoutEventsEnabled;
    }

    public void setStdoutEventsEnabled(Boolean stdoutEventsEnabled) {
        this.stdoutEventsEnabled = stdoutEventsEnabled;
    }

    public Boolean getStdoutSyslog() {
        return stdoutSyslog;
    }

    public void setStdoutSyslog(Boolean stdoutSyslog) {
        this.stdoutSyslog = stdoutSyslog;
    }

    public Integer getStderrCaptureMaxBytes() {
        return stderrCaptureMaxBytes;
    }

    public void setStderrCaptureMaxBytes(Integer stderrCaptureMaxBytes) {
        this.stderrCaptureMaxBytes = stderrCaptureMaxBytes;
    }

    public Boolean getStderrEventsEnabled() {
        return stderrEventsEnabled;
    }

    public void setStderrEventsEnabled(Boolean stderrEventsEnabled) {
        this.stderrEventsEnabled = stderrEventsEnabled;
    }

    public Boolean getStderrSyslog() {
        return stderrSyslog;
    }

    public void setStderrSyslog(Boolean stderrSyslog) {
        this.stderrSyslog = stderrSyslog;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStdoutLogfileMaxBytes() {
        return stdoutLogfileMaxBytes;
    }

    public void setStdoutLogfileMaxBytes(String stdoutLogfileMaxBytes) {
        this.stdoutLogfileMaxBytes = stdoutLogfileMaxBytes;
    }

    public String getStdoutLogfileBackups() {
        return stdoutLogfileBackups;
    }

    public void setStdoutLogfileBackups(String stdoutLogfileBackups) {
        this.stdoutLogfileBackups = stdoutLogfileBackups;
    }

    public String getStderrLogfileMaxBytes() {
        return stderrLogfileMaxBytes;
    }

    public void setStderrLogfileMaxBytes(String stderrLogfileMaxBytes) {
        this.stderrLogfileMaxBytes = stderrLogfileMaxBytes;
    }

    public String getStderrLogfileBackups() {
        return stderrLogfileBackups;
    }

    public void setStderrLogfileBackups(String stderrLogfileBackups) {
        this.stderrLogfileBackups = stderrLogfileBackups;
    }

    public String getStdoutLogfile() {
        return stdoutLogfile;
    }

    public void setStdoutLogfile(String stdoutLogfile) {
        this.stdoutLogfile = stdoutLogfile;
    }

    public String getStderrLogfile() {
        return stderrLogfile;
    }

    public void setStderrLogfile(String stderrLogfile) {
        this.stderrLogfile = stderrLogfile;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}

