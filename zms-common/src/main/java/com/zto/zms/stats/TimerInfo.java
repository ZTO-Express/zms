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

/**
 * Created by superheizai on 2017/9/5.
 */
public class TimerInfo {


    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private double min;
    private double max;
    private double mean;
    private double stddev;
    private double median;
    private double percent75;
    private double percent90;
    private double percent95;
    private double percent98;
    private double percent99;
    private double percent999;


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

    public double getStddev() {
        return stddev;
    }

    public void setStddev(double stddev) {
        this.stddev = stddev;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getPercent75() {
        return percent75;
    }

    public void setPercent75(double percent75) {
        this.percent75 = percent75;
    }

    public double getPercent90() {
        return percent90;
    }

    public void setPercent90(double percent90) {
        this.percent90 = percent90;
    }

    public double getPercent95() {
        return percent95;
    }

    public void setPercent95(double percent95) {
        this.percent95 = percent95;
    }

    public double getPercent98() {
        return percent98;
    }

    public void setPercent98(double percent98) {
        this.percent98 = percent98;
    }

    public double getPercent99() {
        return percent99;
    }

    public void setPercent99(double percent99) {
        this.percent99 = percent99;
    }

    public double getPercent999() {
        return percent999;
    }

    public void setPercent999(double percent999) {
        this.percent999 = percent999;
    }
}

