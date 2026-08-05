import type { PageResult } from '#/api/types';

import { requestClient } from '#/api/request';

export type LogType = 'LOGIN' | 'OPERATION';

export interface LogCleanPolicy {
  latestCleanableBefore: string;
  retentionDays: number;
}

export interface LoginLogItem {
  clientType?: string;
  eventType: string;
  failureReason?: string;
  id: string;
  ip?: string;
  loginName?: string;
  occurredAt: string;
  realName?: string;
  sessionId?: string;
  success: boolean;
  traceId?: string;
  userAgent?: string;
  userId?: string;
}

export interface LoginLogPageParams {
  clientType?: string;
  eventType?: string;
  loginName?: string;
  occurredAtEnd?: string;
  occurredAtStart?: string;
  page: number;
  pageSize: number;
  success?: boolean;
  traceId?: string;
}

export interface OperationLogItem {
  action: string;
  className: string;
  durationMillis: number;
  errorMessage?: string;
  httpMethod?: string;
  id: string;
  ip?: string;
  methodName: string;
  occurredAt: string;
  realName?: string;
  requestParams?: string;
  requestPath?: string;
  success: boolean;
  traceId?: string;
  userAgent?: string;
  userId?: string;
  username?: string;
}

export interface OperationLogPageParams {
  action?: string;
  minDurationMillis?: number;
  occurredAtEnd?: string;
  occurredAtStart?: string;
  page: number;
  pageSize: number;
  requestPath?: string;
  success?: boolean;
  traceId?: string;
  username?: string;
}

export async function pageLoginLogsApi(params: LoginLogPageParams) {
  return requestClient.get<PageResult<LoginLogItem>>('/system/logs/login', {
    params,
  });
}

export async function getLoginLogDetailApi(id: string) {
  return requestClient.get<LoginLogItem>(`/system/logs/login/${id}`);
}

export async function pageOperationLogsApi(params: OperationLogPageParams) {
  return requestClient.get<PageResult<OperationLogItem>>(
    '/system/logs/operation',
    { params },
  );
}

export async function getOperationLogDetailApi(id: string) {
  return requestClient.get<OperationLogItem>(`/system/logs/operation/${id}`);
}

export async function getLogCleanPolicyApi() {
  return requestClient.get<LogCleanPolicy>('/system/logs/clean-policy');
}

export async function cleanSystemLogsApi(type: LogType, before: string) {
  return requestClient.post<{ deletedCount: number }>('/system/logs/clean', {
    before,
    type,
  });
}
