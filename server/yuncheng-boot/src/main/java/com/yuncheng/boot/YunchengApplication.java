package com.yuncheng.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** 平台服务端启动入口。 */
@SpringBootApplication(scanBasePackages = "com.yuncheng")
@ConfigurationPropertiesScan(basePackages = "com.yuncheng")
public class YunchengApplication {

    public static void main(String[] args) {
        SpringApplication.run(YunchengApplication.class, args);
    }
}
