import type { OrgType } from '#/api/common/organization';
import type { AvailabilityResult, RoleSummary } from '#/api/system/types';
import type { PageResult } from '#/api/types';
import type { ExcelImportResult } from '#/components/excel';

import { requestClient } from '#/api/request';

export type UserUniqueField = 'EMAIL' | 'PHONE' | 'USERNAME';
export type UserOrgRelationType = 'ALL' | 'OTHER' | 'PRIMARY';
export type UserOrgScope = 'DIRECT' | 'INCLUDE_DESCENDANTS';
export type PasswordSetupMode = 'DEFAULT' | 'MANUAL';

export interface UserPrimaryOrgSummary {
  fullPath: string;
  id: string;
  orgCode: string;
  orgName: string;
  orgType: OrgType;
  otherOrgCount: number;
}

/** 用户列表项，联系方式已经由后端脱敏。 */
export interface UserListItem {
  avatar?: null | string;
  createdAt: string;
  email?: null | string;
  enabled: boolean;
  id: string;
  loginFailedCount: number;
  loginLocked: boolean;
  loginLockedUntil?: null | string;
  passwordChangedAt: string;
  phone?: null | string;
  primaryOrg: UserPrimaryOrgSummary;
  realName: string;
  roles: RoleSummary[];
  sortOrder: number;
  updatedAt: string;
  username: string;
}

/** 用户详情，联系方式已经由后端脱敏。 */
export interface UserDetail {
  avatar?: null | string;
  createdAt: string;
  createdBy: string;
  email?: null | string;
  enabled: boolean;
  id: string;
  loginFailedCount: number;
  loginLocked: boolean;
  loginLockedUntil?: null | string;
  passwordChangedAt: string;
  phone?: null | string;
  orgIds: string[];
  primaryOrgId: string;
  realName: string;
  roleIds: string[];
  sortOrder: number;
  updatedAt: string;
  updatedBy: string;
  username: string;
}

/** 用户编辑表单数据，联系方式保持原始内容。 */
export interface UserFormData {
  email?: null | string;
  enabled: boolean;
  id: string;
  phone?: null | string;
  orgIds: string[];
  primaryOrgId: string;
  realName: string;
  roleIds: string[];
  sortOrder: number;
  username: string;
}

export interface UserPageParams {
  enabled?: boolean;
  page: number;
  pageSize: number;
  realName?: string;
  orgId?: string;
  orgRelationType?: UserOrgRelationType;
  orgScope?: UserOrgScope;
  username?: string;
}

export type UserExportParams = Omit<UserPageParams, 'page' | 'pageSize'>;

export interface UserCreateRequest {
  email?: string;
  password?: string;
  passwordMode: PasswordSetupMode;
  phone?: string;
  orgIds: string[];
  primaryOrgId: string;
  realName: string;
  roleIds: string[];
  sortOrder: number;
  username: string;
}

export interface UserUpdateRequest {
  email?: string;
  phone?: string;
  orgIds: string[];
  primaryOrgId: string;
  realName: string;
  roleIds: string[];
  sortOrder: number;
}

interface UserUniquenessCheckRequest {
  field: UserUniqueField;
  id?: string;
  value: string;
}

export async function pageUsersApi(params: UserPageParams) {
  return requestClient.get<PageResult<UserListItem>>('/system/users', {
    params,
  });
}

export async function getUserDetailApi(id: string) {
  return requestClient.get<UserDetail>(`/system/users/${id}`);
}

export async function getUserFormDataApi(id: string) {
  return requestClient.get<UserFormData>(`/system/users/${id}/form`);
}

export async function createUserApi(data: UserCreateRequest) {
  return requestClient.post<string>('/system/users', data);
}

export async function downloadUserImportTemplateApi() {
  return requestClient.download<Blob>('/system/users/import-template');
}

export async function importUsersApi(file: File) {
  return requestClient.upload<ExcelImportResult>('/system/users/import', {
    file,
  });
}

export async function exportUsersApi(params: UserExportParams) {
  return requestClient.download<Blob>('/system/users/export', { params });
}

export async function updateUserApi(id: string, data: UserUpdateRequest) {
  return requestClient.put<null>(`/system/users/${id}`, data);
}

export async function checkUserUniquenessApi(data: UserUniquenessCheckRequest) {
  return requestClient.post<AvailabilityResult>(
    '/system/users/uniqueness-check',
    data,
  );
}

export async function changeUserStatusApi(id: string, enabled: boolean) {
  return requestClient.put<null>(`/system/users/${id}/enabled`, { enabled });
}

export async function resetUserPasswordApi(
  id: string,
  data: { password?: string; passwordMode: PasswordSetupMode },
) {
  return requestClient.put<null>(`/system/users/${id}/password`, data);
}

export async function unlockUserLoginApi(id: string) {
  return requestClient.delete<null>(`/system/users/${id}/login-lock`);
}

export async function deleteUserApi(id: string) {
  return requestClient.delete<null>(`/system/users/${id}`);
}

export async function batchDeleteUsersApi(ids: string[]) {
  return requestClient.post<null>('/system/users/batch-delete', { ids });
}
