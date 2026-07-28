import type {
  RoleOption,
  RoleSummary,
  RoleType,
  UniquenessCheckResult,
} from '#/api/system/types';
import type { PageResult } from '#/api/types';

import { requestClient } from '#/api/request';

export type RoleUniqueField = 'ROLE_CODE' | 'ROLE_NAME';

export interface RoleListItem {
  createdAt: string;
  id: string;
  roleCode: string;
  roleName: string;
  roleType: RoleType;
  sortOrder: number;
  updatedAt: string;
  userCount: number;
}

export interface RoleDetail extends RoleListItem {
  createdBy: string;
  updatedBy: string;
}

export interface RolePageParams {
  page: number;
  pageSize: number;
  roleCode?: string;
  roleName?: string;
  roleType?: RoleType;
}

export interface RoleCreateRequest {
  roleCode: string;
  roleName: string;
  roleType: RoleType;
  sortOrder: number;
}

export interface RoleOptionPageParams {
  keyword?: string;
  page: number;
  pageSize: number;
  roleCode?: string;
  roleName?: string;
}

export interface RoleUpdateRequest {
  roleName: string;
  sortOrder: number;
}

export interface RoleUserPageParams {
  page: number;
  pageSize: number;
  realName?: string;
  username?: string;
}

/** 角色用户列表项，不包含用户联系方式。 */
export interface RoleUserListItem {
  enabled: boolean;
  id: string;
  realName: string;
  roles: RoleSummary[];
  username: string;
}

interface RoleUniquenessCheckRequest {
  field: RoleUniqueField;
  id?: string;
  value: string;
}

export async function pageRolesApi(params: RolePageParams) {
  return requestClient.get<PageResult<RoleListItem>>('/system/roles', {
    params,
  });
}

export async function getRoleDetailApi(id: string) {
  return requestClient.get<RoleDetail>(`/system/roles/${id}`);
}

export async function createRoleApi(data: RoleCreateRequest) {
  return requestClient.post<string>('/system/roles', data);
}

export async function updateRoleApi(id: string, data: RoleUpdateRequest) {
  return requestClient.put<null>(`/system/roles/${id}`, data);
}

export async function checkRoleUniquenessApi(data: RoleUniquenessCheckRequest) {
  return requestClient.post<UniquenessCheckResult>(
    '/system/roles/uniqueness-check',
    data,
  );
}

export async function deleteRoleApi(id: string) {
  return requestClient.delete<null>(`/system/roles/${id}`);
}

export async function batchDeleteRolesApi(ids: string[]) {
  return requestClient.post<null>('/system/roles/batch-delete', { ids });
}

export async function pageRoleOptionsApi(params: RoleOptionPageParams) {
  return requestClient.get<PageResult<RoleOption>>('/system/roles/options', {
    params,
  });
}

export async function getRoleOptionsByIdsApi(ids: string[]) {
  return requestClient.post<RoleOption[]>('/system/roles/options/by-ids', {
    ids,
  });
}

export async function pageRoleUsersApi(
  roleId: string,
  params: RoleUserPageParams,
) {
  return requestClient.get<PageResult<RoleUserListItem>>(
    `/system/roles/${roleId}/users`,
    { params },
  );
}

export async function pageRoleCandidateUsersApi(
  roleId: string,
  params: RoleUserPageParams,
) {
  return requestClient.get<PageResult<RoleUserListItem>>(
    `/system/roles/${roleId}/candidate-users`,
    { params },
  );
}

export async function addRoleUsersApi(roleId: string, ids: string[]) {
  return requestClient.post<null>(`/system/roles/${roleId}/users`, { ids });
}

export async function removeRoleUsersApi(roleId: string, ids: string[]) {
  return requestClient.post<null>(
    `/system/roles/${roleId}/users/batch-remove`,
    { ids },
  );
}
