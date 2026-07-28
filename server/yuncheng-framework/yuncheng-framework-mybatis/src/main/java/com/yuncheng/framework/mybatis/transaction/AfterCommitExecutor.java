package com.yuncheng.framework.mybatis.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 在当前数据库事务成功提交后执行非数据库操作。 */
@Component
public class AfterCommitExecutor {

    private static final Logger log = LoggerFactory.getLogger(AfterCommitExecutor.class);

    public void execute(Runnable task) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            runSafely(task);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                runSafely(task);
            }
        });
    }

    private void runSafely(Runnable task) {
        try {
            task.run();
        } catch (RuntimeException exception) {
            log.error("事务提交后的操作执行失败", exception);
        }
    }
}
