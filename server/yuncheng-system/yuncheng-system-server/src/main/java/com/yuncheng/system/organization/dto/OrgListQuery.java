package com.yuncheng.system.organization.dto;

import jakarta.validation.constraints.Size;

/** 组织树根节点或名称、编码查询参数。 */
public class OrgListQuery {

    @Size(max = 100, message = "查询关键字不能超过 100 个字符")
    private String keyword;

    @Size(max = 100, message = "组织名称不能超过 100 个字符")
    private String orgName;

    @Size(max = 64, message = "组织编码不能超过 64 个字符")
    private String orgCode;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }
}
