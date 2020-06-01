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

package com.zto.zms.collector.model;

public class TopicInfo {
    private double tps;
    private double cntInToday;

    public TopicInfo(double tps, double cntInToday) {
        this.tps = tps;
        this.cntInToday = cntInToday;
    }

    public void add(double tps, double cntInToday) {
        this.tps = this.tps + tps;
        this.cntInToday = this.cntInToday + cntInToday;

    }

    public double getTps() {
        return tps;
    }

    public void setTps(double tps) {
        this.tps = tps;
    }

    public double getCntInToday() {
        return cntInToday;
    }

    public void setCntInToday(double cntInToday) {
        this.cntInToday = cntInToday;
    }
}

