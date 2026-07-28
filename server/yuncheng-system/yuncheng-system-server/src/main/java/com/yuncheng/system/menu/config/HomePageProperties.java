package com.yuncheng.system.menu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 平台默认首页配置。 */
@ConfigurationProperties(prefix = "platform.web")
public class HomePageProperties {

    private String homePath = "/workspace";

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }
}
