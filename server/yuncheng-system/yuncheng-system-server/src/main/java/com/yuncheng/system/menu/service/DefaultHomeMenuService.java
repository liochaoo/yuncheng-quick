package com.yuncheng.system.menu.service;

import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.menu.config.HomePageProperties;
import com.yuncheng.system.menu.entity.SystemMenu;
import com.yuncheng.system.menu.enums.MenuType;
import java.util.Collection;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 根据平台配置定位默认首页菜单。 */
@Service
public class DefaultHomeMenuService {

    private final HomePageProperties homePageProperties;
    private final MenuQueryService menuQueryService;

    public DefaultHomeMenuService(
            HomePageProperties homePageProperties,
            MenuQueryService menuQueryService
    ) {
        this.homePageProperties = homePageProperties;
        this.menuQueryService = menuQueryService;
    }

    public Long requireHomeMenuId() {
        return requireHomeMenuId(menuQueryService.allMenus());
    }

    public Long requireHomeMenuId(Collection<SystemMenu> menus) {
        String homePath = homePath();
        SystemMenu homeMenu = menus.stream()
                .filter(menu -> homePath.equals(menu.getRoutePath()))
                .findFirst()
                .orElseThrow(() -> PlatformException.conflict(
                        "默认首页菜单不存在，请检查配置和菜单数据：" + homePath
                ));
        if (homeMenu.getMenuType() == MenuType.CATALOG || homeMenu.getMenuType() == MenuType.BUTTON) {
            throw PlatformException.conflict("默认首页必须配置为可访问的路由菜单：" + homePath);
        }
        return homeMenu.getId();
    }

    private String homePath() {
        String homePath = homePageProperties.getHomePath();
        if (!StringUtils.hasText(homePath)) {
            throw PlatformException.conflict("默认首页路径不能为空");
        }
        return homePath.trim();
    }
}
