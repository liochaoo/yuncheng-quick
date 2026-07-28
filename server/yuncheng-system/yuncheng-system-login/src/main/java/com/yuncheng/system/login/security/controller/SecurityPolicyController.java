package com.yuncheng.system.login.security.controller;

import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.system.login.security.dto.SecurityPolicyResponse;
import com.yuncheng.system.login.security.service.LoginSecurityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 公开当前注册、验证码和密码输入规则。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/auth/security-policy")
public class SecurityPolicyController {

    private final LoginSecurityService securityService;

    public SecurityPolicyController(LoginSecurityService securityService) {
        this.securityService = securityService;
    }

    @GetMapping
    public ApiResponse<SecurityPolicyResponse> get() {
        return ApiResponse.success(securityService.policy());
    }
}
