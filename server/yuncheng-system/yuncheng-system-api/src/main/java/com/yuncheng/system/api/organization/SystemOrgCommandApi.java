package com.yuncheng.system.api.organization;

/** 对外提供的系统组织写入能力。 */
public interface SystemOrgCommandApi {

    Long create(SystemOrgCreateCommand command);
}
