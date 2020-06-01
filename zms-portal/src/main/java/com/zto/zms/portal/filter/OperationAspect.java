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

import com.alibaba.fastjson.JSON;
import com.zto.zms.common.ZmsException;
import com.zto.zms.dal.mapper.OperationLogMapper;
import com.zto.zms.dal.model.OperationLog;
import com.zto.zms.portal.service.user.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Created by liangyong on 2019/5/15.
 */

@Aspect
@Component
public class OperationAspect {

    @Autowired
    private UserService userService;
    @Autowired
    OperationLogMapper operationLogMapper;

    @Pointcut("@annotation(com.zto.zms.portal.filter.Operation)")
    public void operationPointCut() {
    }

    @Before("operationPointCut()")
    public void validAdminRole(JoinPoint joinPoint) {
        LdapUser user = userService.getCurrentUser();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Operation operation = method.getAnnotation(Operation.class);
        boolean isAdmin = false;
        if (operation != null) {
            isAdmin = operation.isAdmin();
        }
        if (isAdmin && !user.isAdmin()) {
            throw ZmsException.NO_PERMISSION_EXCEPTION;
        }
    }


    @AfterReturning("operationPointCut()")
    public void saveOperationLog(JoinPoint joinPoint) {
        LdapUser user = userService.getCurrentUser();
        OperationLog operationLog = new OperationLog();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Operation operation = method.getAnnotation(Operation.class);
        if (operation != null) {
            operationLog.setOperation(operation.value());
        }

        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        operationLog.setName(className.concat(".").concat(methodName));
        String content = JSON.toJSONString(joinPoint.getArgs());
        operationLog.setContent(content.length() > 2000 ? content.substring(0, 2000) : content);
        operationLog.setOpeartor(user.getRealName());
        operationLogMapper.insertSelective(operationLog);
    }

}

