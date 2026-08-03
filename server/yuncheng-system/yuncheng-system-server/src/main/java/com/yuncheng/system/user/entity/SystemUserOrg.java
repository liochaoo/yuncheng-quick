package com.yuncheng.system.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yuncheng.framework.mybatis.entity.BaseEntity;

/** 用户与直接归属组织的关系。 */
@TableName("system_user_org")
public class SystemUserOrg extends BaseEntity {

    private Long userId;
    private Long orgId;

    @TableField("is_primary")
    private Boolean primaryOrg;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Boolean getPrimaryOrg() {
        return primaryOrg;
    }

    public void setPrimaryOrg(Boolean primaryOrg) {
        this.primaryOrg = primaryOrg;
    }
}
