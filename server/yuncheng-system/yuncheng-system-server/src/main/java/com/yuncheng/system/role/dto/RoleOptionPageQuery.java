package com.yuncheng.system.role.dto;

import com.yuncheng.framework.web.page.PageQuery;

/** 角色选择项分页查询参数。 */
public class RoleOptionPageQuery extends PageQuery {

    private String keyword;
    private String roleCode;
    private String roleName;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}
