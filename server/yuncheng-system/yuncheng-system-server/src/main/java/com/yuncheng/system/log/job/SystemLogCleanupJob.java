package com.yuncheng.system.log.job;

import com.yuncheng.framework.job.annotation.PlatformJob;
import com.yuncheng.framework.job.context.JobContext;
import com.yuncheng.framework.job.contract.ClusterJob;
import com.yuncheng.system.log.service.SystemLogCleanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 每日清理超过保留期限的系统日志。 */
@Component
@ConditionalOnProperty(
        prefix = "platform.log.cleanup",
        name = "auto-enabled",
        havingValue = "true",
        matchIfMissing = true
)
@PlatformJob(name = "system-log-cleanup", cron = "0 30 2 * * *", lockAtMostForSeconds = 1800)
public class SystemLogCleanupJob implements ClusterJob {

    private static final Logger log = LoggerFactory.getLogger(SystemLogCleanupJob.class);

    private final SystemLogCleanService cleanService;

    public SystemLogCleanupJob(SystemLogCleanService cleanService) {
        this.cleanService = cleanService;
    }

    @Override
    public void execute(JobContext context) {
        long deletedCount = cleanService.cleanExpired();
        log.info("系统日志到期清理完成，删除数量={}", deletedCount);
    }
}
