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

package com.zto.zms.portal.common;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/4/1
 **/
public enum ServiceStartUserEnum {

    ZOOKEEPER("baseuser", "baseuser"),
    INFLUXDB("baseuser", "baseuser"),
    KAFKA("baseuser", "baseuser"),
    ROCKERMQ("baseuser", "baseuser"),
    ZMS_ALERT("baseuser", "baseuserbaseuser"),
    ZMS_COLLECTOR("baseuser", "baseuser"),
    ZMS_BACKUP_CLUSTER("baseuser", "baseuser");

    private String user;
    private String group;

    ServiceStartUserEnum(String user, String group) {
        this.user = user;
        this.group = group;
    }

    public String getUser() {
        return user;
    }

    public String getGroup() {
        return group;
    }
}

