package com.yuncheng.system.login.auth.controller;

import com.yuncheng.framework.web.client.ClientRequestInfoResolver;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.system.login.auth.dto.AuthenticatedTokens;
import com.yuncheng.system.login.auth.dto.LoginAuthenticationResult;
import com.yuncheng.system.login.auth.dto.LoginRequest;
import com.yuncheng.system.login.auth.dto.RefreshTokenRequest;
import com.yuncheng.system.login.auth.dto.TokenPairResponse;
import com.yuncheng.system.login.auth.enums.ClientType;
import com.yuncheng.system.login.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 移动端登录、刷新和退出接口。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/mobile/auth")
@Tag(name = "移动端认证")
public class MobileAuthController {

    private static final Set<ClientType> CLIENT_TYPES = Set.of(
            ClientType.IOS,
            ClientType.ANDROID,
            ClientType.HARMONYOS,
            ClientType.WECHAT_MINI_PROGRAM
    );

    private final AuthenticationService authenticationService;
    private final ClientRequestInfoResolver requestInfoResolver;

    public MobileAuthController(
            AuthenticationService authenticationService,
            ClientRequestInfoResolver requestInfoResolver
    ) {
        this.authenticationService = authenticationService;
        this.requestInfoResolver = requestInfoResolver;
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public ApiResponse<TokenPairResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest
    ) {
        LoginAuthenticationResult result = authenticationService.login(
                request,
                CLIENT_TYPES,
                requestInfoResolver.resolve(servletRequest)
        );
        if (result.requiresPasswordChange()) {
            throw PlatformException.forbidden("请先通过 Web 登录页面修改初始密码");
        }
        return ApiResponse.success(toTokenPair(result.tokens()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新访问令牌")
    public ApiResponse<TokenPairResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthenticatedTokens tokens = authenticationService.refresh(request.refreshToken(), CLIENT_TYPES);
        return ApiResponse.success(toTokenPair(tokens));
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public ApiResponse<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        authenticationService.logout(
                request.refreshToken(),
                CLIENT_TYPES,
                requestInfoResolver.resolve(servletRequest)
        );
        return ApiResponse.success(null);
    }

    private TokenPairResponse toTokenPair(AuthenticatedTokens tokens) {
        return new TokenPairResponse(tokens.accessToken(), tokens.refreshToken());
    }
}
