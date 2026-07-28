package com.yuncheng.system.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.system.log.constant.LogCleanupConstants;
import com.yuncheng.system.log.entity.SystemLoginLog;
import com.yuncheng.system.log.entity.SystemOperationLog;
import com.yuncheng.system.log.mapper.SystemLoginLogMapper;
import com.yuncheng.system.log.mapper.SystemOperationLogMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 在独立事务中清理一批系统日志。 */
@Service
public class SystemLogCleanBatchService {

    private final SystemLoginLogMapper loginLogMapper;
    private final SystemOperationLogMapper operationLogMapper;

    public SystemLogCleanBatchService(
            SystemLoginLogMapper loginLogMapper,
            SystemOperationLogMapper operationLogMapper
    ) {
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
    }

    @Transactional
    public int cleanLogin(Instant before) {
        List<Long> ids = loginLogMapper.selectList(
                new LambdaQueryWrapper<SystemLoginLog>()
                        .select(SystemLoginLog::getId)
                        .lt(SystemLoginLog::getOccurredAt, before)
                        .orderByAsc(SystemLoginLog::getId)
                        .last("LIMIT " + LogCleanupConstants.CLEAN_BATCH_SIZE)
        ).stream().map(SystemLoginLog::getId).toList();
        return ids.isEmpty() ? 0 : loginLogMapper.deleteByIds(ids);
    }

    @Transactional
    public int cleanOperation(Instant before) {
        List<Long> ids = operationLogMapper.selectList(
                new LambdaQueryWrapper<SystemOperationLog>()
                        .select(SystemOperationLog::getId)
                        .lt(SystemOperationLog::getOccurredAt, before)
                        .orderByAsc(SystemOperationLog::getId)
                        .last("LIMIT " + LogCleanupConstants.CLEAN_BATCH_SIZE)
        ).stream().map(SystemOperationLog::getId).toList();
        return ids.isEmpty() ? 0 : operationLogMapper.deleteByIds(ids);
    }
}
