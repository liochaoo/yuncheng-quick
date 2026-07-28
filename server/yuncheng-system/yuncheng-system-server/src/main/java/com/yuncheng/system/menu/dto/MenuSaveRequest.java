package com.yuncheng.system.menu.dto;

import com.yuncheng.system.menu.enums.MenuType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.Map;

/** 新增或编辑菜单请求。 */
public record MenuSaveRequest(
        @Positive(message = "上级菜单主键必须为正数") Long parentId,
        @NotNull(message = "菜单类型不能为空") MenuType menuType,
        @NotBlank(message = "菜单名称不能为空") @Size(max = 100, message = "菜单名称不能超过 100 个字符") String menuName,
        @Size(max = 100, message = "路由名称不能超过 100 个字符") String routeName,
        @Size(max = 255, message = "路由路径不能超过 255 个字符") String routePath,
        @Size(max = 255, message = "组件路径不能超过 255 个字符") String componentPath,
        @Size(max = 255, message = "重定向路径不能超过 255 个字符") String redirect,
        @Size(max = 128, message = "权限码不能超过 128 个字符") String permissionCode,
        @PositiveOrZero(message = "排序号不能小于 0") Integer sortOrder,
        @Size(max = 500, message = "图标不能超过 500 个字符") String icon,
        @Size(max = 500, message = "激活图标不能超过 500 个字符") String activeIcon,
        @Size(max = 255, message = "激活菜单路径不能超过 255 个字符") String activePath,
        @Size(max = 100, message = "徽标内容不能超过 100 个字符") String badge,
        @Size(max = 20, message = "徽标类型不能超过 20 个字符") String badgeType,
        @Size(max = 32, message = "徽标样式不能超过 32 个字符") String badgeVariants,
        Boolean affixTab,
        @PositiveOrZero(message = "固定标签顺序不能小于 0") Integer affixTabOrder,
        Boolean hideInMenu,
        Boolean hideChildrenInMenu,
        Boolean hideInBreadcrumb,
        Boolean hideInTab,
        Boolean keepAlive,
        Boolean fullPathKey,
        Boolean openInNewWindow,
        Boolean noBasicLayout,
        @Positive(message = "最大标签页数量必须大于 0") Integer maxNumOfOpenTab,
        Map<String, String> query,
        @Size(max = 1000, message = "外部链接不能超过 1000 个字符") String link,
        @Size(max = 1000, message = "内嵌页面地址不能超过 1000 个字符") String iframeSrc
) {
}
