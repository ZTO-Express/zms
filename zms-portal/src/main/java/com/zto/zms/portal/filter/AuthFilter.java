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

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.zto.zms.portal.common.ZmsContent;
import com.zto.zms.portal.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springside.modules.utils.io.IOUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <p>Class: AuthFilter</p>
 * <p> Description: </p>
 *
 * @author lidawei9103@zto.com
 * @version 1.0
 * @date 2019/11/6
 */
@Component
public class AuthFilter implements Filter {

    @Autowired
    private UserService userService;

    // 静态资源
    private static final String STATIC_RESOURCE_REGEX = "(.*(\\.(js|png|css|woff2|ttf))$)|(.*(/favicon.ico)$)";

    // alert模块调用
    private static final String ALERT_RESOURCE_REGEX = "(^(/api/alert/getEffectAlertRulesByEnv))";

    // agent模块调用
    private static final String AGENT_RESOURCE_REGEX = "(^(/api/process/)(.+?))|(^(/api/master/addHost))";

    private static final String BACKUP_CLUSTER_REGEX = "(^(/api/topic/backupMetadata))|(^(/api/consumer/backupMetadata))";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String requestUri = ((HttpServletRequest) servletRequest).getRequestURI();
        if (isStaticResource(requestUri)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (isAlertResource(requestUri)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (isAgentResource(requestUri)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (isBackupClusterResource(requestUri)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        //下载接口跳过登录
        if (!Strings.isNullOrEmpty(requestUri) && requestUri.startsWith("/api/package/script/download")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (!Strings.isNullOrEmpty(requestUri) && requestUri.startsWith("/api/logout")) {
            HttpSession httpSession = ((HttpServletRequest) servletRequest).getSession();
            PrintWriter out = servletResponse.getWriter();
            httpSession.removeAttribute("ldapUser");
            out.write("{\"status\":true}");
            out.flush();
            return;
        }
        if (!Strings.isNullOrEmpty(requestUri) && requestUri.startsWith("/api/login")) {
            String loginName = IOUtil.toString(servletRequest.getInputStream());
            JSONObject jsonObject = JSONObject.parseObject(loginName);
            String username = jsonObject.getString("userName");
            String password = jsonObject.getString("password");
            PrintWriter out = servletResponse.getWriter();
            LdapUser userInfo = getUser(username, password);

            if (userInfo == null) {
                out.write("{\"status\":false,\"statusCode\":\"401\",\"message\":\"Wrong username or password\"}");
            } else {
                HttpSession httpSession = ((HttpServletRequest) servletRequest).getSession();
                httpSession.setAttribute(ZmsContent.LDAP_USER, userInfo);
                out.write("{\"status\":true}");
            }
            out.flush();
            return;
        }

        PrintWriter out;
        HttpSession httpSession = ((HttpServletRequest) servletRequest).getSession();
        LdapUser userInfo = (LdapUser) httpSession.getAttribute(ZmsContent.LDAP_USER);

        if (userInfo == null) {
            out = servletResponse.getWriter();
            out.write("{\"status\":false,\"statusCode\":\"1000\",\"message\":\"unauthorized\"}");
            out.flush();
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private LdapUser getUser(String userName, String password) {
        return userService.authenticate(userName, password);
    }

    private boolean isStaticResource(String uri) {
        return !Strings.isNullOrEmpty(uri) && uri.matches(STATIC_RESOURCE_REGEX);
    }

    private boolean isAlertResource(String uri) {
        return !Strings.isNullOrEmpty(uri) && uri.matches(ALERT_RESOURCE_REGEX);
    }

    private boolean isAgentResource(String uri) {
        return !Strings.isNullOrEmpty(uri) && uri.matches(AGENT_RESOURCE_REGEX);
    }

    private boolean isBackupClusterResource(String uri) {
        return !Strings.isNullOrEmpty(uri) && uri.matches(BACKUP_CLUSTER_REGEX);
    }
}


