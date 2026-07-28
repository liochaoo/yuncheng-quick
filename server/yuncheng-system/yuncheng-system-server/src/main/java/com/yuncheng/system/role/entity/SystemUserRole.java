package com.yuncheng.system.role.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yuncheng.framework.mybatis.entity.BaseEntity;

/** 用户角色关系。 */
@TableName("system_user_role")
public class SystemUserRole extends BaseEntity {

    private Long userId;
    private Long roleId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
