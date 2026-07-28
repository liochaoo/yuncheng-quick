package com.yuncheng.system.log.service;

import com.yuncheng.framework.log.command.LoginLogCommand;
import com.yuncheng.framework.log.command.OperationLogCommand;
import com.yuncheng.system.log.entity.SystemLoginLog;
import com.yuncheng.system.log.entity.SystemOperationLog;
import com.yuncheng.system.log.mapper.SystemLoginLogMapper;
import com.yuncheng.system.log.mapper.SystemOperationLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 在独立事务中持久化系统日志。 */
@Service
public class LogPersistenceService {

    private final SystemLoginLogMapper loginLogMapper;
    private final SystemOperationLogMapper operationLogMapper;

    public LogPersistenceService(
            SystemLoginLogMapper loginLogMapper,
            SystemOperationLogMapper operationLogMapper
    ) {
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLogin(LoginLogCommand command) {
        SystemLoginLog entity = new SystemLoginLog();
        entity.setEventType(command.eventType().name());
        entity.setSuccess(command.success());
        entity.setUserId(command.userId());
        entity.setLoginName(command.loginName());
        entity.setRealName(command.realName());
        entity.setClientType(command.clientType());
        entity.setSessionId(command.sessionId());
        entity.setIp(command.ip());
        entity.setUserAgent(command.userAgent());
        entity.setFailureReason(command.failureReason());
        entity.setTraceId(command.traceId());
        entity.setOccurredAt(command.occurredAt());
        loginLogMapper.insert(entity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOperation(OperationLogCommand command) {
        SystemOperationLog entity = new SystemOperationLog();
        entity.setAction(command.action());
        entity.setClassName(command.className());
        entity.setMethodName(command.methodName());
        entity.setHttpMethod(command.httpMethod());
        entity.setRequestPath(command.requestPath());
        entity.setRequestParams(command.requestParams());
        entity.setSuccess(command.success());
        entity.setErrorMessage(command.errorMessage());
        entity.setDurationMillis(command.durationMillis());
        entity.setUserId(command.userId());
        entity.setUsername(command.username());
        entity.setRealName(command.realName());
        entity.setIp(command.ip());
        entity.setUserAgent(command.userAgent());
        entity.setTraceId(command.traceId());
        entity.setOccurredAt(command.occurredAt());
        operationLogMapper.insert(entity);
    }
}
