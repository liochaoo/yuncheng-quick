import type {
  OrganizationNodeOption,
  OrganizationNodeType,
} from '#/api/common/organization';
import type { PageResult } from '#/api/types';

import { requestClient } from '#/api/request';

export type { OrganizationNodeOption, OrganizationNodeType };

export interface OrganizationNodeDetail {
  createdAt: string;
  createdBy: string;
  depth: number;
  description?: null | string;
  fullPath: string;
  id: string;
  nodeCode: string;
  nodeName: string;
  nodeType: OrganizationNodeType;
  parentId?: null | string;
  parentName?: null | string;
  sortOrder: number;
  updatedAt: string;
  updatedBy: string;
}

export interface OrganizationNodePageParams {
  keyword?: string;
  page: number;
  pageSize: number;
}

export interface OrganizationNodeCreateRequest {
  description?: string;
  nodeCode: string;
  nodeName: string;
  nodeType: OrganizationNodeType;
  parentId?: string;
  sortOrder: number;
}

export interface OrganizationNodeUpdateRequest {
  description?: string;
  nodeCode: string;
  nodeName: string;
  sortOrder: number;
}

export interface OrganizationNodeMoveImpact {
  newFullPath: string;
  nodeCount: number;
}

export async function pageOrganizationNodesApi(
  params: OrganizationNodePageParams,
) {
  return requestClient.get<PageResult<OrganizationNodeOption>>(
    '/system/organization-nodes',
    { params },
  );
}

export async function listOrganizationNodeChildrenApi(parentId?: string) {
  return requestClient.get<OrganizationNodeOption[]>(
    '/system/organization-nodes/children',
    { params: parentId ? { parentId } : undefined },
  );
}

export async function getOrganizationNodeDetailApi(id: string) {
  return requestClient.get<OrganizationNodeDetail>(
    `/system/organization-nodes/${id}`,
  );
}

export async function createOrganizationNodeApi(
  data: OrganizationNodeCreateRequest,
) {
  return requestClient.post<string>('/system/organization-nodes', data);
}

export async function updateOrganizationNodeApi(
  id: string,
  data: OrganizationNodeUpdateRequest,
) {
  return requestClient.put<null>(`/system/organization-nodes/${id}`, data);
}

export async function getOrganizationNodeMoveImpactApi(
  id: string,
  parentId?: string,
) {
  return requestClient.get<OrganizationNodeMoveImpact>(
    `/system/organization-nodes/${id}/move-impact`,
    { params: parentId ? { parentId } : undefined },
  );
}

export async function moveOrganizationNodeApi(id: string, parentId?: string) {
  return requestClient.put<null>(`/system/organization-nodes/${id}/parent`, {
    parentId,
  });
}

export async function deleteOrganizationNodeApi(id: string) {
  return requestClient.delete<null>(`/system/organization-nodes/${id}`);
}
