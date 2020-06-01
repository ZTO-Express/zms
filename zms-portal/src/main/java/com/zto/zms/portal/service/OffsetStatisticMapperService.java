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

package com.zto.zms.portal.service;

import com.zto.zms.service.domain.OffsetStatisticDTO;
import com.zto.zms.service.domain.OffsetStatisticQueryVO;
import com.zto.zms.service.repository.ClusterDailyOffsetsInfoRepository;
import com.zto.zms.service.repository.TopicDailyOffsetsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liangyong on 2018/11/06.
 */
@Service
public class OffsetStatisticMapperService {

    @Autowired
    private ClusterDailyOffsetsInfoRepository clusterDailyOffsetsInfoRepository;

    @Autowired
    private TopicDailyOffsetsRepository topicDailyOffsetsRepository;

    public List<OffsetStatisticDTO> queryClusterOffset(OffsetStatisticQueryVO queryVo) {
        return clusterDailyOffsetsInfoRepository.queryClusterIncrementOffset(queryVo.getClusterName(),
                queryVo.getBeginTime(), queryVo.getEndTime()).stream()
                .map(item ->
                        new OffsetStatisticDTO(item.getClusterName(),
                                item.getTime().getEpochSecond(),
                                "offsets_daily_increasement",
                                Double.valueOf(item.getValue()).longValue()))
                .collect(Collectors.toList());
    }

    public List<OffsetStatisticDTO> queryTopicOffset(OffsetStatisticQueryVO queryVo) {
        return topicDailyOffsetsRepository.queryTopicIncrementOffset(queryVo.getClusterName(), queryVo.getTopicName(),
                queryVo.getBeginTime(), queryVo.getEndTime()).stream()
                .map(item ->
                        new OffsetStatisticDTO(item.getClusterName(),
                                item.getTopicName(), item.getTime().getEpochSecond(),
                                "offsets_daily_increasement",
                                Double.valueOf(item.getValue()).longValue()))
                .collect(Collectors.toList());
    }


}

