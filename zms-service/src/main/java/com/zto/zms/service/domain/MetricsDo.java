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

package com.zto.zms.service.domain;

import com.google.common.collect.Maps;

import java.util.Map;

 /**
  * <p> Description: </p>
  *
  * @author liangyong
  * @date 2018/9/17
  * @since 1.0.0
  */
public class MetricsDo<E> {

    private Map<String, E> segmentMap = Maps.newHashMap();

    private Map<String, String> tagOptions = Maps.newHashMap();

    public Map<String, E> getSegmentMap() {
        return segmentMap;
    }

    public void setSegmentMap(Map<String, E> segmentMap) {
        this.segmentMap = segmentMap;
    }

    public Map<String, String> getTagOptions() {
        return tagOptions;
    }

    public void setTagOptions(Map<String, String> tagOptions) {
        this.tagOptions = tagOptions;
    }
}

