package com.yuncheng.system.user.controller;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.system.user.dto.PasswordResetRequest;
import com.yuncheng.system.user.dto.UserCreateRequest;
import com.yuncheng.system.user.dto.UserDetail;
import com.yuncheng.system.user.dto.UserFormData;
import com.yuncheng.system.user.dto.UserListItem;
import com.yuncheng.system.user.dto.UserPageQuery;
import com.yuncheng.system.user.dto.UserIdListRequest;
import com.yuncheng.system.user.dto.UserStatusRequest;
import com.yuncheng.system.user.dto.UserUniquenessCheckRequest;
import com.yuncheng.system.user.dto.UserUniquenessCheckResult;
import com.yuncheng.system.user.dto.UserUpdateRequest;
import com.yuncheng.system.user.constant.UserPermissionCodes;
import com.yuncheng.system.user.service.UserCommandService;
import com.yuncheng.system.user.service.UserPasswordService;
import com.yuncheng.system.user.service.UserQueryService;
import com.yuncheng.system.user.service.UserUniquenessService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 用户管理接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/system/users")
public class UserController {

    private final UserQueryService queryService;
    private final UserCommandService commandService;
    private final UserPasswordService passwordService;
    private final UserUniquenessService uniquenessService;

    public UserController(
            UserQueryService queryService,
            UserCommandService commandService,
            UserPasswordService passwordService,
            UserUniquenessService uniquenessService
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.passwordService = passwordService;
        this.uniquenessService = uniquenessService;
    }

    @GetMapping
    @RequirePermission(UserPermissionCodes.QUERY)
    public ApiResponse<PageResult<UserListItem>> page(@Valid UserPageQuery query) {
        return ApiResponse.success(queryService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission(UserPermissionCodes.QUERY)
    public ApiResponse<UserDetail> detail(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.detail(id));
    }

    @GetMapping("/{id}/form")
    @RequirePermission(UserPermissionCodes.EDIT)
    public ApiResponse<UserFormData> formData(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.formData(id));
    }

    @PostMapping
    @RequirePermission(UserPermissionCodes.ADD)
    @OperationLog("新增用户")
    public ApiResponse<String> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.success(commandService.create(request).toString());
    }

    @PostMapping("/uniqueness-check")
    @RequirePermission({UserPermissionCodes.ADD, UserPermissionCodes.EDIT})
    public ApiResponse<UserUniquenessCheckResult> checkUniqueness(
            @Valid @RequestBody UserUniquenessCheckRequest request
    ) {
        boolean available = uniquenessService.isAvailable(request.field(), request.value(), request.id());
        return ApiResponse.success(new UserUniquenessCheckResult(available));
    }

    @PutMapping("/{id}")
    @RequirePermission(UserPermissionCodes.EDIT)
    @OperationLog("编辑用户")
    public ApiResponse<Void> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        commandService.update(id, request);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/enabled")
    @RequirePermission(UserPermissionCodes.CHANGE_STATUS)
    @OperationLog("变更用户状态")
    public ApiResponse<Void> changeStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UserStatusRequest request
    ) {
        commandService.changeStatus(id, request.enabled());
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/password")
    @RequirePermission(UserPermissionCodes.RESET_PASSWORD)
    @OperationLog("重置用户密码")
    public ApiResponse<Void> resetPassword(
            @PathVariable @Positive Long id,
            @Valid @RequestBody PasswordResetRequest request
    ) {
        passwordService.reset(id, request.password());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/login-lock")
    @RequirePermission(UserPermissionCodes.UNLOCK)
    @OperationLog("解除用户登录锁定")
    public ApiResponse<Void> unlock(@PathVariable @Positive Long id) {
        passwordService.unlock(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(UserPermissionCodes.DELETE)
    @OperationLog("删除用户")
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        commandService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch-delete")
    @RequirePermission(UserPermissionCodes.DELETE)
    @OperationLog("批量删除用户")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody UserIdListRequest request) {
        commandService.batchDelete(request.ids());
        return ApiResponse.success(null);
    }

}
