package com.yuncheng.system.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.yuncheng.common.constant.BuiltInOrgIds;
import com.yuncheng.common.constant.OperatorConstants;
import com.yuncheng.common.context.CurrentUserContext;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.api.organization.SystemOrgCommandApi;
import com.yuncheng.system.api.organization.SystemOrgCreateCommand;
import com.yuncheng.system.api.organization.SystemOrgType;
import com.yuncheng.system.organization.constant.OrgConstants;
import com.yuncheng.system.organization.dto.OrgCreateRequest;
import com.yuncheng.system.organization.dto.OrgMoveImpact;
import com.yuncheng.system.organization.dto.OrgMoveRequest;
import com.yuncheng.system.organization.dto.OrgUpdateRequest;
import com.yuncheng.system.organization.entity.SystemOrg;
import com.yuncheng.system.organization.mapper.SystemOrgMapper;
import com.yuncheng.system.user.service.UserOrgService;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 新增、编辑、移动和删除组织。 */
@Service
public class OrgCommandService implements SystemOrgCommandApi {

    private static final Pattern ORG_CODE_PATTERN =
            Pattern.compile("[a-z][a-z0-9_-]{0,63}");

    private final SystemOrgMapper orgMapper;
    private final OrgQueryService queryService;
    private final OrgUniquenessService uniquenessService;
    private final IdentifierGenerator identifierGenerator;
    private final CurrentUserContext currentUserContext;
    private final UserOrgService userOrgService;

    public OrgCommandService(
            SystemOrgMapper orgMapper,
            OrgQueryService queryService,
            OrgUniquenessService uniquenessService,
            IdentifierGenerator identifierGenerator,
            CurrentUserContext currentUserContext,
            UserOrgService userOrgService
    ) {
        this.orgMapper = orgMapper;
        this.queryService = queryService;
        this.uniquenessService = uniquenessService;
        this.identifierGenerator = identifierGenerator;
        this.currentUserContext = currentUserContext;
        this.userOrgService = userOrgService;
    }

    @Transactional
    public Long create(OrgCreateRequest request) {
        return createOrg(
                null,
                request.parentId(),
                request.orgType(),
                request.orgCode(),
                request.orgName(),
                request.sortOrder(),
                request.description()
        );
    }

    @Override
    @Transactional
    public Long create(SystemOrgCreateCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("创建系统组织参数不能为空");
        }
        return createOrg(
                command.orgId(),
                command.parentId(),
                command.orgType(),
                command.orgCode(),
                command.orgName(),
                command.sortOrder(),
                command.description()
        );
    }

    @Transactional
    public void update(Long orgId, OrgUpdateRequest request) {
        SystemOrg org = requireOrgForUpdate(orgId);
        String orgCode = normalizeCode(request.orgCode());
        String orgName = normalizeName(request.orgName());
        String description = normalizeOptionalText(
                request.description(),
                OrgConstants.MAX_DESCRIPTION_LENGTH,
                "组织说明"
        );
        uniquenessService.requireCodeAvailable(orgCode, orgId);
        uniquenessService.requireNameAvailable(org.getParentId(), orgName, orgId);
        int sortOrder = normalizeSortOrder(request.sortOrder());
        if (Objects.equals(org.getOrgName(), orgName)) {
            org.setOrgCode(orgCode);
            org.setSortOrder(sortOrder);
            org.setDescription(description);
            orgMapper.updateById(org);
            return;
        }
        String newFullPath = replaceOrgName(org.getFullPath(), orgName);
        orgMapper.updateSubtreeAfterRename(
                orgId,
                org.getPathIds(),
                org.getFullPath(),
                newFullPath,
                orgCode,
                orgName,
                sortOrder,
                description,
                Instant.now(),
                currentOperatorId()
        );
    }

    public OrgMoveImpact moveImpact(Long orgId, Long newParentId) {
        SystemOrg org = queryService.requireOrg(orgId);
        SystemOrg newParent = newParentId == null
                ? null
                : queryService.requireOrg(newParentId);
        MovePlan plan = prepareMove(org, newParent);
        validateMoveDepth(
                orgMapper.selectMaxDepthByPathIds(org.getPathIds()),
                plan.depthDelta()
        );
        int orgCount = Math.toIntExact(orgMapper.selectCount(
                new LambdaQueryWrapper<SystemOrg>()
                        .likeRight(SystemOrg::getPathIds, org.getPathIds())
        ));
        return new OrgMoveImpact(
                orgCount,
                userOrgService.countDistinctUsersInSubtree(org.getPathIds()),
                userOrgService.countRelationsInSubtree(org.getPathIds()),
                plan.newFullPath()
        );
    }

    @Transactional
    public void move(Long orgId, OrgMoveRequest request) {
        SystemOrg org = requireOrgForUpdate(orgId);
        SystemOrg newParent = request.parentId() == null
                ? null
                : requireOrgForUpdate(request.parentId());
        MovePlan plan = prepareMove(org, newParent);
        List<SystemOrg> subtree =
                orgMapper.selectSubtreeForUpdate(org.getPathIds());
        int maxDepth = subtree.stream()
                .mapToInt(SystemOrg::getDepth)
                .max()
                .orElse(org.getDepth());
        validateMoveDepth(maxDepth, plan.depthDelta());
        orgMapper.updateSubtreeAfterMove(
                orgId,
                org.getPathIds(),
                plan.newPathIds(),
                org.getFullPath(),
                plan.newFullPath(),
                request.parentId(),
                plan.depthDelta(),
                Instant.now(),
                currentOperatorId()
        );
    }

    @Transactional
    public void delete(Long orgId) {
        SystemOrg org = requireOrgForUpdate(orgId);
        if (org.getId() == BuiltInOrgIds.DEFAULT_ORG) {
            throw PlatformException.conflict("默认组织不能删除");
        }
        boolean hasChildren = orgMapper.exists(
                new LambdaQueryWrapper<SystemOrg>()
                        .eq(SystemOrg::getParentId, orgId)
        );
        if (hasChildren) {
            throw PlatformException.conflict("组织下仍有下级组织，不能删除");
        }
        if (userOrgService.countByOrgId(orgId) > 0) {
            throw PlatformException.conflict("组织下仍有用户归属，不能删除");
        }
        orgMapper.deleteById(orgId);
    }

    private Long createOrg(
            Long explicitOrgId,
            Long parentId,
            SystemOrgType orgType,
            String rawOrgCode,
            String rawOrgName,
            Integer sortOrder,
            String rawDescription
    ) {
        if (orgType == null) {
            throw PlatformException.badRequest("组织类型不能为空");
        }
        if (explicitOrgId != null && explicitOrgId <= 0) {
            throw new IllegalArgumentException("系统组织主键必须大于 0");
        }
        if (explicitOrgId != null && orgMapper.selectById(explicitOrgId) != null) {
            throw PlatformException.conflict("组织主键已经存在");
        }
        SystemOrg parent = parentId == null ? null : requireOrgForUpdate(parentId);
        validateParentType(parent, orgType);
        int depth = parent == null ? 1 : parent.getDepth() + 1;
        if (depth > OrgConstants.MAX_DEPTH) {
            throw PlatformException.badRequest(
                    "组织层级不能超过 " + OrgConstants.MAX_DEPTH + " 层"
            );
        }
        String orgCode = normalizeCode(rawOrgCode);
        String orgName = normalizeName(rawOrgName);
        uniquenessService.requireCodeAvailable(orgCode, null);
        uniquenessService.requireNameAvailable(parentId, orgName, null);

        SystemOrg org = new SystemOrg();
        Long orgId = explicitOrgId;
        if (orgId == null) {
            orgId = identifierGenerator.nextId(org).longValue();
        }
        org.setId(orgId);
        org.setParentId(parentId);
        org.setOrgType(orgType);
        org.setOrgCode(orgCode);
        org.setOrgName(orgName);
        org.setPathIds(pathIds(parent, orgId));
        org.setFullPath(fullPath(parent, orgName));
        org.setDepth(depth);
        org.setSortOrder(normalizeSortOrder(sortOrder));
        org.setDescription(normalizeOptionalText(
                rawDescription,
                OrgConstants.MAX_DESCRIPTION_LENGTH,
                "组织说明"
        ));
        orgMapper.insert(org);
        return org.getId();
    }

    private MovePlan prepareMove(
            SystemOrg org,
            SystemOrg newParent
    ) {
        Long newParentId = newParent == null ? null : newParent.getId();
        if (Objects.equals(org.getParentId(), newParentId)) {
            throw PlatformException.badRequest("上级组织没有变化");
        }
        if (newParent != null
                && (newParent.getId().equals(org.getId())
                || newParent.getPathIds().startsWith(org.getPathIds()))) {
            throw PlatformException.badRequest("不能移动到当前组织或其下级组织");
        }
        validateParentType(newParent, org.getOrgType());
        uniquenessService.requireNameAvailable(newParentId, org.getOrgName(), org.getId());
        int newDepth = newParent == null ? 1 : newParent.getDepth() + 1;
        int depthDelta = newDepth - org.getDepth();
        String newPathIds = pathIds(newParent, org.getId());
        String newFullPath = fullPath(newParent, org.getOrgName());
        return new MovePlan(newPathIds, newFullPath, depthDelta);
    }

    private void validateParentType(
            SystemOrg parent,
            SystemOrgType childType
    ) {
        if (parent == null) {
            if (childType != SystemOrgType.ORGANIZATION) {
                throw PlatformException.badRequest("顶级组织只能是组织");
            }
            return;
        }
        boolean allowed = switch (parent.getOrgType()) {
            case ORGANIZATION ->
                    childType == SystemOrgType.ORGANIZATION
                            || childType == SystemOrgType.DEPARTMENT;
            case DEPARTMENT ->
                    childType == SystemOrgType.DEPARTMENT
                            || childType == SystemOrgType.GROUP;
            case GROUP -> childType == SystemOrgType.GROUP;
        };
        if (!allowed) {
            throw PlatformException.badRequest("当前上级节点不允许添加该类型的下级组织");
        }
    }

    private SystemOrg requireOrgForUpdate(Long orgId) {
        SystemOrg org = orgId == null
                ? null
                : orgMapper.selectByIdForUpdate(orgId);
        if (org == null) {
            throw PlatformException.notFound("组织不存在");
        }
        return org;
    }

    private String normalizeCode(String value) {
        if (!StringUtils.hasText(value)) {
            throw PlatformException.badRequest("组织编码不能为空");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!ORG_CODE_PATTERN.matcher(normalized).matches()) {
            throw PlatformException.badRequest(
                    "组织编码只能包含字母、数字、下划线和连字符，并以字母开头，"
                            + "长度不能超过 " + OrgConstants.MAX_CODE_LENGTH + " 个字符"
            );
        }
        return normalized;
    }

    private String normalizeName(String value) {
        String normalized = normalizeRequiredText(
                value,
                OrgConstants.MAX_NAME_LENGTH,
                "组织名称"
        );
        if (normalized.contains("/")) {
            throw PlatformException.badRequest("组织名称不能包含斜杠");
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

    private String pathIds(SystemOrg parent, Long orgId) {
        return parent == null ? "/" + orgId + "/" : parent.getPathIds() + orgId + "/";
    }

    private String fullPath(SystemOrg parent, String orgName) {
        return parent == null ? orgName : parent.getFullPath() + " / " + orgName;
    }

    private String replaceOrgName(String fullPath, String orgName) {
        int separatorIndex = fullPath.lastIndexOf(" / ");
        return separatorIndex < 0
                ? orgName
                : fullPath.substring(0, separatorIndex + 3) + orgName;
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private int normalizeSortOrder(Integer value) {
        int normalized = valueOrZero(value);
        if (normalized < 0) {
            throw PlatformException.badRequest("排序号不能小于 0");
        }
        return normalized;
    }

    private void validateMoveDepth(Integer maxDepth, int depthDelta) {
        if (maxDepth == null) {
            throw PlatformException.notFound("组织不存在");
        }
        if (maxDepth + depthDelta > OrgConstants.MAX_DEPTH) {
            throw PlatformException.badRequest(
                    "移动后组织层级不能超过 " + OrgConstants.MAX_DEPTH + " 层"
            );
        }
    }

    private Long currentOperatorId() {
        return currentUserContext.findUserId().orElse(OperatorConstants.SYSTEM_OPERATOR_ID);
    }

    private record MovePlan(String newPathIds, String newFullPath, int depthDelta) {
    }
}
