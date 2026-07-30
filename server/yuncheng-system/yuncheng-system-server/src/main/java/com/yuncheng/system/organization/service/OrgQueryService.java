package com.yuncheng.system.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuncheng.common.constant.BuiltInOrgIds;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.framework.web.page.PageResult;
import com.yuncheng.system.api.organization.SystemOrgInfo;
import com.yuncheng.system.api.organization.SystemOrgQueryApi;
import com.yuncheng.system.organization.dto.OrgDetail;
import com.yuncheng.system.organization.dto.OrgItem;
import com.yuncheng.system.organization.dto.OrgPageQuery;
import com.yuncheng.system.organization.entity.SystemOrg;
import com.yuncheng.system.organization.mapper.SystemOrgMapper;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 查询组织、异步树和普通业务可消费的节点选项。 */
@Service
public class OrgQueryService implements SystemOrgQueryApi {

    private final SystemOrgMapper orgMapper;

    public OrgQueryService(SystemOrgMapper orgMapper) {
        this.orgMapper = orgMapper;
    }

    public PageResult<OrgItem> page(OrgPageQuery query) {
        String keyword = normalizedText(query.getKeyword());
        String codeKeyword = normalizedCode(keyword);
        LambdaQueryWrapper<SystemOrg> wrapper =
                new LambdaQueryWrapper<SystemOrg>();
        if (StringUtils.hasText(keyword)) {
            if (isAscii(keyword)) {
                wrapper.and(nested -> nested
                        .like(SystemOrg::getOrgCode, codeKeyword)
                        .or()
                        .like(SystemOrg::getOrgName, keyword)
                        .or()
                        .like(SystemOrg::getFullPath, keyword));
            } else {
                wrapper.and(nested -> nested
                        .like(SystemOrg::getOrgName, keyword)
                        .or()
                        .like(SystemOrg::getFullPath, keyword));
            }
        } else {
            wrapper.isNull(SystemOrg::getParentId);
        }
        wrapper.orderByAsc(SystemOrg::getSortOrder, SystemOrg::getId);
        IPage<SystemOrg> page = orgMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()),
                wrapper
        );
        List<OrgItem> items = toItems(page.getRecords());
        return PageResult.of(items, page.getTotal(), query);
    }

    public List<OrgItem> children(Long parentId) {
        if (parentId != null) {
            requireOrg(parentId);
        }
        LambdaQueryWrapper<SystemOrg> wrapper =
                new LambdaQueryWrapper<SystemOrg>();
        if (parentId == null) {
            wrapper.isNull(SystemOrg::getParentId);
        } else {
            wrapper.eq(SystemOrg::getParentId, parentId);
        }
        wrapper.orderByAsc(SystemOrg::getSortOrder, SystemOrg::getId);
        return toItems(orgMapper.selectList(wrapper));
    }

    public OrgDetail detail(Long orgId) {
        SystemOrg org = requireOrg(orgId);
        String parentName = org.getParentId() == null
                ? null
                : requireOrg(org.getParentId()).getOrgName();
        return new OrgDetail(
                id(org.getId()),
                id(org.getParentId()),
                parentName,
                org.getOrgType(),
                org.getOrgCode(),
                org.getOrgName(),
                org.getFullPath(),
                org.getDepth(),
                org.getSortOrder(),
                org.getDescription(),
                org.getCreatedAt(),
                id(org.getCreatedBy()),
                org.getUpdatedAt(),
                id(org.getUpdatedBy())
        );
    }

    public OrgItem item(Long orgId) {
        return toItems(List.of(requireOrg(orgId))).getFirst();
    }

    public SystemOrg requireOrg(Long orgId) {
        SystemOrg org = orgId == null ? null : orgMapper.selectById(orgId);
        if (org == null) {
            throw PlatformException.notFound("组织不存在");
        }
        return org;
    }

    @Override
    public Optional<SystemOrgInfo> findById(Long orgId) {
        return Optional.ofNullable(orgId == null ? null : orgMapper.selectById(orgId))
                .map(this::toSystemInfo);
    }

    @Override
    public Optional<SystemOrgInfo> findByCode(String orgCode) {
        String normalized = normalizedCode(orgCode);
        if (!StringUtils.hasText(normalized)) {
            return Optional.empty();
        }
        return Optional.ofNullable(orgMapper.selectOne(
                new LambdaQueryWrapper<SystemOrg>()
                        .eq(SystemOrg::getOrgCode, normalized)
        )).map(this::toSystemInfo);
    }

    private List<OrgItem> toItems(List<SystemOrg> orgs) {
        if (orgs.isEmpty()) {
            return List.of();
        }
        List<Long> orgIds = orgs.stream().map(SystemOrg::getId).toList();
        Set<Long> parentIdsWithChildren = new HashSet<>();
        orgMapper.selectList(new LambdaQueryWrapper<SystemOrg>()
                        .select(SystemOrg::getParentId)
                        .in(SystemOrg::getParentId, orgIds))
                .forEach(child -> parentIdsWithChildren.add(child.getParentId()));
        return orgs.stream()
                .map(org -> new OrgItem(
                        id(org.getId()),
                        id(org.getParentId()),
                        org.getOrgType(),
                        org.getOrgCode(),
                        org.getOrgName(),
                        org.getFullPath(),
                        org.getDepth(),
                        org.getSortOrder(),
                        parentIdsWithChildren.contains(org.getId()),
                        org.getId() == BuiltInOrgIds.DEFAULT_ORG,
                        ancestorIds(org)
                ))
                .toList();
    }

    private List<String> ancestorIds(SystemOrg org) {
        return Arrays.stream(org.getPathIds().split("/"))
                .filter(StringUtils::hasText)
                .filter(value -> !value.equals(org.getId().toString()))
                .toList();
    }

    private SystemOrgInfo toSystemInfo(SystemOrg org) {
        return new SystemOrgInfo(
                org.getId(),
                org.getParentId(),
                org.getOrgType(),
                org.getOrgCode(),
                org.getOrgName(),
                org.getFullPath()
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
