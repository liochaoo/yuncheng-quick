package com.yuncheng.system.menu.controller;

import com.yuncheng.framework.security.authorization.annotation.RequirePermission;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.framework.web.response.AvailabilityResponse;
import com.yuncheng.framework.log.annotation.OperationLog;
import com.yuncheng.system.menu.constant.MenuPermissionCodes;
import com.yuncheng.system.menu.dto.MenuDeleteImpact;
import com.yuncheng.system.menu.dto.MenuDetail;
import com.yuncheng.system.menu.dto.MenuItem;
import com.yuncheng.system.menu.dto.MenuSaveRequest;
import com.yuncheng.system.menu.dto.MenuUniquenessCheckRequest;
import com.yuncheng.system.menu.service.MenuCommandService;
import com.yuncheng.system.menu.service.MenuQueryService;
import com.yuncheng.system.menu.service.MenuUniquenessService;
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

/** 菜单管理接口。 */
@Validated
@RestController
@RequestMapping(WebConstants.API_PREFIX + "/system/menus")
@Tag(name = "菜单管理")
public class MenuController {

    private final MenuQueryService queryService;
    private final MenuCommandService commandService;
    private final MenuUniquenessService uniquenessService;

    public MenuController(
            MenuQueryService queryService,
            MenuCommandService commandService,
            MenuUniquenessService uniquenessService
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.uniquenessService = uniquenessService;
    }

    @GetMapping
    @Operation(summary = "查询菜单树")
    @RequirePermission(MenuPermissionCodes.QUERY)
    public ApiResponse<List<MenuItem>> tree() {
        return ApiResponse.success(queryService.tree());
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询菜单详情")
    @RequirePermission(MenuPermissionCodes.QUERY)
    public ApiResponse<MenuDetail> detail(@PathVariable @Positive Long id) {
        return ApiResponse.success(queryService.detail(id));
    }

    @PostMapping
    @RequirePermission(MenuPermissionCodes.ADD)
    @OperationLog("新增菜单")
    public ApiResponse<String> create(@Valid @RequestBody MenuSaveRequest request) {
        return ApiResponse.success(commandService.create(request).toString());
    }

    @PostMapping("/uniqueness-check")
    @Operation(summary = "校验菜单唯一性")
    @RequirePermission({MenuPermissionCodes.ADD, MenuPermissionCodes.EDIT})
    public ApiResponse<AvailabilityResponse> checkUniqueness(
            @Valid @RequestBody MenuUniquenessCheckRequest request
    ) {
        boolean available = uniquenessService.isAvailable(
                request.field(),
                request.value(),
                request.parentId(),
                request.id()
        );
        return ApiResponse.success(new AvailabilityResponse(available));
    }

    @PutMapping("/{id}")
    @RequirePermission(MenuPermissionCodes.EDIT)
    @OperationLog("编辑菜单")
    public ApiResponse<Void> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody MenuSaveRequest request
    ) {
        commandService.update(id, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/deletion-impact")
    @Operation(summary = "查询菜单删除影响")
    @RequirePermission(MenuPermissionCodes.DELETE)
    public ApiResponse<MenuDeleteImpact> deletionImpact(@PathVariable @Positive Long id) {
        return ApiResponse.success(commandService.deletionImpact(id));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(MenuPermissionCodes.DELETE)
    @OperationLog("删除菜单")
    public ApiResponse<Void> delete(@PathVariable @Positive Long id) {
        commandService.delete(id);
        return ApiResponse.success(null);
    }
}
