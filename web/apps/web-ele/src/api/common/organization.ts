import type { PageResult } from '#/api/types';

import { requestClient } from '#/api/request';

export type OrganizationNodeType = 'DEPARTMENT' | 'GROUP' | 'ORGANIZATION';

/** 跨业务模块消费的组织节点摘要。 */
export interface OrganizationNodeOption {
  ancestorIds: string[];
  depth: number;
  fullPath: string;
  hasChildren: boolean;
  id: string;
  nodeCode: string;
  nodeName: string;
  nodeType: OrganizationNodeType;
  parentId?: null | string;
  protectedNode: boolean;
  sortOrder: number;
}

export interface OrganizationNodeSearchParams {
  keyword: string;
  page: number;
  pageSize: number;
}

/** 按上级节点异步加载组织节点，空上级表示顶级组织。 */
export async function listOrganizationNodeOptionsApi(parentId?: string) {
  return requestClient.get<OrganizationNodeOption[]>('/organization-nodes', {
    params: parentId ? { parentId } : undefined,
  });
}

/** 按名称、编码或完整路径分页搜索组织节点。 */
export async function searchOrganizationNodeOptionsApi(
  params: OrganizationNodeSearchParams,
) {
  return requestClient.get<PageResult<OrganizationNodeOption>>(
    '/organization-nodes/search',
    { params },
  );
}

/** 根据主键恢复已选组织节点。 */
export async function getOrganizationNodeOptionApi(id: string) {
  return requestClient.get<OrganizationNodeOption>(`/organization-nodes/${id}`);
}
