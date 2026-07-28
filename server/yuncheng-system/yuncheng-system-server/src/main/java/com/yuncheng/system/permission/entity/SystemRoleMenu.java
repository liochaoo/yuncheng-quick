package com.yuncheng.system.permission.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yuncheng.framework.mybatis.entity.BaseEntity;

/** 角色菜单权限关系。 */
@TableName("system_role_menu")
public class SystemRoleMenu extends BaseEntity {

    private Long roleId;
    private Long menuId;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
}
