package com.yuncheng.system.security.controller;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.system.security.constant.SecurityPermissionCodes;
import com.yuncheng.system.security.dto.SecurityPolicyData;
import com.yuncheng.system.security.dto.SecurityPolicyUpdateRequest;
import com.yuncheng.system.security.service.SecurityPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 在线读取和维护安全策略。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/system/security")
@Tag(name = "安全策略管理")
public class SecurityPolicyManagementController {

    private final SecurityPolicyService securityPolicyService;

    public SecurityPolicyManagementController(SecurityPolicyService securityPolicyService) {
        this.securityPolicyService = securityPolicyService;
    }

    @GetMapping
    @Operation(summary = "查询安全设置")
    @RequirePermission(SecurityPermissionCodes.QUERY)
    public ApiResponse<SecurityPolicyData> get() {
        return ApiResponse.success(securityPolicyService.current());
    }

    @PutMapping
    @RequirePermission(SecurityPermissionCodes.EDIT)
    @OperationLog("修改安全设置")
    public ApiResponse<SecurityPolicyData> update(
            @Valid @RequestBody SecurityPolicyUpdateRequest request
    ) {
        return ApiResponse.success(securityPolicyService.update(request));
    }
}
