package com.yuncheng.system.api.organization;

/** 对外提供的系统组织节点写入能力。 */
public interface SystemOrganizationCommandApi {

    Long create(SystemOrganizationCreateCommand command);
}
