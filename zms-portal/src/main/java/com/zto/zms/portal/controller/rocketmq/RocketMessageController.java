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

package com.zto.zms.portal.controller.rocketmq;

import com.zto.zms.portal.dto.MessageView;
import com.zto.zms.portal.result.Result;
import com.zto.zms.portal.service.RocketMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * MQ消息检索
 */
@RestController
@RequestMapping("/api/rocketmq/message")
public class RocketMessageController {

    private Logger logger = LoggerFactory.getLogger(RocketMessageController.class);

    @Autowired
    RocketMessageService messageService;

    /**
     * 根据key查询
     */
    @RequestMapping(value = "/viewByKey", method = RequestMethod.GET)
    public Result<List<MessageView>> queryByKey(@RequestParam(value = "envId") Integer envId,
                                                @RequestParam(value = "topicName") String topicName,
                                                @RequestParam(value = "key") String key) {
        try {
            return Result.success(messageService.queryMessageByTopicAndKey(envId, topicName, key));
        } catch (Exception ex) {
            logger.error("view topic message {}  by key {} error {}", topicName, key, ex);
            return Result.error("401", ex.getMessage());
        }
    }

    /**
     * 根据消息Id查询
     */
    @RequestMapping(value = "/viewByMsgId", method = RequestMethod.GET)
    public Result<List<MessageView>> queryByMsgId(@RequestParam(value = "envId") Integer envId,
                                                  @RequestParam(value = "topicName") String topicName,
                                                  @RequestParam(value = "msgId") String msgId) {
        try {
            return Result.success(messageService.viewMessage(envId, topicName, msgId));
        } catch (Exception ex) {
            logger.error("view topic message {}  by msgId {} error {}", topicName, msgId, ex);

            return Result.error("401", ex.getMessage());
        }
    }

    @RequestMapping(value = "/queryMessageByTime", method = RequestMethod.GET)
    public Result<List<MessageView>> queryMessageByTime(@RequestParam(value = "envId") Integer envId,
                                                        @RequestParam(value = "topicName") String topicName,
                                                        @RequestParam(value = "beginTime") Long beginTime,
                                                        @RequestParam(value = "endTime") Long endTime) {
        try {
            if (null == beginTime || null == endTime) {
                return Result.error("401", "beginTime and endTime required.");
            }
            List<MessageView> messageViewList = messageService.queryMessageByTime(envId, topicName, beginTime, endTime);
            return Result.success(messageViewList);
        } catch (Exception ex) {
            logger.error("queryMessageByTime fail envId={}, topic={}, beginTime={}, endTime={}, errMsg={}", envId, topicName, beginTime, endTime, ex);
            return Result.error("401", ex.getMessage());
        }
    }
}

