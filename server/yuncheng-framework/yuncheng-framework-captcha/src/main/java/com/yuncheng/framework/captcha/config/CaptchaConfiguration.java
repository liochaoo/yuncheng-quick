package com.yuncheng.framework.captcha.config;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.DefaultBuiltInResources;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 配置随服务发布的验证码背景图和滑块模板资源。 */
@Configuration(proxyBeanMethods = false)
public class CaptchaConfiguration {

    private static final int BACKGROUND_COUNT = 8;

    @Bean
    public ResourceStore captchaResourceStore() {
        LocalMemoryResourceStore store = new LocalMemoryResourceStore();
        DefaultBuiltInResources builtInResources =
                new DefaultBuiltInResources(DefaultBuiltInResources.PATH_PREFIX);
        builtInResources.addDefaultTemplate(CaptchaTypeConstant.SLIDER, store);
        for (char name = 'a'; name < 'a' + BACKGROUND_COUNT; name++) {
            store.addResource(
                    CaptchaTypeConstant.SLIDER,
                    new Resource("classpath", "captcha/" + name + ".jpg", "default")
            );
        }
        return store;
    }
}
