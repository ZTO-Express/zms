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

package com.zto.zms.portal.selector;

import com.zto.zms.utils.Assert;
import com.zto.zms.service.manager.ZmsContextManager;
import com.zto.zms.service.manager.ZookeeperDatasourceManager;
import com.zto.zms.service.manager.ZookeeperDatasourceManagerAdapt;
import com.zto.zms.service.selector.ZookeeperSelector;
import com.zto.zms.zookeeper.ZmsZkClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/1/20
 **/
@Component
public class PortalZookeeperSelector implements ZookeeperSelector {
    @Autowired
    private ZookeeperDatasourceManagerAdapt zookeeperDatasourceManagerAdapt;

    @Override
    public ZmsZkClient currentZkClient() {
        Integer envId = ZmsContextManager.getEnv();
        Assert.that(null != envId && 0 != envId, "The current environment is not specified");
        ZookeeperDatasourceManager datasourceManager = zookeeperDatasourceManagerAdapt.getDatasource(envId);
        return null == datasourceManager ? null : datasourceManager.getZkClient();
    }

}

