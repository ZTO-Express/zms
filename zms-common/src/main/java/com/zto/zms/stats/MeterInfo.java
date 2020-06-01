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
public class MeterInfo {


    private long count;

    private double mean;

    private double min1Rate;

    private double min5Rate;

    private double min15Rate;

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getMin1Rate() {
        return min1Rate;
    }

    public void setMin1Rate(double min1Rate) {
        this.min1Rate = min1Rate;
    }

    public double getMin5Rate() {
        return min5Rate;
    }

    public void setMin5Rate(double min5Rate) {
        this.min5Rate = min5Rate;
    }

    public double getMin15Rate() {
        return min15Rate;
    }

    public void setMin15Rate(double min15Rate) {
        this.min15Rate = min15Rate;
    }


}

