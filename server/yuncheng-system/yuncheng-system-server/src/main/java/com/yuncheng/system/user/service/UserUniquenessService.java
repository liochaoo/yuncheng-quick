package com.yuncheng.system.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuncheng.framework.web.exception.PlatformException;
import com.yuncheng.system.user.entity.SystemUser;
import com.yuncheng.system.user.enums.UserUniqueField;
import com.yuncheng.system.user.mapper.SystemUserMapper;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 统一处理用户字段的唯一性查询和保存校验。 */
@Service
public class UserUniquenessService {

    private final SystemUserMapper userMapper;

    public UserUniquenessService(SystemUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public boolean isAvailable(UserUniqueField field, String value, Long excludedUserId) {
        String normalized = normalize(field, value);
        if (normalized == null) {
            return true;
        }
        LambdaQueryWrapper<SystemUser> wrapper = new LambdaQueryWrapper<SystemUser>()
                .ne(excludedUserId != null, SystemUser::getId, excludedUserId);
        switch (field) {
            case USERNAME -> wrapper.eq(SystemUser::getUsername, normalized);
            case PHONE -> wrapper.eq(SystemUser::getPhone, normalized);
            case EMAIL -> wrapper.eq(SystemUser::getEmail, normalized);
        }
        return userMapper.selectCount(wrapper) == 0;
    }

    public void requireAvailable(UserUniqueField field, String value, Long excludedUserId) {
        if (isAvailable(field, value, excludedUserId)) {
            return;
        }
        String message = switch (field) {
            case USERNAME -> "登录名已经存在";
            case PHONE -> "手机号码已经存在";
            case EMAIL -> "电子邮箱已经存在";
        };
        throw PlatformException.conflict(message);
    }

    private String normalize(UserUniqueField field, String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        return switch (field) {
            case USERNAME, EMAIL -> normalized.toLowerCase(Locale.ROOT);
            case PHONE -> normalized;
        };
    }
}
