package com.yuncheng.system.log.controller;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.system.log.constant.LogPermissionCodes;
import com.yuncheng.system.log.dto.LogCleanPolicy;
import com.yuncheng.system.log.dto.LogCleanRequest;
import com.yuncheng.system.log.dto.LogCleanResult;
import com.yuncheng.system.log.dto.LoginLogItem;
import com.yuncheng.system.log.dto.LoginLogPageQuery;
import com.yuncheng.system.log.dto.OperationLogItem;
import com.yuncheng.system.log.dto.OperationLogPageQuery;
import com.yuncheng.system.log.service.SystemLogCleanService;
import com.yuncheng.system.log.service.SystemLogQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 系统日志查询和清理接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/system/logs")
public class SystemLogController {

    private final SystemLogQueryService queryService;
    private final SystemLogCleanService cleanService;

    public SystemLogController(
            SystemLogQueryService queryService,
            SystemLogCleanService cleanService
    ) {
        this.queryService = queryService;
        this.cleanService = cleanService;
    }

    @GetMapping("/login")
    @RequirePermission(LogPermissionCodes.QUERY)
    public ApiResponse<PageResult<LoginLogItem>> loginPage(@Valid LoginLogPageQuery query) {
        return ApiResponse.success(queryService.loginPage(query));
    }

    @GetMapping("/login/{id}")
    @RequirePermission(LogPermissionCodes.QUERY)
    public ApiResponse<LoginLogItem> loginDetail(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.loginDetail(id));
    }

    @GetMapping("/operation")
    @RequirePermission(LogPermissionCodes.QUERY)
    public ApiResponse<PageResult<OperationLogItem>> operationPage(@Valid OperationLogPageQuery query) {
        return ApiResponse.success(queryService.operationPage(query));
    }

    @GetMapping("/operation/{id}")
    @RequirePermission(LogPermissionCodes.QUERY)
    public ApiResponse<OperationLogItem> operationDetail(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.operationDetail(id));
    }

    @GetMapping("/clean-policy")
    @RequirePermission(LogPermissionCodes.CLEAN)
    public ApiResponse<LogCleanPolicy> cleanPolicy() {
        return ApiResponse.success(cleanService.policy());
    }

    @PostMapping("/clean")
    @RequirePermission(LogPermissionCodes.CLEAN)
    @OperationLog("清理系统日志")
    public ApiResponse<LogCleanResult> clean(@Valid @RequestBody LogCleanRequest request) {
        return ApiResponse.success(new LogCleanResult(cleanService.clean(request.type(), request.before())));
    }
}
