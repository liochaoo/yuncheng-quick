import type { OrgOption, OrgType } from '#/api/common/organization';

import { requestClient } from '#/api/request';

export type { OrgOption, OrgType };

export interface OrgDetail {
  createdAt: string;
  createdBy: string;
  depth: number;
  description?: null | string;
  fullPath: string;
  id: string;
  orgCode: string;
  orgName: string;
  orgType: OrgType;
  parentId?: null | string;
  parentName?: null | string;
  sortOrder: number;
  updatedAt: string;
  updatedBy: string;
}

export interface OrgListParams {
  keyword?: string;
  orgCode?: string;
  orgName?: string;
}

export interface OrgUniquenessCheckRequest {
  field: 'ORG_CODE' | 'ORG_NAME';
  id?: string;
  parentId?: string;
  value: string;
}

export interface OrgCreateRequest {
  description?: string;
  orgCode: string;
  orgName: string;
  orgType: OrgType;
  parentId?: string;
  sortOrder: number;
}

export interface OrgUpdateRequest {
  description?: string;
  orgCode: string;
  orgName: string;
  sortOrder: number;
}

export interface OrgMoveImpact {
  newFullPath: string;
  orgCount: number;
  relationCount: number;
  userCount: number;
}

export async function listOrgsApi(params: OrgListParams) {
  return requestClient.get<OrgOption[]>('/system/orgs', { params });
}

export async function listOrgChildrenApi(parentId?: string) {
  return requestClient.get<OrgOption[]>('/system/orgs/children', {
    params: parentId ? { parentId } : undefined,
  });
}

export async function getOrgDetailApi(id: string) {
  return requestClient.get<OrgDetail>(`/system/orgs/${id}`);
}

export async function createOrgApi(data: OrgCreateRequest) {
  return requestClient.post<string>('/system/orgs', data);
}

export async function checkOrgUniquenessApi(data: OrgUniquenessCheckRequest) {
  return requestClient.post<{ available: boolean }>(
    '/system/orgs/uniqueness-check',
    data,
  );
}

export async function updateOrgApi(id: string, data: OrgUpdateRequest) {
  return requestClient.put<null>(`/system/orgs/${id}`, data);
}

export async function getOrgMoveImpactApi(id: string, parentId?: string) {
  return requestClient.get<OrgMoveImpact>(`/system/orgs/${id}/move-impact`, {
    params: parentId ? { parentId } : undefined,
  });
}

export async function moveOrgApi(id: string, parentId?: string) {
  return requestClient.put<null>(`/system/orgs/${id}/parent`, {
    parentId,
  });
}

export async function deleteOrgApi(id: string) {
  return requestClient.delete<null>(`/system/orgs/${id}`);
}
