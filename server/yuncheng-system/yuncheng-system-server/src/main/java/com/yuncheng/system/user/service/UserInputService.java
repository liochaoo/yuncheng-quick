package com.yuncheng.system.user.service;

import com.yuncheng.framework.web.exception.PlatformException;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 统一规范化并校验用户输入字段。 */
@Service
public class UserInputService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[a-z][a-z0-9._-]{2,49}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$"
    );

    /** 规范化用于登录、查询和比较的登录名。 */
    public String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw PlatformException.badRequest("登录名不能为空");
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    /** 规范化并校验准备新建的登录名。 */
    public String normalizeNewUsername(String username) {
        String normalized = normalizeUsername(username);
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw PlatformException.badRequest("登录名格式不正确");
        }
        return normalized;
    }

    public String normalizeRealName(String realName) {
        if (!StringUtils.hasText(realName)) {
            throw PlatformException.badRequest("姓名不能为空");
        }
        String normalized = realName.trim();
        if (normalized.length() > 64) {
            throw PlatformException.badRequest("姓名不能超过 64 个字符");
        }
        return normalized;
    }

    public String normalizePhone(String phone) {
        String normalized = nullable(phone);
        if (normalized != null && !PHONE_PATTERN.matcher(normalized).matches()) {
            throw PlatformException.badRequest("手机号码格式不正确");
        }
        return normalized;
    }

    public String normalizeEmail(String email) {
        String normalized = nullable(email);
        if (normalized == null) {
            return null;
        }
        normalized = normalized.toLowerCase(Locale.ROOT);
        if (normalized.length() > 254 || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw PlatformException.badRequest("电子邮箱格式不正确");
        }
        return normalized;
    }

    public String requireEmail(String email) {
        String normalized = normalizeEmail(email);
        if (normalized == null) {
            throw PlatformException.badRequest("电子邮箱不能为空");
        }
        return normalized;
    }

    private String nullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
