package com.zto.zms.portal.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springside.modules.utils.net.NetUtil;

import java.net.InetAddress;
import java.text.MessageFormat;

/**
 * <p>Description: </p>
 *
 * @author lidawei
 * @date 2020/5/21
 * @since
 **/
@Configuration
public class CommonConfig {

    @Value("${install.script.url}")
    private String installScriptUrl;

    @Value("${zms.portal.secretKey}")
    private String zmsPortalSecretKey;

    @Value("${zms.portal.url:}")
    private String zmsPortalUrl;

    @Autowired
    private Environment environment;

    public String getPort() {
        return environment.getProperty("local.server.port");
    }

    public String getInstallScriptUrl() {
        return installScriptUrl;
    }

    public void setInstallScriptUrl(String installScriptUrl) {
        this.installScriptUrl = installScriptUrl;
    }

    public String getZmsPortalSecretKey() {
        return zmsPortalSecretKey;
    }

    public void setZmsPortalSecretKey(String zmsPortalSecretKey) {
        this.zmsPortalSecretKey = zmsPortalSecretKey;
    }

    public String getZmsPortalUrl() {
        if (StringUtils.isEmpty(zmsPortalUrl)) {
            InetAddress localAddress = NetUtil.getLocalAddress();
            String ip = localAddress.getHostAddress();
            this.zmsPortalUrl = MessageFormat.format("http://{0}:{1}", ip, environment.getProperty("local.server.port"));
        }
        return zmsPortalUrl;
    }

    public void setZmsPortalUrl(String zmsPortalUrl) {
        this.zmsPortalUrl = zmsPortalUrl;
    }
}
