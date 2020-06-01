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

package com.zto.zms.stats;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by superheizai on 2017/9/5.
 */
public class ProducerStats extends StatsInfo {
    List<MeterInfo> meters = new ArrayList<>();

    List<TimerInfo> timers = new ArrayList<>();

    List<DistributionInfo> distributions = new ArrayList<>();

    public List<MeterInfo> getMeters() {
        return meters;
    }

    public void setMeters(List<MeterInfo> meters) {
        this.meters = meters;
    }

    public List<TimerInfo> getTimers() {
        return timers;
    }

    public void setTimers(List<TimerInfo> timers) {
        this.timers = timers;
    }

    public List<DistributionInfo> getDistributions() {
        return distributions;
    }

    public void setDistributions(List<DistributionInfo> distributions) {
        this.distributions = distributions;
    }
}

