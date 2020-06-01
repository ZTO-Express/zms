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

package com.zto.zms.backup.listener;

import com.zto.zms.zookeeper.ZmsZkClient;
import org.I0Itec.zkclient.IZkChildListener;

import java.util.List;

/**
 * <p>Description: 元数据新增节点监听</p>
 *
 * @author lidawei
 * @date 2020/5/11
 **/
public class MetadataAddListener {
	private List<String> currentNodes;
	private ZmsZkClient zmsZkClient;

	public MetadataAddListener(ZmsZkClient zmsZkClient) {
		this.zmsZkClient = zmsZkClient;
	}

	public void nodeChildChangeListener(String nodeRoot, IZkChildListener listener) {
		this.currentNodes = zmsZkClient.subscribeChildChanges(nodeRoot, listener);
	}

	public List<String> getCurrentNodes() {
		return currentNodes;
	}

	public void setCurrentNodes(List<String> currentNodes) {
		this.currentNodes = currentNodes;
	}
}

