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

import com.zto.zms.service.manager.ZmsContextManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * <p>Class: ZmsWebFilter</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/1/9
 **/
public class ZmsWebFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(ZmsWebFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String envId = request.getParameter("envId");

            if (StringUtils.isBlank(envId)) {
                envId = "0";
            }
            logger.debug("Current environment:{}", envId);
            ZmsContextManager.setEnv(Integer.valueOf(envId));
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }
}

