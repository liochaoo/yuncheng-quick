package com.yuncheng.framework.log;

import com.yuncheng.framework.log.command.LoginLogCommand;

/** 提交登录日志的技术组件接口。 */
public interface LoginLogApi {

    void record(LoginLogCommand command);
}
