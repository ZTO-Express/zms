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

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.zto.zms.client.common.ZmsMessage;
import com.zto.zms.common.BrokerType;
import com.zto.zms.common.ZmsException;
import com.zto.zms.service.domain.page.PageResult;
import com.zto.zms.client.producer.Producer;
import com.zto.zms.client.producer.SendResult;
import com.zto.zms.client.producer.SendCallback;
import com.zto.zms.dal.domain.alert.AlertRuleDTO;
import com.zto.zms.dal.domain.alert.AlertRuleEnvironmentRefDTO;
import com.zto.zms.dal.domain.alert.EnvironmentRefDTO;
import com.zto.zms.dal.mapper.*;
import com.zto.zms.dal.model.AlertRuleConfig;
import com.zto.zms.dal.model.AlertRuleEnvironmentRef;
import com.zto.zms.dal.model.ZmsServiceEntity;
import com.zto.zms.portal.filter.LdapUser;
import com.zto.zms.portal.manage.MultiEnvironmentProducerManager;
import com.zto.zms.portal.result.ID;
import com.zto.zms.portal.result.Result;
import com.zto.zms.service.config.ZmsConsumerConf;
import com.zto.zms.service.domain.alert.AlertRuleCheckDTO;
import com.zto.zms.service.domain.alert.QueryAlertVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springside.modules.utils.collection.CollectionUtil;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private static final String MQ_TPS_TEMPLATE = "select Count(value) from \"mq_topic_info\" where \"clusterName\" = '{clusterName}' AND \"name\" = 'tps' AND \"topicName\" = '%s' and \"value\" %s %d and  \"time\" > now()-%s ;";
    private static final String KAFKA_TPS_TEMPLATE = "SELECT Count(value) FROM \"kafka_topic_info\" WHERE \"clusterName\" = '{clusterName}' AND \"topicName\" = '%s' AND \"name\" = 'oneMinuteRate' AND \"attributeName\" = 'MessagesInPerSec' and brokerName='' and \"value\" %s %d and \"time\" > now()-%s ;";
    private static final String MQ_CONSUMER_TPS_TEMPLATE = "select Count(value)  FROM \"mq_consumer_number_info\" WHERE \"clusterName\" = '{clusterName}' AND \"consumerGroup\" = '%s' AND \"name\" = 'tps'  and \"value\" %s %d and \"time\" > now()-%s ;";
    private static final String MQ_CONSUMER_LATENCY_TEMPLATE = "select Count(value)  FROM \"mq_consumer_number_info\" WHERE \"clusterName\" = '{clusterName}' AND \"consumerGroup\" = '%s' AND \"name\" = 'latency' and \"value\" %s %d and \"time\" > now()-%s ;";
    private static final String MQ_CONSUMER_LASTCONSUMINGTIME_TEMPLATE = "select Count(value)  FROM \"mq_consumer_number_info\" WHERE \"clusterName\" = '{clusterName}' AND \"consumerGroup\" = '%s' AND \"name\" = 'lastConsumingTime' and \"value\" %s %d and \"time\" > now()-%s ;";
    private static final String KAFKA_CONSUMER_TPS_TEMPLATE = "select Count(value)  FROM \"kafka_consumer_number_info\" WHERE \"clusterName\" = '{clusterName}' AND \"consumerGroup\" = '%s' AND \"name\" = 'tps'  and \"value\" %s %d and \"time\" > now()-%s ;";
    private static final String KAFKA_CONSUMER_LATENCY_TEMPLATE = "select Count(value)  FROM \"kafka_consumer_number_info\" WHERE \"clusterName\" = '{clusterName}' AND \"consumerGroup\" = '%s' AND \"name\" = 'latency' and \"value\" %s %d and \"time\" > now()-%s ;";
    private static final String ERROR_MESSAGE = "告警名称：%s\n告警类型：%s\n告警阀值：%s时间内，%s%s次触发%s%s%s";

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    @Autowired
    private MultiEnvironmentProducerManager multiEnvironmentProducerManager;
    @Autowired
    private AlertRuleDetailMapper alertRuleDetailMapper;
    @Autowired
    private AlertRuleEnvironmentRefMapper alertRuleEnvironmentRefMapper;
    @Autowired
    private TopicEnvironmentRefMapper topicEnvironmentRefMapper;
    @Autowired
    private ConsumerEnvironmentRefMapper consumerEnvironmentRefMapper;
    @Autowired
    private ZmsConsumerConf zmsConsumerConf;
    @Autowired
    private ZmsServiceMapper zmsServiceMapper;
    @Autowired
    private ZmsEnvironmentMapper zmsEnvironmentMapper;

    public PageResult<AlertRuleDTO> queryAlerts(QueryAlertVO queryVo) {
        int total = alertRuleDetailMapper.countAlertUserRules(queryVo.getKeyWord());
        if (total == 0) {
            return new PageResult<>(queryVo.getCurrentPage(), queryVo.getPageSize());
        }
        List<AlertRuleDTO> alertRuleDTOList = alertRuleDetailMapper.queryAlertUserRulesPage(queryVo.getKeyWord(), queryVo.getOffset(), queryVo.getPageSize()).stream().map(item -> {
            AlertRuleDTO alertRuleDto = new AlertRuleDTO();
            BeanUtils.copyProperties(item, alertRuleDto);
            List<AlertRuleEnvironmentRefDTO> alertRuleEnvironmentRefDtos = alertRuleEnvironmentRefMapper.queryEnvironmentRefByAlertRuleId(item.getId());
            if(CollectionUtil.isEmpty(alertRuleEnvironmentRefDtos) ) {
                alertRuleDto.setEnvironmentRefDtos(Lists.newArrayList());
                return alertRuleDto;
            }
            List<EnvironmentRefDTO> ruleEnvironments;
            if("topic".equals(alertRuleDto.getType())){
                ruleEnvironments=topicEnvironmentRefMapper.gettopicenvsByName(alertRuleDto.getName());
            } else {
                ruleEnvironments=consumerEnvironmentRefMapper.getconsumerenvsByName(alertRuleDto.getName());
            }
            if(CollectionUtil.isEmpty(ruleEnvironments) ) {
                alertRuleDto.setEnvironmentRefDtos(Lists.newArrayList());
                return alertRuleDto;
            }
            Map<Integer,EnvironmentRefDTO> envs = ruleEnvironments.stream().collect(Collectors.toMap(EnvironmentRefDTO::getEnvironmentId,i -> i,(o1,o2) -> o1));
            //筛选告警所在环境
            ruleEnvironments = alertRuleEnvironmentRefDtos.stream().map(t-> envs.get(t.getEnvironmentId())).collect(Collectors.toList());
            alertRuleDto.setEnvironmentRefDtos(ruleEnvironments);
            return alertRuleDto;
        }).collect(Collectors.toList());
        return new PageResult<>(queryVo.getCurrentPage(), queryVo.getPageSize(), total, alertRuleDTOList);
    }

    public List<String> queryAlertNames() {
        return alertRuleDetailMapper.queryAlertUserRuleNames();
    }

    @Transactional
    public Result<ID> insert(AlertRuleDTO alertRuleDto, String userName) {
        logger.info("alert rule {} create", JSON.toJSONString(alertRuleDto));
        List<AlertRuleConfig> alertRules = alertRuleDetailMapper.queryAlertRules(alertRuleDto.getName(), alertRuleDto.getField(), alertRuleDto.getType());
        if (!CollectionUtils.isEmpty(alertRules)) {
            return Result.error("500", "存在重复记录");
        }
        List<EnvironmentRefDTO> environmentRefDtoList = getEnvironmentRefDto(alertRuleDto);

        AlertRuleConfig alertRule = new AlertRuleConfig();
        BeanUtils.copyProperties(alertRuleDto, alertRule);
        alertRule.setInfluxSql(generateSql(alertRule, environmentRefDtoList.get(0).getServiceId()));
        if (StringUtils.isBlank(alertRule.getDescription())) {
            alertRule.setDescription(generateErrorMsg(alertRule));
        }

        int count = alertRuleDetailMapper.insert(alertRule);
        if (count < 1) {
            return Result.error("500", "告警新增失败");
        }
        // 新增告警和环境集群关系
        insertAlertRuleEnvironmentRef(environmentRefDtoList, userName, alertRule.getId());
        // 发送到alert模块
        sendAlertRule(environmentRefDtoList, alertRule, "ADD");

        return Result.success(ID.newId(count));
    }

    private List<EnvironmentRefDTO> getEnvironmentRefDto(AlertRuleDTO alertRuleDto) {
        List<EnvironmentRefDTO> environmentRefDtoList;
        if ("topic".equals(alertRuleDto.getType())) {
            environmentRefDtoList = topicEnvironmentRefMapper.queryEnvironmentRefByTopicName(alertRuleDto.getName(),
                    alertRuleDto.getRefList().stream().map(AlertRuleEnvironmentRefDTO::getEnvironmentId).collect(Collectors.toList()));
        } else {
            environmentRefDtoList = consumerEnvironmentRefMapper.queryEnvironmentRefByConsumerName(alertRuleDto.getName(),
                    alertRuleDto.getRefList().stream().map(AlertRuleEnvironmentRefDTO::getEnvironmentId).collect(Collectors.toList()));
        }
        if (CollectionUtils.isEmpty(environmentRefDtoList)) {
            throw new ZmsException("消费组/主题名的环境不存在", 500);
        }
        return environmentRefDtoList;
    }

    @Transactional
    public Result<ID> update(AlertRuleDTO alertRuleDto, LdapUser ldapUser) {
        AlertRuleConfig alertRuleConfig = alertRuleDetailMapper.selectByPrimaryKey(alertRuleDto.getId());
        //权限判断
        if (!ldapUser.isAdmin() && StringUtils.compare(alertRuleConfig.getAlertUser(), ldapUser.getRealName()) != 0) {
            throw ZmsException.NO_PERMISSION_MODIFY;
        }

        List<EnvironmentRefDTO> environmentRefDtoList = getEnvironmentRefDto(alertRuleDto);

        AlertRuleConfig alertRule = new AlertRuleConfig();
        BeanUtils.copyProperties(alertRuleDto, alertRule);
        alertRule.setInfluxSql(generateSql(alertRule, environmentRefDtoList.get(0).getServiceId()));
        if (StringUtils.isBlank(alertRule.getDescription())) {
            alertRule.setDescription(generateErrorMsg(alertRule));
        }

        int count = alertRuleDetailMapper.updateById(alertRule);
        if (count < 1) {
            return Result.error("500", "告警修改失败");
        }
        // 删除对应环境集群关系，重新添加
        alertRuleEnvironmentRefMapper.deleteAlertRuleId(alertRule.getId());
        insertAlertRuleEnvironmentRef(environmentRefDtoList, ldapUser.getRealName(), alertRule.getId());
        // 发送到alert模块
        sendAlertRule(environmentRefDtoList, alertRule, "UPDATE");

        return Result.success(ID.newId(count));
    }

    /**
     * 生成告警查询sql
     */
    private String generateSql(AlertRuleConfig alertRule, Integer serviceId) {
        ZmsServiceEntity serviceEntity = zmsServiceMapper.getById(serviceId);
        String template;
        if ("topic".equalsIgnoreCase(alertRule.getType())) {
            if (BrokerType.ROCKETMQ.getName().equalsIgnoreCase(serviceEntity.getServerType())) {
                template = String.format(MQ_TPS_TEMPLATE, alertRule.getName(), alertRule.getOperator(), alertRule.getTarget(), alertRule.getScope());
            } else {
                template = String.format(KAFKA_TPS_TEMPLATE, alertRule.getName(), alertRule.getOperator(), alertRule.getTarget(), alertRule.getScope());
            }
        } else {
            if (BrokerType.ROCKETMQ.getName().equalsIgnoreCase(serviceEntity.getServerType())) {
                if ("tps".equalsIgnoreCase(alertRule.getField())) {
                    template = String.format(MQ_CONSUMER_TPS_TEMPLATE, alertRule.getName(), alertRule.getOperator(), alertRule.getTarget(), alertRule.getScope());
                } else if ("latency".equalsIgnoreCase(alertRule.getField())) {
                    template = String.format(MQ_CONSUMER_LATENCY_TEMPLATE, alertRule.getName(), alertRule.getOperator(), alertRule.getTarget(), alertRule.getScope());
                } else {
                    template = String.format(MQ_CONSUMER_LASTCONSUMINGTIME_TEMPLATE, alertRule.getName(), alertRule.getOperator(), alertRule.getTarget(), alertRule.getScope());
                }
            } else {
                if ("tps".equalsIgnoreCase(alertRule.getField())) {
                    template = String.format(KAFKA_CONSUMER_TPS_TEMPLATE, alertRule.getName(), alertRule.getOperator(), alertRule.getTarget(), alertRule.getScope());
                } else {
                    template = String.format(KAFKA_CONSUMER_LATENCY_TEMPLATE, alertRule.getName(), alertRule.getOperator(), alertRule.getTarget(), alertRule.getScope());
                }
            }
        }
        return template;
    }

    /**
     * 生成告警发送信息
     */
    private String generateErrorMsg(AlertRuleConfig alertRule) {
        String triggerOperatorTxt = "";
        if (">".equals(alertRule.getTriggerOperator())) {
            triggerOperatorTxt = "超过";
        } else if ("<".equals(alertRule.getTriggerOperator())) {
            triggerOperatorTxt = "少于";
        }
        return String.format(ERROR_MESSAGE, alertRule.getName(), alertRule.getType(), alertRule.getScope(), triggerOperatorTxt, alertRule.getTriggerTimes(), alertRule.getField(), alertRule.getOperator(), alertRule.getTarget());
    }

    /**
     * 新增告警和环境集群关系
     */
    private void insertAlertRuleEnvironmentRef(List<EnvironmentRefDTO> environmentRefDtoList, String userName, Long alertRuleId) {
        environmentRefDtoList.forEach(item -> {
            AlertRuleEnvironmentRef ref = new AlertRuleEnvironmentRef();
            BeanUtils.copyProperties(item, ref);
            ref.setAlertRuleId(alertRuleId);
            ref.setCreator(userName);
            alertRuleEnvironmentRefMapper.insert(ref);
        });
    }

    @Transactional
    public Result<ID> delete(AlertRuleConfig alertRule) {
        if (StringUtils.isEmpty(alertRule.getName())) {
            alertRule = alertRuleDetailMapper.selectByPrimaryKey(alertRule.getId());
        }
        List<EnvironmentRefDTO> environmentRefDtoList = alertRuleEnvironmentRefMapper.queryEnvironmentRefByAlertRuleId(alertRule.getId())
                .stream().map(item -> {
                    EnvironmentRefDTO environmentRefDto = new EnvironmentRefDTO();
                    environmentRefDto.setEnvironmentId(item.getEnvironmentId());
                    environmentRefDto.setEnvironmentName(item.getEnvironmentName());
                    return environmentRefDto;
                }).collect(Collectors.toList());
        // 删除对应环境集群关系
        alertRuleEnvironmentRefMapper.deleteAlertRuleId(alertRule.getId());
        int count = alertRuleDetailMapper.deleteByPrimaryKey(alertRule.getId());

        if (count > 0) {
            sendAlertRule(environmentRefDtoList, alertRule, "DELETE");
        }
        return Result.success(ID.newId(count));
    }

    /**
     * 告警新增修改删除后发送到alert模块
     */
    private void sendAlertRule(List<EnvironmentRefDTO> environmentRefDtoList, AlertRuleConfig alertRule, String tag) {
        environmentRefDtoList.forEach(item -> {
            AlertRuleConfig sendRule = getClusterName(item.getEnvironmentId(), item.getEnvironmentName(), alertRule);
            String jsonString = JSON.toJSONString(sendRule);
            Producer producer = multiEnvironmentProducerManager.getCurrentProducer(item.getEnvironmentId(), zmsConsumerConf.getAlertTopic());
            try {
                ZmsMessage zmsMessage = new ZmsMessage();
                zmsMessage.setTags(tag);
                zmsMessage.setPayload(jsonString.getBytes("UTF-8"));
                producer.asyncSend(zmsMessage, new SendCallback() {
                    @Override
                    public void onException(Throwable exception) {
                        logger.error("send {} zms {} alert rule {} {} error", tag, item.getEnvironmentName(), alertRule.getType(), alertRule.getName(), exception);
                    }

                    @Override
                    public void onResult(SendResult response) {
                        logger.info("send {} zms {} alert rule {} {} succeed", tag, item.getEnvironmentName(), alertRule.getType(), alertRule.getName());
                    }
                });
            } catch (UnsupportedEncodingException e) {
                logger.error("serialize {} zms {} alert rule {} error", tag, item.getEnvironmentName(), jsonString, e);
            }
        });
    }

    public Boolean uniqueCheck(AlertRuleCheckDTO alertRule) {
        int count = alertRuleDetailMapper.uniqueCheck(alertRule.getName().toLowerCase(),
                alertRule.getField().toLowerCase(), alertRule.getType().toLowerCase(), alertRule.getId());
        return count == 0;
    }

    public List<AlertRuleConfig> getEffectAlertRulesByEnv(String env) {
        Integer envId = zmsEnvironmentMapper.getIdByName(env);
        List<AlertRuleConfig> rules = alertRuleDetailMapper.selectEffectAlertRulesByEnvId(envId).stream()
                .map(rule -> getClusterName(envId, env, rule)).collect(Collectors.toList());
        return rules.stream().filter(rule -> !rule.getInfluxSql().contains("{clusterName}")).collect(Collectors.toList());
    }

    private AlertRuleConfig getClusterName(Integer envId, String env, AlertRuleConfig rule) {
        AlertRuleConfig sendRule = new AlertRuleConfig();
        BeanUtils.copyProperties(rule, sendRule);
        if (sendRule.getInfluxSql().contains("{clusterName}")) {
            String cluster;
            if ("topic".equalsIgnoreCase(sendRule.getType())) {
                cluster = topicEnvironmentRefMapper.getByEnvIdAndTopicName(envId, sendRule.getName());
            } else {
                cluster = consumerEnvironmentRefMapper.getByEnvIdAndConsumerName(envId, sendRule.getName());
            }
            if (StringUtils.isEmpty(cluster)) {
                logger.error("{}环境，告警名称{}所在集群已被删除", env, sendRule.getName());
            } else {
                sendRule.setInfluxSql(sendRule.getInfluxSql().replace("{clusterName}", cluster));
            }
        }
        return sendRule;
    }
}

