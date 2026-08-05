package com.yuncheng.framework.security.jwt;

/** JWT 类型常量。 */
public final class JwtTokenTypes {

    public static final String ACCESS = "at+jwt";
    public static final String REFRESH = "rt+jwt";
    public static final String PASSWORD_CHANGE = "pwd-change+jwt";

    private JwtTokenTypes() {
    }
}
