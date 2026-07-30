package com.yuncheng.init.system.service;

import com.yuncheng.common.constant.BuiltInOrgIds;
import com.yuncheng.system.api.organization.SystemOrgCommandApi;
import com.yuncheng.system.api.organization.SystemOrgCreateCommand;
import com.yuncheng.system.api.organization.SystemOrgInfo;
import com.yuncheng.system.api.organization.SystemOrgType;
import com.yuncheng.system.api.organization.SystemOrgQueryApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 初始化平台固定 ID 的默认组织。 */
@Service
public class SystemOrgInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(SystemOrgInitializer.class);
    private static final String DEFAULT_CODE = "default";
    private static final String DEFAULT_NAME = "默认组织";

    private final SystemOrgQueryApi orgQueryApi;
    private final SystemOrgCommandApi orgCommandApi;

    public SystemOrgInitializer(
            SystemOrgQueryApi orgQueryApi,
            SystemOrgCommandApi orgCommandApi
    ) {
        this.orgQueryApi = orgQueryApi;
        this.orgCommandApi = orgCommandApi;
    }

    public Long initialize() {
        SystemOrgInfo existingById = orgQueryApi
                .findById(BuiltInOrgIds.DEFAULT_ORG)
                .orElse(null);
        if (existingById != null) {
            if (existingById.orgType() != SystemOrgType.ORGANIZATION) {
                throw initializationConflict(
                        "固定默认组织主键对应的组织类型不正确，orgId="
                                + BuiltInOrgIds.DEFAULT_ORG
                );
            }
            return existingById.orgId();
        }
        SystemOrgInfo existingByCode = orgQueryApi.findByCode(DEFAULT_CODE)
                .orElse(null);
        if (existingByCode != null) {
            throw initializationConflict(
                    "默认组织编码已绑定其他主键，orgCode=" + DEFAULT_CODE
                            + "，existingOrgId=" + existingByCode.orgId()
            );
        }
        return orgCommandApi.create(new SystemOrgCreateCommand(
                BuiltInOrgIds.DEFAULT_ORG,
                null,
                SystemOrgType.ORGANIZATION,
                DEFAULT_CODE,
                DEFAULT_NAME,
                0,
                "平台初始化的默认组织"
        ));
    }

    private IllegalStateException initializationConflict(String message) {
        log.error("默认组织初始化冲突：{}", message);
        return new IllegalStateException(message);
    }
}
