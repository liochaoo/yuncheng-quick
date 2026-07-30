package com.yuncheng.system.api.organization;

import java.util.Optional;

/** 对外提供的系统组织查询能力。 */
public interface SystemOrgQueryApi {

    Optional<SystemOrgInfo> findById(Long orgId);

    Optional<SystemOrgInfo> findByCode(String orgCode);
}
