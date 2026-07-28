package com.yuncheng.system.login.auth.service;

import com.yuncheng.framework.log.LoginEventType;
import com.yuncheng.framework.log.LoginLogApi;
import com.yuncheng.framework.log.command.LoginLogCommand;
import com.yuncheng.framework.web.client.ClientRequestInfo;
import com.yuncheng.framework.web.constant.WebConstants;
import java.time.Instant;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/** 在认证请求线程中组装并提交登录日志。 */
@Service
public class LoginLogRecorder {

    private static final int MAX_FAILURE_REASON_LENGTH = 500;

    private final LoginLogApi loginLogApi;

    public LoginLogRecorder(LoginLogApi loginLogApi) {
        this.loginLogApi = loginLogApi;
    }

    public void loginSuccess(
            Long userId,
            String loginName,
            String realName,
            String clientType,
            String sessionId,
            ClientRequestInfo requestInfo
    ) {
        record(
                LoginEventType.LOGIN,
                true,
                userId,
                loginName,
                realName,
                clientType,
                sessionId,
                requestInfo,
                null
        );
    }

    public void loginFailure(
            String loginName,
            String clientType,
            ClientRequestInfo requestInfo,
            String reason
    ) {
        record(
                LoginEventType.LOGIN,
                false,
                null,
                loginName,
                null,
                clientType,
                null,
                requestInfo,
                reason
        );
    }

    public void logout(
            Long userId,
            String loginName,
            String realName,
            String clientType,
            String sessionId,
            ClientRequestInfo requestInfo
    ) {
        record(
                LoginEventType.LOGOUT,
                true,
                userId,
                loginName,
                realName,
                clientType,
                sessionId,
                requestInfo,
                null
        );
    }

    private void record(
            LoginEventType eventType,
            boolean success,
            Long userId,
            String loginName,
            String realName,
            String clientType,
            String sessionId,
            ClientRequestInfo requestInfo,
            String failureReason
    ) {
        loginLogApi.record(new LoginLogCommand(
                eventType,
                success,
                userId,
                loginName,
                realName,
                clientType,
                sessionId,
                requestInfo == null ? null : requestInfo.ip(),
                requestInfo == null ? null : requestInfo.userAgent(),
                truncate(failureReason),
                MDC.get(WebConstants.TRACE_ID_MDC_KEY),
                Instant.now()
        ));
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_FAILURE_REASON_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_FAILURE_REASON_LENGTH);
    }
}
