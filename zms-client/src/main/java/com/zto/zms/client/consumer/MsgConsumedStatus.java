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

package com.zto.zms.client.consumer;

/**
 * Created by superheizai on 2017/8/16.
 */
public enum MsgConsumedStatus {

    SUCCEED(-1), FAILURE(0), RETRY(0), RETRY_1S(1), RETRY_5S(2), RETRY_10S(3), RETRY_30S(4), RETRY_1M(5), RETRY_2M(6), RETRY_3M(7), RETRY_4M(8), RETRY_5M(9), RETRY_6M(10), RETRY_7M(11), RETRY_8M(12), RETRY_9M(13), RETRY_10M(14), RETRY_20M(15), RETRY_30M(16), RETRY_1H(17), RETRY_2H(18);

    int level;

    MsgConsumedStatus(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}



