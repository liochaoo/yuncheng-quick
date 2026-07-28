import type { PageResult } from '#/api/types';

import { requestClient } from '#/api/request';

export interface OnlineSessionItem {
  clientType: string;
  current: boolean;
  expiresAt: string;
  loginIp?: string;
  loginTime: string;
  realName?: string;
  sessionId: string;
  userAgent?: string;
  userId: string;
  username: string;
}

export interface OnlineSessionPageParams {
  page: number;
  pageSize: number;
  username?: string;
}

export async function pageOnlineSessionsApi(params: OnlineSessionPageParams) {
  return requestClient.get<PageResult<OnlineSessionItem>>('/system/sessions', {
    params,
  });
}

export async function getOnlineSessionDetailApi(sessionId: string) {
  return requestClient.get<OnlineSessionItem>(
    `/system/sessions/${encodeURIComponent(sessionId)}`,
  );
}

export async function kickoutOnlineSessionApi(sessionId: string) {
  return requestClient.delete<null>(
    `/system/sessions/${encodeURIComponent(sessionId)}`,
  );
}

export async function batchKickoutOnlineSessionsApi(sessionIds: string[]) {
  return requestClient.post<null>('/system/sessions/batch-kickout', {
    sessionIds,
  });
}
