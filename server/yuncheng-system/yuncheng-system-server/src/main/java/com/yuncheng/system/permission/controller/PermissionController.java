package com.yuncheng.system.permission.controller;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.system.permission.cache.RebuildableCacheService;
import com.yuncheng.system.permission.constant.AuthorizationPermissionCodes;
import com.yuncheng.system.permission.dto.PermissionMenuNode;
import com.yuncheng.system.permission.dto.RolePermissionRequest;
import com.yuncheng.system.permission.dto.RolePermissionResponse;
import com.yuncheng.system.permission.service.PermissionCommandService;
import com.yuncheng.system.permission.service.PermissionQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 给角色分配菜单权限。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/system/permissions")
public class PermissionController {

    private final PermissionQueryService queryService;
    private final PermissionCommandService commandService;
    private final RebuildableCacheService rebuildableCacheService;

    public PermissionController(
            PermissionQueryService queryService,
            PermissionCommandService commandService,
            RebuildableCacheService rebuildableCacheService
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.rebuildableCacheService = rebuildableCacheService;
    }

    @GetMapping("/menu-tree")
    @RequirePermission(AuthorizationPermissionCodes.QUERY)
    public ApiResponse<List<PermissionMenuNode>> menuTree() {
        return ApiResponse.success(queryService.menuTree());
    }

    @GetMapping("/roles/{roleId}")
    @RequirePermission(AuthorizationPermissionCodes.QUERY)
    public ApiResponse<RolePermissionResponse> rolePermission(@PathVariable @Positive Long roleId) {
        return ApiResponse.success(queryService.rolePermission(roleId));
    }

    @PutMapping("/roles/{roleId}")
    @RequirePermission(AuthorizationPermissionCodes.ASSIGN)
    @OperationLog("分配角色权限")
    public ApiResponse<Void> save(
            @PathVariable @Positive Long roleId,
            @Valid @RequestBody RolePermissionRequest request
    ) {
        commandService.save(roleId, request.menuIds());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/cache")
    @RequirePermission(AuthorizationPermissionCodes.CLEAR_CACHE)
    @OperationLog("清空可重建缓存")
    public ApiResponse<Long> clearCache() {
        return ApiResponse.success(rebuildableCacheService.clearAll());
    }
}
