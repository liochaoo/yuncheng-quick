package com.yuncheng.system.log.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** 系统日志异步写入线程池。 */
@Configuration(proxyBeanMethods = false)
public class SystemLogAsyncConfiguration {

    public static final String TASK_EXECUTOR_BEAN_NAME = "systemLogTaskExecutor";

    @Bean(TASK_EXECUTOR_BEAN_NAME)
    public TaskExecutor systemLogTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("system-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.initialize();
        return executor;
    }
}
