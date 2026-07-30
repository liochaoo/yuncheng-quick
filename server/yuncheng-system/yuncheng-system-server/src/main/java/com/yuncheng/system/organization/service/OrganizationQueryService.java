package com.yuncheng.system.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuncheng.common.constant.BuiltInOrganizationIds;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.system.api.organization.SystemOrganizationInfo;
import com.yuncheng.system.api.organization.SystemOrganizationQueryApi;
import com.yuncheng.system.organization.dto.OrganizationNodeDetail;
import com.yuncheng.system.organization.dto.OrganizationNodeItem;
import com.yuncheng.system.organization.dto.OrganizationNodePageQuery;
import com.yuncheng.system.organization.entity.SystemOrganizationNode;
import com.yuncheng.system.organization.mapper.SystemOrganizationNodeMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 查询组织节点、异步树和普通业务可消费的节点选项。 */
@Service
public class OrganizationQueryService implements SystemOrganizationQueryApi {

    private final SystemOrganizationNodeMapper nodeMapper;

    public OrganizationQueryService(SystemOrganizationNodeMapper nodeMapper) {
        this.nodeMapper = nodeMapper;
    }

    public PageResult<OrganizationNodeItem> page(OrganizationNodePageQuery query) {
        String keyword = normalizedText(query.getKeyword());
        String codeKeyword = normalizedCode(keyword);
        LambdaQueryWrapper<SystemOrganizationNode> wrapper =
                new LambdaQueryWrapper<SystemOrganizationNode>();
        if (StringUtils.hasText(keyword)) {
            if (isAscii(keyword)) {
                wrapper.and(nested -> nested
                        .like(SystemOrganizationNode::getNodeCode, codeKeyword)
                        .or()
                        .like(SystemOrganizationNode::getNodeName, keyword)
                        .or()
                        .like(SystemOrganizationNode::getFullPath, keyword));
            } else {
                wrapper.and(nested -> nested
                        .like(SystemOrganizationNode::getNodeName, keyword)
                        .or()
                        .like(SystemOrganizationNode::getFullPath, keyword));
            }
        } else {
            wrapper.isNull(SystemOrganizationNode::getParentId);
        }
        wrapper.orderByAsc(SystemOrganizationNode::getSortOrder, SystemOrganizationNode::getId);
        IPage<SystemOrganizationNode> page = nodeMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()),
                wrapper
        );
        List<OrganizationNodeItem> items = toItems(page.getRecords());
        return PageResult.of(items, page.getTotal(), query);
    }

    public List<OrganizationNodeItem> children(Long parentId) {
        if (parentId != null) {
            requireNode(parentId);
        }
        LambdaQueryWrapper<SystemOrganizationNode> wrapper =
                new LambdaQueryWrapper<SystemOrganizationNode>();
        if (parentId == null) {
            wrapper.isNull(SystemOrganizationNode::getParentId);
        } else {
            wrapper.eq(SystemOrganizationNode::getParentId, parentId);
        }
        wrapper.orderByAsc(SystemOrganizationNode::getSortOrder, SystemOrganizationNode::getId);
        return toItems(nodeMapper.selectList(wrapper));
    }

    public OrganizationNodeDetail detail(Long nodeId) {
        SystemOrganizationNode node = requireNode(nodeId);
        String parentName = node.getParentId() == null
                ? null
                : requireNode(node.getParentId()).getNodeName();
        return new OrganizationNodeDetail(
                id(node.getId()),
                id(node.getParentId()),
                parentName,
                node.getNodeType(),
                node.getNodeCode(),
                node.getNodeName(),
                node.getFullPath(),
                node.getDepth(),
                node.getSortOrder(),
                node.getDescription(),
                node.getCreatedAt(),
                id(node.getCreatedBy()),
                node.getUpdatedAt(),
                id(node.getUpdatedBy())
        );
    }

    public OrganizationNodeItem item(Long nodeId) {
        return toItems(List.of(requireNode(nodeId))).getFirst();
    }

    public SystemOrganizationNode requireNode(Long nodeId) {
        SystemOrganizationNode node = nodeId == null ? null : nodeMapper.selectById(nodeId);
        if (node == null) {
            throw PlatformException.notFound("组织节点不存在");
        }
        return node;
    }

    @Override
    public Optional<SystemOrganizationInfo> findById(Long nodeId) {
        return Optional.ofNullable(nodeId == null ? null : nodeMapper.selectById(nodeId))
                .map(this::toSystemInfo);
    }

    @Override
    public Optional<SystemOrganizationInfo> findByCode(String nodeCode) {
        String normalized = normalizedCode(nodeCode);
        if (!StringUtils.hasText(normalized)) {
            return Optional.empty();
        }
        return Optional.ofNullable(nodeMapper.selectOne(
                new LambdaQueryWrapper<SystemOrganizationNode>()
                        .eq(SystemOrganizationNode::getNodeCode, normalized)
        )).map(this::toSystemInfo);
    }

    private List<OrganizationNodeItem> toItems(List<SystemOrganizationNode> nodes) {
        if (nodes.isEmpty()) {
            return List.of();
        }
        List<Long> nodeIds = nodes.stream().map(SystemOrganizationNode::getId).toList();
        Set<Long> parentIdsWithChildren = new HashSet<>();
        nodeMapper.selectList(new LambdaQueryWrapper<SystemOrganizationNode>()
                        .select(SystemOrganizationNode::getParentId)
                        .in(SystemOrganizationNode::getParentId, nodeIds))
                .forEach(child -> parentIdsWithChildren.add(child.getParentId()));
        return nodes.stream()
                .map(node -> new OrganizationNodeItem(
                        id(node.getId()),
                        id(node.getParentId()),
                        node.getNodeType(),
                        node.getNodeCode(),
                        node.getNodeName(),
                        node.getFullPath(),
                        node.getDepth(),
                        node.getSortOrder(),
                        parentIdsWithChildren.contains(node.getId()),
                        node.getId() == BuiltInOrganizationIds.DEFAULT_ORGANIZATION,
                        ancestorIds(node)
                ))
                .toList();
    }

    private List<String> ancestorIds(SystemOrganizationNode node) {
        return Arrays.stream(node.getPathIds().split("/"))
                .filter(StringUtils::hasText)
                .filter(value -> !value.equals(node.getId().toString()))
                .toList();
    }

    private SystemOrganizationInfo toSystemInfo(SystemOrganizationNode node) {
        return new SystemOrganizationInfo(
                node.getId(),
                node.getParentId(),
                node.getNodeType(),
                node.getNodeCode(),
                node.getNodeName(),
                node.getFullPath()
        );
    }

    private String normalizedCode(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : null;
    }

    private String normalizedText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private boolean isAscii(String value) {
        return value.chars().allMatch(character -> character <= 0x7f);
    }

    private String id(Long value) {
        return value == null ? null : value.toString();
    }
}
