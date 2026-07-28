package com.yuncheng.system.role.dto;

import com.yuncheng.framework.web.page.PageQuery;
import com.yuncheng.system.role.enums.RoleType;

/** 角色分页查询参数。 */
public class RolePageQuery extends PageQuery {

    private String roleCode;
    private String roleName;
    private RoleType roleType;

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public RoleType getRoleType() { return roleType; }
    public void setRoleType(RoleType roleType) { this.roleType = roleType; }
}
