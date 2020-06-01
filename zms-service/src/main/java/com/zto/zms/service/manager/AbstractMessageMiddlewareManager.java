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

package com.zto.zms.service.manager;

import com.zto.zms.common.ZmsConst;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.service.process.MiddlewareProcess;
import com.zto.zms.zookeeper.ZmsZkClient;
import org.I0Itec.zkclient.IZkDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Class: MessageMiddlewareManager</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/4/2
 **/
public abstract class AbstractMessageMiddlewareManager implements MiddlewareProcess {

    private Logger logger = LoggerFactory.getLogger(AbstractMessageMiddlewareManager.class);

    protected final ClusterMetadata clusterMetadata;
    protected final String clusterName;
    private final ZmsZkClient zmsZkClient;
    protected RollBack rollBack;

    AbstractMessageMiddlewareManager(ZmsZkClient zmsZkClient, ClusterMetadata clusterMetadata, RollBack rollBack) {
        this.zmsZkClient = zmsZkClient;
        this.clusterMetadata = clusterMetadata;
        this.clusterName = clusterMetadata.getClusterName();
        this.rollBack = rollBack;
        init();
    }

    private void init() {
        create();
        //监听节点修改
        nodeChangeListener();
    }


    /**
     * 创建资源
     */
    abstract void create();

    /**
     * 销毁释放资源
     */
    abstract void destroy();


    /**
     * 对集群进行监听
     */
    public void nodeChangeListener() {
        zmsZkClient.subscribeDataChanges(String.join("/", ZmsConst.ZK.CLUSTER_ZKPATH, getClusterMetadata().getClusterName()),
                new IZkDataListener() {
                    @Override
                    public void handleDataChange(String zkPath, Object zkValue) {
                        logger.info("Zookeeper node handleDataChange:{}", zkValue);
                        destroy();
                    }

                    @Override
                    public void handleDataDeleted(String s) {
                        logger.info("Zookeeper node handleDataDeleted:{}", s);
                        destroy();
                    }
                });
    }

    public ClusterMetadata getClusterMetadata() {
        return clusterMetadata;
    }

    interface RollBack {
        /**
         * 移除消息管理类
         */
        void destroy();
    }
}

