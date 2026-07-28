package com.yuncheng.system.role.dto;

import com.yuncheng.framework.web.page.PageQuery;

/** 角色用户分页查询参数。 */
public class RoleUserPageQuery extends PageQuery {

    private String username;
    private String realName;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
}
