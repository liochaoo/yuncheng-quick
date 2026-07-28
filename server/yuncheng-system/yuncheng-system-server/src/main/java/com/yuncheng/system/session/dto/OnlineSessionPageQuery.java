package com.yuncheng.system.session.dto;

import com.yuncheng.framework.web.page.PageQuery;

/** 在线会话分页查询参数。 */
public class OnlineSessionPageQuery extends PageQuery {

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
