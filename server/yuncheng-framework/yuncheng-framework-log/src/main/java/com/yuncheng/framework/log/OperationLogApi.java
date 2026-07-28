package com.yuncheng.framework.log;

import com.yuncheng.framework.log.command.OperationLogCommand;

/** 提交操作日志的技术组件接口。 */
public interface OperationLogApi {

    void record(OperationLogCommand command);
}
