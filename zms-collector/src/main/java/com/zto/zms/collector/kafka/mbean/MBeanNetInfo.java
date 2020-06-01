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

package com.zto.zms.collector.kafka.mbean;

/**
 * Created by liangyong on 2018/8/14.
 */
public class MBeanNetInfo {

    private long count;
    private double min;
    private double max;
    private double mean;
    private double stdDev;
    private double _50thPercentile;
    private double _75thPercentile;
    private double _95thPercentile;
    private double _98thPercentile;
    private double _99thPercentile;
    private double _999thPercentile;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getStdDev() {
        return stdDev;
    }

    public void setStdDev(double stdDev) {
        this.stdDev = stdDev;
    }

    public double get_50thPercentile() {
        return _50thPercentile;
    }

    public void set_50thPercentile(double _50thPercentile) {
        this._50thPercentile = _50thPercentile;
    }

    public double get_75thPercentile() {
        return _75thPercentile;
    }

    public void set_75thPercentile(double _75thPercentile) {
        this._75thPercentile = _75thPercentile;
    }

    public double get_95thPercentile() {
        return _95thPercentile;
    }

    public void set_95thPercentile(double _95thPercentile) {
        this._95thPercentile = _95thPercentile;
    }

    public double get_98thPercentile() {
        return _98thPercentile;
    }

    public void set_98thPercentile(double _98thPercentile) {
        this._98thPercentile = _98thPercentile;
    }

    public double get_99thPercentile() {
        return _99thPercentile;
    }

    public void set_99thPercentile(double _99thPercentile) {
        this._99thPercentile = _99thPercentile;
    }

    public double get_999thPercentile() {
        return _999thPercentile;
    }

    public void set_999thPercentile(double _999thPercentile) {
        this._999thPercentile = _999thPercentile;
    }
}

