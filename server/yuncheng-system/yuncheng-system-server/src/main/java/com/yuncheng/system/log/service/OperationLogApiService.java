package com.yuncheng.system.log.service;

import com.yuncheng.framework.log.OperationLogApi;
import com.yuncheng.framework.log.command.OperationLogCommand;
import com.yuncheng.framework.mybatis.transaction.AfterCommitExecutor;
import org.springframework.stereotype.Service;

/** 系统操作日志对外接口实现。 */
@Service
public class OperationLogApiService implements OperationLogApi {

    private final AfterCommitExecutor afterCommitExecutor;
    private final SystemLogAsyncWriter asyncWriter;

    public OperationLogApiService(
            AfterCommitExecutor afterCommitExecutor,
            SystemLogAsyncWriter asyncWriter
    ) {
        this.afterCommitExecutor = afterCommitExecutor;
        this.asyncWriter = asyncWriter;
    }

    @Override
    public void record(OperationLogCommand command) {
        if (command.success()) {
            afterCommitExecutor.execute(() -> asyncWriter.writeOperation(command));
            return;
        }
        asyncWriter.writeOperation(command);
    }
}
