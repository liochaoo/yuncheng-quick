package com.yuncheng.system.organization.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.yuncheng.framework.mybatis.entity.BaseEntity;
import com.yuncheng.system.api.organization.SystemOrganizationNodeType;

/** 系统组织、部门或小组节点。 */
@TableName("system_organization_node")
public class SystemOrganizationNode extends BaseEntity {

    private Long parentId;
    private SystemOrganizationNodeType nodeType;
    private String nodeCode;
    private String nodeName;
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

    public SystemOrganizationNodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(SystemOrganizationNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
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
