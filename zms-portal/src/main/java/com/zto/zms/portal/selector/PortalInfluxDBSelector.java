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
import com.zto.zms.service.manager.InfluxDBDatasourceManager;
import com.zto.zms.service.manager.InfluxDBDatasourceManagerAdapt;
import com.zto.zms.service.manager.ZmsContextManager;
import com.zto.zms.service.selector.InfluxDBSelector;
import org.influxdb.InfluxDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p> Description: </p>
 *
 * @author lidawei
 * @version 1.0
 * @date 2020/1/20
 */
@Component
public class PortalInfluxDBSelector implements InfluxDBSelector {
    @Autowired
    private InfluxDBDatasourceManagerAdapt influxDBDatasourceManagerAdapt;

    @Override
    public InfluxDB currentInfluxDB() {
        Integer envId = ZmsContextManager.getEnv();
        Assert.that(null != envId && 0 != envId, "The current environment is not specified");
        InfluxDBDatasourceManager influxDbDatasourceManager = influxDBDatasourceManagerAdapt.getDatasource(envId);
        return null == influxDbDatasourceManager ? null : influxDbDatasourceManager.getInfluxDB();
    }
}

