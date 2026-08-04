package com.yuncheng.system.role.controller;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.web.response.AvailabilityResponse;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.system.permission.constant.AuthorizationPermissionCodes;
import com.yuncheng.system.role.constant.RolePermissionCodes;
import com.yuncheng.system.role.dto.RoleCreateRequest;
import com.yuncheng.system.role.dto.RoleDetail;
import com.yuncheng.system.role.dto.RoleListItem;
import com.yuncheng.system.role.dto.RolePageQuery;
import com.yuncheng.system.role.dto.RoleIdListRequest;
import com.yuncheng.system.role.dto.RoleOption;
import com.yuncheng.system.role.dto.RoleOptionPageQuery;
import com.yuncheng.system.role.dto.RoleUpdateRequest;
import com.yuncheng.system.role.dto.RoleUniquenessCheckRequest;
import com.yuncheng.system.role.dto.RoleUserIdsRequest;
import com.yuncheng.system.role.dto.RoleUserPageQuery;
import com.yuncheng.system.role.dto.RoleUserListItem;
import com.yuncheng.system.role.service.RoleCommandService;
import com.yuncheng.system.role.service.RoleQueryService;
import com.yuncheng.system.role.service.RoleUserService;
import com.yuncheng.system.role.service.RoleUniquenessService;
import com.yuncheng.system.user.constant.UserPermissionCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 角色管理及角色用户关系接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/system/roles")
@Tag(name = "角色管理")
public class RoleController {

    private final RoleQueryService queryService;
    private final RoleCommandService commandService;
    private final RoleUserService roleUserService;
    private final RoleUniquenessService uniquenessService;

    public RoleController(
            RoleQueryService queryService,
            RoleCommandService commandService,
            RoleUserService roleUserService,
            RoleUniquenessService uniquenessService
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.roleUserService = roleUserService;
        this.uniquenessService = uniquenessService;
    }

    @GetMapping
    @Operation(summary = "分页查询角色")
    @RequirePermission(RolePermissionCodes.QUERY)
    public ApiResponse<PageResult<RoleListItem>> page(@Valid RolePageQuery query) {
        return ApiResponse.success(queryService.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询角色详情")
    @RequirePermission(RolePermissionCodes.QUERY)
    public ApiResponse<RoleDetail> detail(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.detail(id));
    }

    @PostMapping
    @RequirePermission(RolePermissionCodes.ADD)
    @OperationLog("新增角色")
    public ApiResponse<String> create(@Valid @RequestBody RoleCreateRequest request) {
        return ApiResponse.success(commandService.create(request).toString());
    }

    @PostMapping("/uniqueness-check")
    @Operation(summary = "校验角色唯一性")
    @RequirePermission({RolePermissionCodes.ADD, RolePermissionCodes.EDIT})
    public ApiResponse<AvailabilityResponse> checkUniqueness(
            @Valid @RequestBody RoleUniquenessCheckRequest request
    ) {
        boolean available = uniquenessService.isAvailable(request.field(), request.value(), request.id());
        return ApiResponse.success(new AvailabilityResponse(available));
    }

    @PutMapping("/{id}")
    @RequirePermission(RolePermissionCodes.EDIT)
    @OperationLog("编辑角色")
    public ApiResponse<Void> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody RoleUpdateRequest request
    ) {
        commandService.update(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    @RequirePermission(RolePermissionCodes.DELETE)
    @OperationLog("删除角色")
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        commandService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/batch-delete")
    @RequirePermission(RolePermissionCodes.DELETE)
    @OperationLog("批量删除角色")
    public ApiResponse<Void> batchDelete(@Valid @RequestBody RoleIdListRequest request) {
        commandService.batchDelete(request.ids());
        return ApiResponse.success(null);
    }

    @GetMapping("/options")
    @Operation(summary = "分页查询角色选项")
    @RequirePermission({
            RolePermissionCodes.QUERY,
            UserPermissionCodes.QUERY,
            AuthorizationPermissionCodes.QUERY
    })
    public ApiResponse<PageResult<RoleOption>> options(@Valid RoleOptionPageQuery query) {
        return ApiResponse.success(queryService.pageOptions(query));
    }

    @PostMapping("/options/by-ids")
    @Operation(summary = "按主键查询角色选项")
    @RequirePermission({
            RolePermissionCodes.QUERY,
            UserPermissionCodes.QUERY,
            AuthorizationPermissionCodes.QUERY
    })
    public ApiResponse<List<RoleOption>> optionsByIds(@Valid @RequestBody RoleIdListRequest request) {
        return ApiResponse.success(queryService.optionsByIds(request.ids()));
    }

    @GetMapping("/{id}/users")
    @Operation(summary = "分页查询角色用户")
    @RequirePermission(RolePermissionCodes.ASSIGN_USER)
    public ApiResponse<PageResult<RoleUserListItem>> users(
            @PathVariable @Positive Long id,
            @Valid RoleUserPageQuery query
    ) {
        return ApiResponse.success(roleUserService.assignedUsers(id, query));
    }

    @GetMapping("/{id}/candidate-users")
    @Operation(summary = "分页查询待选角色用户")
    @RequirePermission(RolePermissionCodes.ASSIGN_USER)
    public ApiResponse<PageResult<RoleUserListItem>> candidateUsers(
            @PathVariable @Positive Long id,
            @Valid RoleUserPageQuery query
    ) {
        return ApiResponse.success(roleUserService.candidateUsers(id, query));
    }

    @PostMapping("/{id}/users")
    @RequirePermission(RolePermissionCodes.ASSIGN_USER)
    @OperationLog("添加角色用户")
    public ApiResponse<Void> addUsers(
            @PathVariable @Positive Long id,
            @Valid @RequestBody RoleUserIdsRequest request
    ) {
        roleUserService.addUsers(id, request.ids());
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/users/batch-remove")
    @RequirePermission(RolePermissionCodes.ASSIGN_USER)
    @OperationLog("移除角色用户")
    public ApiResponse<Void> removeUsers(
            @PathVariable @Positive Long id,
            @Valid @RequestBody RoleUserIdsRequest request
    ) {
        roleUserService.removeUsers(id, request.ids());
        return ApiResponse.success(null);
    }
}
