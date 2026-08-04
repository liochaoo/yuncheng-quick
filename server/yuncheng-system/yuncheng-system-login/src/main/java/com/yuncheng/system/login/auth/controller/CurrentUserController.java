package com.yuncheng.system.login.auth.controller;

import com.yuncheng.common.context.CurrentUser;
import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.framework.web.constant.WebConstants;
import com.yuncheng.framework.web.response.ApiResponse;
import com.yuncheng.system.menu.cache.MenuCacheService;
import com.yuncheng.system.menu.config.HomePageProperties;
import com.yuncheng.system.menu.dto.MenuRoute;
import com.yuncheng.system.permission.cache.PermissionCacheService;
import com.yuncheng.system.user.dto.CurrentUserInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 当前登录用户的基本信息、权限码和菜单接口。 */
@RestController
@RequestMapping(WebConstants.API_PREFIX)
@Tag(name = "当前用户")
public class CurrentUserController {

    private final CurrentUserContext currentUserContext;
    private final PermissionCacheService permissionCacheService;
    private final MenuCacheService menuCacheService;
    private final HomePageProperties homePageProperties;

    public CurrentUserController(
            CurrentUserContext currentUserContext,
            PermissionCacheService permissionCacheService,
            MenuCacheService menuCacheService,
            HomePageProperties homePageProperties
    ) {
        this.currentUserContext = currentUserContext;
        this.permissionCacheService = permissionCacheService;
        this.menuCacheService = menuCacheService;
        this.homePageProperties = homePageProperties;
    }

    @GetMapping("/user/info")
    @Operation(summary = "查询当前用户信息")
    public ApiResponse<CurrentUserInfo> userInfo() {
        CurrentUser user = currentUserContext.getUser();
        return ApiResponse.success(new CurrentUserInfo(
                user.userId().toString(),
                user.username(),
                user.realName(),
                user.avatar(),
                user.roleCodes(),
                homePageProperties.getHomePath()
        ));
    }

    @GetMapping("/auth/codes")
    @Operation(summary = "查询当前用户权限码")
    public ApiResponse<List<String>> accessCodes() {
        return ApiResponse.success(
                permissionCacheService.getPermissionCodes(currentUserContext.getUserId())
        );
    }

    @GetMapping("/menu/all")
    @Operation(summary = "查询当前用户菜单")
    public ApiResponse<List<MenuRoute>> menus() {
        return ApiResponse.success(
                menuCacheService.getUserMenus(currentUserContext.getUserId())
        );
    }
}
