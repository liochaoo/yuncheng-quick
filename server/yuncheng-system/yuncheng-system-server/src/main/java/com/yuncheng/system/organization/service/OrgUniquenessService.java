package com.yuncheng.system.organization.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.organization.entity.SystemOrg;
import com.yuncheng.system.organization.enums.OrgUniqueField;
import com.yuncheng.system.organization.mapper.SystemOrgMapper;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** 校验组织编码和同级名称唯一性。 */
@Service
public class OrgUniquenessService {

    private final SystemOrgMapper orgMapper;

    public OrgUniquenessService(SystemOrgMapper orgMapper) {
        this.orgMapper = orgMapper;
    }

    public void requireCodeAvailable(String orgCode, Long excludedOrgId) {
        boolean exists = orgMapper.exists(new LambdaQueryWrapper<SystemOrg>()
                .eq(SystemOrg::getOrgCode, orgCode)
                .ne(excludedOrgId != null, SystemOrg::getId, excludedOrgId));
        if (exists) {
            throw PlatformException.conflict("组织编码已存在");
        }
    }

    public void requireNameAvailable(Long parentId, String orgName, Long excludedOrgId) {
        LambdaQueryWrapper<SystemOrg> wrapper =
                new LambdaQueryWrapper<SystemOrg>()
                        .eq(SystemOrg::getOrgName, orgName)
                        .ne(excludedOrgId != null, SystemOrg::getId, excludedOrgId);
        if (parentId == null) {
            wrapper.isNull(SystemOrg::getParentId);
        } else {
            wrapper.eq(SystemOrg::getParentId, parentId);
        }
        if (orgMapper.exists(wrapper)) {
            throw PlatformException.conflict("同级组织名称已存在");
        }
    }

    public boolean isAvailable(
            OrgUniqueField field,
            String value,
            Long parentId,
            Long excludedOrgId
    ) {
        String normalized = value.trim();
        return switch (field) {
            case ORG_CODE -> !orgMapper.exists(new LambdaQueryWrapper<SystemOrg>()
                    .eq(SystemOrg::getOrgCode, normalized.toLowerCase(Locale.ROOT))
                    .ne(excludedOrgId != null, SystemOrg::getId, excludedOrgId));
            case ORG_NAME -> {
                LambdaQueryWrapper<SystemOrg> wrapper =
                        new LambdaQueryWrapper<SystemOrg>()
                                .eq(SystemOrg::getOrgName, normalized)
                                .ne(excludedOrgId != null, SystemOrg::getId, excludedOrgId);
                if (parentId == null) {
                    wrapper.isNull(SystemOrg::getParentId);
                } else {
                    wrapper.eq(SystemOrg::getParentId, parentId);
                }
                yield !orgMapper.exists(wrapper);
            }
        };
    }
}
