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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@EnableLdapRepositories
public class LdapSource {
    @Autowired
    private LdapConfig ldapConfig;

    @Bean
    LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(null == ldapConfig.getUrl() ? "" : ldapConfig.getUrl());
        contextSource.setBase(null == ldapConfig.getBase() ? "" : ldapConfig.getBase());
        contextSource.setUserDn(null == ldapConfig.getUserDn() ? "" : ldapConfig.getUserDn());
        contextSource.setPassword(null == ldapConfig.getPassword() ? "" : ldapConfig.getPassword());
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean
    LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        ldapTemplate.setIgnorePartialResultException(false);
        return ldapTemplate;
    }
}

