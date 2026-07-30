package com.yuncheng.system.api.organization;

import java.util.Optional;

/** 对外提供的系统组织节点查询能力。 */
public interface SystemOrganizationQueryApi {

    Optional<SystemOrganizationInfo> findById(Long nodeId);

    Optional<SystemOrganizationInfo> findByCode(String nodeCode);
}
