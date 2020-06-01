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

package com.zto.zms.service.util;

import java.util.Calendar;
import java.util.Date;


public class DateUtils {

    public static final int GROUP_INTERVAL = 500;

    public static Long getYestoryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        return calendar.getTimeInMillis();
    }

    public static Long getLastHour() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) - 1);
        return calendar.getTimeInMillis();
    }

    public static Long getNanosecond(Long millisecond) {
        return new Date(millisecond).getTime() * 1000000;
    }

    public static boolean isGreaterOneday(Long beginTime, Long endTime) {
        return (endTime - beginTime) > 86400000;
    }

    public static Long intervalTime(Long beginTime, Long endTime) {
        return (endTime - beginTime) / GROUP_INTERVAL;
    }

}

