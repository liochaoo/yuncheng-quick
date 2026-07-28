package com.yuncheng.framework.job.registrar;

import com.yuncheng.framework.job.annotation.PlatformJob;
import com.yuncheng.framework.job.contract.ClusterJob;
import com.yuncheng.framework.job.executor.ClusterJobExecutor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

/** 发现并注册所有实现统一契约的定时任务。 */
@Component
public class PlatformJobRegistrar implements SmartInitializingSingleton {

    private final List<ClusterJob> jobs;
    private final TaskScheduler taskScheduler;
    private final ClusterJobExecutor executor;

    public PlatformJobRegistrar(
            List<ClusterJob> jobs,
            TaskScheduler taskScheduler,
            ClusterJobExecutor executor
    ) {
        this.jobs = jobs;
        this.taskScheduler = taskScheduler;
        this.executor = executor;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Set<String> registeredNames = new HashSet<>();
        for (ClusterJob job : jobs) {
            Class<?> jobClass = AopUtils.getTargetClass(job);
            PlatformJob definition = AnnotatedElementUtils.findMergedAnnotation(
                    jobClass,
                    PlatformJob.class
            );
            if (definition == null) {
                throw new IllegalStateException("定时任务必须声明 @PlatformJob：" + jobClass.getName());
            }
            if (definition.name().isBlank()) {
                throw new IllegalStateException("定时任务名称不能为空：" + jobClass.getName());
            }
            if (definition.lockAtMostForSeconds() <= 0) {
                throw new IllegalStateException("定时任务锁时长必须大于 0：" + definition.name());
            }
            if (!registeredNames.add(definition.name())) {
                throw new IllegalStateException("定时任务名称重复：" + definition.name());
            }
            taskScheduler.schedule(
                    () -> executor.execute(job, definition),
                    new CronTrigger(definition.cron())
            );
        }
    }
}
