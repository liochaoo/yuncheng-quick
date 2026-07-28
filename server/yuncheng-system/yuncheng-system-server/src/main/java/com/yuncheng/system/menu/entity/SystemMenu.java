package com.yuncheng.system.menu.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yuncheng.framework.mybatis.entity.BaseEntity;
import com.yuncheng.system.menu.enums.MenuType;

/** 系统菜单及权限节点。 */
@TableName("system_menu")
public class SystemMenu extends BaseEntity {

    private Long parentId;
    private MenuType menuType;
    private String menuName;
    private String routeName;
    private String routePath;
    private String componentPath;
    private String redirect;
    private String permissionCode;
    private Integer sortOrder;
    private String icon;
    private String activeIcon;
    private String activePath;
    private String badge;
    private String badgeType;
    private String badgeVariants;
    private Boolean affixTab;
    private Integer affixTabOrder;
    private Boolean hideInMenu;
    private Boolean hideChildrenInMenu;
    private Boolean hideInBreadcrumb;
    private Boolean hideInTab;
    private Boolean keepAlive;
    private Boolean fullPathKey;
    private Boolean openInNewWindow;
    private Boolean noBasicLayout;
    private Integer maxNumOfOpenTab;
    private String queryParams;
    private String link;
    private String iframeSrc;

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public MenuType getMenuType() { return menuType; }
    public void setMenuType(MenuType menuType) { this.menuType = menuType; }
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }
    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }
    public String getRoutePath() { return routePath; }
    public void setRoutePath(String routePath) { this.routePath = routePath; }
    public String getComponentPath() { return componentPath; }
    public void setComponentPath(String componentPath) { this.componentPath = componentPath; }
    public String getRedirect() { return redirect; }
    public void setRedirect(String redirect) { this.redirect = redirect; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getActiveIcon() { return activeIcon; }
    public void setActiveIcon(String activeIcon) { this.activeIcon = activeIcon; }
    public String getActivePath() { return activePath; }
    public void setActivePath(String activePath) { this.activePath = activePath; }
    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
    public String getBadgeType() { return badgeType; }
    public void setBadgeType(String badgeType) { this.badgeType = badgeType; }
    public String getBadgeVariants() { return badgeVariants; }
    public void setBadgeVariants(String badgeVariants) { this.badgeVariants = badgeVariants; }
    public Boolean getAffixTab() { return affixTab; }
    public void setAffixTab(Boolean affixTab) { this.affixTab = affixTab; }
    public Integer getAffixTabOrder() { return affixTabOrder; }
    public void setAffixTabOrder(Integer affixTabOrder) { this.affixTabOrder = affixTabOrder; }
    public Boolean getHideInMenu() { return hideInMenu; }
    public void setHideInMenu(Boolean hideInMenu) { this.hideInMenu = hideInMenu; }
    public Boolean getHideChildrenInMenu() { return hideChildrenInMenu; }
    public void setHideChildrenInMenu(Boolean hideChildrenInMenu) { this.hideChildrenInMenu = hideChildrenInMenu; }
    public Boolean getHideInBreadcrumb() { return hideInBreadcrumb; }
    public void setHideInBreadcrumb(Boolean hideInBreadcrumb) { this.hideInBreadcrumb = hideInBreadcrumb; }
    public Boolean getHideInTab() { return hideInTab; }
    public void setHideInTab(Boolean hideInTab) { this.hideInTab = hideInTab; }
    public Boolean getKeepAlive() { return keepAlive; }
    public void setKeepAlive(Boolean keepAlive) { this.keepAlive = keepAlive; }
    public Boolean getFullPathKey() { return fullPathKey; }
    public void setFullPathKey(Boolean fullPathKey) { this.fullPathKey = fullPathKey; }
    public Boolean getOpenInNewWindow() { return openInNewWindow; }
    public void setOpenInNewWindow(Boolean openInNewWindow) { this.openInNewWindow = openInNewWindow; }
    public Boolean getNoBasicLayout() { return noBasicLayout; }
    public void setNoBasicLayout(Boolean noBasicLayout) { this.noBasicLayout = noBasicLayout; }
    public Integer getMaxNumOfOpenTab() { return maxNumOfOpenTab; }
    public void setMaxNumOfOpenTab(Integer maxNumOfOpenTab) { this.maxNumOfOpenTab = maxNumOfOpenTab; }
    public String getQueryParams() { return queryParams; }
    public void setQueryParams(String queryParams) { this.queryParams = queryParams; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public String getIframeSrc() { return iframeSrc; }
    public void setIframeSrc(String iframeSrc) { this.iframeSrc = iframeSrc; }
}
