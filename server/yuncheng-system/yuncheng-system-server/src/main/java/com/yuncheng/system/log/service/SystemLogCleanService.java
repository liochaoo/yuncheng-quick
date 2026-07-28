package com.yuncheng.system.log.service;

import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.log.config.SystemLogCleanupProperties;
import com.yuncheng.system.log.constant.LogCleanupConstants;
import com.yuncheng.system.log.dto.LogCleanPolicy;
import com.yuncheng.system.log.dto.LogType;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Service;

/** 分批清理到期或指定时间以前的系统日志。 */
@Service
public class SystemLogCleanService {

    private final SystemLogCleanBatchService cleanBatchService;
    private final SystemLogCleanupProperties cleanupProperties;

    public SystemLogCleanService(
            SystemLogCleanBatchService cleanBatchService,
            SystemLogCleanupProperties cleanupProperties
    ) {
        this.cleanBatchService = cleanBatchService;
        this.cleanupProperties = cleanupProperties;
    }

    public long clean(LogType type, Instant before) {
        requireExpired(before);
        return switch (type) {
            case LOGIN -> cleanLogin(before);
            case OPERATION -> cleanOperation(before);
        };
    }

    public long cleanExpired() {
        Instant before = latestCleanableBefore();
        return cleanLogin(before) + cleanOperation(before);
    }

    public LogCleanPolicy policy() {
        return new LogCleanPolicy(
                cleanupProperties.getRetentionDays(),
                latestCleanableBefore()
        );
    }

    private Instant latestCleanableBefore() {
        return Instant.now().minus(Duration.ofDays(cleanupProperties.getRetentionDays()));
    }

    private void requireExpired(Instant before) {
        if (before.isAfter(latestCleanableBefore())) {
            throw PlatformException.badRequest(
                    "只能清理 " + cleanupProperties.getRetentionDays() + " 天以前的日志"
            );
        }
    }

    private long cleanLogin(Instant before) {
        long total = 0;
        while (true) {
            int count = cleanBatchService.cleanLogin(before);
            total += count;
            if (count < LogCleanupConstants.CLEAN_BATCH_SIZE) {
                return total;
            }
        }
    }

    private long cleanOperation(Instant before) {
        long total = 0;
        while (true) {
            int count = cleanBatchService.cleanOperation(before);
            total += count;
            if (count < LogCleanupConstants.CLEAN_BATCH_SIZE) {
                return total;
            }
        }
    }
}
