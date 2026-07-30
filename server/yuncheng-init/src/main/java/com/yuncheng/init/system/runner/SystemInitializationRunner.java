package com.yuncheng.init.system.runner;

import com.yuncheng.init.system.service.SystemAdminInitializer;
import com.yuncheng.init.system.service.SystemOrgInitializer;
import com.yuncheng.init.system.service.SystemRoleInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 在数据库迁移完成后初始化默认组织、平台保留角色和管理员账号。 */
@Order(0)
@Component
public class SystemInitializationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemInitializationRunner.class);

    private final SystemOrgInitializer orgInitializer;
    private final SystemRoleInitializer roleInitializer;
    private final SystemAdminInitializer adminInitializer;

    public SystemInitializationRunner(
            SystemOrgInitializer orgInitializer,
            SystemRoleInitializer roleInitializer,
            SystemAdminInitializer adminInitializer
    ) {
        this.orgInitializer = orgInitializer;
        this.roleInitializer = roleInitializer;
        this.adminInitializer = adminInitializer;
    }

    @Override
    public void run(ApplicationArguments args) {
        orgInitializer.initialize();
        Long superAdminRoleId = roleInitializer.initialize();
        adminInitializer.initialize(superAdminRoleId);
        log.info("默认组织、平台保留角色和管理员账号检查完成");
    }
}
