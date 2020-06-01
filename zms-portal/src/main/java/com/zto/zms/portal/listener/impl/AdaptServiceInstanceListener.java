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

package com.zto.zms.portal.listener.impl;

import com.zto.zms.dal.mapper.ZmsServiceMapper;
import com.zto.zms.dal.model.ServiceInstance;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.listener.ServiceInstanceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/20
 **/
@Component
public class AdaptServiceInstanceListener implements ServiceInstanceListener {
    @Autowired
    private Map<String, ServiceInstanceListener> serviceInstanceListenerMap;
    @Autowired
    private ZmsServiceMapper serviceMapper;
    @Autowired
    private ExecutorService executor;

    public static final Logger logger = LoggerFactory.getLogger(AdaptServiceInstanceListener.class);

    @Override
    public void start(Integer serviceId, Integer processId) {
        executor.execute(() -> {
            ZmsServiceEntity serviceEntity = serviceMapper.getById(serviceId);
            String key = "ServiceEventListener-" + serviceEntity.getServerType().toLowerCase();
            if (serviceInstanceListenerMap.containsKey(key)) {
                serviceInstanceListenerMap.get(key).start(serviceId, processId);
            }
        });
    }

    @Override
    public void remove(ServiceInstance serviceInstance) {
        ZmsServiceEntity serviceEntity = serviceMapper.getById(serviceInstance.getServiceId());
        String key = "ServiceEventListener-" + serviceEntity.getServerType().toLowerCase();
        if (serviceInstanceListenerMap.containsKey(key)) {
            serviceInstanceListenerMap.get(key).remove(serviceInstance);
        }
    }
}

