package com.yuncheng.init.system.runner;

import com.yuncheng.init.system.service.SystemAdminInitializer;
import com.yuncheng.init.system.service.SystemRoleInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 在数据库迁移完成后初始化平台保留角色和管理员账号。 */
@Order(0)
@Component
public class SystemInitializationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemInitializationRunner.class);

    private final SystemRoleInitializer roleInitializer;
    private final SystemAdminInitializer adminInitializer;

    public SystemInitializationRunner(
            SystemRoleInitializer roleInitializer,
            SystemAdminInitializer adminInitializer
    ) {
        this.roleInitializer = roleInitializer;
        this.adminInitializer = adminInitializer;
    }

    @Override
    public void run(ApplicationArguments args) {
        Long superAdminRoleId = roleInitializer.initialize();
        adminInitializer.initialize(superAdminRoleId);
        log.info("平台保留角色和管理员账号检查完成");
    }
}
