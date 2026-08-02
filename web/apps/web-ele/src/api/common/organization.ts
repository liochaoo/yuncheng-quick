import { requestClient } from '#/api/request';

export type OrgType = 'DEPARTMENT' | 'GROUP' | 'ORGANIZATION';

export interface OrgIdentity {
  id: string;
  orgCode: string;
  orgName: string;
  orgType: OrgType;
}

/** 跨业务模块消费的组织摘要。 */
export interface OrgOption {
  ancestorIds: string[];
  depth: number;
  fullPath: string;
  hasChildren: boolean;
  id: string;
  orgCode: string;
  orgName: string;
  orgType: OrgType;
  parentId?: null | string;
  protectedOrg: boolean;
  sortOrder: number;
}

/** 根据直接归属节点动态推导的完整组织上下文。 */
export interface OrgContextOption extends OrgOption {
  department?: null | OrgIdentity;
  group?: null | OrgIdentity;
  organization?: null | OrgIdentity;
  topDepartment?: null | OrgIdentity;
  topGroup?: null | OrgIdentity;
  topOrganization?: null | OrgIdentity;
}

export interface OrgSearchParams {
  keyword: string;
}

/** 按上级节点异步加载组织，空上级表示顶级组织。 */
export async function listOrgOptionsApi(parentId?: string) {
  return requestClient.get<OrgOption[]>('/orgs', {
    params: parentId ? { parentId } : undefined,
  });
}

/** 按名称、编码或完整路径搜索组织。 */
export async function searchOrgOptionsApi(params: OrgSearchParams) {
  return requestClient.get<OrgOption[]>('/orgs/search', { params });
}

/** 根据主键恢复已选组织。 */
export async function getOrgOptionApi(id: string) {
  return requestClient.get<OrgContextOption>(`/orgs/${id}`);
}

/** 批量恢复已选组织并返回动态组织上下文。 */
export async function getOrgOptionsByIdsApi(ids: string[]) {
  if (ids.length === 0) return [];
  return requestClient.post<OrgContextOption[]>('/orgs/by-ids', { ids });
}
