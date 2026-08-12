/** 用户管理权限码。 */
export const USER_PERMISSION_CODES = {
  ADD: 'system:user:add',
  CHANGE_STATUS: 'system:user:change-status',
  DELETE: 'system:user:delete',
  EDIT: 'system:user:edit',
  EXPORT: 'system:user:export',
  RESET_PASSWORD: 'system:user:reset-password',
  UNLOCK: 'system:user:unlock',
} as const;
