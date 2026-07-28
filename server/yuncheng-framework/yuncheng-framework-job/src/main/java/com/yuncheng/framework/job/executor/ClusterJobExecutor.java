package com.yuncheng.framework.job.executor;

import com.yuncheng.common.constant.OperatorConstants;
import com.yuncheng.framework.job.annotation.PlatformJob;
import com.yuncheng.framework.job.context.JobContext;
import com.yuncheng.framework.job.contract.ClusterJob;
import com.yuncheng.framework.job.lock.RedisJobLock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/** 统一执行任务锁、Trace ID和异常隔离。 */
@Component
public class ClusterJobExecutor {

    private static final Logger log = LoggerFactory.getLogger(ClusterJobExecutor.class);
    private static final String TRACE_ID_MDC_KEY = "traceId";

    private final RedisJobLock jobLock;

    public ClusterJobExecutor(RedisJobLock jobLock) {
        this.jobLock = jobLock;
    }

    public void execute(ClusterJob job, PlatformJob definition) {
        String token;
        try {
            token = jobLock.tryLock(
                    definition.name(),
                    Duration.ofSeconds(definition.lockAtMostForSeconds())
            );
        } catch (RuntimeException exception) {
            log.error("定时任务获取 Redis 锁失败，已跳过本次执行，job={}", definition.name(), exception);
            return;
        }
        if (token == null) {
            log.debug("定时任务未取得 Redis 锁，已跳过本次执行，job={}", definition.name());
            return;
        }
        String traceId = UUID.randomUUID().toString().replace("-", "");
        long startedAtNanos = System.nanoTime();
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        try {
            job.execute(new JobContext(
                    definition.name(),
                    traceId,
                    OperatorConstants.SYSTEM_OPERATOR_ID,
                    Instant.now()
            ));
            long durationMillis = elapsedMillis(startedAtNanos);
            log.info("定时任务执行完成，job={}，durationMillis={}", definition.name(), durationMillis);
        } catch (RuntimeException exception) {
            log.error(
                    "定时任务执行失败，job={}，durationMillis={}",
                    definition.name(),
                    elapsedMillis(startedAtNanos),
                    exception
            );
        } finally {
            warnIfLockMayHaveExpired(definition, elapsedMillis(startedAtNanos));
            try {
                jobLock.unlock(definition.name(), token);
            } catch (RuntimeException exception) {
                log.error("定时任务释放 Redis 锁失败，job={}", definition.name(), exception);
            }
            MDC.remove(TRACE_ID_MDC_KEY);
        }
    }

    private void warnIfLockMayHaveExpired(PlatformJob definition, long durationMillis) {
        if (durationMillis < Duration.ofSeconds(
                definition.lockAtMostForSeconds()
        ).toMillis()) {
            return;
        }
        log.warn(
                "定时任务执行时间达到锁的最长持有时间，可能发生重复执行，job={}，durationMillis={}",
                definition.name(),
                durationMillis
        );
    }

    private long elapsedMillis(long startedAtNanos) {
        return Math.max(0, (System.nanoTime() - startedAtNanos) / 1_000_000);
    }
}
