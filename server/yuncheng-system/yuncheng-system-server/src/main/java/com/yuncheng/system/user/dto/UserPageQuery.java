package com.yuncheng.system.user.dto;

import com.yuncheng.framework.web.page.PageQuery;
import com.yuncheng.system.user.enums.UserOrgRelationType;
import com.yuncheng.system.user.enums.UserOrgScope;
import jakarta.validation.constraints.Positive;

/** 用户分页查询参数。 */
public class UserPageQuery extends PageQuery {

    private String username;
    private String realName;
    private Boolean enabled;
    @Positive(message = "组织主键必须为正数")
    private Long orgId;
    private UserOrgScope orgScope = UserOrgScope.DIRECT;
    private UserOrgRelationType orgRelationType = UserOrgRelationType.ALL;
    private String orgPathIds;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public UserOrgScope getOrgScope() { return orgScope; }
    public void setOrgScope(UserOrgScope orgScope) {
        this.orgScope = orgScope == null ? UserOrgScope.DIRECT : orgScope;
    }
    public UserOrgRelationType getOrgRelationType() { return orgRelationType; }
    public void setOrgRelationType(UserOrgRelationType orgRelationType) {
        this.orgRelationType = orgRelationType == null
                ? UserOrgRelationType.ALL
                : orgRelationType;
    }
    public String getOrgPathIds() { return orgPathIds; }
    public void setOrgPathIds(String orgPathIds) { this.orgPathIds = orgPathIds; }
    public boolean isIncludeDescendants() {
        return orgScope == UserOrgScope.INCLUDE_DESCENDANTS;
    }
    public boolean isPrimaryOrgOnly() {
        return orgRelationType == UserOrgRelationType.PRIMARY;
    }
    public boolean isOtherOrgOnly() {
        return orgRelationType == UserOrgRelationType.OTHER;
    }
}
