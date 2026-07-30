package com.yuncheng.system.organization.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yuncheng.framework.mybatis.entity.BaseEntity;
import com.yuncheng.system.api.organization.SystemOrgType;

/** 系统组织、部门或小组节点。 */
@TableName("system_org")
public class SystemOrg extends BaseEntity {

    private Long parentId;
    private SystemOrgType orgType;
    private String orgCode;
    private String orgName;
    private String pathIds;
    private String fullPath;
    private Integer depth;
    private Integer sortOrder;
    private String description;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public SystemOrgType getOrgType() {
        return orgType;
    }

    public void setOrgType(SystemOrgType orgType) {
        this.orgType = orgType;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getPathIds() {
        return pathIds;
    }

    public void setPathIds(String pathIds) {
        this.pathIds = pathIds;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
