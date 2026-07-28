package com.yuncheng.framework.job.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/** 定时任务基础设施配置。 */
@Configuration(proxyBeanMethods = false)
public class JobConfiguration {

    @Bean("springApplicationName")
    String springApplicationName(Environment environment) {
        return environment.getProperty("spring.application.name", "application");
    }

    @Bean
    @ConditionalOnMissingBean(TaskScheduler.class)
    TaskScheduler platformJobTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("platform-job-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        return scheduler;
    }
}
