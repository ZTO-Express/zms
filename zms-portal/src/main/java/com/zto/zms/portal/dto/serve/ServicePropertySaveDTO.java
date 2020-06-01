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

import java.util.List;

/**
 * 服务配置修改、实例配置修改入参
 *
 * @author sun kai
 * @date 2020/02/18
 */
public class ServicePropertySaveDTO {

    private Integer serviceId;

    private List<ServicePropertyInstanceQueryDTO> propertyQueryList;

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public List<ServicePropertyInstanceQueryDTO> getPropertyQueryList() {
        return propertyQueryList;
    }

    public void setPropertyQueryList(List<ServicePropertyInstanceQueryDTO> propertyQueryList) {
        this.propertyQueryList = propertyQueryList;
    }
}

