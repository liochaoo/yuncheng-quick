package com.yuncheng.framework.mybatis.config;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 配置平台统一使用的雪花 ID 生成器。 */
@Configuration(proxyBeanMethods = false)
public class SnowflakeIdConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SnowflakeIdConfiguration.class);
    private static final long MIN_NODE_ID = 0;
    private static final long MAX_NODE_ID = 31;

    @Bean
    public IdentifierGenerator identifierGenerator(SnowflakeIdProperties properties) {
        validate("数据中心编号", properties.getDatacenterId());
        validate("工作节点编号", properties.getWorkerId());
        log.info(
                "雪花 ID 生成器初始化完成，数据中心编号：{}，工作节点编号：{}",
                properties.getDatacenterId(),
                properties.getWorkerId()
        );
        return new DefaultIdentifierGenerator(
                properties.getWorkerId(),
                properties.getDatacenterId()
        );
    }

    private void validate(String name, long value) {
        if (value < MIN_NODE_ID || value > MAX_NODE_ID) {
            String message = name + "必须在 " + MIN_NODE_ID + "～" + MAX_NODE_ID + " 之间";
            log.error("雪花 ID 配置错误：{}，当前值：{}", message, value);
            throw new IllegalStateException(message);
        }
    }
}
