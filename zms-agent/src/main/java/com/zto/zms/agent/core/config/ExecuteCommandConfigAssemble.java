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

import com.google.common.base.Joiner;
import com.zto.zms.writer.IniFileWriter;

import java.text.MessageFormat;
import java.util.List;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/4
 **/
public class ExecuteCommandConfigAssemble implements CommandConfigAssemble {
	private String command;
	private String directory;
	private String user;

	private List<String> args;

	public static ExecuteCommandConfigAssemble newInstance() {
		return new ExecuteCommandConfigAssemble();
	}

	@Override
	public List<IniFileWriter.IniFileEntity> assemble(List<IniFileWriter.IniFileEntity> options) {
		String execCommand = args == null ? command : MessageFormat.format("{0} {1}", this.command, Joiner.on(" ").join(args));
		options.add(new IniFileWriter.IniFileEntity("command", execCommand));
		options.add(new IniFileWriter.IniFileEntity("directory", directory));
		options.add(new IniFileWriter.IniFileEntity("user", user));
		return options;
	}

	public String getCommand() {
		return command;
	}

	public ExecuteCommandConfigAssemble command(String command, List<String> args) {
		this.command = command;
		this.args = args;
		return this;
	}

	public String getDirectory() {
		return directory;
	}

	public ExecuteCommandConfigAssemble directory(String directory) {
		this.directory = directory;
		return this;
	}

	public String getUser() {
		return user;
	}

	public ExecuteCommandConfigAssemble user(String user) {
		this.user = user;
		return this;
	}
}

