import type { MenuType } from './menu';

import { requestClient } from '#/api/request';

export interface PermissionMenuNode {
  children?: PermissionMenuNode[];
  grantable: boolean;
  id: string;
  menuName: string;
  menuType: MenuType;
  parentId?: null | string;
  permissionCode?: null | string;
  systemRoleOnly: boolean;
}

export interface RolePermission {
  menuIds: string[];
  readOnly: boolean;
  roleId: string;
}

export async function getPermissionMenuTreeApi() {
  return requestClient.get<PermissionMenuNode[]>(
    '/system/permissions/menu-tree',
  );
}

export async function getRolePermissionApi(roleId: string) {
  return requestClient.get<RolePermission>(
    `/system/permissions/roles/${roleId}`,
  );
}

export async function saveRolePermissionApi(roleId: string, menuIds: string[]) {
  return requestClient.put<null>(`/system/permissions/roles/${roleId}`, {
    menuIds,
  });
}

export async function clearRebuildableCacheApi() {
  return requestClient.delete<number>('/system/permissions/cache');
}
