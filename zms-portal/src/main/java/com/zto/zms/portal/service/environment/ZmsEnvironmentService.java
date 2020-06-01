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

package com.zto.zms.portal.service.environment;

import com.zto.zms.common.ZmsServiceTypeEnum;
import com.zto.zms.utils.Assert;
import com.zto.zms.dal.mapper.ServiceInstanceMapper;
import com.zto.zms.dal.mapper.ZmsEnvironmentMapper;
import com.zto.zms.dal.mapper.ZmsHostMapper;
import com.zto.zms.dal.mapper.ZmsServiceMapper;
import com.zto.zms.dal.model.ServiceInstance;
import com.zto.zms.dal.model.ZmsEnvironment;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.common.InstanceStatusEnum;
import com.zto.zms.portal.common.ZmsContentEnum;
import com.zto.zms.portal.common.ZmsEnvironmentStatusEnum;
import com.zto.zms.portal.dto.ZmsEnvironmentRespDTO;
import com.zto.zms.portal.dto.serve.ZmsServiceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/1/6
 **/
@Service
public class ZmsEnvironmentService {

    @Autowired
    private ZmsEnvironmentMapper environmentMapper;
    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private ZmsHostMapper zmsHostMapper;
    @Autowired
    private EnvDataSourceService envDataSourceService;

    public int addEvn(ZmsEnvironment zmsEnvironment) {
        boolean isExistName = environmentMapper.isExistName(zmsEnvironment.getEnvironmentName());
        Assert.that(!isExistName, "环境名称已经存在");
        zmsEnvironment.setEnvironmentStatus(ZmsEnvironmentStatusEnum.CREATE.name());
        return environmentMapper.insert(zmsEnvironment);
    }

    public List<ZmsEnvironment> listEnvironment() {
        return environmentMapper.listEnableEnv().stream()
                .map(item -> {
                    ZmsEnvironment environment = new ZmsEnvironment();
                    environment.setId(item.getId());
                    environment.setEnvironmentName(item.getEnvironmentName());
                    return environment;
                }).collect(Collectors.toList());
    }

    /**
     * 查询所有环境信息
     */
    public List<ZmsEnvironmentRespDTO> listAll(Integer envId) {
        List<ZmsEnvironment> environments = environmentMapper.listAll();
        if (null != envId) {
            environments = environments.stream().filter(item -> item.getId().equals(envId)).collect(Collectors.toList());
        }
        return environments.stream().map(item -> {
            List<ZmsServiceEntity> services = zmsServiceMapper.listByEnvId(item.getId());
            List<ServiceInstance> instances = serviceInstanceMapper.listAllByEnvId(item.getId());
            int hostCount = zmsHostMapper.countByEnvId(item.getId());

            // 服务健康程度
            List<ZmsServiceVO> serviceVoList = services.stream().map(service -> {
                ZmsServiceVO serviceVo = new ZmsServiceVO();
                BeanUtils.copyProperties(service, serviceVo);
                List<ServiceInstance> instanceList = instances.stream()
                        .filter(instance -> instance.getServiceId().equals(service.getId())).collect(Collectors.toList());
                boolean stop = instanceList.stream().anyMatch(instance -> InstanceStatusEnum.STOP.name().equals(instance.getInstanceStatus()));
                boolean start = instanceList.stream().anyMatch(instance -> InstanceStatusEnum.START.name().equals(instance.getInstanceStatus()));
                if (start && !stop) {
                    serviceVo.setHealthState(0);
                } else if (!start && stop) {
                    serviceVo.setHealthState(2);
                } else {
                    serviceVo.setHealthState(1);
                }
                return serviceVo;
            }).sorted(Comparator.comparing(ZmsServiceVO::getHealthState).reversed()).collect(Collectors.toList());

            ZmsEnvironmentRespDTO respDto = new ZmsEnvironmentRespDTO();
            BeanUtils.copyProperties(item, respDto);
            respDto.setHostCount(hostCount);
            respDto.setServices(serviceVoList);
            // 集群健康程度
            if (null != item.getZkServiceId() && null != item.getInfluxdbServiceId()) {
                respDto.setHealthState(0);
            } else if (null == item.getZkServiceId()) {
                respDto.setHealthState(2);
            } else {
                respDto.setHealthState(1);
            }
            return respDto;
        }).collect(Collectors.toList());
    }

    public int rename(Integer id, String environmentName) {
        boolean isExist = environmentMapper.isExist(id);
        Assert.that(isExist, "环境不存在");
        boolean isExistName = environmentMapper.isExistName(environmentName);
        Assert.that(!isExistName, "环境名称已经存在");
        return environmentMapper.rename(id, environmentName);
    }

    /**
     * 设置环境数据源
     *
     * @param envId             环境ID
     * @param zkServiceId       zookeeper服务ID
     * @param influxDbServiceId influxDB服务ID
     */
    public int loadDatabase(Integer envId, Integer zkServiceId, Integer influxDbServiceId) {
        if (null == zkServiceId) {
            envDataSourceService.removeEnvZkClient(envId);
        } else {
            ZmsServiceEntity zmsServiceEntity = zmsServiceMapper.getById(zkServiceId);
            Assert.that(zmsServiceEntity != null && ZmsContentEnum.ENABLE.name().equals(zmsServiceEntity.getServerStatus()), "Zookeeper服务不可用");
            Assert.that(ZmsServiceTypeEnum.ZOOKEEPER.name().equals(zmsServiceEntity.getServerType()), "选择服务不是Zookeeper");
            //重新加载环境zk
            envDataSourceService.reloadEnvZkClient(envId, zkServiceId);
        }

        if (null == influxDbServiceId) {
            envDataSourceService.removeEnvInfluxDb(envId);
        } else {
            ZmsServiceEntity influxDbService = zmsServiceMapper.getById(influxDbServiceId);
            Assert.that(influxDbService != null && ZmsContentEnum.ENABLE.name().equals(influxDbService.getServerStatus()), "influxDB服务不可用");
            Assert.that(ZmsServiceTypeEnum.INFLUXDB.name().equals(influxDbService.getServerType()), "选择服务不是influxDB");

            //重新加载环境influxDB
            envDataSourceService.reloadEnvInfluxDb(envId, influxDbServiceId);
        }
        return environmentMapper.updateDatabase(envId, zkServiceId, influxDbServiceId);
    }
}

