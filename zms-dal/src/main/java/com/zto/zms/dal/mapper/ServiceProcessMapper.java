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

package com.zto.zms.dal.mapper;

import com.zto.zms.common.ServiceProcess;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceProcessMapper {

    int insert(com.zto.zms.dal.model.ServiceProcess record);

    int insertList(@Param("list") List<com.zto.zms.dal.model.ServiceProcess> records);

    /**
     * 查询服务各最近一次启动成功进程
     *
     * @param serviceId
     * @return
     */
    List<Integer> listLastSuccessProcess(Integer serviceId);

    List<ServiceProcess> listLastSuccessProcessByServiceIds(@Param("serviceIds") List<Integer> serviceIds);

    List<ServiceProcess> listLastSuccessProcessByServiceIdsAndHost(@Param("serviceIds") List<Integer> serviceIds, @Param("hostId") Integer hostId);


    List<ServiceProcess> queryLastSuccessProcess(Integer serviceId);

    List<ServiceProcess> queryLastRunningProcess(Integer serviceId);

    List<Integer> queryLastSuccessProcessByInstanceIds(@Param("serviceId") Integer serviceId, @Param("instanceIds") List<Integer> instanceIds);

    /**
     * 根据id查询
     *
     * @param processId
     * @return
     */
    ServiceProcess getByProcessId(Integer processId);

    String getProgramName(Integer processId);

    String getProgramType(Integer processId);

    /**
     * 最近一次服务进程启动id
     *
     * @param instanceId
     * @return
     */
    Integer listLastSuccessProcessByInstance(Integer instanceId);

    /**
     * 返回最新进程
     *
     * @param instanceId
     * @return
     */
    com.zto.zms.dal.model.ServiceProcess listLastProcessByInstance(Integer instanceId);

    /**
     * 查询正在运行的进程，如果没有，返回最近新建进程
     *
     * @param instanceId
     * @return
     */
    com.zto.zms.dal.model.ServiceProcess listRunningProcessByInstance(Integer instanceId);

    /**
     * 更新成功
     *
     * @param processId
     * @return
     */
    boolean updateSuccess(Integer processId);

    /**
     * 更新失败
     *
     * @param processId
     * @return
     */
    boolean updateFailure(Integer processId);

    /**
     * supervisor同步运行状态
     *
     * @param processId
     * @param runningStatus
     * @return
     */
    boolean updateRunningStatus(@Param("processId") Integer processId, @Param("runningStatus") String runningStatus);

    /**
     * 根据processId 查询服务状态(小写)
     *
     * @param processId
     * @return
     */
    String getServiceType(Integer processId);

    /**
     * 查询进程运行状态
     */
    List<com.zto.zms.dal.model.ServiceProcess> getRunningStatus(@Param("processIds") List<Integer> processIds);

    /**
     * @param instanceId
     * @return
     */
    List<Integer> queryLastRunningProcessByInstanceId(Integer instanceId);

    boolean stopByAdmin(@Param("processId") Integer processId, @Param("modifier") String modifier);

    String getEnvironmentName(Integer processId);

    String getProgramTypeByInstanceId(Integer instanceId);

    boolean isFirstProcess(Integer id);
}
