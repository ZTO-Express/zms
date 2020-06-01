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

package com.zto.zms.portal.dto.consumer;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by liangyong on 2019/3/1.
 */
public class ConsumerStatusAllDTO {

    private Long diffTotal;
    private double consumeTps;

    private List<ConsumerStatusDTO> consumerStatusList;
    private List<ConsumerProgressDTO> consumerProgressList;
    private List<ConsumerZmsRegisterDTO> consumerZmsRegisterList;

    public Long getDiffTotal() {
        return diffTotal;
    }

    public void setDiffTotal(Long diffTotal) {
        this.diffTotal = diffTotal;
    }

    public double getConsumeTps() {
        return new BigDecimal(consumeTps).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public void setConsumeTps(double consumeTps) {
        this.consumeTps = consumeTps;
    }

    public List<ConsumerStatusDTO> getConsumerStatusList() {
        return consumerStatusList;
    }

    public void setConsumerStatusList(List<ConsumerStatusDTO> consumerStatusList) {
        this.consumerStatusList = consumerStatusList;
    }

    public List<ConsumerProgressDTO> getConsumerProgressList() {
        return consumerProgressList;
    }

    public void setConsumerProgressList(List<ConsumerProgressDTO> consumerProgressList) {
        this.consumerProgressList = consumerProgressList;
    }

    public List<ConsumerZmsRegisterDTO> getConsumerZmsRegisterList() {
        return consumerZmsRegisterList;
    }

    public void setConsumerZmsRegisterList(List<ConsumerZmsRegisterDTO> consumerZmsRegisterList) {
        this.consumerZmsRegisterList = consumerZmsRegisterList;
    }
}

