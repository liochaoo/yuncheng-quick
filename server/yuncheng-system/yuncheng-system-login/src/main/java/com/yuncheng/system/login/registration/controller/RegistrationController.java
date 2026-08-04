package com.yuncheng.system.login.registration.controller;

import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.system.login.registration.dto.RegisterEmailCodeRequest;
import com.yuncheng.system.login.registration.dto.RegisterRequest;
import com.yuncheng.system.login.registration.service.RegistrationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 用户注册接口。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/auth/register")
@Tag(name = "用户注册")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/email-code")
    @OperationLog("发送注册邮箱验证码")
    public ApiResponse<Void> sendEmailCode(
            @Valid @RequestBody RegisterEmailCodeRequest request
    ) {
        registrationService.sendEmailCode(request);
        return ApiResponse.success(null);
    }

    @PostMapping
    @OperationLog("用户注册")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        registrationService.register(request);
        return ApiResponse.success(null);
    }
}
