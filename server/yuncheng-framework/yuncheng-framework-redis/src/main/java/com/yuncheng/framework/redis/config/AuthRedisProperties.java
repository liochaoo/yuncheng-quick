package com.yuncheng.framework.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 认证相关 Redis Key 配置。 */
@ConfigurationProperties(prefix = "platform.auth.redis")
public class AuthRedisProperties {

    private String keyPrefix = "yuncheng-quick";

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String runtimeKey(String suffix) {
        return keyPrefix + ":runtime:auth:" + suffix;
    }

    public String cacheKey(String suffix) {
        return keyPrefix + ":cache:auth:" + suffix;
    }
}
