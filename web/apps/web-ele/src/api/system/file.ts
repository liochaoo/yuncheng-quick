import type { FileApi } from '#/api/core';
import type { PageResult } from '#/api/types';

import { requestClient } from '#/api/request';

export interface FileListItem extends FileApi.Record {
  storagePlatform: string;
}

export interface FileDetail extends FileListItem {
  createdBy: null | string;
  objectKey: string;
  sha256: string;
  updatedAt: null | string;
  updatedBy: null | string;
}

export interface FilePageParams {
  accessType?: FileApi.AccessType;
  businessType?: string;
  originalName?: string;
  page: number;
  pageSize: number;
  policyCode?: string;
  storagePlatform?: string;
}

/** 分页查询文件记录。 */
export async function pageFilesApi(params: FilePageParams) {
  return requestClient.get<PageResult<FileListItem>>('/system/files', {
    params,
  });
}

/** 查询文件详情。 */
export async function getFileDetailApi(id: string) {
  return requestClient.get<FileDetail>(`/system/files/${id}`);
}

/** 通过管理接口上传文件。 */
export async function uploadSystemFileApi(file: File, policyCode: string) {
  return requestClient.upload<FileDetail>('/system/files', {
    file,
    policyCode,
  });
}

/** 通过管理接口删除文件。 */
export async function deleteSystemFileApi(id: string) {
  return requestClient.delete<null>(`/system/files/${id}`);
}

/** 通过管理接口批量删除文件。 */
export async function batchDeleteSystemFilesApi(ids: string[]) {
  return requestClient.post<null>('/system/files/batch-delete', { ids });
}
