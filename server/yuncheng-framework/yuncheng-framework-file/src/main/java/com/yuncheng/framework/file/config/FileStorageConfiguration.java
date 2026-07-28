package com.yuncheng.framework.file.config;

import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储组件配置。
 */
@EnableFileStorage
@Configuration(proxyBeanMethods = false)
public class FileStorageConfiguration {
}
