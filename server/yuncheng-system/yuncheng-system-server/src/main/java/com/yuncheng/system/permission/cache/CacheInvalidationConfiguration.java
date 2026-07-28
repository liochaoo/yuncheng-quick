package com.yuncheng.system.permission.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** 可重建访问缓存的异步失效线程池。 */
@Configuration(proxyBeanMethods = false)
public class CacheInvalidationConfiguration {

    public static final String EXECUTOR_NAME = "cacheInvalidationTaskExecutor";

    @Bean(name = EXECUTOR_NAME)
    ThreadPoolTaskExecutor cacheInvalidationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(256);
        executor.setThreadNamePrefix("cache-invalidation-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        return executor;
    }
}
