package com.yuncheng.init.system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 首次初始化管理员配置。 */
@ConfigurationProperties(prefix = "platform.init.admin")
public class AdminInitializationProperties {

    private String username = "admin";
    private String password = "";
    private String realName = "管理员";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
}
