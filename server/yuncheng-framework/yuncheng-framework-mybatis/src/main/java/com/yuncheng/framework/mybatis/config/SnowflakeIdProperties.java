package com.yuncheng.framework.mybatis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 雪花 ID 节点配置。 */
@ConfigurationProperties(prefix = "platform.mybatis.id-generator")
public class SnowflakeIdProperties {

    private long datacenterId = 0;
    private long workerId = 0;

    public long getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(long datacenterId) {
        this.datacenterId = datacenterId;
    }

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }
}
