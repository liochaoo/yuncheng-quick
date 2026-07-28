package com.yuncheng.common.constant;

import java.time.Duration;

/** 可重建数据缓存的统一有效期档位。 */
public final class CacheTtlConstants {

    /** 变化较快、允许较快回源的数据。 */
    public static final Duration SHORT = Duration.ofMinutes(5);

    /** 一般业务数据。 */
    public static final Duration MEDIUM = Duration.ofMinutes(30);

    /** 变化较少、回源成本较高的数据。 */
    public static final Duration LONG = Duration.ofHours(2);

    /** 很少变化、允许长时间复用的数据。 */
    public static final Duration VERY_LONG = Duration.ofDays(1);

    private CacheTtlConstants() {
    }
}
