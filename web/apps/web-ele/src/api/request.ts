/**
 * 该文件可自行根据业务逻辑进行调整
 */
import type { RequestClientOptions } from '@vben/request';

import { useAppConfig } from '@vben/hooks';
import { preferences } from '@vben/preferences';
import {
  authenticateResponseInterceptor,
  defaultResponseInterceptor,
  errorMessageResponseInterceptor,
  RequestClient,
} from '@vben/request';
import { useAccessStore } from '@vben/stores';

import { ElMessage } from 'element-plus';

import { useAuthStore } from '#/store';

import { refreshTokenApi } from './core';

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);

/**
 * 同一个异常可能依次经过认证客户端和普通客户端。
 * 记录已经展示过的异常对象，避免在刷新 Token 等嵌套请求链路中重复提示。
 */
const notifiedErrors = new WeakSet<object>();

function isObject(value: unknown): value is object {
  return typeof value === 'object' && value !== null;
}

function showRequestError(message: string, error: any) {
  const responseData = error?.response?.data;
  const errorObjects = [error, responseData].filter(isObject);
  if (errorObjects.some((item) => notifiedErrors.has(item))) {
    return;
  }
  errorObjects.forEach((item) => notifiedErrors.add(item));

  // 并行请求返回相同错误时，使用 Element Plus 自带的消息合并能力。
  ElMessage({ grouping: true, message, type: 'error' });
}

/**
 * 把后端返回的同一 API 服务地址转换为 RequestClient 可用的相对地址。
 *
 * 后端返回的文件地址需要保留完整的 `/api` 前缀，供 img 等原生元素直接访问；
 * 通过已经配置 baseURL 的 RequestClient 请求时，则需要去掉重复的 base path。
 */
export function toRequestClientUrl(url: string) {
  if (!url || !apiURL) return url;

  const absoluteUrlPattern = /^[a-z][a-z\d+\-.]*:\/\//i;
  const base = new URL(apiURL, window.location.origin);
  const target = new URL(url, window.location.origin);
  if (absoluteUrlPattern.test(url) && target.origin !== base.origin) {
    return url;
  }

  const basePath = base.pathname.replace(/\/+$/, '');
  if (
    basePath &&
    (target.pathname === basePath || target.pathname.startsWith(`${basePath}/`))
  ) {
    const path = target.pathname.slice(basePath.length) || '/';
    return `${path}${target.search}${target.hash}`;
  }
  return url;
}

/**
 * 添加平台统一响应解析。
 */
function addDataResponseInterceptor(client: RequestClient) {
  client.addResponseInterceptor(
    defaultResponseInterceptor({
      codeField: 'code',
      dataField: 'data',
      successCode: 0,
    }),
  );
}

/**
 * 添加通用错误提示。
 */
function addErrorResponseInterceptor(client: RequestClient) {
  client.addResponseInterceptor(
    errorMessageResponseInterceptor((msg: string, error) => {
      // 优先展示后端返回的中文错误信息。
      const responseData = error?.response?.data ?? error ?? {};
      const errorMessage = responseData?.message ?? '';
      showRequestError(errorMessage || msg, error);
    }),
  );
}

function createRequestClient(baseURL: string, options?: RequestClientOptions) {
  const client = new RequestClient({
    ...options,
    baseURL,
  });

  /**
   * 重新认证逻辑
   */
  async function doReAuthenticate() {
    console.warn('Access Token 或 Refresh Token 无效或已过期');
    const accessStore = useAccessStore();
    const authStore = useAuthStore();
    accessStore.setAccessToken(null);
    if (
      preferences.app.loginExpiredMode === 'modal' &&
      accessStore.isAccessChecked
    ) {
      accessStore.setLoginExpired(true);
    } else {
      await authStore.logout();
    }
  }

  /**
   * 刷新token逻辑
   */
  async function doRefreshToken() {
    const accessStore = useAccessStore();
    const { accessToken: newToken } = await refreshTokenApi();
    accessStore.setAccessToken(newToken);
    return newToken;
  }

  function formatToken(token: null | string) {
    return token ? `Bearer ${token}` : null;
  }

  // 请求头处理
  client.addRequestInterceptor({
    fulfilled: async (config) => {
      const accessStore = useAccessStore();

      config.headers.Authorization = formatToken(accessStore.accessToken);
      config.headers['Accept-Language'] = preferences.app.locale;
      return config;
    },
  });

  // 处理返回的响应数据格式
  addDataResponseInterceptor(client);

  // token过期的处理
  client.addResponseInterceptor(
    authenticateResponseInterceptor({
      client,
      doReAuthenticate,
      doRefreshToken,
      enableRefreshToken: preferences.app.enableRefreshToken,
      formatToken,
    }),
  );

  // 通用的错误处理，如果没有进入上面的错误处理逻辑，就会进入这里
  addErrorResponseInterceptor(client);

  return client;
}

export const requestClient = createRequestClient(apiURL, {
  responseReturn: 'data',
});

/**
 * 认证接口客户端。
 *
 * 与普通请求使用相同的响应解析和错误处理，但不添加 Access Token，
 * 也不处理 401 自动刷新，避免认证接口进入刷新循环。
 */
export const authRequestClient = new RequestClient({
  baseURL: apiURL,
  responseReturn: 'data',
  withCredentials: true,
});

addDataResponseInterceptor(authRequestClient);
addErrorResponseInterceptor(authRequestClient);
