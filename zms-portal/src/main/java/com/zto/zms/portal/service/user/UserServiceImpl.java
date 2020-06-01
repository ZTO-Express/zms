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

package com.zto.zms.portal.service.user;

import com.google.common.collect.Lists;
import com.zto.zms.portal.common.ZmsContent;
import com.zto.zms.portal.config.LdapConfig;
import com.zto.zms.portal.config.LdapMappingConfig;
import com.zto.zms.portal.filter.LdapUser;
import com.zto.zms.service.config.ZmsConf;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * <p> Description: </p>
 *
 * @author lidawei9103@zto.com
 * @version 1.0
 * @date 2019/11/6
 */
@Service
public class UserServiceImpl implements UserService, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private LdapMappingConfig ldapMapping;
    @Autowired
    private LdapTemplate ldapTemplate;
    @Autowired
    private LdapConfig ldapConfig;
    @Autowired
    private ZmsConf zmsConf;

    private static List<LdapUser> all;

    @Override
    public List<LdapUser> findAll() {
        if (all == null) {
            synchronized (this) {
                if (all == null) {
                    if (StringUtils.isNotEmpty(ldapConfig.getUrl())) {
                        all = listUser("*");
                        new Thread(this).start();
                    } else {
                        all = getAdminUser();
                    }
                }
            }
        }
        return all;
    }

    private List<LdapUser> getAdminUser() {
        List<LdapUser> userList = Lists.newArrayList();
        if (null == zmsConf.getAdminUser()) {
            return userList;
        }
        for (String adminUser : zmsConf.getAdminUser().split(",")) {
            String[] userPass = adminUser.split(":");
            LdapUser localUser = new LdapUser();
            localUser.setRealName(userPass[0]);
            userList.add(localUser);
        }
        return userList;
    }

    @Override
    public LdapUser getCurrentUser() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attrs != null;
        HttpServletRequest request = attrs.getRequest();
        HttpSession session = request.getSession();
        return (LdapUser) session.getAttribute(ZmsContent.LDAP_USER);
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                logger.error("+++ thread sleep is exception", e);
            }
            try {
                all = listUser("*");
            } catch (Exception e) {
                logger.error("+++ get user info from ldap is exception" + e);
            }
        }
    }

    @Override
    public LdapUser authenticate(String username, String password) {
        // 判断配置文件是否配置当前用户
        if (zmsConf.isAdminUser(username, password)) {
            LdapUser localUser = new LdapUser();
            localUser.setRealName(username);
            localUser.setUserId(username);
            localUser.setAdmin(true);
            return localUser;
        }
        if (StringUtils.isNotEmpty(ldapConfig.getUrl())) {
            String filter = searchFilter(username);
            boolean b = ldapTemplate.authenticate("", filter, password);
            if (b) {
                return findByUserId(username);
            }
        }
        return null;
    }

    public LdapUser findByUserId(String username) {
        List<LdapUser> users = listUser(username);
        return CollectionUtils.isEmpty(users) ? null : users.get(0);
    }

    private AttributesMapper<LdapUser> userMapper = attrs -> {
        LdapUser user = new LdapUser();
        if (attrs.get(ldapMapping.getRealName()) != null) {
            user.setRealName(String.valueOf(attrs.get(ldapMapping.getRealName()).get()));
        }
        if (attrs.get(ldapMapping.getEmail()) != null) {
            user.setEmail(String.valueOf(attrs.get(ldapMapping.getEmail()).get()));
        }
        if (attrs.get(ldapMapping.getMobile()) != null) {
            user.setMobile(String.valueOf(attrs.get(ldapMapping.getMobile()).get()));
        }
        if (attrs.get(ldapMapping.getUserId()) != null) {
            user.setUserId(String.valueOf(attrs.get(ldapMapping.getUserId()).get()));
        }
        boolean isAdmin = zmsConf.isAdminUser(user.getUserId());
        user.setAdmin(isAdmin);
        return user;
    };

    private String searchFilter(String username) {
        return ldapConfig.getSearchFilter().replace("{user}", username);
    }

    private List<LdapUser> listUser(String username) {
        return ldapTemplate.search(
                LdapQueryBuilder.query()
                        .attributes(ldapMapping.getEmail(), ldapMapping.getRealName(), ldapMapping.getUserId(), ldapMapping.getMobile())
                        .filter(searchFilter(username)), userMapper);
    }
}

