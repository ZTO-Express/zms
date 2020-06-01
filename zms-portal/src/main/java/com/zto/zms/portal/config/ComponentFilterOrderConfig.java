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

package com.zto.zms.portal.config;

import com.zto.zms.portal.filter.AuthFilter;
import com.zto.zms.portal.filter.ZmsWebFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>Class: ComponentFilterOrderConfig</p>
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/1/9
 **/

@Configuration
public class ComponentFilterOrderConfig {

    @Bean
    public FilterRegistrationBean<AuthFilter> authenticationFilter(@Autowired AuthFilter authFilter) {
        FilterRegistrationBean<AuthFilter> authenticationFilter = new FilterRegistrationBean<>();
        authenticationFilter.addUrlPatterns("/api/*");
        authenticationFilter.setOrder(1);
        authenticationFilter.setFilter(authFilter);
        return authenticationFilter;
    }

    @Bean
    public FilterRegistrationBean<ZmsWebFilter> zmsEnvironmentFilter() {
        FilterRegistrationBean<ZmsWebFilter> zmsEnvironmentFilter = new FilterRegistrationBean<>();
        zmsEnvironmentFilter.addUrlPatterns("/api/*");
        zmsEnvironmentFilter.setOrder(2);
        zmsEnvironmentFilter.setEnabled(true);
        zmsEnvironmentFilter.setFilter(new ZmsWebFilter());
        return zmsEnvironmentFilter;
    }
}

