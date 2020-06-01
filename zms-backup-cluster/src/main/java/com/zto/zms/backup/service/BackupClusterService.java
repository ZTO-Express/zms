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

package com.zto.zms.backup.service;

import com.google.common.collect.Lists;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.metadata.ConsumerGroupMetadata;
import com.zto.zms.metadata.TopicMetadata;
import com.zto.zms.backup.listener.ConsumerGroupMetadataListener;
import com.zto.zms.backup.listener.MetadataAddListener;
import com.zto.zms.backup.listener.TopicMetadataListener;
import com.zto.zms.backup.util.ConfigUtil;
import com.zto.zms.service.manager.ZookeeperDatasourceManager;
import com.zto.zms.service.manager.ZookeeperDatasourceManagerAdapt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 *
 * @author lidawei
 * @date 2020/5/11
 * @since 1.0.0
 **/
@Component
public class BackupClusterService implements CommandLineRunner {

	private static Logger logger = LoggerFactory.getLogger(BackupClusterService.class);

	@Autowired
	private ZookeeperDatasourceManagerAdapt zookeeperDatasourceManagerAdapt;

	@Override
	public void run(String... args) throws Exception {
		init();
		backupClusterStart();
		//Keep the thread
		new CountDownLatch(1).await();
	}


	public void init() {
		String originZmsServer = ConfigUtil.getConfig(ConfigUtil.ORIGIN_ZMZ_ZK);
		zookeeperDatasourceManagerAdapt.reload(ConfigUtil.getOriginEnvId(), originZmsServer);

		String currentZmsServer = ConfigUtil.getConfig(ConfigUtil.CURRENT_ZMS_ZK);
		zookeeperDatasourceManagerAdapt.reload(ConfigUtil.getCurrentEnvId(), currentZmsServer);
	}

	/**
	 * 启动集群备份
	 */
	public void backupClusterStart() {
		ZookeeperDatasourceManager originDatasource = zookeeperDatasourceManagerAdapt.getDatasource(ConfigUtil.getOriginEnvId());
		ZookeeperDatasourceManager currentDatasource = zookeeperDatasourceManagerAdapt.getDatasource(ConfigUtil.getCurrentEnvId());

		Map<String, String> backupClusterMapper = ConfigUtil.getBackupClusterMap();
		//主题元数据监听
		topicMetadataListener(originDatasource, currentDatasource, backupClusterMapper);
		//消费组元数据监听
		consumerGroupMetadataListener(originDatasource, currentDatasource, backupClusterMapper);
	}

	/**
	 * 监听备份环境的主题元数据变更
	 *
	 * @param originDatasource
	 * @param currentDatasource
	 * @param backupClusterMapper
	 */
	public void topicMetadataListener(ZookeeperDatasourceManager originDatasource,
									  ZookeeperDatasourceManager currentDatasource, Map<String, String> backupClusterMapper) {
		//原集群主题关联关系
		List<TopicMetadata> originTopicMetadataList = listTopicMetadata(originDatasource);
		Map<String, List<TopicMetadata>> originClusterTopicMap = getClusterTopicMap(originTopicMetadataList);
		//现集群主题关联关系
		List<TopicMetadata> currentTopicMetadataList = listTopicMetadata(currentDatasource);
		Map<String, TopicMetadata> currentNameTopicMetadataMap =
				currentTopicMetadataList.stream().collect(Collectors.toMap(TopicMetadata::getName, item -> item));
		//主题元数据节点变更监听
		initTopicMetadataListener(originDatasource, backupClusterMapper);

		//初始化
		for (Map.Entry entry : backupClusterMapper.entrySet()) {
			String originCluster = entry.getKey().toString();
			String currentCluster = entry.getValue().toString();
			if (!originClusterTopicMap.containsKey(originCluster)) {
				logger.error("Zookeeper does not exist on this cluster node:{}", originCluster);
				continue;
			}
			List<TopicMetadata> originClusterTopicMetadataList = originClusterTopicMap.get(originCluster);
			//对比主题
			for (TopicMetadata topicMetadata : originClusterTopicMetadataList) {
				//新增
				if (!currentNameTopicMetadataMap.containsKey(topicMetadata.getName())) {
					TopicMetadataListener topicMetadataListener =
							new TopicMetadataListener(currentCluster, topicMetadata.getName());
					//新增主题
					topicMetadataListener.backup();
				}
			}
		}
	}

	/**
	 * 对topic父节点进行监听，如果有新增topic，同步topic信息
	 *
	 * @param originDatasource
	 * @param backupClusterMapper
	 */
	public void initTopicMetadataListener(ZookeeperDatasourceManager originDatasource, Map<String, String> backupClusterMapper) {
		//新增主题监听
		MetadataAddListener metadataAddListener = new MetadataAddListener(originDatasource.getZkClient());
		metadataAddListener.nodeChildChangeListener(ZmsConst.ZK.TOPIC_ZKPATH, (String parentPath, List<String> currentChilds) -> {
			List<String> currentNodes = metadataAddListener.getCurrentNodes();
			for (String name : currentChilds) {
				//新增节点
				if (!currentNodes.contains(name)) {
					try {
						TopicMetadata topicMetadata = originDatasource.getZkClient().readTopicMetadata(name);
						String originCluster = topicMetadata.getClusterMetadata().getClusterName();
						//如果不是备份集群，跳过
						if (!backupClusterMapper.containsKey(originCluster)) {
							continue;
						}
						String currentCluster = backupClusterMapper.get(originCluster);
						//新增资源
						TopicMetadataListener topicMetadataListener =
								new TopicMetadataListener(currentCluster, name);
						topicMetadataListener.backup();
					} catch (Exception e) {
						logger.error("Backup topic metadata error", e);
					}
				}
			}
			metadataAddListener.setCurrentNodes(currentChilds);
		});
	}


	/**
	 * 监听备份环境的消费组元数据变更
	 *
	 * @param originDatasource
	 * @param currentDatasource
	 * @param backupClusterMapper
	 */
	private void consumerGroupMetadataListener(ZookeeperDatasourceManager originDatasource,
											   ZookeeperDatasourceManager currentDatasource, Map<String, String> backupClusterMapper) {
		//原集群消费组关联关系
		List<ConsumerGroupMetadata> originTopicMetadataList = listConsumerGroupMetadata(originDatasource);
		Map<String, List<ConsumerGroupMetadata>> originClusterConsumerMap = originTopicMetadataList.stream()
				.collect(Collectors.groupingBy(item -> item.getClusterMetadata().getClusterName()));
		Map<String, ConsumerGroupMetadata> originNameConsumerMetadataMap =
				originTopicMetadataList.stream().collect(Collectors.toMap(ConsumerGroupMetadata::getName, item -> item));
		//现集群消费组关联关系
		List<ConsumerGroupMetadata> currentTopicMetadataList = listConsumerGroupMetadata(currentDatasource);
		Map<String, ConsumerGroupMetadata> currentNameConsumerMetadataMap =
				currentTopicMetadataList.stream().collect(Collectors.toMap(ConsumerGroupMetadata::getName, item -> item));
		//消费组元数据节点变更监听
		initConsumerGroupMetadata(originDatasource, backupClusterMapper);

		//初始化
		for (Map.Entry entry : backupClusterMapper.entrySet()) {
			String originCluster = entry.getKey().toString();
			String currentCluster = entry.getValue().toString();
			if (!originClusterConsumerMap.containsKey(originCluster)) {
				logger.error("Zookeeper does not exist on this cluster node:{}", originCluster);
				continue;
			}
			List<ConsumerGroupMetadata> originClusterConsumerMetadataList = originClusterConsumerMap.get(originCluster);
			//对比主题
			for (ConsumerGroupMetadata consumerGroupMetadata : originClusterConsumerMetadataList) {
				//新增
				if (!currentNameConsumerMetadataMap.containsKey(consumerGroupMetadata.getName())) {
					ConsumerGroupMetadataListener consumerGroupMetadataListener =
							new ConsumerGroupMetadataListener(currentCluster, consumerGroupMetadata.getBindingTopic(), consumerGroupMetadata.getName());
					//新增主题
					consumerGroupMetadataListener.backup();
				}
			}
		}
	}

	/**
	 * 对consumerGroup父节点进行监听，如果有新增consumerGroup，同步consumerGroup信息
	 *
	 * @param originDatasource
	 * @param backupClusterMapper
	 */
	private void initConsumerGroupMetadata(ZookeeperDatasourceManager originDatasource, Map<String, String> backupClusterMapper) {
		//新增主题监听
		MetadataAddListener metadataAddListener = new MetadataAddListener(originDatasource.getZkClient());
		metadataAddListener.nodeChildChangeListener(ZmsConst.ZK.CONSUMERGROUP_ZKPATH, (String parentPath, List<String> currentChildes) -> {
			List<String> currentNodes = metadataAddListener.getCurrentNodes();
			for (String node : currentChildes) {
				//新增节点
				if (!currentNodes.contains(node)) {
					try {
						ConsumerGroupMetadata consumerGroupMetadata = originDatasource.getZkClient().readConsumerGroupMetadata(node);
						String originCluster = consumerGroupMetadata.getClusterMetadata().getClusterName();
						//如果不是备份集群，跳过
						if (!backupClusterMapper.containsKey(originCluster)) {
							continue;
						}
						String currentCluster = backupClusterMapper.get(originCluster);
						//新增资源
						ConsumerGroupMetadataListener consumerGroupMetadataListener =
								new ConsumerGroupMetadataListener(currentCluster, consumerGroupMetadata.getBindingTopic(), consumerGroupMetadata.getName());
						consumerGroupMetadataListener.backup();
					} catch (Exception e) {
						logger.error("Backup consumer metadata error", e);
					}
				}
			}
			metadataAddListener.setCurrentNodes(currentChildes);
		});
	}


	public List<TopicMetadata> listTopicMetadata(ZookeeperDatasourceManager datasource) {
		boolean topicMetadataExist = datasource.getZkClient().exists(ZmsConst.ZK.TOPIC_ZKPATH);
		if (!topicMetadataExist) {
			return Lists.newArrayList();
		}
		List<String> currentMetadataNameList = datasource.getZkClient().getChildren(ZmsConst.ZK.TOPIC_ZKPATH);
		List<TopicMetadata> metadataList = new ArrayList<>(currentMetadataNameList.size());
		for (String name : currentMetadataNameList) {
			try {
				//topic元数据
				TopicMetadata topicMetadata = datasource.getZkClient().readTopicMetadata(name);
				if (null != topicMetadata) {
					metadataList.add(topicMetadata);
				}
			} catch (Exception e) {
				logger.warn(MessageFormat.format("同步主题异常:{0}", name), e);
			}
		}
		return metadataList;
	}

	public List<ConsumerGroupMetadata> listConsumerGroupMetadata(ZookeeperDatasourceManager datasource) {
		boolean consumerMetadataExist = datasource.getZkClient().exists(ZmsConst.ZK.CONSUMERGROUP_ZKPATH);
		if (!consumerMetadataExist) {
			return Lists.newArrayList();
		}
		List<String> currentMetadataNameList = datasource.getZkClient().getChildren(ZmsConst.ZK.CONSUMERGROUP_ZKPATH);
		List<ConsumerGroupMetadata> metadataList = new ArrayList<>(currentMetadataNameList.size());
		for (String name : currentMetadataNameList) {
			try {
				//元数据
				ConsumerGroupMetadata metadata = datasource.getZkClient().readConsumerGroupMetadata(name);
				if (null != metadata) {
					metadataList.add(metadata);
				}
			} catch (Exception e) {
				logger.warn(MessageFormat.format("同步消费组异常:{0}", name), e);
			}
		}
		return metadataList;
	}


	public Map<String, List<TopicMetadata>> getClusterTopicMap(List<TopicMetadata> topicMetadataList) {
		return topicMetadataList
				.stream()
				.collect(Collectors.groupingBy(item -> item.getClusterMetadata().getClusterName()));
	}

}

