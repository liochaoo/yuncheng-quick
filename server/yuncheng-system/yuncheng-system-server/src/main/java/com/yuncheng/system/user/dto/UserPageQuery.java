package com.yuncheng.system.user.dto;

import com.yuncheng.framework.web.page.PageQuery;

/** 用户分页查询参数。 */
public class UserPageQuery extends PageQuery {

    private String username;
    private String realName;
    private Boolean enabled;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
