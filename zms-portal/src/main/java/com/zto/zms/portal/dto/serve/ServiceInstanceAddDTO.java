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

package com.zto.zms.portal.dto.serve;

import com.zto.zms.dal.model.ServiceInstance;
import com.zto.zms.dal.model.ZmsServiceEntity;

import java.util.List;

/**
 * 添加服务、添加服务实例传入实体
 *
 * @author sun kai
 * @date 2020/03/02
 */
public class ServiceInstanceAddDTO {

    private ZmsServiceEntity service;

    private List<ServiceInstance> instanceList;

    private List<ServicePropertyQueryDTO> propertyList;

    public ZmsServiceEntity getService() {
        return service;
    }

    public void setService(ZmsServiceEntity service) {
        this.service = service;
    }

    public List<ServiceInstance> getInstanceList() {
        return instanceList;
    }

    public void setInstanceList(List<ServiceInstance> instanceList) {
        this.instanceList = instanceList;
    }

    public List<ServicePropertyQueryDTO> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<ServicePropertyQueryDTO> propertyList) {
        this.propertyList = propertyList;
    }
}

