import { requestClient, toRequestClientUrl } from '#/api/request';

export namespace FileApi {
  export type AccessType = 'PRIVATE' | 'PUBLIC';

  /** 后端统一返回的文件记录。 */
  export interface Record {
    accessType: AccessType;
    businessId: null | string;
    businessPosition: null | string;
    businessType: null | string;
    contentType: string;
    createdAt: string;
    downloadUrl: string;
    fileExtension: string;
    fileSize: number;
    id: string;
    originalName: string;
    policyCode: string;
    previewUrl: string;
    sortOrder: number;
  }

  /** 文件与业务数据的关联信息。 */
  export interface Association {
    businessId: string;
    businessPosition: string;
    businessType: string;
    sortOrder?: number;
  }

  /** 上传文件时可附带的参数。 */
  export interface UploadParams {
    association?: Association;
    policyCode?: string;
  }
}

/** 上传文件。 */
export async function uploadFileApi(
  file: Blob | File,
  params: FileApi.UploadParams = {},
) {
  const { association, policyCode = 'attachment' } = params;
  return requestClient.upload<FileApi.Record>('/files', {
    file,
    policyCode,
    ...association,
  });
}

/** 查询某个业务位置已经关联的文件。 */
export async function listBusinessFilesApi(
  association: Omit<FileApi.Association, 'sortOrder'>,
) {
  return requestClient.get<FileApi.Record[]>('/files', {
    params: association,
  });
}

/** 为已经上传的文件建立业务关联。 */
export async function associateFileApi(
  id: string,
  association: FileApi.Association,
) {
  return requestClient.put<null>(`/files/${id}/association`, association);
}

/** 移除文件与业务数据的关联，不删除物理文件。 */
export async function removeFileAssociationApi(id: string) {
  return requestClient.delete<null>(`/files/${id}/association`);
}

/** 删除文件记录和物理文件。 */
export async function deleteFileApi(id: string) {
  return requestClient.delete<null>(`/files/${id}`);
}

/** 通过认证请求读取文件预览内容。 */
export async function getFilePreviewBlobApi(file: FileApi.Record) {
  return requestClient.download<Blob>(toRequestClientUrl(file.previewUrl));
}

/** 通过认证请求读取文件下载内容。 */
export async function getFileDownloadBlobApi(file: FileApi.Record) {
  return requestClient.download<Blob>(toRequestClientUrl(file.downloadUrl));
}
