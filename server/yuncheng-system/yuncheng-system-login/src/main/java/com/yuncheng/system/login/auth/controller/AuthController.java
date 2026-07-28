package com.yuncheng.system.login.auth.controller;

import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.client.ClientRequestInfo;
import com.yuncheng.framework.web.client.ClientRequestInfoResolver;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.system.login.auth.dto.AuthenticatedTokens;
import com.yuncheng.system.login.auth.dto.LoginRequest;
import com.yuncheng.system.login.auth.dto.TokenResponse;
import com.yuncheng.system.login.auth.enums.ClientType;
import com.yuncheng.system.login.auth.service.AuthenticationService;
import com.yuncheng.system.login.auth.service.LoginLogRecorder;
import com.yuncheng.system.login.auth.support.RefreshCookieManager;
import com.yuncheng.system.login.security.service.LoginSecurityService;
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
public class AuthController {

    private static final Set<ClientType> CLIENT_TYPES = Set.of(ClientType.WEB);

    private final AuthenticationService authenticationService;
    private final LoginSecurityService securityService;
    private final RefreshCookieManager cookieManager;
    private final ClientRequestInfoResolver requestInfoResolver;
    private final LoginLogRecorder loginLogRecorder;

    public AuthController(
            AuthenticationService authenticationService,
            LoginSecurityService securityService,
            RefreshCookieManager cookieManager,
            ClientRequestInfoResolver requestInfoResolver,
            LoginLogRecorder loginLogRecorder
    ) {
        this.authenticationService = authenticationService;
        this.securityService = securityService;
        this.cookieManager = cookieManager;
        this.requestInfoResolver = requestInfoResolver;
        this.loginLogRecorder = loginLogRecorder;
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(
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
        AuthenticatedTokens tokens = authenticationService.login(request, CLIENT_TYPES, requestInfo);
        cookieManager.write(response, tokens.refreshToken(), tokens.sessionExpiresAt());
        return ApiResponse.success(new TokenResponse(tokens.accessToken()));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        AuthenticatedTokens tokens = authenticationService.refresh(cookieManager.read(request), CLIENT_TYPES);
        cookieManager.write(response, tokens.refreshToken(), tokens.sessionExpiresAt());
        return ApiResponse.success(new TokenResponse(tokens.accessToken()));
    }

    @PostMapping("/logout")
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
