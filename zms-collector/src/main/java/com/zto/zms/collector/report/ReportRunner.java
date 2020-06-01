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

/**
 * <p>Title: Reporter.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: www.zto.com</p>
 */
package com.zto.zms.collector.report;

import com.google.common.collect.Maps;
import com.zto.zms.common.ZmsConst;
import com.zto.zms.metadata.ClusterMetadata;
import com.zto.zms.utils.ExecutorServiceUtils;
import com.zto.zms.service.config.ZmsConf;
import com.zto.zms.service.router.ZkClientRouter;
import org.I0Itec.zkclient.IZkDataListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ReportRunner implements CommandLineRunner, DisposableBean {

    private Map<String, ScheduledExecutorService> serviceMap = Maps.newHashMap();
    private AtomicInteger threadNum = new AtomicInteger();
    private Map<String, ClusterMetadata> currentClusters = Maps.newHashMap();
    @Autowired
    private Map<String, IReporter> reporterMap;
    @Autowired
    private ZkClientRouter zkClientRouter;

    @Autowired
    private ZmsConf zmsConf;

    @Autowired
    private ZmsClientReporter zmsClientReporter;

    @Autowired
    private ExtensionReporter extensionReporter;

    @Override
    public void destroy() {
        remove(serviceMap.keySet().toArray(new String[serviceMap.size()]));
    }

    @Override
    public void run(String... args) {
        zmsClientReporter.refreshZmsClientProducerInfo();
        zmsClientReporter.refreshZmsClientConsumerInfo();

        refresh();
        zkClientRouter.currentZkClient()
                .subscribeChildChanges(ZmsConst.ZK.CLUSTER_ZKPATH, (path, children) -> refresh());
        extensionReporter.start();
    }

    private void refresh() {
        Map<String, ClusterMetadata> newClusters = zkClientRouter.readClusters();
        newClusters.keySet().stream()
                .filter(clusterName ->
                        !currentClusters.containsKey(clusterName)
                                && !zmsConf.isIgnoreCluster(clusterName))
                .forEach(clusterName -> {
                    refreshCluster(newClusters.get(clusterName));
                    zkClientRouter.currentZkClient().subscribeDataChanges(String.join("/", ZmsConst.ZK.CLUSTER_ZKPATH, clusterName), zkDataListener);
                });
        this.currentClusters.keySet().stream()
                .filter(clusterName -> !newClusters.containsKey(clusterName))
                .forEach(clusterName -> {
                    removeCluster(currentClusters.get(clusterName));
                    zkClientRouter.currentZkClient().unsubscribeDataChanges(String.join("/", ZmsConst.ZK.CLUSTER_ZKPATH, clusterName), zkDataListener);
                });
        this.currentClusters = newClusters;
    }

    public void refreshCluster(ClusterMetadata cluster) {
        this.initialMetadata(cluster);
        this.refreshMetaOfCluster(cluster);
        this.collectMqMetrics(cluster);
        this.collectRtTime(cluster);
        this.recordMaxOffset(cluster);
        this.recordMaxOffsetInflux(cluster);
    }

    public void removeCluster(ClusterMetadata cluster) {
        String clusterName = cluster.getClusterName();
        String clusterType = cluster.getBrokerType().getName();
        this.remove("MetaService-" + clusterName + "-" + clusterType);
        this.remove("MetricsCollector-" + clusterName + "-" + clusterType);
        this.remove("RT-" + clusterName + "-" + clusterType);
        this.remove("ClusterOffsetInfluxService-" + clusterName + "-" + clusterType);
        this.remove("ClusterOffsetService-" + clusterName + "-" + clusterType);
    }

    IZkDataListener zkDataListener = new IZkDataListener() {
        @Override
        public void handleDataChange(String zkPath, Object zkValue) {
            String clusterPath = zkPath.replace(String.join("/", ZmsConst.ZK.CLUSTER_ZKPATH).concat("/"), "");
            ClusterMetadata cluster = zkClientRouter.readClusterMetadata(clusterPath);
            String clusterName = cluster.getClusterName();
            if (currentClusters.containsKey(clusterName)) {
                removeCluster(currentClusters.get(clusterName));
            }
            refreshCluster(cluster);
        }

        @Override
        public void handleDataDeleted(String s){
        }
    };

    public void submit(final String serviceName, Runnable runnable, long initialDelay,
                       long period, TimeUnit unit) {
        serviceMap.put(serviceName, Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, serviceName + threadNum.addAndGet(1))));
        serviceMap.get(serviceName).scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }

    public void remove(String... serviceNames) {
        Arrays.stream(serviceNames)
                .filter(serviceName -> serviceMap.containsKey(serviceName))
                .forEach(
                        serviceName -> ExecutorServiceUtils.gracefullyShutdown(serviceMap.remove(serviceName))
                );
    }


    public void initialMetadata(ClusterMetadata cluster) {
        this.reporterMap.get(cluster.getBrokerType().getName()).refreshMetaOfCluster(cluster);
    }

    public void refreshMetaOfCluster(ClusterMetadata cluster) {
        submit("MetaService-" + cluster.getClusterName() + "-" + cluster.getBrokerType().getName(),
                () -> this.reporterMap.get(cluster.getBrokerType().getName()).refreshMetaOfCluster(cluster),
                60,
                5 * 60,
                TimeUnit.SECONDS);
    }

    public void collectMqMetrics(ClusterMetadata cluster) {
        submit("MetricsCollector-" + cluster.getClusterName() + "-" + cluster.getBrokerType().getName(),
                () -> this.reporterMap.get(cluster.getBrokerType().getName()).collectMqMetrics(cluster),
                60,
                10,
                TimeUnit.SECONDS);

    }

    public void collectRtTime(ClusterMetadata cluster) {
        submit("RT-" + cluster.getClusterName() + "-" + cluster.getBrokerType().getName(),
                () -> this.reporterMap.get(cluster.getBrokerType().getName()).collectRtTime(cluster),
                60,
                1,
                TimeUnit.SECONDS);
    }

    public void recordMaxOffset(ClusterMetadata cluster) {
        LocalDateTime currentTime = LocalDateTime.now().withHour(5).withMinute(0).withSecond(0);
        long oneDay = 24 * 60 * 60 * 1000;
        long initDelay = currentTime.toInstant(ZoneOffset.of("+8")).toEpochMilli() - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
        submit("ClusterOffsetService-" + cluster.getClusterName() + "-" + cluster.getBrokerType().getName(),
                () -> this.reporterMap.get(cluster.getBrokerType().getName()).recordMaxOffset(cluster),
                initDelay,
                oneDay,
                TimeUnit.MILLISECONDS);
    }

    public void recordMaxOffsetInflux(ClusterMetadata cluster) {
        submit("ClusterOffsetInfluxService-" + cluster.getClusterName() + "-" + cluster.getBrokerType().getName(),
                () -> this.reporterMap.get(cluster.getBrokerType().getName()).recordMaxOffsetInflux(cluster),
                60,
                60,
                TimeUnit.SECONDS);
    }
}

