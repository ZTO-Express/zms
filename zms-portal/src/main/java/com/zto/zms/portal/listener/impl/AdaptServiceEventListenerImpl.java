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

import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.listener.ServiceEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p>Class: AdaptServiceEventListenerImpl</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/3/30
 **/
@Component
public class AdaptServiceEventListenerImpl implements ServiceEventListener {
    @Autowired
    private Map<String, ServiceEventListener> serviceInstanceListenerMap;

    @Override
    public void remove(ZmsServiceEntity zmsServiceEntity) {
        String key = "ServiceEventListener-" + zmsServiceEntity.getServerType().toLowerCase();
        if (serviceInstanceListenerMap.containsKey(key)) {
            serviceInstanceListenerMap.get(key).remove(zmsServiceEntity);
        }
    }
}

