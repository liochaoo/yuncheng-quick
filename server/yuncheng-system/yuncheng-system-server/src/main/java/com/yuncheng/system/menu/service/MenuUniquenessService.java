package com.yuncheng.system.menu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.menu.entity.SystemMenu;
import com.yuncheng.system.menu.enums.MenuUniqueField;
import com.yuncheng.system.menu.mapper.SystemMenuMapper;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** 统一处理菜单字段的唯一性查询和保存校验。 */
@Service
public class MenuUniquenessService {

    private final SystemMenuMapper menuMapper;

    public MenuUniquenessService(SystemMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    public boolean isAvailable(
            MenuUniqueField field,
            String value,
            Long parentId,
            Long excludedMenuId
    ) {
        String normalized = normalize(field, value);
        LambdaQueryWrapper<SystemMenu> wrapper = new LambdaQueryWrapper<SystemMenu>()
                .ne(excludedMenuId != null, SystemMenu::getId, excludedMenuId);
        switch (field) {
            case MENU_NAME -> sibling(wrapper, parentId).eq(SystemMenu::getMenuName, normalized);
            case ROUTE_NAME -> wrapper.eq(SystemMenu::getRouteName, normalized);
            case ROUTE_PATH -> wrapper.eq(SystemMenu::getRoutePath, normalized);
            case PERMISSION_CODE -> wrapper.eq(SystemMenu::getPermissionCode, normalized);
        }
        return menuMapper.selectCount(wrapper) == 0;
    }

    public void requireAvailable(SystemMenu menu, Long excludedMenuId) {
        requireAvailable(MenuUniqueField.MENU_NAME, menu.getMenuName(), menu.getParentId(), excludedMenuId);
        if (menu.getRouteName() != null) {
            requireAvailable(MenuUniqueField.ROUTE_NAME, menu.getRouteName(), menu.getParentId(), excludedMenuId);
        }
        if (menu.getRoutePath() != null) {
            requireAvailable(MenuUniqueField.ROUTE_PATH, menu.getRoutePath(), menu.getParentId(), excludedMenuId);
        }
        if (menu.getPermissionCode() != null) {
            requireAvailable(
                    MenuUniqueField.PERMISSION_CODE,
                    menu.getPermissionCode(),
                    menu.getParentId(),
                    excludedMenuId
            );
        }
    }

    private void requireAvailable(
            MenuUniqueField field,
            String value,
            Long parentId,
            Long excludedMenuId
    ) {
        if (isAvailable(field, value, parentId, excludedMenuId)) {
            return;
        }
        String message = switch (field) {
            case MENU_NAME -> "同一级菜单中名称已经存在";
            case ROUTE_NAME -> "路由名称已经存在";
            case ROUTE_PATH -> "菜单路由路径已经存在";
            case PERMISSION_CODE -> "权限码已经存在";
        };
        throw PlatformException.conflict(message);
    }

    private LambdaQueryWrapper<SystemMenu> sibling(
            LambdaQueryWrapper<SystemMenu> wrapper,
            Long parentId
    ) {
        return wrapper
                .eq(parentId != null, SystemMenu::getParentId, parentId)
                .isNull(parentId == null, SystemMenu::getParentId);
    }

    private String normalize(MenuUniqueField field, String value) {
        String normalized = value.trim();
        return field == MenuUniqueField.PERMISSION_CODE
                ? normalized.toLowerCase(Locale.ROOT)
                : normalized;
    }
}
