package com.yuncheng.system.log.service;

import com.yuncheng.framework.log.LoginLogApi;
import com.yuncheng.framework.log.command.LoginLogCommand;
import org.springframework.stereotype.Service;

/** 系统登录日志对外接口实现。 */
@Service
public class LoginLogApiService implements LoginLogApi {

    private final SystemLogAsyncWriter asyncWriter;

    public LoginLogApiService(SystemLogAsyncWriter asyncWriter) {
        this.asyncWriter = asyncWriter;
    }

    @Override
    public void record(LoginLogCommand command) {
        asyncWriter.writeLogin(command);
    }
}
