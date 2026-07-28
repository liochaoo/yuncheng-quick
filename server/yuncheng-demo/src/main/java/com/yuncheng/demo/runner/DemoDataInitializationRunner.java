package com.yuncheng.demo.runner;

import com.yuncheng.demo.service.DemoMassDataInitializer;
import com.yuncheng.demo.service.DemoUserInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 在系统启动时初始化开发测试数据。 */
@Order(100)
@Component
public class DemoDataInitializationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializationRunner.class);

    private final DemoUserInitializer demoUserInitializer;
    private final DemoMassDataInitializer demoMassDataInitializer;

    public DemoDataInitializationRunner(
            DemoUserInitializer demoUserInitializer,
            DemoMassDataInitializer demoMassDataInitializer
    ) {
        this.demoUserInitializer = demoUserInitializer;
        this.demoMassDataInitializer = demoMassDataInitializer;
    }

    @Override
    public void run(ApplicationArguments args) {
        demoUserInitializer.initialize();
        demoMassDataInitializer.initialize();
        log.info("开发测试数据检查完成");
    }
}
