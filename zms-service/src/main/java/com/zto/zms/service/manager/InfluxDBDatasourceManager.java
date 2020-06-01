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

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import java.util.concurrent.TimeUnit;


/**
 * <p>Class: InfluxDBDatasourceManager</p>
 * <p> Description: </p>
 *
 * @author lidawei
 * @version 1.0
 * @date 2020/4/7
 */
public class InfluxDBDatasourceManager implements DatasourceManager {
    private String influxDBUrl;
    private InfluxDB influxDB;

    public InfluxDBDatasourceManager(String influxDBUrl) {
        this.influxDBUrl = influxDBUrl;
        init();
    }

    private void init() {
        create();
    }

    @Override
    public DatasourceManager create() {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(30);
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.dispatcher(dispatcher);

        ConnectionPool connectionPool = new ConnectionPool(30, 3, TimeUnit.MINUTES);
        client.connectionPool(connectionPool);

        influxDB = InfluxDBFactory.connect(influxDBUrl, client);
        influxDB.enableBatch(200, 1, TimeUnit.SECONDS);
        return this;
    }

    @Override
    public void destroy() {
        if (null != influxDB) {
            influxDB.close();
        }
    }

    public InfluxDB getInfluxDB() {
        return influxDB;
    }
}

