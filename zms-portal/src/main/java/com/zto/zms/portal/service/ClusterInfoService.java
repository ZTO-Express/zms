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

import com.zto.zms.common.ZmsException;
import com.zto.zms.service.domain.cluster.ClusterInfoDTO;
import com.zto.zms.service.domain.cluster.ClusterInfoQueryVO;
import com.zto.zms.service.repository.ClusterNumberRepository;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by liangyong on 2018/9/27.
 */
@Service
public class ClusterInfoService {

    @Autowired
    private ClusterNumberRepository clusterNumberRepository;
    @Autowired
    private ExecutorService executorService;

    public List<ClusterInfoDTO> queryClusterMetrics(ClusterInfoQueryVO queryVo) {
        List<ClusterInfoDTO> resultLst = Lists.newArrayList();
        try {
            List<Future<List<ClusterInfoDTO>>> futures = new ArrayList<>();
            futures.add(executorService.submit(() -> queryMaxClusterInfo(queryVo, "totalTps")));
            futures.add(executorService.submit(() -> queryLatestClusterInfo(queryVo, "totalTps")));
            futures.add(executorService.submit(() -> queryMaxClusterInfo(queryVo, "clusterNums")));
            futures.add(executorService.submit(() -> queryLatestClusterInfo(queryVo, "clusterNums")));
            for (Future<List<ClusterInfoDTO>> future : futures) {
                resultLst.addAll(future.get());
            }
        } catch (ExecutionException | InterruptedException e) {
            throw ZmsException.FUTURE_GET_EXCEPTION;
        }
        return resultLst;
    }


    public List<ClusterInfoDTO> queryMaxClusterInfo(ClusterInfoQueryVO queryVo, String name) {
        return this.clusterNumberRepository.queryMaxClusterInfo(queryVo, name, (queryVo.getEndTime() - queryVo.getBeginTime()) / 200)
                .stream()
                .filter(item -> !StringUtils.isEmpty(item.getClusterName()))
                .map(item -> new ClusterInfoDTO(item.getTime().getEpochSecond(), item.getClusterName(), item.getName()
                        , item.getValue()))
                .collect(Collectors.toList());
    }

    public List<ClusterInfoDTO> queryLatestClusterInfo(ClusterInfoQueryVO queryVo, String name) {
        return this.clusterNumberRepository.queryLatestClusterInfo(queryVo, name)
                .stream().map(item -> new ClusterInfoDTO(item.getTime().getEpochSecond(), item.getClusterName(), item.getName()
                        , item.getValue()))
                .collect(Collectors.toList());
    }

}

