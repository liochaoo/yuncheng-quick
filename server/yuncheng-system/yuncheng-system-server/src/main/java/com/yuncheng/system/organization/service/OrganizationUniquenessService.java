package com.yuncheng.system.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.organization.entity.SystemOrganizationNode;
import com.yuncheng.system.organization.mapper.SystemOrganizationNodeMapper;
import org.springframework.stereotype.Service;

/** 校验组织节点编码和同级名称唯一性。 */
@Service
public class OrganizationUniquenessService {

    private final SystemOrganizationNodeMapper nodeMapper;

    public OrganizationUniquenessService(SystemOrganizationNodeMapper nodeMapper) {
        this.nodeMapper = nodeMapper;
    }

    public void requireCodeAvailable(String nodeCode, Long excludedNodeId) {
        boolean exists = nodeMapper.exists(new LambdaQueryWrapper<SystemOrganizationNode>()
                .eq(SystemOrganizationNode::getNodeCode, nodeCode)
                .ne(excludedNodeId != null, SystemOrganizationNode::getId, excludedNodeId));
        if (exists) {
            throw PlatformException.conflict("组织节点编码已存在");
        }
    }

    public void requireNameAvailable(Long parentId, String nodeName, Long excludedNodeId) {
        LambdaQueryWrapper<SystemOrganizationNode> wrapper =
                new LambdaQueryWrapper<SystemOrganizationNode>()
                        .eq(SystemOrganizationNode::getNodeName, nodeName)
                        .ne(excludedNodeId != null, SystemOrganizationNode::getId, excludedNodeId);
        if (parentId == null) {
            wrapper.isNull(SystemOrganizationNode::getParentId);
        } else {
            wrapper.eq(SystemOrganizationNode::getParentId, parentId);
        }
        if (nodeMapper.exists(wrapper)) {
            throw PlatformException.conflict("同级组织节点名称已存在");
        }
    }
}
