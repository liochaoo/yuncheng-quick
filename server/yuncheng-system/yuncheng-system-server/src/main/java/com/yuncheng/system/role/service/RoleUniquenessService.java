package com.yuncheng.system.role.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.role.entity.SystemRole;
import com.yuncheng.system.role.enums.RoleUniqueField;
import com.yuncheng.system.role.mapper.SystemRoleMapper;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** 统一处理角色字段的唯一性查询和保存校验。 */
@Service
public class RoleUniquenessService {

    private final SystemRoleMapper roleMapper;

    public RoleUniquenessService(SystemRoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public boolean isAvailable(RoleUniqueField field, String value, Long excludedRoleId) {
        String normalized = normalize(field, value);
        LambdaQueryWrapper<SystemRole> wrapper = new LambdaQueryWrapper<SystemRole>()
                .ne(excludedRoleId != null, SystemRole::getId, excludedRoleId);
        switch (field) {
            case ROLE_CODE -> wrapper.eq(SystemRole::getRoleCode, normalized);
            case ROLE_NAME -> wrapper.eq(SystemRole::getRoleName, normalized);
        }
        return roleMapper.selectCount(wrapper) == 0;
    }

    public void requireAvailable(RoleUniqueField field, String value, Long excludedRoleId) {
        if (isAvailable(field, value, excludedRoleId)) {
            return;
        }
        String message = switch (field) {
            case ROLE_CODE -> "角色编码已经存在";
            case ROLE_NAME -> "角色名称已经存在";
        };
        throw PlatformException.conflict(message);
    }

    private String normalize(RoleUniqueField field, String value) {
        String normalized = value.trim();
        return field == RoleUniqueField.ROLE_CODE
                ? normalized.toLowerCase(Locale.ROOT)
                : normalized;
    }
}
