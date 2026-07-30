import type { PageResult } from '#/api/types';

import { requestClient } from '#/api/request';

export type OrgType = 'DEPARTMENT' | 'GROUP' | 'ORGANIZATION';

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

export interface OrgSearchParams {
  keyword: string;
  page: number;
  pageSize: number;
}

/** 按上级节点异步加载组织，空上级表示顶级组织。 */
export async function listOrgOptionsApi(parentId?: string) {
  return requestClient.get<OrgOption[]>('/orgs', {
    params: parentId ? { parentId } : undefined,
  });
}

/** 按名称、编码或完整路径分页搜索组织。 */
export async function searchOrgOptionsApi(params: OrgSearchParams) {
  return requestClient.get<PageResult<OrgOption>>('/orgs/search', { params });
}

/** 根据主键恢复已选组织。 */
export async function getOrgOptionApi(id: string) {
  return requestClient.get<OrgOption>(`/orgs/${id}`);
}
