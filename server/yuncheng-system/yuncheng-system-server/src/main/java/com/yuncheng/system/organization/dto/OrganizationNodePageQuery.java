package com.yuncheng.system.organization.dto;

import com.yuncheng.framework.web.page.PageQuery;
import jakarta.validation.constraints.Size;

/** 组织节点根级列表或关键字搜索参数。 */
public class OrganizationNodePageQuery extends PageQuery {

    @Size(max = 100, message = "查询关键字不能超过 100 个字符")
    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
