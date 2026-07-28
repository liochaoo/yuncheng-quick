/** 超级管理员角色编码，与后端系统角色常量保持一致。 */
export const SUPER_ADMIN_ROLE_CODE = 'super-admin';

/** 注册用户默认角色编码，与后端系统角色常量保持一致。 */
export const DEFAULT_USER_ROLE_CODE = 'default';

export const NON_DELETABLE_ROLE_CODES = new Set([
  DEFAULT_USER_ROLE_CODE,
  SUPER_ADMIN_ROLE_CODE,
]);
