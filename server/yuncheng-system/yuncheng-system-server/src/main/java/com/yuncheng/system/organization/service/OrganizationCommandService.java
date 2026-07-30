package com.yuncheng.system.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.yuncheng.common.constant.BuiltInOrganizationIds;
import com.yuncheng.common.constant.OperatorConstants;
import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.api.organization.SystemOrganizationCommandApi;
import com.yuncheng.system.api.organization.SystemOrganizationCreateCommand;
import com.yuncheng.system.api.organization.SystemOrganizationNodeType;
import com.yuncheng.system.organization.constant.OrganizationConstants;
import com.yuncheng.system.organization.dto.OrganizationNodeCreateRequest;
import com.yuncheng.system.organization.dto.OrganizationNodeMoveImpact;
import com.yuncheng.system.organization.dto.OrganizationNodeMoveRequest;
import com.yuncheng.system.organization.dto.OrganizationNodeUpdateRequest;
import com.yuncheng.system.organization.entity.SystemOrganizationNode;
import com.yuncheng.system.organization.mapper.SystemOrganizationNodeMapper;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 新增、编辑、移动和删除组织节点。 */
@Service
public class OrganizationCommandService implements SystemOrganizationCommandApi {

    private static final Pattern NODE_CODE_PATTERN =
            Pattern.compile("[a-z][a-z0-9_-]{0,63}");

    private final SystemOrganizationNodeMapper nodeMapper;
    private final OrganizationQueryService queryService;
    private final OrganizationUniquenessService uniquenessService;
    private final IdentifierGenerator identifierGenerator;
    private final CurrentUserContext currentUserContext;

    public OrganizationCommandService(
            SystemOrganizationNodeMapper nodeMapper,
            OrganizationQueryService queryService,
            OrganizationUniquenessService uniquenessService,
            IdentifierGenerator identifierGenerator,
            CurrentUserContext currentUserContext
    ) {
        this.nodeMapper = nodeMapper;
        this.queryService = queryService;
        this.uniquenessService = uniquenessService;
        this.identifierGenerator = identifierGenerator;
        this.currentUserContext = currentUserContext;
    }

    @Transactional
    public Long create(OrganizationNodeCreateRequest request) {
        return createNode(
                null,
                request.parentId(),
                request.nodeType(),
                request.nodeCode(),
                request.nodeName(),
                request.sortOrder(),
                request.description()
        );
    }

    @Override
    @Transactional
    public Long create(SystemOrganizationCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("创建系统组织节点参数不能为空");
        }
        return createNode(
                command.nodeId(),
                command.parentId(),
                command.nodeType(),
                command.nodeCode(),
                command.nodeName(),
                command.sortOrder(),
                command.description()
        );
    }

    @Transactional
    public void update(Long nodeId, OrganizationNodeUpdateRequest request) {
        SystemOrganizationNode node = requireNodeForUpdate(nodeId);
        String nodeCode = normalizeCode(request.nodeCode());
        String nodeName = normalizeName(request.nodeName());
        String description = normalizeOptionalText(
                request.description(),
                OrganizationConstants.MAX_DESCRIPTION_LENGTH,
                "节点说明"
        );
        uniquenessService.requireCodeAvailable(nodeCode, nodeId);
        uniquenessService.requireNameAvailable(node.getParentId(), nodeName, nodeId);
        String newFullPath = replaceNodeName(node.getFullPath(), nodeName);
        nodeMapper.updateSubtreeAfterEdit(
                nodeId,
                node.getPathIds(),
                node.getFullPath(),
                newFullPath,
                nodeCode,
                nodeName,
                valueOrZero(request.sortOrder()),
                description,
                Instant.now(),
                currentOperatorId()
        );
    }

    public OrganizationNodeMoveImpact moveImpact(Long nodeId, Long newParentId) {
        SystemOrganizationNode node = queryService.requireNode(nodeId);
        SystemOrganizationNode newParent = newParentId == null
                ? null
                : queryService.requireNode(newParentId);
        MovePlan plan = prepareMove(node, newParent);
        int nodeCount = Math.toIntExact(nodeMapper.selectCount(
                new LambdaQueryWrapper<SystemOrganizationNode>()
                        .likeRight(SystemOrganizationNode::getPathIds, node.getPathIds())
        ));
        return new OrganizationNodeMoveImpact(nodeCount, plan.newFullPath());
    }

    @Transactional
    public void move(Long nodeId, OrganizationNodeMoveRequest request) {
        SystemOrganizationNode node = requireNodeForUpdate(nodeId);
        SystemOrganizationNode newParent = request.parentId() == null
                ? null
                : requireNodeForUpdate(request.parentId());
        MovePlan plan = prepareMove(node, newParent);
        List<SystemOrganizationNode> subtree =
                nodeMapper.selectSubtreeForUpdate(node.getPathIds());
        int maxDepth = subtree.stream()
                .mapToInt(SystemOrganizationNode::getDepth)
                .max()
                .orElse(node.getDepth());
        if (maxDepth + plan.depthDelta() > OrganizationConstants.MAX_DEPTH) {
            throw PlatformException.badRequest(
                    "移动后组织层级不能超过 " + OrganizationConstants.MAX_DEPTH + " 层"
            );
        }
        nodeMapper.updateSubtreeAfterMove(
                nodeId,
                node.getPathIds(),
                plan.newPathIds(),
                node.getFullPath(),
                plan.newFullPath(),
                request.parentId(),
                plan.depthDelta(),
                Instant.now(),
                currentOperatorId()
        );
    }

    @Transactional
    public void delete(Long nodeId) {
        SystemOrganizationNode node = requireNodeForUpdate(nodeId);
        if (node.getId() == BuiltInOrganizationIds.DEFAULT_ORGANIZATION) {
            throw PlatformException.conflict("默认组织不能删除");
        }
        boolean hasChildren = nodeMapper.exists(
                new LambdaQueryWrapper<SystemOrganizationNode>()
                        .eq(SystemOrganizationNode::getParentId, nodeId)
        );
        if (hasChildren) {
            throw PlatformException.conflict("组织节点下仍有下级节点，不能删除");
        }
        nodeMapper.deleteById(nodeId);
    }

    private Long createNode(
            Long explicitNodeId,
            Long parentId,
            SystemOrganizationNodeType nodeType,
            String rawNodeCode,
            String rawNodeName,
            Integer sortOrder,
            String rawDescription
    ) {
        if (nodeType == null) {
            throw PlatformException.badRequest("节点类型不能为空");
        }
        if (explicitNodeId != null && explicitNodeId <= 0) {
            throw new IllegalArgumentException("系统组织节点主键必须大于 0");
        }
        if (explicitNodeId != null && nodeMapper.selectById(explicitNodeId) != null) {
            throw PlatformException.conflict("组织节点主键已经存在");
        }
        SystemOrganizationNode parent = parentId == null ? null : requireNodeForUpdate(parentId);
        validateParentType(parent, nodeType);
        int depth = parent == null ? 1 : parent.getDepth() + 1;
        if (depth > OrganizationConstants.MAX_DEPTH) {
            throw PlatformException.badRequest(
                    "组织层级不能超过 " + OrganizationConstants.MAX_DEPTH + " 层"
            );
        }
        String nodeCode = normalizeCode(rawNodeCode);
        String nodeName = normalizeName(rawNodeName);
        uniquenessService.requireCodeAvailable(nodeCode, null);
        uniquenessService.requireNameAvailable(parentId, nodeName, null);

        SystemOrganizationNode node = new SystemOrganizationNode();
        Long nodeId = explicitNodeId;
        if (nodeId == null) {
            nodeId = identifierGenerator.nextId(node).longValue();
        }
        node.setId(nodeId);
        node.setParentId(parentId);
        node.setNodeType(nodeType);
        node.setNodeCode(nodeCode);
        node.setNodeName(nodeName);
        node.setPathIds(pathIds(parent, nodeId));
        node.setFullPath(fullPath(parent, nodeName));
        node.setDepth(depth);
        node.setSortOrder(valueOrZero(sortOrder));
        node.setDescription(normalizeOptionalText(
                rawDescription,
                OrganizationConstants.MAX_DESCRIPTION_LENGTH,
                "节点说明"
        ));
        nodeMapper.insert(node);
        return node.getId();
    }

    private MovePlan prepareMove(
            SystemOrganizationNode node,
            SystemOrganizationNode newParent
    ) {
        Long newParentId = newParent == null ? null : newParent.getId();
        if (Objects.equals(node.getParentId(), newParentId)) {
            throw PlatformException.badRequest("上级组织没有变化");
        }
        if (newParent != null
                && (newParent.getId().equals(node.getId())
                || newParent.getPathIds().startsWith(node.getPathIds()))) {
            throw PlatformException.badRequest("不能移动到当前节点或其下级节点");
        }
        validateParentType(newParent, node.getNodeType());
        uniquenessService.requireNameAvailable(newParentId, node.getNodeName(), node.getId());
        int newDepth = newParent == null ? 1 : newParent.getDepth() + 1;
        int depthDelta = newDepth - node.getDepth();
        String newPathIds = pathIds(newParent, node.getId());
        String newFullPath = fullPath(newParent, node.getNodeName());
        return new MovePlan(newPathIds, newFullPath, depthDelta);
    }

    private void validateParentType(
            SystemOrganizationNode parent,
            SystemOrganizationNodeType childType
    ) {
        if (parent == null) {
            if (childType != SystemOrganizationNodeType.ORGANIZATION) {
                throw PlatformException.badRequest("顶级节点只能是组织");
            }
            return;
        }
        boolean allowed = switch (parent.getNodeType()) {
            case ORGANIZATION ->
                    childType == SystemOrganizationNodeType.ORGANIZATION
                            || childType == SystemOrganizationNodeType.DEPARTMENT;
            case DEPARTMENT ->
                    childType == SystemOrganizationNodeType.DEPARTMENT
                            || childType == SystemOrganizationNodeType.GROUP;
            case GROUP -> childType == SystemOrganizationNodeType.GROUP;
        };
        if (!allowed) {
            throw PlatformException.badRequest("当前上级节点不允许添加该类型的下级节点");
        }
    }

    private SystemOrganizationNode requireNodeForUpdate(Long nodeId) {
        SystemOrganizationNode node = nodeId == null
                ? null
                : nodeMapper.selectByIdForUpdate(nodeId);
        if (node == null) {
            throw PlatformException.notFound("组织节点不存在");
        }
        return node;
    }

    private String normalizeCode(String value) {
        if (!StringUtils.hasText(value)) {
            throw PlatformException.badRequest("节点编码不能为空");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!NODE_CODE_PATTERN.matcher(normalized).matches()) {
            throw PlatformException.badRequest(
                    "节点编码只能包含字母、数字、下划线和连字符，并以字母开头，"
                            + "长度不能超过 " + OrganizationConstants.MAX_CODE_LENGTH + " 个字符"
            );
        }
        return normalized;
    }

    private String normalizeName(String value) {
        String normalized = normalizeRequiredText(
                value,
                OrganizationConstants.MAX_NAME_LENGTH,
                "节点名称"
        );
        if (normalized.contains("/")) {
            throw PlatformException.badRequest("节点名称不能包含斜杠");
        }
        return normalized;
    }

    private String normalizeRequiredText(String value, int maxLength, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw PlatformException.badRequest(fieldName + "不能为空");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw PlatformException.badRequest(fieldName + "不能超过 " + maxLength + " 个字符");
        }
        return normalized;
    }

    private String normalizeOptionalText(String value, int maxLength, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw PlatformException.badRequest(fieldName + "不能超过 " + maxLength + " 个字符");
        }
        return normalized;
    }

    private String pathIds(SystemOrganizationNode parent, Long nodeId) {
        return parent == null ? "/" + nodeId + "/" : parent.getPathIds() + nodeId + "/";
    }

    private String fullPath(SystemOrganizationNode parent, String nodeName) {
        return parent == null ? nodeName : parent.getFullPath() + " / " + nodeName;
    }

    private String replaceNodeName(String fullPath, String nodeName) {
        int separatorIndex = fullPath.lastIndexOf(" / ");
        return separatorIndex < 0
                ? nodeName
                : fullPath.substring(0, separatorIndex + 3) + nodeName;
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private Long currentOperatorId() {
        return currentUserContext.findUserId().orElse(OperatorConstants.SYSTEM_OPERATOR_ID);
    }

    private record MovePlan(String newPathIds, String newFullPath, int depthDelta) {
    }
}
