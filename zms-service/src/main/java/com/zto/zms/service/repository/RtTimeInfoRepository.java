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

package com.zto.zms.service.repository;

import com.zto.zms.service.domain.RtTimeInfoQueryVO;
import com.zto.zms.service.domain.influxdb.InfluxTagInfo;
import com.zto.zms.service.influx.InfluxdbClient;
import com.zto.zms.service.influx.InfluxdbModelTransformer;
import com.zto.zms.service.influx.RtTimeInfo;
import com.zto.zms.service.util.StringFormatUtils;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by liangyong on 2018/9/26.
 */

@Repository
public class RtTimeInfoRepository {

    @Autowired
    private InfluxdbClient influxdbClient;

    public List<RtTimeInfo> queryMaxRtTimeInfo(RtTimeInfoQueryVO queryVo, String name, Long intervalTime) {

        String sql = "select max(value) as value,* from rt_time where time >= {0} and time < {1}";
        sql = StringFormatUtils.format(sql, queryVo.getBeginTime(), queryVo.getEndTime());
        StringBuilder sqlBuilder = new StringBuilder(sql);
        if (null != queryVo.getBrokerName()) {
            sqlBuilder.append(" and brokerName='").append(queryVo.getBrokerName()).append("'");
        }
        if (null != queryVo.getClusterName()) {
            sqlBuilder.append(" and clusterName='").append(queryVo.getClusterName()).append("'");
        }
        if (null != name) {
            sqlBuilder.append(" and \"name\" = '").append(name).append("'");
        }
        sqlBuilder.append(" group by time(").append(intervalTime).append("ms)");
        return getQueryResult(sqlBuilder.toString());
    }

    public List<RtTimeInfo> queryLatestRtTimeInfo(RtTimeInfoQueryVO queryVo, String name) {
        String sql = "select * from rt_time where time >= {0} and time < {1}";
        sql = StringFormatUtils.format(sql, queryVo.getBeginTime(), queryVo.getEndTime());
        StringBuilder sqlBuilder = new StringBuilder(sql);
        if (null != queryVo.getBrokerName()) {
            sqlBuilder.append(" and brokerName='").append(queryVo.getBrokerName()).append("'");
        }
        if (null != queryVo.getClusterName()) {
            sqlBuilder.append(" and clusterName='").append(queryVo.getClusterName()).append("'");
        }
        if (null != name) {
            sqlBuilder.append(" and \"name\" = '").append(name).append("'");
        }
        sqlBuilder.append(" order by time desc limit 1");
        return getQueryResult(sqlBuilder.toString());
    }

    public List<RtTimeInfo> getQueryResult(String sql) {
        QueryResult queryResult = influxdbClient.query(sql);
        InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
        return resultMapper.toPOJO(queryResult, RtTimeInfo.class);
    }

    public List<InfluxTagInfo> queryRtTimeTagInfo(String tagName, String clusterName) {
        String sql = StringFormatUtils.format("show tag values from rt_time with key={0} where clusterName='{1}'",
                tagName, clusterName);
        return getQueryTagResult(sql);
    }

    private List<InfluxTagInfo> getQueryTagResult(String sql) {
        QueryResult queryResult = influxdbClient.query(sql);
        return InfluxdbModelTransformer.InstanceHolder.getInstance().toPOJOList(queryResult, InfluxTagInfo.class);
    }


}

