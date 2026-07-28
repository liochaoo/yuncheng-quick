package com.yuncheng.system.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.system.log.dto.LoginLogItem;
import com.yuncheng.system.log.dto.LoginLogPageQuery;
import com.yuncheng.system.log.dto.OperationLogItem;
import com.yuncheng.system.log.dto.OperationLogPageQuery;
import com.yuncheng.system.log.entity.SystemLoginLog;
import com.yuncheng.system.log.entity.SystemOperationLog;
import com.yuncheng.system.log.mapper.SystemLoginLogMapper;
import com.yuncheng.system.log.mapper.SystemOperationLogMapper;
import java.util.List;
import org.springframework.stereotype.Service;

/** 查询登录日志和操作日志。 */
@Service
public class SystemLogQueryService {

    private final SystemLoginLogMapper loginLogMapper;
    private final SystemOperationLogMapper operationLogMapper;

    public SystemLogQueryService(
            SystemLoginLogMapper loginLogMapper,
            SystemOperationLogMapper operationLogMapper
    ) {
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
    }

    public PageResult<LoginLogItem> loginPage(LoginLogPageQuery query) {
        Page<SystemLoginLog> page = loginLogMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()),
                loginWrapper(query)
        );
        List<LoginLogItem> items = page.getRecords().stream().map(this::toLoginItem).toList();
        return new PageResult<>(items, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public LoginLogItem loginDetail(Long id) {
        SystemLoginLog entity = loginLogMapper.selectById(id);
        if (entity == null) {
            throw PlatformException.notFound("登录日志不存在");
        }
        return toLoginItem(entity);
    }

    public PageResult<OperationLogItem> operationPage(OperationLogPageQuery query) {
        Page<SystemOperationLog> page = operationLogMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()),
                operationWrapper(query)
        );
        List<OperationLogItem> items = page.getRecords().stream().map(this::toOperationItem).toList();
        return new PageResult<>(items, page.getTotal(), query.getPage(), query.getPageSize());
    }

    public OperationLogItem operationDetail(Long id) {
        SystemOperationLog entity = operationLogMapper.selectById(id);
        if (entity == null) {
            throw PlatformException.notFound("操作日志不存在");
        }
        return toOperationItem(entity);
    }

    private LambdaQueryWrapper<SystemLoginLog> loginWrapper(LoginLogPageQuery query) {
        return new LambdaQueryWrapper<SystemLoginLog>()
                .like(hasText(query.getLoginName()), SystemLoginLog::getLoginName, trim(query.getLoginName()))
                .eq(hasText(query.getEventType()), SystemLoginLog::getEventType, trim(query.getEventType()))
                .eq(query.getSuccess() != null, SystemLoginLog::getSuccess, query.getSuccess())
                .eq(hasText(query.getClientType()), SystemLoginLog::getClientType, trim(query.getClientType()))
                .eq(hasText(query.getTraceId()), SystemLoginLog::getTraceId, trim(query.getTraceId()))
                .orderByDesc(SystemLoginLog::getOccurredAt)
                .orderByDesc(SystemLoginLog::getId);
    }

    private LambdaQueryWrapper<SystemOperationLog> operationWrapper(OperationLogPageQuery query) {
        return new LambdaQueryWrapper<SystemOperationLog>()
                .like(hasText(query.getAction()), SystemOperationLog::getAction, trim(query.getAction()))
                .like(hasText(query.getUsername()), SystemOperationLog::getUsername, trim(query.getUsername()))
                .like(hasText(query.getRequestPath()), SystemOperationLog::getRequestPath, trim(query.getRequestPath()))
                .eq(query.getSuccess() != null, SystemOperationLog::getSuccess, query.getSuccess())
                .eq(hasText(query.getTraceId()), SystemOperationLog::getTraceId, trim(query.getTraceId()))
                .orderByDesc(SystemOperationLog::getOccurredAt)
                .orderByDesc(SystemOperationLog::getId);
    }

    private LoginLogItem toLoginItem(SystemLoginLog entity) {
        return new LoginLogItem(
                entity.getId().toString(),
                entity.getEventType(),
                Boolean.TRUE.equals(entity.getSuccess()),
                entity.getUserId() == null ? null : entity.getUserId().toString(),
                entity.getLoginName(),
                entity.getRealName(),
                entity.getClientType(),
                entity.getSessionId(),
                entity.getIp(),
                entity.getUserAgent(),
                entity.getFailureReason(),
                entity.getTraceId(),
                entity.getOccurredAt()
        );
    }

    private OperationLogItem toOperationItem(SystemOperationLog entity) {
        return new OperationLogItem(
                entity.getId().toString(),
                entity.getAction(),
                entity.getClassName(),
                entity.getMethodName(),
                entity.getHttpMethod(),
                entity.getRequestPath(),
                entity.getRequestParams(),
                Boolean.TRUE.equals(entity.getSuccess()),
                entity.getErrorMessage(),
                entity.getDurationMillis() == null ? 0 : entity.getDurationMillis(),
                entity.getUserId() == null ? null : entity.getUserId().toString(),
                entity.getUsername(),
                entity.getRealName(),
                entity.getIp(),
                entity.getUserAgent(),
                entity.getTraceId(),
                entity.getOccurredAt()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
