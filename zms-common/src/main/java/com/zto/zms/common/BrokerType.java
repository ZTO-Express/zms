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

package com.zto.zms.common;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 集群类型
 * Created by superheizai on 2017/7/26.
 */
public enum BrokerType {

    KAFKA("kafka"), ROCKETMQ("rocketmq");

    private String name;

    BrokerType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<String> getBrokerTypes() {
        List<String> list = new ArrayList<>();
        for (BrokerType clusterType : BrokerType.values()) {
            list.add(clusterType.getName());
        }
        return list;
    }

    public static BrokerType parse(String property) {
        if (StringUtils.isEmpty(property)) {
            return null;
        }

        return BrokerType.valueOf(property.toUpperCase());

    }
}

