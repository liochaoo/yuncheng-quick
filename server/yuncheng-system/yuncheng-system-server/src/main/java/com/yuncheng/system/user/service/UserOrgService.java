package com.yuncheng.system.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.organization.entity.SystemOrg;
import com.yuncheng.system.organization.service.OrgQueryService;
import com.yuncheng.system.user.constant.UserOrgConstants;
import com.yuncheng.system.user.dto.UserOrgAssignment;
import com.yuncheng.system.user.dto.UserPrimaryOrgSummary;
import com.yuncheng.system.user.entity.SystemUserOrg;
import com.yuncheng.system.user.mapper.SystemUserOrgMapper;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;

/** 维护和查询用户的直接组织归属。 */
@Service
public class UserOrgService {

    private final SystemUserOrgMapper userOrgMapper;
    private final OrgQueryService orgQueryService;

    public UserOrgService(
            SystemUserOrgMapper userOrgMapper,
            OrgQueryService orgQueryService
    ) {
        this.userOrgMapper = userOrgMapper;
        this.orgQueryService = orgQueryService;
    }

    public void replace(Long userId, Collection<Long> requestedOrgIds, Long primaryOrgId) {
        requirePositiveIds(userId, primaryOrgId);
        Set<Long> orgIds = validate(requestedOrgIds, primaryOrgId);
        orgQueryService.requireOrgs(orgIds);
        List<SystemUserOrg> existing = relationsByUserIds(List.of(userId));
        Map<Long, SystemUserOrg> existingByOrgId = existing.stream()
                .collect(java.util.stream.Collectors.toMap(
                        SystemUserOrg::getOrgId,
                        relation -> relation
                ));
        Long existingPrimaryOrgId = existing.stream()
                .filter(relation -> Boolean.TRUE.equals(relation.getPrimary()))
                .map(SystemUserOrg::getOrgId)
                .findFirst()
                .orElse(null);
        if (existingByOrgId.keySet().equals(orgIds)
                && Objects.equals(existingPrimaryOrgId, primaryOrgId)) {
            return;
        }
        if (!Objects.equals(existingPrimaryOrgId, primaryOrgId)
                && existingPrimaryOrgId != null) {
            SystemUserOrg existingPrimary = existingByOrgId.get(existingPrimaryOrgId);
            existingPrimary.setPrimary(false);
            userOrgMapper.updateById(existingPrimary);
        }
        List<Long> removedRelationIds = existing.stream()
                .filter(relation -> !orgIds.contains(relation.getOrgId()))
                .map(SystemUserOrg::getId)
                .toList();
        if (!removedRelationIds.isEmpty()) {
            userOrgMapper.deleteByIds(removedRelationIds);
        }
        insertBatch(orgIds.stream()
                .filter(orgId -> !existingByOrgId.containsKey(orgId))
                .map(orgId -> relation(userId, orgId, orgId.equals(primaryOrgId)))
                .toList());
        if (!Objects.equals(existingPrimaryOrgId, primaryOrgId)
                && existingByOrgId.containsKey(primaryOrgId)) {
            SystemUserOrg newPrimary = existingByOrgId.get(primaryOrgId);
            newPrimary.setPrimary(true);
            userOrgMapper.updateById(newPrimary);
        }
    }

    public void bindPrimary(Long userId, Long orgId) {
        replace(userId, List.of(orgId), orgId);
    }

    public void bindPrimaryBatch(Map<Long, Long> userPrimaryOrgIds) {
        if (userPrimaryOrgIds == null || userPrimaryOrgIds.isEmpty()) {
            return;
        }
        userPrimaryOrgIds.forEach(this::requirePositiveIds);
        orgQueryService.requireOrgs(new LinkedHashSet<>(userPrimaryOrgIds.values()));
        insertBatch(userPrimaryOrgIds.entrySet().stream()
                .map(entry -> relation(entry.getKey(), entry.getValue(), true))
                .toList());
    }

    public UserOrgAssignment assignment(Long userId) {
        List<SystemUserOrg> relations = relationsByUserIds(List.of(userId));
        if (relations.isEmpty()) {
            throw PlatformException.serviceUnavailable("用户组织归属数据不完整");
        }
        SystemUserOrg primary = relations.stream()
                .filter(relation -> Boolean.TRUE.equals(relation.getPrimary()))
                .findFirst()
                .orElseThrow(() -> PlatformException.serviceUnavailable(
                        "用户主归属数据不完整"
                ));
        return new UserOrgAssignment(
                relations.stream()
                        .map(SystemUserOrg::getOrgId)
                        .sorted()
                        .map(String::valueOf)
                        .toList(),
                primary.getOrgId().toString()
        );
    }

    public Map<Long, UserPrimaryOrgSummary> primarySummariesByUserIds(
            Collection<Long> userIds
    ) {
        List<SystemUserOrg> relations = relationsByUserIds(userIds);
        if (relations.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> relationCounts = new HashMap<>();
        relations.forEach(relation -> relationCounts.merge(relation.getUserId(), 1, Integer::sum));
        List<SystemUserOrg> primaryRelations = relations.stream()
                .filter(relation -> Boolean.TRUE.equals(relation.getPrimary()))
                .toList();
        Map<Long, SystemOrg> orgs = orgQueryService.requireOrgs(
                primaryRelations.stream().map(SystemUserOrg::getOrgId).toList()
        );
        Map<Long, UserPrimaryOrgSummary> result = new LinkedHashMap<>();
        primaryRelations.forEach(relation -> {
            SystemOrg org = orgs.get(relation.getOrgId());
            result.put(relation.getUserId(), new UserPrimaryOrgSummary(
                    org.getId().toString(),
                    org.getOrgType(),
                    org.getOrgCode(),
                    org.getOrgName(),
                    org.getFullPath(),
                    Math.max(0, relationCounts.getOrDefault(relation.getUserId(), 1) - 1)
            ));
        });
        return result;
    }

    public long countByOrgId(Long orgId) {
        return userOrgMapper.selectCount(new LambdaQueryWrapper<SystemUserOrg>()
                .eq(SystemUserOrg::getOrgId, orgId));
    }

    public long countDistinctUsersInSubtree(String pathIds) {
        return userOrgMapper.countDistinctUsersInSubtree(pathIds);
    }

    public long countRelationsInSubtree(String pathIds) {
        return userOrgMapper.countRelationsInSubtree(pathIds);
    }

    public void deleteByUserId(Long userId) {
        userOrgMapper.delete(new LambdaQueryWrapper<SystemUserOrg>()
                .eq(SystemUserOrg::getUserId, userId));
    }

    private List<SystemUserOrg> relationsByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userOrgMapper.selectList(new LambdaQueryWrapper<SystemUserOrg>()
                .in(SystemUserOrg::getUserId, userIds)
                .orderByDesc(SystemUserOrg::getPrimary)
                .orderByAsc(SystemUserOrg::getId));
    }

    private Set<Long> validate(Collection<Long> requestedOrgIds, Long primaryOrgId) {
        if (requestedOrgIds == null || requestedOrgIds.isEmpty()) {
            throw PlatformException.badRequest("至少需要选择一个组织归属");
        }
        if (requestedOrgIds.size() > UserOrgConstants.MAX_ORG_COUNT) {
            throw PlatformException.badRequest(
                    "单个用户最多归属 " + UserOrgConstants.MAX_ORG_COUNT + " 个组织"
            );
        }
        if (requestedOrgIds.contains(null)) {
            throw PlatformException.badRequest("组织主键不能为空");
        }
        Set<Long> orgIds = new LinkedHashSet<>(requestedOrgIds);
        if (orgIds.size() != requestedOrgIds.size()) {
            throw PlatformException.badRequest("组织归属不能重复");
        }
        if (primaryOrgId == null || !orgIds.contains(primaryOrgId)) {
            throw PlatformException.badRequest("主归属必须包含在组织归属中");
        }
        return orgIds;
    }

    private SystemUserOrg relation(Long userId, Long orgId, boolean primary) {
        SystemUserOrg relation = new SystemUserOrg();
        relation.setUserId(userId);
        relation.setOrgId(orgId);
        relation.setPrimary(primary);
        return relation;
    }

    private void insertBatch(List<SystemUserOrg> relations) {
        if (!relations.isEmpty()) {
            userOrgMapper.insert(relations, UserOrgConstants.BATCH_SIZE);
        }
    }

    private void requirePositiveIds(Long userId, Long primaryOrgId) {
        if (userId == null || userId <= 0
                || primaryOrgId == null || primaryOrgId <= 0) {
            throw new IllegalArgumentException("用户和主归属组织主键必须大于 0");
        }
    }
}
