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

package com.zto.zms.portal.service.host;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zto.zms.common.ZmsException;
import com.zto.zms.service.domain.page.PageResult;
import com.zto.zms.utils.Assert;
import com.zto.zms.common.ZmsHost;
import com.zto.zms.dal.domain.host.ZmsHostVO;
import com.zto.zms.dal.mapper.ServiceInstanceMapper;
import com.zto.zms.dal.mapper.ZmsHostMapper;
import com.zto.zms.dal.model.ServiceInstance;
import com.zto.zms.dal.util.AesEncryptUtil;
import com.zto.zms.portal.common.InstanceStatusEnum;
import com.zto.zms.portal.common.ZmsServiceStatusEnum;
import com.zto.zms.portal.config.CommonConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuhao.zhang
 * @description
 * @date 2020/1/6
 */
@Service
public class HostServiceImpl implements HostService {
    public static final Logger logger = LoggerFactory.getLogger(HostServiceImpl.class);

    @Autowired
    private ZmsHostMapper zmsHostMapper;
    @Autowired
    private ServiceInstanceMapper serviceInstanceMapper;
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 主机列表
     *
     * @param envId
     * @param hostIp
     * @return
     */
    @Override
    public PageResult<ZmsHostVO> pageHostList(int envId, String hostIp, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<com.zto.zms.dal.model.ZmsHost> hostPage = zmsHostMapper.pageHostList(envId, hostIp);
        PageInfo<com.zto.zms.dal.model.ZmsHost> pageInfo = new PageInfo<>(hostPage);
        List<ZmsHostVO> resultLst = hostPage.stream()
                .map(item -> {
                    ZmsHostVO zmsHostVo = new ZmsHostVO();
                    BeanUtils.copyProperties(item, zmsHostVo);
                    if (StringUtils.isNotEmpty(zmsHostVo.getTotalMem())) {
                        zmsHostVo.setTotalMem(Integer.parseInt(zmsHostVo.getTotalMem()) / 1000 + "M");
                    }
                    List<ServiceInstance> serviceInstances = serviceInstanceMapper.hostServices(zmsHostVo.getId());
                    zmsHostVo.setServices(serviceInstances);
                    return zmsHostVo;
                }).collect(Collectors.toList());
        return new PageResult<>(pageNo, pageSize, pageInfo.getTotal(), resultLst);
    }

    /**
     * 主机详情
     *
     * @param envId
     * @param hostId
     * @return
     */
    @Override
    public ZmsHostVO queryHostDetail(int envId, int hostId) {
        ZmsHostVO zmsHostVo = new ZmsHostVO();
        com.zto.zms.dal.model.ZmsHost zmsHost = zmsHostMapper.queryHostDetail(envId, hostId);
        BeanUtils.copyProperties(zmsHost, zmsHostVo);
        List<ServiceInstance> serviceInstances = serviceInstanceMapper.hostServices(zmsHostVo.getId());
        zmsHostVo.setServices(serviceInstances);
        return zmsHostVo;
    }

    /**
     * 添加主机
     *
     * @param zmsHost
     * @return
     */
    @Override
    public int addHost(ZmsHost zmsHost) {
        if (StringUtils.isEmpty(zmsHost.getToken())) {
            throw new ZmsException("主机token为空", 200);
        }
        try {
            String signStr = AesEncryptUtil.decrypt(zmsHost.getToken(), commonConfig.getZmsPortalSecretKey());
            ZmsHost zmsHostEnv = JSON.parseObject(signStr, ZmsHost.class);
            Assert.that(null != zmsHostEnv && null != zmsHostEnv.getEnvironmentId(), "环境不存在");
            zmsHost.setEnvironmentId(zmsHostEnv.getEnvironmentId());
        } catch (Exception e) {
            logger.error("主机 : {} token : {} ", zmsHost.getHostIp(), zmsHost.getToken());
            throw new ZmsException("主机token校验失败", 200);
        }
        // 判断主机是否存在
        com.zto.zms.dal.model.ZmsHost host = zmsHostMapper.getByEnvIdHostIp(zmsHost.getEnvironmentId(), zmsHost.getHostIp());
        if (null == host) {
            return zmsHostMapper.insert(zmsHost);
        }
        zmsHost.setId(host.getId());
        return zmsHostMapper.updateById(zmsHost);
    }

    /**
     * 向环境添加主机
     *
     * @param envId
     * @return
     */
    @Override
    public String hostInit(int envId) {
        if (0 == envId) {
            throw new ZmsException("环境id为空", 200);
        }
        String signStr = getToken(envId);
        StringBuilder insScript = new StringBuilder();
        insScript.append(MessageFormat.format(commonConfig.getInstallScriptUrl(), commonConfig.getZmsPortalUrl()))
                .append(" " + signStr)
                .append(" " + commonConfig.getZmsPortalUrl());
        return insScript.toString();
    }

    @Override
    public String getToken(int envId) {
        ZmsHost zmsHost = new ZmsHost();
        zmsHost.setEnvironmentId(envId);
        return AesEncryptUtil.encrypt(JSON.toJSONString(zmsHost), commonConfig.getZmsPortalSecretKey());
    }

    /**
     * 启用主机
     *
     * @param envId
     * @param hostIds
     * @return
     */
    @Override
    public int hostEnabled(int envId, List<Integer> hostIds) {
        return zmsHostMapper.updateHostEnabledOrDisabled(envId, hostIds, ZmsServiceStatusEnum.ENABLE.name());
    }

    /**
     * 停用主机
     *
     * @param envId
     * @param hostIds
     * @return
     */
    @Override
    public int hostDisabled(int envId, List<Integer> hostIds) {
        //校验该主机是否有服务在运行
        List<ServiceInstance> serviceInstances = serviceInstanceMapper.findByHostIdsAndStatus(hostIds, InstanceStatusEnum.START.name());
        if (serviceInstances != null && serviceInstances.size() > 0) {
            throw new ZmsException("该主机有服务运行,请您先停止该主机上的服务!!!", 200);
        }
        return zmsHostMapper.updateHostEnabledOrDisabled(envId, hostIds, ZmsServiceStatusEnum.DISABLE.name());
    }
}

