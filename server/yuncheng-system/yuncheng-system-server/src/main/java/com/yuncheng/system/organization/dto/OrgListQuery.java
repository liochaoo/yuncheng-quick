package com.yuncheng.system.organization.dto;

import jakarta.validation.constraints.Size;

/** 组织管理树根节点或关键字查询参数。 */
public class OrgListQuery {

    @Size(max = 100, message = "查询关键字不能超过 100 个字符")
    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
