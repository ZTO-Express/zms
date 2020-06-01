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

import com.zto.zms.zookeeper.ZmsZkClient;
import kafka.utils.ZKStringSerializer$;

/**
 * <p>Class: ZookeeperDatasourceManager</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/4/3
 **/
public class ZookeeperDatasourceManager implements DatasourceManager {
    private ZmsZkClient zkClient;
    private String zmsServer;

    public ZookeeperDatasourceManager(String zmsServer) {
        this.zmsServer = zmsServer;
        init();
    }

    private void init() {
        create();
    }

    @Override
    public DatasourceManager create() {
        zkClient = new ZmsZkClient(zmsServer, 20 * 1000, 10 * 1000, ZKStringSerializer$.MODULE$);
        return this;
    }

    public ZmsZkClient getZkClient() {
        return zkClient;
    }

    @Override
    public void destroy() {
        if (null != zkClient) {
            zkClient.close();
        }
    }
}

