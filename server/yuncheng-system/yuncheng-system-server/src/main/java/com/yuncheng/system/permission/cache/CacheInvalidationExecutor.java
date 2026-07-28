package com.yuncheng.system.permission.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Component;

/** 提交并隔离可重建缓存的异步失效任务。 */
@Component
public class CacheInvalidationExecutor {

    private static final Logger log = LoggerFactory.getLogger(CacheInvalidationExecutor.class);

    private final TaskExecutor taskExecutor;

    public CacheInvalidationExecutor(
            @Qualifier(CacheInvalidationConfiguration.EXECUTOR_NAME) TaskExecutor taskExecutor
    ) {
        this.taskExecutor = taskExecutor;
    }

    public void execute(Runnable task) {
        try {
            taskExecutor.execute(() -> runSafely(task));
        } catch (TaskRejectedException exception) {
            log.error("缓存失效任务提交失败，将由缓存 TTL 最终淘汰旧数据", exception);
        }
    }

    private void runSafely(Runnable task) {
        try {
            task.run();
        } catch (RuntimeException exception) {
            log.error("异步缓存失效任务执行失败，将由缓存 TTL 最终淘汰旧数据", exception);
        }
    }
}
