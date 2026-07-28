package com.yuncheng.system.login.password.controller;

import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.system.login.auth.support.RefreshCookieManager;
import com.yuncheng.system.login.password.dto.PasswordEmailCodeRequest;
import com.yuncheng.system.login.password.dto.PasswordResetRequest;
import com.yuncheng.system.login.password.service.PasswordRecoveryService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 邮箱找回密码接口。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/auth/password")
public class PasswordRecoveryController {

    private final PasswordRecoveryService recoveryService;
    private final RefreshCookieManager cookieManager;

    public PasswordRecoveryController(
            PasswordRecoveryService recoveryService,
            RefreshCookieManager cookieManager
    ) {
        this.recoveryService = recoveryService;
        this.cookieManager = cookieManager;
    }

    @PostMapping("/email-code")
    @OperationLog("申请发送找回密码验证码")
    public ApiResponse<Void> sendEmailCode(
            @Valid @RequestBody PasswordEmailCodeRequest request
    ) {
        recoveryService.sendEmailCode(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/reset")
    @OperationLog("找回密码")
    public ApiResponse<Void> resetPassword(
            @Valid @RequestBody PasswordResetRequest request,
            HttpServletResponse response
    ) {
        recoveryService.resetPassword(request);
        cookieManager.clear(response);
        return ApiResponse.success(null);
    }
}
