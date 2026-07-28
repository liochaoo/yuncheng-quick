package com.yuncheng.framework.job.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 声明需要由平台统一调度并通过 Redis 锁控制的定时任务，Cron 使用服务器默认时区。 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlatformJob {

    String name();

    String cron();

    long lockAtMostForSeconds();
}
