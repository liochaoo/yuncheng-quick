import { rawRequestClient, requestClient } from '#/api/request';

export interface OpenApiStatus {
  documentUrl: null | string;
  enabled: boolean;
}

export async function getOpenApiStatusApi() {
  return requestClient.get<OpenApiStatus>('/openapi/config');
}

export async function getOpenApiDocumentApi(documentUrl: string) {
  return rawRequestClient.get<Record<string, unknown>>(documentUrl);
}
