package com.yuncheng.init.system.service;

import com.yuncheng.common.constant.BuiltInOrganizationIds;
import com.yuncheng.system.api.organization.SystemOrganizationCommandApi;
import com.yuncheng.system.api.organization.SystemOrganizationCreateCommand;
import com.yuncheng.system.api.organization.SystemOrganizationInfo;
import com.yuncheng.system.api.organization.SystemOrganizationNodeType;
import com.yuncheng.system.api.organization.SystemOrganizationQueryApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 初始化平台固定 ID 的默认组织。 */
@Service
public class SystemOrganizationInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(SystemOrganizationInitializer.class);
    private static final String DEFAULT_CODE = "default";
    private static final String DEFAULT_NAME = "默认组织";

    private final SystemOrganizationQueryApi organizationQueryApi;
    private final SystemOrganizationCommandApi organizationCommandApi;

    public SystemOrganizationInitializer(
            SystemOrganizationQueryApi organizationQueryApi,
            SystemOrganizationCommandApi organizationCommandApi
    ) {
        this.organizationQueryApi = organizationQueryApi;
        this.organizationCommandApi = organizationCommandApi;
    }

    public Long initialize() {
        SystemOrganizationInfo existingById = organizationQueryApi
                .findById(BuiltInOrganizationIds.DEFAULT_ORGANIZATION)
                .orElse(null);
        if (existingById != null) {
            if (existingById.nodeType() != SystemOrganizationNodeType.ORGANIZATION) {
                throw initializationConflict(
                        "固定默认组织主键对应的节点类型不正确，nodeId="
                                + BuiltInOrganizationIds.DEFAULT_ORGANIZATION
                );
            }
            return existingById.nodeId();
        }
        SystemOrganizationInfo existingByCode = organizationQueryApi.findByCode(DEFAULT_CODE)
                .orElse(null);
        if (existingByCode != null) {
            throw initializationConflict(
                    "默认组织编码已绑定其他主键，nodeCode=" + DEFAULT_CODE
                            + "，existingNodeId=" + existingByCode.nodeId()
            );
        }
        return organizationCommandApi.create(new SystemOrganizationCreateCommand(
                BuiltInOrganizationIds.DEFAULT_ORGANIZATION,
                null,
                SystemOrganizationNodeType.ORGANIZATION,
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
