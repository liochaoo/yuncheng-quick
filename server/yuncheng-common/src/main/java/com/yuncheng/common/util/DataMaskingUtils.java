package com.yuncheng.common.util;

/** 常用数据脱敏工具。 */
public final class DataMaskingUtils {

    private DataMaskingUtils() {
    }

    /** 手机号码保留必要的首尾字符，中间内容使用星号替代。 */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        int length = phone.length();
        if (length <= 2) {
            return "*".repeat(length);
        }
        if (length <= 7) {
            return phone.substring(0, 1)
                    + "*".repeat(length - 2)
                    + phone.substring(length - 1);
        }
        return phone.substring(0, 3)
                + "*".repeat(length - 7)
                + phone.substring(length - 4);
    }

    /** 电子邮箱保留域名，本地名称仅保留必要的首尾字符。 */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        int separator = email.lastIndexOf('@');
        if (separator <= 0) {
            return maskText(email);
        }
        String localPart = email.substring(0, separator);
        return maskText(localPart) + email.substring(separator);
    }

    private static String maskText(String value) {
        int length = value.length();
        if (length <= 1) {
            return "*";
        }
        if (length == 2) {
            return value.substring(0, 1) + "*";
        }
        return value.substring(0, 1)
                + "*".repeat(length - 2)
                + value.substring(length - 1);
    }
}
