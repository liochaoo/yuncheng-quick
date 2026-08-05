package com.yuncheng.system.login.auth.controller;

import com.yuncheng.framework.web.client.ClientRequestInfo;
import com.yuncheng.framework.web.client.ClientRequestInfoResolver;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.system.login.auth.dto.AuthenticatedTokens;
import com.yuncheng.system.login.auth.dto.LoginAuthenticationResult;
import com.yuncheng.system.login.auth.dto.LoginRequest;
import com.yuncheng.system.login.auth.dto.LoginResponse;
import com.yuncheng.system.login.auth.dto.RequiredPasswordChangeRequest;
import com.yuncheng.system.login.auth.dto.TokenResponse;
import com.yuncheng.system.login.auth.enums.ClientType;
import com.yuncheng.system.login.auth.service.AuthenticationService;
import com.yuncheng.system.login.auth.service.LoginLogRecorder;
import com.yuncheng.system.login.auth.service.RequiredPasswordChangeService;
import com.yuncheng.system.login.auth.support.RefreshCookieManager;
import com.yuncheng.system.login.security.service.LoginSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 登录、刷新和退出接口。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/auth")
@Tag(name = "Web 登录认证")
public class AuthController {

    private static final Set<ClientType> CLIENT_TYPES = Set.of(ClientType.WEB);

    private final AuthenticationService authenticationService;
    private final LoginSecurityService securityService;
    private final RefreshCookieManager cookieManager;
    private final ClientRequestInfoResolver requestInfoResolver;
    private final LoginLogRecorder loginLogRecorder;
    private final RequiredPasswordChangeService requiredPasswordChangeService;

    public AuthController(
            AuthenticationService authenticationService,
            LoginSecurityService securityService,
            RefreshCookieManager cookieManager,
            ClientRequestInfoResolver requestInfoResolver,
            LoginLogRecorder loginLogRecorder,
            RequiredPasswordChangeService requiredPasswordChangeService
    ) {
        this.authenticationService = authenticationService;
        this.securityService = securityService;
        this.cookieManager = cookieManager;
        this.requestInfoResolver = requestInfoResolver;
        this.loginLogRecorder = loginLogRecorder;
        this.requiredPasswordChangeService = requiredPasswordChangeService;
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse response
    ) {
        ClientRequestInfo requestInfo = requestInfoResolver.resolve(servletRequest);
        try {
            securityService.verifyLogin(request.captchaVerification());
        } catch (RuntimeException exception) {
            loginLogRecorder.loginFailure(
                    request.username(),
                    request.clientType() == null ? null : request.clientType().name(),
                    requestInfo,
                    exception.getMessage()
            );
            throw exception;
        }
        LoginAuthenticationResult result = authenticationService.login(request, CLIENT_TYPES, requestInfo);
        if (result.requiresPasswordChange()) {
            cookieManager.clear(response);
            return ApiResponse.success(new LoginResponse(null, true, result.passwordChangeToken()));
        }
        AuthenticatedTokens tokens = result.tokens();
        cookieManager.write(response, tokens.refreshToken(), tokens.sessionExpiresAt());
        return ApiResponse.success(new LoginResponse(tokens.accessToken(), false, null));
    }

    @PostMapping("/password/change-required")
    @Operation(summary = "完成登录前强制密码修改")
    public ApiResponse<Void> changeRequiredPassword(
            @Valid @RequestBody RequiredPasswordChangeRequest request,
            HttpServletResponse response
    ) {
        cookieManager.clear(response);
        requiredPasswordChangeService.change(request.passwordChangeToken(), request.newPassword());
        return ApiResponse.success(null);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新访问令牌")
    public ApiResponse<TokenResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        AuthenticatedTokens tokens = authenticationService.refresh(cookieManager.read(request), CLIENT_TYPES);
        cookieManager.write(response, tokens.refreshToken(), tokens.sessionExpiresAt());
        return ApiResponse.success(new TokenResponse(tokens.accessToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public ApiResponse<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            authenticationService.logout(
                    cookieManager.read(request),
                    CLIENT_TYPES,
                    requestInfoResolver.resolve(request)
            );
        } finally {
            cookieManager.clear(response);
        }
        return ApiResponse.success(null);
    }
}
