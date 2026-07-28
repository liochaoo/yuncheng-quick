package com.yuncheng.system.log.service;

import com.yuncheng.framework.log.command.LoginLogCommand;
import com.yuncheng.framework.log.command.OperationLogCommand;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.system.log.config.SystemLogAsyncConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

/** 将不可变日志数据提交到专用异步线程池。 */
@Service
public class SystemLogAsyncWriter {

    private static final Logger log = LoggerFactory.getLogger(SystemLogAsyncWriter.class);

    private final TaskExecutor taskExecutor;
    private final LogPersistenceService persistenceService;

    public SystemLogAsyncWriter(
            @Qualifier(SystemLogAsyncConfiguration.TASK_EXECUTOR_BEAN_NAME) TaskExecutor taskExecutor,
            LogPersistenceService persistenceService
    ) {
        this.taskExecutor = taskExecutor;
        this.persistenceService = persistenceService;
    }

    public void writeLogin(LoginLogCommand command) {
        submit(command.traceId(), () -> persistenceService.saveLogin(command));
    }

    public void writeOperation(OperationLogCommand command) {
        submit(command.traceId(), () -> persistenceService.saveOperation(command));
    }

    private void submit(String traceId, Runnable task) {
        try {
            taskExecutor.execute(() -> {
                if (traceId != null && !traceId.isBlank()) {
                    MDC.put(WebConstants.TRACE_ID_MDC_KEY, traceId);
                }
                try {
                    task.run();
                } catch (RuntimeException exception) {
                    log.error("异步写入系统日志失败", exception);
                } finally {
                    MDC.remove(WebConstants.TRACE_ID_MDC_KEY);
                }
            });
        } catch (RuntimeException exception) {
            log.error("系统日志线程池繁忙，本次日志未写入", exception);
        }
    }
}
