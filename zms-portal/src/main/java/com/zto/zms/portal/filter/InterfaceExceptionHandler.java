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

package com.zto.zms.portal.filter;

import com.zto.zms.common.ZmsException;
import com.zto.zms.portal.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>Class: InterfaceExceptionHandler</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/1/6
 **/
@RestControllerAdvice
public class InterfaceExceptionHandler {
    public static final Logger logger = LoggerFactory.getLogger(InterfaceExceptionHandler.class);

    @ResponseBody
    @ExceptionHandler(ZmsException.class)
    public Result<String> zmsException(ZmsException e) {
        logger.error(e.getMessage(), e);
        return Result.error(String.valueOf(e.getCode()), e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> runtimeException(IllegalArgumentException e) {
        logger.error(e.getMessage(), e);
        return Result.error("401", e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public Result<String> exception(Exception e) {
        logger.error(e.getMessage(), e);
        return Result.error("500", "系统异常");
    }

}

