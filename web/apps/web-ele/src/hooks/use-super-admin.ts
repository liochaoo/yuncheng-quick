import { computed } from 'vue';

import { useUserStore } from '@vben/stores';

import { SUPER_ADMIN_ROLE_CODE } from '#/views/system/_shared/constants';

/** 判断当前登录用户是否具有超级管理员角色。 */
export function useIsSuperAdmin() {
  const userStore = useUserStore();
  return computed(() => userStore.userRoles.includes(SUPER_ADMIN_ROLE_CODE));
}
