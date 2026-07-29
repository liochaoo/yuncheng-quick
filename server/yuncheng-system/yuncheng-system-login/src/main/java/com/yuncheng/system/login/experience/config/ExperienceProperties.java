package com.yuncheng.system.login.experience.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 体验环境运行配置。 */
@ConfigurationProperties(prefix = "platform.experience")
public class ExperienceProperties {

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
