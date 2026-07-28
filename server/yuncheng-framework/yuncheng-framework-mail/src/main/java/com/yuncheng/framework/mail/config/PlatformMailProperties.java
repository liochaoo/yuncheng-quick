package com.yuncheng.framework.mail.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 平台发件配置。 */
@ConfigurationProperties(prefix = "platform.mail")
public class PlatformMailProperties {

    private boolean mockEnabled = true;
    private String fromAddress = "";
    private String fromName = "平台";

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public void setMockEnabled(boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
}
